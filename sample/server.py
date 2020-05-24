#!/usr/bin/env python

import RPi.GPIO as GPIO
import tensorflow as tf
import requests
import json
import sys
import io
import os
import shutil
import datetime
import picamera
from picamera import Color
import time
import numpy as np
from subprocess import Popen, PIPE
from PIL import Image
from string import Template
from struct import Struct
from threading import Thread
from time import sleep, time
from http.server import HTTPServer, BaseHTTPRequestHandler
from wsgiref.simple_server import make_server
from ws4py.websocket import WebSocket
from ws4py.server.wsgirefserver import (
    WSGIServer,
    WebSocketWSGIHandler,
    WebSocketWSGIRequestHandler,
)
from ws4py.server.wsgiutils import WebSocketWSGIApplication
from inputimeout import inputimeout, TimeoutOccurred
from sklearn.neighbors import LocalOutlierFactor
from sklearn.preprocessing import MinMaxScaler
from keras.models import model_from_json
from keras import backend as K

###########################################
# CONFIGURATION
WIDTH = 640
HEIGHT = 480
FRAMERATE = 24
HTTP_PORT = 8082
WS_PORT = 8084
COLOR = u'#444'
BGCOLOR = u'#333'
JSMPEG_MAGIC = b'jsmp'
JSMPEG_HEADER = Struct('>4sHH')
VFLIP = False
HFLIP = False
SCORE_THRESHOLD_UNDER = 6
SCORE_THRESHOLD_UPPER = 10
TOKEN = 'xoxb-638261944790-636109036896-UGHogl7nRXpQ27IeKiFu3qms'
CHANNEL = 'CJHMZFA49'
WATER_COMMENT = "土が乾いていますので、水やりが必要です。"
NOT_WATER_COMMENT = "土は乾いていませんので、水やりは不要です。"
LOOK_COMMENT = "現在の状況は、この様になっています。"

###########################################

class StreamingHttpHandler(BaseHTTPRequestHandler):
    def do_HEAD(self):
        self.do_GET()

    def do_GET(self):
        if self.path == '/':
            self.send_response(301)
            self.send_header('Location', '/index.html')
            self.end_headers()
            return
        elif self.path == '/jsmpg.js':
            content_type = 'application/javascript'
            content = self.server.jsmpg_content
        elif self.path == '/index.html':
            content_type = 'text/html; charset=utf-8'
            tpl = Template(self.server.index_template)
            content = tpl.safe_substitute(dict(
                WS_PORT=WS_PORT, WIDTH=WIDTH, HEIGHT=HEIGHT, COLOR=COLOR,
                BGCOLOR=BGCOLOR))
        else:
            self.send_error(404, 'File not found')
            return
        content = content.encode('utf-8')
        self.send_response(200)
        self.send_header('Content-Type', content_type)
        self.send_header('Content-Length', len(content))
        self.send_header('Last-Modified', self.date_time_string(time()))
        self.end_headers()
        if self.command == 'GET':
            self.wfile.write(content)


class StreamingHttpServer(HTTPServer):
    def __init__(self):
        super(StreamingHttpServer, self).__init__(
                ('', HTTP_PORT), StreamingHttpHandler)
        with io.open('index.html', 'r') as f:
            self.index_template = f.read()
        with io.open('jsmpg.js', 'r') as f:
            self.jsmpg_content = f.read()


class StreamingWebSocket(WebSocket):
    def opened(self):
        self.send(JSMPEG_HEADER.pack(JSMPEG_MAGIC, WIDTH, HEIGHT), binary=True)


class BroadcastOutput(object):
    def __init__(self, camera):
        print('Spawning background conversion process')
        self.converter = Popen([
            'ffmpeg',
            '-f', 'rawvideo',
            '-pix_fmt', 'yuv420p',
            '-s', '%dx%d' % camera.resolution,
            '-r', str(float(camera.framerate)),
            '-i', '-',
            '-f', 'mpeg1video',
            '-b', '800k',
            '-r', str(float(camera.framerate)),
            '-'],
            stdin=PIPE, stdout=PIPE, stderr=io.open(os.devnull, 'wb'),
            shell=False, close_fds=True)

    def write(self, b):
        self.converter.stdin.write(b)

    def flush(self):
        print('Waiting for background conversion process to exit')
        self.converter.stdin.close()
        self.converter.wait()


