from slackbot.bot import respond_to, listen_to
import server

waterWork = False
notWaterWork = False

# 「水あげる」「監視」等に反応するようにします
@listen_to('(みず|水)+.*(監視|かんし|管理|かんり|自動|じどう|見|観)+.*(開始|かいし|すたーと|スタート)')
@respond_to('(みず|水)+.*(監視|かんし|管理|かんり|自動|じどう|見|観)+.*(開始|かいし|すたーと|スタート)')
def monitoringWater(message, *something):
    global waterWork
    global notWaterWork
    # 水やり監視起動
    if waterWork :
        message.reply('水やり監視モードが既に起動中です。')
    elif notWaterWork :
        message.reply('通常監視モードが既に起動中です。重複起動はできません。')
    else :
        message.reply('水やり監視モードを起動します。')
        waterWork = True
        server.main()

# 「普通」「監視」等に反応するようにします
@listen_to('(ふつう|普通|つうじょう|通常|ただ)+.*(監視|かんし|管理|かんり|見|観)+.*(開始|かいし|すたーと|スタート)')
@respond_to('(ふつう|普通|つうじょう|通常|ただ)+.*(監視|かんし|管理|かんり|見|観)+.*(開始|かいし|すたーと|スタート)')
def monitoringNotWater(message, *something):
    global waterWork
    global notWaterWork
    # 通常監視起動
    if waterWork :
        message.reply('水やり監視モードが既に起動中です。重複起動はできません。')
    elif notWaterWork :
        message.reply('通常監視モードが既に起動中です。')
    else :
        message.reply('通常監視モードを起動します。')
        notWaterWork = True
        server.autoLook()

# 「監視」「終了」等に反応するようにします
@listen_to('(監視|かんし|管理|かんり|自動|じどう|見|観)+.*(終|もういいよ|お疲れ|おつ)')
@respond_to('(監視|かんし|管理|かんり|自動|じどう|見|観)+.*(終|もういいよ|お疲れ|おつ)')
def monitoringFinish(message, *something):
    global waterWork
    global notWaterWork
    # 監視終了
    if waterWork :
        message.reply('水やり監視モードを終了します。')
        waterWork = False
        server.setWhileMove(False)
    elif notWaterWork :
        message.reply('通常監視モードを終了します。')
        notWaterWork = False
        server.setWhileMove(False)
    else :
        message.reply('現在監視は行っていません。')

# 「水あげる」等に反応するようにします
@listen_to('(みず|水)+(上げ|あげ|やり)+(確認|かくにん)')
@respond_to('(みず|水)+(上げ|あげ|やり)+(確認|かくにん)')
def water(message, *something):
    global waterWork
    global notWaterWork
    # 水やり
    if waterWork :
        message.reply('水やり監視モードが既に起動中です。水やりは私に任せてください。')
    else :
        message.reply('土の乾き具合を確認します。')
        if notWaterWork :
            server.setWater(True)
        else :
            server.setWater(True)
            server.single()

# 「ようす」等に反応するようにします
@listen_to('(ようす|様子|げんざい|現在|いま|今)')
@respond_to('(ようす|様子|げんざい|現在|いま|今)')
def check(message, *something):
    # 様子見
    global waterWork
    global notWaterWork
    message.reply('現在の様子を確認します。')
    if waterWork :
        server.setLook(True)
    if notWaterWork :
        server.setLook(True)
    else :
        server.setLook(True)
        server.single()

# 「おはよう」等に反応するようにします
@listen_to('おは+(よ|よう)')
@respond_to('おは+(よ|よう)')
def morning(message, *something):
    # おはよう
    message.reply('おはようございます、今日も一日がんばりましょう。')

# 「こんにちは」等に反応するようにします
@listen_to('こん+(にちは|にちわ|ちは|ちわ)')
@respond_to('こん+(にちは|にちわ|ちは|ちわ)')
def after(message, *something):
    # こんにちは
    message.reply('こんにちは、お昼は眠たくなってきますね。')

# 「こんばんは」等に反応するようにします
@listen_to('こん+(ばんは|ばんわ)')
@respond_to('こん+(ばんは|ばんわ)')
def night(message, *something):
    # こんばんは
    message.reply('こんばんは、私も今日はそこそこに頑張りました。')

# 「おやすみ」等に反応するようにします
@listen_to('おや+(す|すみ)')
@respond_to('おや+(す|すみ)')
@listen_to('また+(あした|明日)')
@respond_to('また+(あした|明日)')
def sleep(message, *something):
    # おやすみ
    message.reply('おやすみなさい、今日も一日お疲れ様でした。')

# 「いる」等に反応するようにします
@respond_to('(おき|起き)+てる')
def there(message, *something):
    # いる
    message.reply('います。')

# 「アカシヤ」等に反応するようにします
@respond_to('(あか|アカ)+(しあ|しや|シア|シヤ)')
def akashiya(message, *something):
    # アカシヤ
    message.reply('おすすめは2の小です。')