class BroadcastThread(Thread):
    def __init__(self, converter, websocket_server):
        super(BroadcastThread, self).__init__()
        self.converter = converter
        self.websocket_server = websocket_server

    def run(self):
        try:
            while True:
                buf = self.converter.stdout.read1(32768)
                if buf:
                    self.websocket_server.manager.broadcast(buf, binary=True)
                elif self.converter.poll() is not None:
                    break
        finally:
            self.converter.stdout.close()


path = "pictures/"
if not os.path.exists(path):
    os.mkdir(path)

graph = tf.get_default_graph()
model_path = "model/"
if os.path.exists(model_path):
    # LOF
    print("LOF model building...")
    x_train = np.loadtxt(model_path + "train.csv",delimiter=",")

    ms = MinMaxScaler()
    x_train = ms.fit_transform(x_train)

    # fit the LOF model
    clf = LocalOutlierFactor(n_neighbors=5)
    clf.fit(x_train)

    # DOC
    print("DOC Model loading...")
    model = model_from_json(open(model_path + 'model.json').read())
    model.load_weights(model_path + 'weights.h5')
    print("loading finish")
else:
    print("Nothing model folder")

whileMove = True
water = False
look = False

def main():
    global graph
    global whileMove
    print('Initializing camera')
    with picamera.PiCamera() as camera:
        camera.resolution = (WIDTH, HEIGHT)
        camera.framerate = FRAMERATE
        camera.vflip = VFLIP # flips image rightside up, as needed
        camera.hflip = HFLIP # flips image left-right, as needed
        sleep(1) # camera warm-up time
        print('Initializing websockets server on port %d' % WS_PORT)
        WebSocketWSGIHandler.http_version = '1.1'
        websocket_server = make_server(
            '', WS_PORT,
            server_class=WSGIServer,
            handler_class=WebSocketWSGIRequestHandler,
            app=WebSocketWSGIApplication(handler_cls=StreamingWebSocket))
        websocket_server.initialize_websockets_manager()
        websocket_thread = Thread(target=websocket_server.serve_forever)
        print('Initializing HTTP server on port %d' % HTTP_PORT)
        http_server = StreamingHttpServer()
        http_thread = Thread(target=http_server.serve_forever)
        print('Initializing broadcast thread')
        output = BroadcastOutput(camera)
        broadcast_thread = BroadcastThread(output.converter, websocket_server)
        print('Starting recording')
        camera.start_recording(output, 'yuv')
        try:
            print('Starting websockets thread')
            websocket_thread.start()
            print('Starting HTTP server thread')
            http_thread.start()
            print('Starting broadcast thread')
            broadcast_thread.start()
            mean_NO = 0
            score_mean = np.zeros(10)
            m_input_size, m_input_size = 96, 96
            picture_dir = '/home/pi/work/picture/'

            scorelist = []
            scorecheck = True

            while whileMove:
                camera.wait_recording(2)
                camera.annotate_text = ''

##############
                with graph.as_default():
                    global water
                    global look
                    fileName = str(datetime.datetime.today().strftime("%Y%m%d_%H%M%S")) + '.jpg'
                    camera.capture(picture_dir + fileName,resize=(480, 480))
                    img = np.array(Image.open(picture_dir + fileName))[192:288, 192:288]
                    Image.fromarray(img).save(picture_dir + 'resize_' + fileName)
                    img = img.reshape((1,m_input_size, m_input_size,3))
                    test = model.predict(img/255)
                    test = test.reshape((len(test),-1))
                    test = ms.transform(test)
                    score = -clf._decision_function(test)
                    score_mean[mean_NO] = score[0]
                    mean_NO += 1
                    if mean_NO == len(score_mean):
                         mean_NO = 0
                    nowscore = np.mean(score_mean)

                    print("{:.1f} Score".format(nowscore))

                    for index, item in enumerate(scorelist):
                        if not item - 0.2 < nowscore < item + 0.2:
                            scorecheck = False
                            print("値が+-0.1を超えました")
                            camera.annotate_foreground = Color('blue')


                    if 0 < len(scorelist) < 5:
                        scorelist.pop(0)

                    scorelist.append(nowscore)

                    if scorecheck :
                        if SCORE_THRESHOLD_UNDER < np.mean(score_mean) < SCORE_THRESHOLD_UPPER:
                            camera.annotate_foreground = Color('red')
                        else :
                            camera.annotate_foreground = Color('green')
                        if look :
                            files = {'file': open(picture_dir + fileName, 'rb')}
                            sendSlack(fileName, files, LOOK_COMMENT)
                            look = False
                            print("現在の状態を送信")
                        if whileMove and (SCORE_THRESHOLD_UNDER < np.mean(score_mean) < SCORE_THRESHOLD_UPPER) :
                            files = {'file': open(picture_dir + fileName, 'rb')}
                            sendSlack(fileName, files, WATER_COMMENT)
                            print("水やりが必要")
                    scorecheck = True
                    camera.annotate_text = str('{:.1f}'.format(nowscore)) + " Score"
                    #sleep(3)
##############

        except KeyboardInterrupt:
            pass
        finally:
            whileMove = True
            print('Stopping recording')
            camera.stop_recording()
            print('Waiting for broadcast thread to finish')
            broadcast_thread.join()
            print('Shutting down HTTP server')
            http_server.shutdown()
            print('Shutting down websockets server')
            websocket_server.shutdown()
            print('Waiting for HTTP server thread to finish')
            http_thread.join()
            print('Waiting for websockets thread to finish')
            websocket_thread.join()

def autoLook():
    global graph
    global whileMove
    print('Initializing camera')
    with picamera.PiCamera() as camera:
        camera.resolution = (WIDTH, HEIGHT)
        camera.framerate = FRAMERATE
        camera.vflip = VFLIP # flips image rightside up, as needed
        camera.hflip = HFLIP # flips image left-right, as needed
        sleep(1) # camera warm-up time
        print('Initializing websockets server on port %d' % WS_PORT)
        WebSocketWSGIHandler.http_version = '1.1'
        websocket_server = make_server(
            '', WS_PORT,
            server_class=WSGIServer,
            handler_class=WebSocketWSGIRequestHandler,
            app=WebSocketWSGIApplication(handler_cls=StreamingWebSocket))
        websocket_server.initialize_websockets_manager()
        websocket_thread = Thread(target=websocket_server.serve_forever)
        print('Initializing HTTP server on port %d' % HTTP_PORT)
        http_server = StreamingHttpServer()
        http_thread = Thread(target=http_server.serve_forever)
        print('Initializing broadcast thread')
        output = BroadcastOutput(camera)
        broadcast_thread = BroadcastThread(output.converter, websocket_server)
        print('Starting recording')
        camera.start_recording(output, 'yuv')
        try:
            print('Starting websockets thread')
            websocket_thread.start()
            print('Starting HTTP server thread')
            http_thread.start()
            print('Starting broadcast thread')
            broadcast_thread.start()
            mean_NO = 0
            score_mean = np.zeros(10)
            m_input_size, m_input_size = 96, 96
            picture_dir = '/home/pi/work/picture/'
            while whileMove:
                camera.wait_recording(2)
                camera.annotate_text = ''

##############
                with graph.as_default():
                    global water
                    global look

                    fileName = str(datetime.datetime.today().strftime("%Y%m%d_%H%M%S")) + '.jpg'
                    camera.capture(picture_dir + fileName,resize=(480, 480))
                    img = np.array(Image.open(picture_dir + fileName))[192:288, 192:288]
                    Image.fromarray(img).save(picture_dir + 'resize_' + fileName)
                    img = img.reshape((1,m_input_size, m_input_size,3))
                    test = model.predict(img/255)
                    test = test.reshape((len(test),-1))
                    test = ms.transform(test)
                    score = -clf._decision_function(test)
                    score_mean[mean_NO] = score[0]
                    mean_NO += 1
                    if mean_NO == len(score_mean):
                         mean_NO = 0
                    nowscore = np.mean(score_mean)
                    print("{:.1f} Score".format(nowscore))

                    if SCORE_THRESHOLD_UNDER < np.mean(score_mean) < SCORE_THRESHOLD_UPPER:
                        camera.annotate_foreground = Color('red')
                    else :
                        camera.annotate_foreground = Color('green')


                    if look :
                        files = {'file': open(picture_dir + fileName, 'rb')}
                        sendSlack(fileName, files, LOOK_COMMENT)
                        look = False
                        print("現在の状態を送信")
                    elif water :
                        if SCORE_THRESHOLD_UNDER < np.mean(score_mean) < SCORE_THRESHOLD_UPPER:
                            files = {'file': open(picture_dir + fileName, 'rb')}
                            sendSlack(fileName, files, WATER_COMMENT)
                            print("水やりが必要")
                        else :
                            files = {'file': open(picture_dir + fileName, 'rb')}
                            sendSlack(fileName, files, NOT_WATER_COMMENT)
                            print("水は十分")
                        water = False
                    camera.annotate_text = str('{:.1f}'.format(nowscore)) + " Score"

                    #if whileMove :
                    #    sleep(4)
##############

        except KeyboardInterrupt:
            pass
        finally:
            whileMove = True
            print('Stopping recording')
            camera.stop_recording()
            print('Waiting for broadcast thread to finish')
            broadcast_thread.join()
            print('Shutting down HTTP server')
            http_server.shutdown()
            print('Shutting down websockets server')
            websocket_server.shutdown()
            print('Waiting for HTTP server thread to finish')
            http_thread.join()
            print('Waiting for websockets thread to finish')
            websocket_thread.join()

def single():
    global graph
    global water
    global look
    print('Initializing camera')
    with picamera.PiCamera() as camera:
        camera.resolution = (WIDTH, HEIGHT)
        camera.framerate = FRAMERATE
        camera.vflip = VFLIP # flips image rightside up, as needed
        camera.hflip = HFLIP # flips image left-right, as needed
        sleep(1) # camera warm-up time

        try:
            mean_NO = 0
            score_mean = np.zeros(10)
            m_input_size, m_input_size = 96, 96
            picture_dir = '/home/pi/work/picture/'

##############
            with graph.as_default():
                fileName = str(datetime.datetime.today().strftime("%Y%m%d_%H%M%S")) + '.jpg'
                camera.capture(picture_dir + fileName,resize=(480, 480))
                img = np.array(Image.open(picture_dir + fileName))[192:288, 192:288]
                Image.fromarray(img).save(picture_dir + 'resize_' + fileName)
                img = img.reshape((1,m_input_size, m_input_size,3))
                test = model.predict(img/255)
                test = test.reshape((len(test),-1))
                test = ms.transform(test)
                score = -clf._decision_function(test)
                score_mean[mean_NO] = score[0]
                mean_NO += 1
                if mean_NO == len(score_mean):
                     mean_NO = 0
                print("{:.1f} Score".format(np.mean(score_mean)))

                GPIO.setmode(GPIO.BCM)
                GPIO.setup(27, GPIO.OUT)
                
                if look :
                    files = {'file': open(picture_dir + fileName, 'rb')}
                    sendSlack(fileName, files, LOOK_COMMENT)
                    look = False
                    print("現在の状態を送信")
                elif water :
                    if SCORE_THRESHOLD_UNDER < np.mean(score_mean) < SCORE_THRESHOLD_UPPER:
                        files = {'file': open(picture_dir + fileName, 'rb')}
                        sendSlack(fileName, files, WATER_COMMENT)
                        print("水やりが必要")
                    else :
                        files = {'file': open(picture_dir + fileName, 'rb')}
                        sendSlack(fileName, files, NOT_WATER_COMMENT)
                        print("水は十分")
                    water = False
                    
                sleep(2)
                GPIO.setup(27, GPIO.IN)

##############

        except KeyboardInterrupt:
            pass
        finally:
            print('Finish')

# 引数を使ってSlackにメッセージを飛ばします
def sendSlack(fileName, files, comment):
    param = {
        'token':TOKEN,
        'channels':CHANNEL,
        'filename':"filename",
        'initial_comment': comment,
        'title': fileName
    }
    requests.post(url="https://slack.com/api/files.upload",params=param, files=files)

# 引数を使ってwaterの値を変更します
def setWater(status):
    global water
    water = status

# 引数を使ってlookの値を変更します
def setLook(status):
    global look
    look = status

# 引数を使ってwhileMoveの値を変更します
def setWhileMove(status):
    global whileMove
    whileMove = status

# waterの値を返却します
def getWater():
    global water
    return water

# lookの値を返却します
def getLook():
    global look
    return look

# whileMoveの値を返却します
def getWhileMove():
    global whileMove
    return whileMove

if __name__ == '__main__':
    main()
