/*
002 * Licensed to the Apache Software Foundation (ASF) under one or more
003 * contributor license agreements.  See the NOTICE file distributed with
004 * this work for additional information regarding copyright ownership.
005 * The ASF licenses this file to You under the Apache License, Version 2.0
006 * (the "License"); you may not use this file except in compliance with
007 * the License.  You may obtain a copy of the License at
008 *
009 *      http://www.apache.org/licenses/LICENSE-2.0
010 *
011 * Unless required by applicable law or agreed to in writing, software
012 * distributed under the License is distributed on an "AS IS" BASIS,
013 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
014 * See the License for the specific language governing permissions and
015 * limitations under the License.
016 */
017package org.apache.commons.lang3;
018
019import java.io.UnsupportedEncodingException;
020import java.nio.charset.Charset;
021import java.text.Normalizer;
022import java.util.ArrayList;
023import java.util.Arrays;
024import java.util.Iterator;
025import java.util.List;
026import java.util.Locale;
027import java.util.Objects;
028import java.util.function.Supplier;
029import java.util.regex.Pattern;
030
031/**
032 * <p>Operations on {@link java.lang.String} that are
033 * {@code null} safe.</p>
034 *
035 * <ul>
036 *  <li><b>IsEmpty/IsBlank</b>
037 *      - checks if a String contains text</li>
038 *  <li><b>Trim/Strip</b>
039 *      - removes leading and trailing whitespace</li>
040 *  <li><b>Equals/Compare</b>
041 *      - compares two strings null-safe</li>
042 *  <li><b>startsWith</b>
043 *      - check if a String starts with a prefix null-safe</li>
044 *  <li><b>endsWith</b>
045 *      - check if a String ends with a suffix null-safe</li>
046 *  <li><b>IndexOf/LastIndexOf/Contains</b>
047 *      - null-safe index-of checks
048 *  <li><b>IndexOfAny/LastIndexOfAny/IndexOfAnyBut/LastIndexOfAnyBut</b>
049 *      - index-of any of a set of Strings</li>
050 *  <li><b>ContainsOnly/ContainsNone/ContainsAny</b>
051 *      - does String contains only/none/any of these characters</li>
052 *  <li><b>Substring/Left/Right/Mid</b>
053 *      - null-safe substring extractions</li>
054 *  <li><b>SubstringBefore/SubstringAfter/SubstringBetween</b>
055 *      - substring extraction relative to other strings</li>
056 *  <li><b>Split/Join</b>
057 *      - splits a String into an array of substrings and vice versa</li>
058 *  <li><b>Remove/Delete</b>
059 *      - removes part of a String</li>
060 *  <li><b>Replace/Overlay</b>
061 *      - Searches a String and replaces one String with another</li>
062 *  <li><b>Chomp/Chop</b>
063 *      - removes the last part of a String</li>
064 *  <li><b>AppendIfMissing</b>
065 *      - appends a suffix to the end of the String if not present</li>
066 *  <li><b>PrependIfMissing</b>
067 *      - prepends a prefix to the start of the String if not present</li>
068 *  <li><b>LeftPad/RightPad/Center/Repeat</b>
069 *      - pads a String</li>
070 *  <li><b>UpperCase/LowerCase/SwapCase/Capitalize/Uncapitalize</b>
071 *      - changes the case of a String</li>
072 *  <li><b>CountMatches</b>
073 *      - counts the number of occurrences of one String in another</li>
074 *  <li><b>IsAlpha/IsNumeric/IsWhitespace/IsAsciiPrintable</b>
075 *      - checks the characters in a String</li>
076 *  <li><b>DefaultString</b>
077 *      - protects against a null input String</li>
078 *  <li><b>Rotate</b>
079 *      - rotate (circular shift) a String</li>
080 *  <li><b>Reverse/ReverseDelimited</b>
081 *      - reverses a String</li>
082 *  <li><b>Abbreviate</b>
083 *      - abbreviates a string using ellipsis or another given String</li>
084 *  <li><b>Difference</b>
085 *      - compares Strings and reports on their differences</li>
086 *  <li><b>LevenshteinDistance</b>
087 *      - the number of changes needed to change one String into another</li>
088 * </ul>
089 *
090 * <p>The {@code StringUtils} class defines certain words related to
091 * String handling.</p>
092 *
093 * <ul>
094 *  <li>null - {@code null}</li>
095 *  <li>empty - a zero-length string ({@code ""})</li>
096 *  <li>space - the space character ({@code ' '}, char 32)</li>
097 *  <li>whitespace - the characters defined by {@link Character#isWhitespace(char)}</li>
098 *  <li>trim - the characters &lt;= 32 as in {@link String#trim()}</li>
099 * </ul>
100 *
101 * <p>{@code StringUtils} handles {@code null} input Strings quietly.
102 * That is to say that a {@code null} input will return {@code null}.
103 * Where a {@code boolean} or {@code int} is being returned
104 * details vary by method.</p>
105 *
106 * <p>A side effect of the {@code null} handling is that a
107 * {@code NullPointerException} should be considered a bug in
108 * {@code StringUtils}.</p>
109 *
110 * <p>Methods in this class give sample code to explain their operation.
111 * The symbol {@code *} is used to indicate any input including {@code null}.</p>
112 *
113 * <p>#ThreadSafe#</p>
114 * @see java.lang.String
115 * @since 1.0
116 */
117//@Immutable
118public class StringUtils {
119
120    private static final int STRING_BUILDER_SIZE = 256;
121
122    // Performance testing notes (JDK 1.4, Jul03, scolebourne)
123    // Whitespace:
124    // Character.isWhitespace() is faster than WHITESPACE.indexOf()
125    // where WHITESPACE is a string of all whitespace characters
126    //
127    // Character access:
128    // String.charAt(n) versus toCharArray(), then array[n]
129    // String.charAt(n) is about 15% worse for a 10K string
130    // They are about equal for a length 50 string
131    // String.charAt(n) is about 4 times better for a length 3 string
132    // String.charAt(n) is best bet overall
133    //
134    // Append:
135    // String.concat about twice as fast as StringBuffer.append
136    // (not sure who tested this)
137
138    /**
139     * A String for a space character.
140     *
141     * @since 3.2
142     */
143    public static final String SPACE = " ";
144
145    /**
146     * The empty String {@code ""}.
147     * @since 2.0
148     */
149    public static final String EMPTY = "";
150
151    /**
152     * A String for linefeed LF ("\n").
153     *
154     * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6">JLF: Escape Sequences
155     *      for Character and String Literals</a>
156     * @since 3.2
157     */
158    public static final String LF = "\n";
159
160    /**
161     * A String for carriage return CR ("\r").
162     *
163     * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6">JLF: Escape Sequences
164     *      for Character and String Literals</a>
165     * @since 3.2
166     */
167    public static final String CR = "\r";
168
169    /**
170     * Represents a failed index search.
171     * @since 2.1
172     */
173    public static final int INDEX_NOT_FOUND = -1;
174
175    /**
176     * <p>The maximum size to which the padding constant(s) can expand.</p>
177     */
178    private static final int PAD_LIMIT = 8192;
179
180    // Abbreviating
181    //-----------------------------------------------------------------------
182    /**
183     * <p>Abbreviates a String using ellipses. This will turn
184     * "Now is the time for all good men" into "Now is the time for..."</p>
185     *
186     * <p>Specifically:</p>
187     * <ul>
188     *   <li>If the number of characters in {@code str} is less than or equal to
189     *       {@code maxWidth}, return {@code str}.</li>
190     *   <li>Else abbreviate it to {@code (substring(str, 0, max-3) + "...")}.</li>
191     *   <li>If {@code maxWidth} is less than {@code 4}, throw an
192     *       {@code IllegalArgumentException}.</li>
193     *   <li>In no case will it return a String of length greater than
194     *       {@code maxWidth}.</li>
195     * </ul>
196     *
197     * <pre>
198     * StringUtils.abbreviate(null, *)      = null
199     * StringUtils.abbreviate("", 4)        = ""
200     * StringUtils.abbreviate("abcdefg", 6) = "abc..."
201     * StringUtils.abbreviate("abcdefg", 7) = "abcdefg"
202     * StringUtils.abbreviate("abcdefg", 8) = "abcdefg"
203     * StringUtils.abbreviate("abcdefg", 4) = "a..."
204     * StringUtils.abbreviate("abcdefg", 3) = IllegalArgumentException
205     * </pre>
206     *
207     * @param str  the String to check, may be null
208     * @param maxWidth  maximum length of result String, must be at least 4
209     * @return abbreviated String, {@code null} if null String input
210     * @throws IllegalArgumentException if the width is too small
211     * @since 2.0
212     */
213    public static String abbreviate(final String str, final int maxWidth) {
214        return abbreviate(str, "...", 0, maxWidth);
215    }
216
217    /**
218     * <p>Abbreviates a String using ellipses. This will turn
219     * "Now is the time for all good men" into "...is the time for..."</p>
220     *
221     * <p>Works like {@code abbreviate(String, int)}, but allows you to specify
222     * a "left edge" offset.  Note that this left edge is not necessarily going to
223     * be the leftmost character in the result, or the first character following the
224     * ellipses, but it will appear somewhere in the result.
225     *
226     * <p>In no case will it return a String of length greater than
227     * {@code maxWidth}.</p>
228     *
229     * <pre>
230     * StringUtils.abbreviate(null, *, *)                = null
231     * StringUtils.abbreviate("", 0, 4)                  = ""
232     * StringUtils.abbreviate("abcdefghijklmno", -1, 10) = "abcdefg..."
233     * StringUtils.abbreviate("abcdefghijklmno", 0, 10)  = "abcdefg..."
234     * StringUtils.abbreviate("abcdefghijklmno", 1, 10)  = "abcdefg..."
235     * StringUtils.abbreviate("abcdefghijklmno", 4, 10)  = "abcdefg..."
236     * StringUtils.abbreviate("abcdefghijklmno", 5, 10)  = "...fghi..."
237     * StringUtils.abbreviate("abcdefghijklmno", 6, 10)  = "...ghij..."
238     * StringUtils.abbreviate("abcdefghijklmno", 8, 10)  = "...ijklmno"
239     * StringUtils.abbreviate("abcdefghijklmno", 10, 10) = "...ijklmno"
240     * StringUtils.abbreviate("abcdefghijklmno", 12, 10) = "...ijklmno"
241     * StringUtils.abbreviate("abcdefghij", 0, 3)        = IllegalArgumentException
242     * StringUtils.abbreviate("abcdefghij", 5, 6)        = IllegalArgumentException
243     * </pre>
244     *
245     * @param str  the String to check, may be null
246     * @param offset  left edge of source String
247     * @param maxWidth  maximum length of result String, must be at least 4
248     * @return abbreviated String, {@code null} if null String input
249     * @throws IllegalArgumentException if the width is too small
250     * @since 2.0
251     */
252    public static String abbreviate(final String str, final int offset, final int maxWidth) {
253        return abbreviate(str, "...", offset, maxWidth);
254    }
255
256    /**
257     * <p>Abbreviates a String using another given String as replacement marker. This will turn
258     * "Now is the time for all good men" into "Now is the time for..." if "..." was defined
259     * as the replacement marker.</p>
260     *
261     * <p>Specifically:</p>
262     * <ul>
263     *   <li>If the number of characters in {@code str} is less than or equal to
264     *       {@code maxWidth}, return {@code str}.</li>
265     *   <li>Else abbreviate it to {@code (substring(str, 0, max-abbrevMarker.length) + abbrevMarker)}.</li>
266     *   <li>If {@code maxWidth} is less than {@code abbrevMarker.length + 1}, throw an
267     *       {@code IllegalArgumentException}.</li>
268     *   <li>In no case will it return a String of length greater than
269     *       {@code maxWidth}.</li>
270     * </ul>
271     *
272     * <pre>
273     * StringUtils.abbreviate(null, "...", *)      = null
274     * StringUtils.abbreviate("abcdefg", null, *)  = "abcdefg"
275     * StringUtils.abbreviate("", "...", 4)        = ""
276     * StringUtils.abbreviate("abcdefg", ".", 5)   = "abcd."
277     * StringUtils.abbreviate("abcdefg", ".", 7)   = "abcdefg"
278     * StringUtils.abbreviate("abcdefg", ".", 8)   = "abcdefg"
279     * StringUtils.abbreviate("abcdefg", "..", 4)  = "ab.."
280     * StringUtils.abbreviate("abcdefg", "..", 3)  = "a.."
281     * StringUtils.abbreviate("abcdefg", "..", 2)  = IllegalArgumentException
282     * StringUtils.abbreviate("abcdefg", "...", 3) = IllegalArgumentException
283     * </pre>
284     *
285     * @param str  the String to check, may be null
286     * @param abbrevMarker  the String used as replacement marker
287     * @param maxWidth  maximum length of result String, must be at least {@code abbrevMarker.length + 1}
288     * @return abbreviated String, {@code null} if null String input
289     * @throws IllegalArgumentException if the width is too small
290     * @since 3.6
291     */
292    public static String abbreviate(final String str, final String abbrevMarker, final int maxWidth) {
293        return abbreviate(str, abbrevMarker, 0, maxWidth);
294    }
295    /**
296     * <p>Abbreviates a String using a given replacement marker. This will turn
297     * "Now is the time for all good men" into "...is the time for..." if "..." was defined
298     * as the replacement marker.</p>
299     *
300     * <p>Works like {@code abbreviate(String, String, int)}, but allows you to specify
301     * a "left edge" offset.  Note that this left edge is not necessarily going to
302     * be the leftmost character in the result, or the first character following the
303     * replacement marker, but it will appear somewhere in the result.
304     *
305     * <p>In no case will it return a String of length greater than {@code maxWidth}.</p>
306     *
307     * <pre>
308     * StringUtils.abbreviate(null, null, *, *)                 = null
309     * StringUtils.abbreviate("abcdefghijklmno", null, *, *)    = "abcdefghijklmno"
310     * StringUtils.abbreviate("", "...", 0, 4)                  = ""
311     * StringUtils.abbreviate("abcdefghijklmno", "---", -1, 10) = "abcdefg---"
312     * StringUtils.abbreviate("abcdefghijklmno", ",", 0, 10)    = "abcdefghi,"
313     * StringUtils.abbreviate("abcdefghijklmno", ",", 1, 10)    = "abcdefghi,"
314     * StringUtils.abbreviate("abcdefghijklmno", ",", 2, 10)    = "abcdefghi,"
315     * StringUtils.abbreviate("abcdefghijklmno", "::", 4, 10)   = "::efghij::"
316     * StringUtils.abbreviate("abcdefghijklmno", "...", 6, 10)  = "...ghij..."
317     * StringUtils.abbreviate("abcdefghijklmno", "*", 9, 10)    = "*ghijklmno"
318     * StringUtils.abbreviate("abcdefghijklmno", "'", 10, 10)   = "'ghijklmno"
319     * StringUtils.abbreviate("abcdefghijklmno", "!", 12, 10)   = "!ghijklmno"
320     * StringUtils.abbreviate("abcdefghij", "abra", 0, 4)       = IllegalArgumentException
321     * StringUtils.abbreviate("abcdefghij", "...", 5, 6)        = IllegalArgumentException
322     * </pre>
323     *
324     * @param str  the String to check, may be null
325     * @param abbrevMarker  the String used as replacement marker
326     * @param offset  left edge of source String
327     * @param maxWidth  maximum length of result String, must be at least 4
328     * @return abbreviated String, {@code null} if null String input
329     * @throws IllegalArgumentException if the width is too small
330     * @since 3.6
331     */
332    public static String abbreviate(final String str, final String abbrevMarker, int offset, final int maxWidth) {
333        if (isEmpty(str) && isEmpty(abbrevMarker)) {
334            return str;
335        } else if (isNotEmpty(str) && EMPTY.equals(abbrevMarker) && maxWidth > 0) {
336            return str.substring(0, maxWidth);
337        } else if (isEmpty(str) || isEmpty(abbrevMarker)) {
338            return str;
339        }
340        final int abbrevMarkerLength = abbrevMarker.length();
341        final int minAbbrevWidth = abbrevMarkerLength + 1;
342        final int minAbbrevWidthOffset = abbrevMarkerLength + abbrevMarkerLength + 1;
343
344        if (maxWidth < minAbbrevWidth) {
345            throw new IllegalArgumentException(String.format("Minimum abbreviation width is %d", minAbbrevWidth));
346        }
347        if (str.length() <= maxWidth) {
348            return str;
349        }
350        if (offset > str.length()) {
351            offset = str.length();
352        }
353        if (str.length() - offset < maxWidth - abbrevMarkerLength) {
354            offset = str.length() - (maxWidth - abbrevMarkerLength);
355        }
356        if (offset <= abbrevMarkerLength+1) {
357            return str.substring(0, maxWidth - abbrevMarkerLength) + abbrevMarker;
358        }
359        if (maxWidth < minAbbrevWidthOffset) {
360            throw new IllegalArgumentException(String.format("Minimum abbreviation width with offset is %d", minAbbrevWidthOffset));
361        }
362        if (offset + maxWidth - abbrevMarkerLength < str.length()) {
363            return abbrevMarker + abbreviate(str.substring(offset), abbrevMarker, maxWidth - abbrevMarkerLength);
364        }
365        return abbrevMarker + str.substring(str.length() - (maxWidth - abbrevMarkerLength));
366    }
367
368    /**
369     * <p>Abbreviates a String to the length passed, replacing the middle characters with the supplied
370     * replacement String.</p>
371     *
372     * <p>This abbreviation only occurs if the following criteria is met:</p>
373     * <ul>
374     * <li>Neither the String for abbreviation nor the replacement String are null or empty </li>
375     * <li>The length to truncate to is less than the length of the supplied String</li>
376     * <li>The length to truncate to is greater than 0</li>
377     * <li>The abbreviated String will have enough room for the length supplied replacement String
378     * and the first and last characters of the supplied String for abbreviation</li>
379     * </ul>
380     * <p>Otherwise, the returned String will be the same as the supplied String for abbreviation.
381     * </p>
382     *
383     * <pre>
384     * StringUtils.abbreviateMiddle(null, null, 0)      = null
385     * StringUtils.abbreviateMiddle("abc", null, 0)      = "abc"
386     * StringUtils.abbreviateMiddle("abc", ".", 0)      = "abc"
387     * StringUtils.abbreviateMiddle("abc", ".", 3)      = "abc"
388     * StringUtils.abbreviateMiddle("abcdef", ".", 4)     = "ab.f"
389     * </pre>
390     *
391     * @param str  the String to abbreviate, may be null
392     * @param middle the String to replace the middle characters with, may be null
393     * @param length the length to abbreviate {@code str} to.
394     * @return the abbreviated String if the above criteria is met, or the original String supplied for abbreviation.
395     * @since 2.5
396     */
397    public static String abbreviateMiddle(final String str, final String middle, final int length) {
398        if (isEmpty(str) || isEmpty(middle)) {
399            return str;
400        }
401
402        if (length >= str.length() || length < middle.length()+2) {
403            return str;
404        }
405
406        final int targetSting = length-middle.length();
407        final int startOffset = targetSting/2+targetSting%2;
408        final int endOffset = str.length()-targetSting/2;
409
410        return str.substring(0, startOffset) +
411            middle +
412            str.substring(endOffset);
413    }
414
415    /**
416     * Appends the suffix to the end of the string if the string does not
417     * already end with the suffix.
418     *
419     * @param str The string.
420     * @param suffix The suffix to append to the end of the string.
421     * @param ignoreCase Indicates whether the compare should ignore case.
422     * @param suffixes Additional suffixes that are valid terminators (optional).
423     *
424     * @return A new String if suffix was appended, the same string otherwise.
425     */
426    private static String appendIfMissing(final String str, final CharSequence suffix, final boolean ignoreCase, final CharSequence... suffixes) {
427        if (str == null || isEmpty(suffix) || endsWith(str, suffix, ignoreCase)) {
428            return str;
429        }
430        if (ArrayUtils.isNotEmpty(suffixes)) {
431            for (final CharSequence s : suffixes) {
432                if (endsWith(str, s, ignoreCase)) {
433                    return str;
434                }
435            }
436        }
437        return str + suffix.toString();
438    }
439
440    /**
441     * Appends the suffix to the end of the string if the string does not
442     * already end with any of the suffixes.
443     *
444     * <pre>
445     * StringUtils.appendIfMissing(null, null) = null
446     * StringUtils.appendIfMissing("abc", null) = "abc"
447     * StringUtils.appendIfMissing("", "xyz") = "xyz"
448     * StringUtils.appendIfMissing("abc", "xyz") = "abcxyz"
449     * StringUtils.appendIfMissing("abcxyz", "xyz") = "abcxyz"
450     * StringUtils.appendIfMissing("abcXYZ", "xyz") = "abcXYZxyz"
451     * </pre>
452     * <p>With additional suffixes,</p>
453     * <pre>
454     * StringUtils.appendIfMissing(null, null, null) = null
455     * StringUtils.appendIfMissing("abc", null, null) = "abc"
456     * StringUtils.appendIfMissing("", "xyz", null) = "xyz"
457     * StringUtils.appendIfMissing("abc", "xyz", new CharSequence[]{null}) = "abcxyz"
458     * StringUtils.appendIfMissing("abc", "xyz", "") = "abc"
459     * StringUtils.appendIfMissing("abc", "xyz", "mno") = "abcxyz"
460     * StringUtils.appendIfMissing("abcxyz", "xyz", "mno") = "abcxyz"
461     * StringUtils.appendIfMissing("abcmno", "xyz", "mno") = "abcmno"
462     * StringUtils.appendIfMissing("abcXYZ", "xyz", "mno") = "abcXYZxyz"
463     * StringUtils.appendIfMissing("abcMNO", "xyz", "mno") = "abcMNOxyz"
464     * </pre>
465     *
466     * @param str The string.
467     * @param suffix The suffix to append to the end of the string.
468     * @param suffixes Additional suffixes that are valid terminators.
469     *
470     * @return A new String if suffix was appended, the same string otherwise.
471     *
472     * @since 3.2
473     */
474    public static String appendIfMissing(final String str, final CharSequence suffix, final CharSequence... suffixes) {
475        return appendIfMissing(str, suffix, false, suffixes);
476    }
477
478    /**
479     * Appends the suffix to the end of the string if the string does not
480     * already end, case insensitive, with any of the suffixes.
481     *
482     * <pre>
483     * StringUtils.appendIfMissingIgnoreCase(null, null) = null
484     * StringUtils.appendIfMissingIgnoreCase("abc", null) = "abc"
485     * StringUtils.appendIfMissingIgnoreCase("", "xyz") = "xyz"
486     * StringUtils.appendIfMissingIgnoreCase("abc", "xyz") = "abcxyz"
487     * StringUtils.appendIfMissingIgnoreCase("abcxyz", "xyz") = "abcxyz"
488     * StringUtils.appendIfMissingIgnoreCase("abcXYZ", "xyz") = "abcXYZ"
489     * </pre>
490     * <p>With additional suffixes,</p>
491     * <pre>
492     * StringUtils.appendIfMissingIgnoreCase(null, null, null) = null
493     * StringUtils.appendIfMissingIgnoreCase("abc", null, null) = "abc"
494     * StringUtils.appendIfMissingIgnoreCase("", "xyz", null) = "xyz"
495     * StringUtils.appendIfMissingIgnoreCase("abc", "xyz", new CharSequence[]{null}) = "abcxyz"
496     * StringUtils.appendIfMissingIgnoreCase("abc", "xyz", "") = "abc"
497     * StringUtils.appendIfMissingIgnoreCase("abc", "xyz", "mno") = "abcxyz"
498     * StringUtils.appendIfMissingIgnoreCase("abcxyz", "xyz", "mno") = "abcxyz"
499     * StringUtils.appendIfMissingIgnoreCase("abcmno", "xyz", "mno") = "abcmno"
500     * StringUtils.appendIfMissingIgnoreCase("abcXYZ", "xyz", "mno") = "abcXYZ"
501     * StringUtils.appendIfMissingIgnoreCase("abcMNO", "xyz", "mno") = "abcMNO"
502     * </pre>
503     *
504     * @param str The string.
505     * @param suffix The suffix to append to the end of the string.
506     * @param suffixes Additional suffixes that are valid terminators.
507     *
508     * @return A new String if suffix was appended, the same string otherwise.
509     *
510     * @since 3.2
511     */
512    public static String appendIfMissingIgnoreCase(final String str, final CharSequence suffix, final CharSequence... suffixes) {
513        return appendIfMissing(str, suffix, true, suffixes);
514    }
515
516    /**
517     * <p>Capitalizes a String changing the first character to title case as
518     * per {@link Character#toTitleCase(int)}. No other characters are changed.</p>
519     *
520     * <p>For a word based algorithm, see {@link org.apache.commons.lang3.text.WordUtils#capitalize(String)}.
521     * A {@code null} input String returns {@code null}.</p>
522     *
523     * <pre>
524     * StringUtils.capitalize(null)  = null
525     * StringUtils.capitalize("")    = ""
526     * StringUtils.capitalize("cat") = "Cat"
527     * StringUtils.capitalize("cAt") = "CAt"
528     * StringUtils.capitalize("'cat'") = "'cat'"
529     * </pre>
530     *
531     * @param str the String to capitalize, may be null
532     * @return the capitalized String, {@code null} if null String input
533     * @see org.apache.commons.lang3.text.WordUtils#capitalize(String)
534     * @see #uncapitalize(String)
535     * @since 2.0
536     */
537    public static String capitalize(final String str) {
538        final int strLen = length(str);
539        if (strLen == 0) {
540            return str;
541        }
542
543        final int firstCodepoint = str.codePointAt(0);
544        final int newCodePoint = Character.toTitleCase(firstCodepoint);
545        if (firstCodepoint == newCodePoint) {
546            // already capitalized
547            return str;
548        }
549
550        final int newCodePoints[] = new int[strLen]; // cannot be longer than the char array
551        int outOffset = 0;
552        newCodePoints[outOffset++] = newCodePoint; // copy the first codepoint
553        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; ) {
554            final int codepoint = str.codePointAt(inOffset);
555            newCodePoints[outOffset++] = codepoint; // copy the remaining ones
556            inOffset += Character.charCount(codepoint);
557         }
558        return new String(newCodePoints, 0, outOffset);
559    }
560
561    // Centering
562    //-----------------------------------------------------------------------
563    /**
564     * <p>Centers a String in a larger String of size {@code size}
565     * using the space character (' ').</p>
566     *
567     * <p>If the size is less than the String length, the String is returned.
568     * A {@code null} String returns {@code null}.
569     * A negative size is treated as zero.</p>
570     *
571     * <p>Equivalent to {@code center(str, size, " ")}.</p>
572     *
573     * <pre>
574     * StringUtils.center(null, *)   = null
575     * StringUtils.center("", 4)     = "    "
576     * StringUtils.center("ab", -1)  = "ab"
577     * StringUtils.center("ab", 4)   = " ab "
578     * StringUtils.center("abcd", 2) = "abcd"
579     * StringUtils.center("a", 4)    = " a  "
580     * </pre>
581     *
582     * @param str  the String to center, may be null
583     * @param size  the int size of new String, negative treated as zero
584     * @return centered String, {@code null} if null String input
585     */
586    public static String center(final String str, final int size) {
587        return center(str, size, ' ');
588    }
589
590    /**
591     * <p>Centers a String in a larger String of size {@code size}.
592     * Uses a supplied character as the value to pad the String with.</p>
593     *
594     * <p>If the size is less than the String length, the String is returned.
595     * A {@code null} String returns {@code null}.
596     * A negative size is treated as zero.</p>
597     *
598     * <pre>
599     * StringUtils.center(null, *, *)     = null
600     * StringUtils.center("", 4, ' ')     = "    "
601     * StringUtils.center("ab", -1, ' ')  = "ab"
602     * StringUtils.center("ab", 4, ' ')   = " ab "
603     * StringUtils.center("abcd", 2, ' ') = "abcd"
604     * StringUtils.center("a", 4, ' ')    = " a  "
605     * StringUtils.center("a", 4, 'y')    = "yayy"
606     * </pre>
607     *
608     * @param str  the String to center, may be null
609     * @param size  the int size of new String, negative treated as zero
610     * @param padChar  the character to pad the new String with
611     * @return centered String, {@code null} if null String input
612     * @since 2.0
613     */
614    public static String center(String str, final int size, final char padChar) {
615        if (str == null || size <= 0) {
616            return str;
617        }
618        final int strLen = str.length();
619        final int pads = size - strLen;
620        if (pads <= 0) {
621            return str;
622        }
623        str = leftPad(str, strLen + pads / 2, padChar);
624        str = rightPad(str, size, padChar);
625        return str;
626    }
627
628    /**
629     * <p>Centers a String in a larger String of size {@code size}.
630     * Uses a supplied String as the value to pad the String with.</p>
631     *
632     * <p>If the size is less than the String length, the String is returned.
633     * A {@code null} String returns {@code null}.
634     * A negative size is treated as zero.</p>
635     *
636     * <pre>
637     * StringUtils.center(null, *, *)     = null
638     * StringUtils.center("", 4, " ")     = "    "
639     * StringUtils.center("ab", -1, " ")  = "ab"
640     * StringUtils.center("ab", 4, " ")   = " ab "
641     * StringUtils.center("abcd", 2, " ") = "abcd"
642     * StringUtils.center("a", 4, " ")    = " a  "
643     * StringUtils.center("a", 4, "yz")   = "yayz"
644     * StringUtils.center("abc", 7, null) = "  abc  "
645     * StringUtils.center("abc", 7, "")   = "  abc  "
646     * </pre>
647     *
648     * @param str  the String to center, may be null
649     * @param size  the int size of new String, negative treated as zero
650     * @param padStr  the String to pad the new String with, must not be null or empty
651     * @return centered String, {@code null} if null String input
652     * @throws IllegalArgumentException if padStr is {@code null} or empty
653     */
654    public static String center(String str, final int size, String padStr) {
655        if (str == null || size <= 0) {
656            return str;
657        }
658        if (isEmpty(padStr)) {
659            padStr = SPACE;
660        }
661        final int strLen = str.length();
662        final int pads = size - strLen;
663        if (pads <= 0) {
664            return str;
665        }
666        str = leftPad(str, strLen + pads / 2, padStr);
667        str = rightPad(str, size, padStr);
668        return str;
669    }
670
671    // Chomping
672    //-----------------------------------------------------------------------
673    /**
674     * <p>Removes one newline from end of a String if it's there,
675     * otherwise leave it alone.  A newline is &quot;{@code \n}&quot;,
676     * &quot;{@code \r}&quot;, or &quot;{@code \r\n}&quot;.</p>
677     *
678     * <p>NOTE: This method changed in 2.0.
679     * It now more closely matches Perl chomp.</p>
680     *
681     * <pre>
682     * StringUtils.chomp(null)          = null
683     * StringUtils.chomp("")            = ""
684     * StringUtils.chomp("abc \r")      = "abc "
685     * StringUtils.chomp("abc\n")       = "abc"
686     * StringUtils.chomp("abc\r\n")     = "abc"
687     * StringUtils.chomp("abc\r\n\r\n") = "abc\r\n"
688     * StringUtils.chomp("abc\n\r")     = "abc\n"
689     * StringUtils.chomp("abc\n\rabc")  = "abc\n\rabc"
690     * StringUtils.chomp("\r")          = ""
691     * StringUtils.chomp("\n")          = ""
692     * StringUtils.chomp("\r\n")        = ""
693     * </pre>
694     *
695     * @param str  the String to chomp a newline from, may be null
696     * @return String without newline, {@code null} if null String input
697     */
698    public static String chomp(final String str) {
699        if (isEmpty(str)) {
700            return str;
701        }
702
703        if (str.length() == 1) {
704            final char ch = str.charAt(0);
705            if (ch == CharUtils.CR || ch == CharUtils.LF) {
706                return EMPTY;
707            }
708            return str;
709        }
710
711        int lastIdx = str.length() - 1;
712        final char last = str.charAt(lastIdx);
713
714        if (last == CharUtils.LF) {
715            if (str.charAt(lastIdx - 1) == CharUtils.CR) {
716                lastIdx--;
717            }
718        } else if (last != CharUtils.CR) {
719            lastIdx++;
720        }
721        return str.substring(0, lastIdx);
722    }
723
724    /**
725     * <p>Removes {@code separator} from the end of
726     * {@code str} if it's there, otherwise leave it alone.</p>
727     *
728     * <p>NOTE: This method changed in version 2.0.
729     * It now more closely matches Perl chomp.
730     * For the previous behavior, use {@link #substringBeforeLast(String, String)}.
731     * This method uses {@link String#endsWith(String)}.</p>
732     *
733     * <pre>
734     * StringUtils.chomp(null, *)         = null
735     * StringUtils.chomp("", *)           = ""
736     * StringUtils.chomp("foobar", "bar") = "foo"
737     * StringUtils.chomp("foobar", "baz") = "foobar"
738     * StringUtils.chomp("foo", "foo")    = ""
739     * StringUtils.chomp("foo ", "foo")   = "foo "
740     * StringUtils.chomp(" foo", "foo")   = " "
741     * StringUtils.chomp("foo", "foooo")  = "foo"
742     * StringUtils.chomp("foo", "")       = "foo"
743     * StringUtils.chomp("foo", null)     = "foo"
744     * </pre>
745     *
746     * @param str  the String to chomp from, may be null
747     * @param separator  separator String, may be null
748     * @return String without trailing separator, {@code null} if null String input
749     * @deprecated This feature will be removed in Lang 4.0, use {@link StringUtils#removeEnd(String, String)} instead
750     */
751    @Deprecated
752    public static String chomp(final String str, final String separator) {
753        return removeEnd(str, separator);
754    }
755
756    // Chopping
757    //-----------------------------------------------------------------------
758    /**
759     * <p>Remove the last character from a String.</p>
760     *
761     * <p>If the String ends in {@code \r\n}, then remove both
762     * of them.</p>
763     *
764     * <pre>
765     * StringUtils.chop(null)          = null
766     * StringUtils.chop("")            = ""
767     * StringUtils.chop("abc \r")      = "abc "
768     * StringUtils.chop("abc\n")       = "abc"
769     * StringUtils.chop("abc\r\n")     = "abc"
770     * StringUtils.chop("abc")         = "ab"
771     * StringUtils.chop("abc\nabc")    = "abc\nab"
772     * StringUtils.chop("a")           = ""
773     * StringUtils.chop("\r")          = ""
774     * StringUtils.chop("\n")          = ""
775     * StringUtils.chop("\r\n")        = ""
776     * </pre>
777     *
778     * @param str  the String to chop last character from, may be null
779     * @return String without last character, {@code null} if null String input
780     */
781    public static String chop(final String str) {
782        if (str == null) {
783            return null;
784        }
785        final int strLen = str.length();
786        if (strLen < 2) {
787            return EMPTY;
788        }
789        final int lastIdx = strLen - 1;
790        final String ret = str.substring(0, lastIdx);
791        final char last = str.charAt(lastIdx);
792        if (last == CharUtils.LF && ret.charAt(lastIdx - 1) == CharUtils.CR) {
793            return ret.substring(0, lastIdx - 1);
794        }
795        return ret;
796    }
797
798    // Compare
799    //-----------------------------------------------------------------------
800    /**
801     * <p>Compare two Strings lexicographically, as per {@link String#compareTo(String)}, returning :</p>
802     * <ul>
803     *  <li>{@code int = 0}, if {@code str1} is equal to {@code str2} (or both {@code null})</li>
804     *  <li>{@code int < 0}, if {@code str1} is less than {@code str2}</li>
805     *  <li>{@code int > 0}, if {@code str1} is greater than {@code str2}</li>
806     * </ul>
807     *
808     * <p>This is a {@code null} safe version of :</p>
809     * <blockquote><pre>str1.compareTo(str2)</pre></blockquote>
810     *
811     * <p>{@code null} value is considered less than non-{@code null} value.
812     * Two {@code null} references are considered equal.</p>
813     *
814     * <pre>
815     * StringUtils.compare(null, null)   = 0
816     * StringUtils.compare(null , "a")   &lt; 0
817     * StringUtils.compare("a", null)    &gt; 0
818     * StringUtils.compare("abc", "abc") = 0
819     * StringUtils.compare("a", "b")     &lt; 0
820     * StringUtils.compare("b", "a")     &gt; 0
821     * StringUtils.compare("a", "B")     &gt; 0
822     * StringUtils.compare("ab", "abc")  &lt; 0
823     * </pre>
824     *
825     * @see #compare(String, String, boolean)
826     * @see String#compareTo(String)
827     * @param str1  the String to compare from
828     * @param str2  the String to compare to
829     * @return &lt; 0, 0, &gt; 0, if {@code str1} is respectively less, equal or greater than {@code str2}
830     * @since 3.5
831     */
832    public static int compare(final String str1, final String str2) {
833        return compare(str1, str2, true);
834    }
835
836    /**
837     * <p>Compare two Strings lexicographically, as per {@link String#compareTo(String)}, returning :</p>
838     * <ul>
839     *  <li>{@code int = 0}, if {@code str1} is equal to {@code str2} (or both {@code null})</li>
840     *  <li>{@code int < 0}, if {@code str1} is less than {@code str2}</li>
841     *  <li>{@code int > 0}, if {@code str1} is greater than {@code str2}</li>
842     * </ul>
843     *
844     * <p>This is a {@code null} safe version of :</p>
845     * <blockquote><pre>str1.compareTo(str2)</pre></blockquote>
846     *
847     * <p>{@code null} inputs are handled according to the {@code nullIsLess} parameter.
848     * Two {@code null} references are considered equal.</p>
849     *
850     * <pre>
851     * StringUtils.compare(null, null, *)     = 0
852     * StringUtils.compare(null , "a", true)  &lt; 0
853     * StringUtils.compare(null , "a", false) &gt; 0
854     * StringUtils.compare("a", null, true)   &gt; 0
855     * StringUtils.compare("a", null, false)  &lt; 0
856     * StringUtils.compare("abc", "abc", *)   = 0
857     * StringUtils.compare("a", "b", *)       &lt; 0
858     * StringUtils.compare("b", "a", *)       &gt; 0
859     * StringUtils.compare("a", "B", *)       &gt; 0
860     * StringUtils.compare("ab", "abc", *)    &lt; 0
861     * </pre>
862     *
863     * @see String#compareTo(String)
864     * @param str1  the String to compare from
865     * @param str2  the String to compare to
866     * @param nullIsLess  whether consider {@code null} value less than non-{@code null} value
867     * @return &lt; 0, 0, &gt; 0, if {@code str1} is respectively less, equal ou greater than {@code str2}
868     * @since 3.5
869     */
870    public static int compare(final String str1, final String str2, final boolean nullIsLess) {
871        if (str1 == str2) {
872            return 0;
873        }
874        if (str1 == null) {
875            return nullIsLess ? -1 : 1;
876        }
877        if (str2 == null) {
878            return nullIsLess ? 1 : - 1;
879        }
880        return str1.compareTo(str2);
881    }
882
883    /**
884     * <p>Compare two Strings lexicographically, ignoring case differences,
885     * as per {@link String#compareToIgnoreCase(String)}, returning :</p>
886     * <ul>
887     *  <li>{@code int = 0}, if {@code str1} is equal to {@code str2} (or both {@code null})</li>
888     *  <li>{@code int < 0}, if {@code str1} is less than {@code str2}</li>
889     *  <li>{@code int > 0}, if {@code str1} is greater than {@code str2}</li>
890     * </ul>
891     *
892     * <p>This is a {@code null} safe version of :</p>
893     * <blockquote><pre>str1.compareToIgnoreCase(str2)</pre></blockquote>
894     *
895     * <p>{@code null} value is considered less than non-{@code null} value.
896     * Two {@code null} references are considered equal.
897     * Comparison is case insensitive.</p>
898     *
899     * <pre>
900     * StringUtils.compareIgnoreCase(null, null)   = 0
901     * StringUtils.compareIgnoreCase(null , "a")   &lt; 0
902     * StringUtils.compareIgnoreCase("a", null)    &gt; 0
903     * StringUtils.compareIgnoreCase("abc", "abc") = 0
904     * StringUtils.compareIgnoreCase("abc", "ABC") = 0
905     * StringUtils.compareIgnoreCase("a", "b")     &lt; 0
906     * StringUtils.compareIgnoreCase("b", "a")     &gt; 0
907     * StringUtils.compareIgnoreCase("a", "B")     &lt; 0
908     * StringUtils.compareIgnoreCase("A", "b")     &lt; 0
909     * StringUtils.compareIgnoreCase("ab", "ABC")  &lt; 0
910     * </pre>
911     *
912     * @see #compareIgnoreCase(String, String, boolean)
913     * @see String#compareToIgnoreCase(String)
914     * @param str1  the String to compare from
915     * @param str2  the String to compare to
916     * @return &lt; 0, 0, &gt; 0, if {@code str1} is respectively less, equal ou greater than {@code str2},
917     *          ignoring case differences.
918     * @since 3.5
919     */
920    public static int compareIgnoreCase(final String str1, final String str2) {
921        return compareIgnoreCase(str1, str2, true);
922    }
923
924    /**
925     * <p>Compare two Strings lexicographically, ignoring case differences,
926     * as per {@link String#compareToIgnoreCase(String)}, returning :</p>
927     * <ul>
928     *  <li>{@code int = 0}, if {@code str1} is equal to {@code str2} (or both {@code null})</li>
929     *  <li>{@code int < 0}, if {@code str1} is less than {@code str2}</li>
930     *  <li>{@code int > 0}, if {@code str1} is greater than {@code str2}</li>
931     * </ul>
932     *
933     * <p>This is a {@code null} safe version of :</p>
934     * <blockquote><pre>str1.compareToIgnoreCase(str2)</pre></blockquote>
935     *
936     * <p>{@code null} inputs are handled according to the {@code nullIsLess} parameter.
937     * Two {@code null} references are considered equal.
938     * Comparison is case insensitive.</p>
939     *
940     * <pre>
941     * StringUtils.compareIgnoreCase(null, null, *)     = 0
942     * StringUtils.compareIgnoreCase(null , "a", true)  &lt; 0
943     * StringUtils.compareIgnoreCase(null , "a", false) &gt; 0
944     * StringUtils.compareIgnoreCase("a", null, true)   &gt; 0
945     * StringUtils.compareIgnoreCase("a", null, false)  &lt; 0
946     * StringUtils.compareIgnoreCase("abc", "abc", *)   = 0
947     * StringUtils.compareIgnoreCase("abc", "ABC", *)   = 0
948     * StringUtils.compareIgnoreCase("a", "b", *)       &lt; 0
949     * StringUtils.compareIgnoreCase("b", "a", *)       &gt; 0
950     * StringUtils.compareIgnoreCase("a", "B", *)       &lt; 0
951     * StringUtils.compareIgnoreCase("A", "b", *)       &lt; 0
952     * StringUtils.compareIgnoreCase("ab", "abc", *)    &lt; 0
953     * </pre>
954     *
955     * @see String#compareToIgnoreCase(String)
956     * @param str1  the String to compare from
957     * @param str2  the String to compare to
958     * @param nullIsLess  whether consider {@code null} value less than non-{@code null} value
959     * @return &lt; 0, 0, &gt; 0, if {@code str1} is respectively less, equal ou greater than {@code str2},
960     *          ignoring case differences.
961     * @since 3.5
962     */
963    public static int compareIgnoreCase(final String str1, final String str2, final boolean nullIsLess) {
964        if (str1 == str2) {
965            return 0;
966        }
967        if (str1 == null) {
968            return nullIsLess ? -1 : 1;
969        }
970        if (str2 == null) {
971            return nullIsLess ? 1 : - 1;
972        }
973        return str1.compareToIgnoreCase(str2);
974    }
975
976    /**
977     * <p>Checks if CharSequence contains a search CharSequence, handling {@code null}.
978     * This method uses {@link String#indexOf(String)} if possible.</p>
979     *
980     * <p>A {@code null} CharSequence will return {@code false}.</p>
981     *
982     * <pre>
983     * StringUtils.contains(null, *)     = false
984     * StringUtils.contains(*, null)     = false
985     * StringUtils.contains("", "")      = true
986     * StringUtils.contains("abc", "")   = true
987     * StringUtils.contains("abc", "a")  = true
988     * StringUtils.contains("abc", "z")  = false
989     * </pre>
990     *
991     * @param seq  the CharSequence to check, may be null
992     * @param searchSeq  the CharSequence to find, may be null
993     * @return true if the CharSequence contains the search CharSequence,
994     *  false if not or {@code null} string input
995     * @since 2.0
996     * @since 3.0 Changed signature from contains(String, String) to contains(CharSequence, CharSequence)
997     */
998    public static boolean contains(final CharSequence seq, final CharSequence searchSeq) {
999        if (seq == null || searchSeq == null) {
1000            return false;
1001        }
1002        return CharSequenceUtils.indexOf(seq, searchSeq, 0) >= 0;
1003    }
1004
1005    // Contains
1006    //-----------------------------------------------------------------------
1007    /**
1008     * <p>Checks if CharSequence contains a search character, handling {@code null}.
1009     * This method uses {@link String#indexOf(int)} if possible.</p>
1010     *
1011     * <p>A {@code null} or empty ("") CharSequence will return {@code false}.</p>
1012     *
1013     * <pre>
1014     * StringUtils.contains(null, *)    = false
1015     * StringUtils.contains("", *)      = false
1016     * StringUtils.contains("abc", 'a') = true
1017     * StringUtils.contains("abc", 'z') = false
1018     * </pre>
1019     *
1020     * @param seq  the CharSequence to check, may be null
1021     * @param searchChar  the character to find
1022     * @return true if the CharSequence contains the search character,
1023     *  false if not or {@code null} string input
1024     * @since 2.0
1025     * @since 3.0 Changed signature from contains(String, int) to contains(CharSequence, int)
1026     */
1027    public static boolean contains(final CharSequence seq, final int searchChar) {
1028        if (isEmpty(seq)) {
1029            return false;
1030        }
1031        return CharSequenceUtils.indexOf(seq, searchChar, 0) >= 0;
1032    }
1033
1034    // ContainsAny
1035    //-----------------------------------------------------------------------
1036    /**
1037     * <p>Checks if the CharSequence contains any character in the given
1038     * set of characters.</p>
1039     *
1040     * <p>A {@code null} CharSequence will return {@code false}.
1041     * A {@code null} or zero length search array will return {@code false}.</p>
1042     *
1043     * <pre>
1044     * StringUtils.containsAny(null, *)                  = false
1045     * StringUtils.containsAny("", *)                    = false
1046     * StringUtils.containsAny(*, null)                  = false
1047     * StringUtils.containsAny(*, [])                    = false
1048     * StringUtils.containsAny("zzabyycdxx", ['z', 'a']) = true
1049     * StringUtils.containsAny("zzabyycdxx", ['b', 'y']) = true
1050     * StringUtils.containsAny("zzabyycdxx", ['z', 'y']) = true
1051     * StringUtils.containsAny("aba", ['z'])             = false
1052     * </pre>
1053     *
1054     * @param cs  the CharSequence to check, may be null
1055     * @param searchChars  the chars to search for, may be null
1056     * @return the {@code true} if any of the chars are found,
1057     * {@code false} if no match or null input
1058     * @since 2.4
1059     * @since 3.0 Changed signature from containsAny(String, char[]) to containsAny(CharSequence, char...)
1060     */
1061    public static boolean containsAny(final CharSequence cs, final char... searchChars) {
1062        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
1063            return false;
1064        }
1065        final int csLength = cs.length();
1066        final int searchLength = searchChars.length;
1067        final int csLast = csLength - 1;
1068        final int searchLast = searchLength - 1;
1069        for (int i = 0; i < csLength; i++) {
1070            final char ch = cs.charAt(i);
1071            for (int j = 0; j < searchLength; j++) {
1072                if (searchChars[j] == ch) {
1073                    if (Character.isHighSurrogate(ch)) {
1074                        if (j == searchLast) {
1075                            // missing low surrogate, fine, like String.indexOf(String)
1076                            return true;
1077                        }
1078                        if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
1079                            return true;
1080                        }
1081                    } else {
1082                        // ch is in the Basic Multilingual Plane
1083                        return true;
1084                    }
1085                }
1086            }
1087        }
1088        return false;
1089    }
1090
1091    /**
1092     * <p>
1093     * Checks if the CharSequence contains any character in the given set of characters.
1094     * </p>
1095     *
1096     * <p>
1097     * A {@code null} CharSequence will return {@code false}. A {@code null} search CharSequence will return
1098     * {@code false}.
1099     * </p>
1100     *
1101     * <pre>
1102     * StringUtils.containsAny(null, *)               = false
1103     * StringUtils.containsAny("", *)                 = false
1104     * StringUtils.containsAny(*, null)               = false
1105     * StringUtils.containsAny(*, "")                 = false
1106     * StringUtils.containsAny("zzabyycdxx", "za")    = true
1107     * StringUtils.containsAny("zzabyycdxx", "by")    = true
1108     * StringUtils.containsAny("zzabyycdxx", "zy")    = true
1109     * StringUtils.containsAny("zzabyycdxx", "\tx")   = true
1110     * StringUtils.containsAny("zzabyycdxx", "$.#yF") = true
1111     * StringUtils.containsAny("aba", "z")            = false
1112     * </pre>
1113     *
1114     * @param cs
1115     *            the CharSequence to check, may be null
1116     * @param searchChars
1117     *            the chars to search for, may be null
1118     * @return the {@code true} if any of the chars are found, {@code false} if no match or null input
1119     * @since 2.4
1120     * @since 3.0 Changed signature from containsAny(String, String) to containsAny(CharSequence, CharSequence)
1121     */
1122    public static boolean containsAny(final CharSequence cs, final CharSequence searchChars) {
1123        if (searchChars == null) {
1124            return false;
1125        }
1126        return containsAny(cs, CharSequenceUtils.toCharArray(searchChars));
1127    }
1128
1129    /**
1130     * <p>Checks if the CharSequence contains any of the CharSequences in the given array.</p>
1131     *
1132     * <p>
1133     * A {@code null} {@code cs} CharSequence will return {@code false}. A {@code null} or zero
1134     * length search array will return {@code false}.
1135     * </p>
1136     *
1137     * <pre>
1138     * StringUtils.containsAny(null, *)            = false
1139     * StringUtils.containsAny("", *)              = false
1140     * StringUtils.containsAny(*, null)            = false
1141     * StringUtils.containsAny(*, [])              = false
1142     * StringUtils.containsAny("abcd", "ab", null) = true
1143     * StringUtils.containsAny("abcd", "ab", "cd") = true
1144     * StringUtils.containsAny("abc", "d", "abc")  = true
1145     * </pre>
1146     *
1147     *
1148     * @param cs The CharSequence to check, may be null
1149     * @param searchCharSequences The array of CharSequences to search for, may be null.
1150     * Individual CharSequences may be null as well.
1151     * @return {@code true} if any of the search CharSequences are found, {@code false} otherwise
1152     * @since 3.4
1153     */
1154    public static boolean containsAny(final CharSequence cs, final CharSequence... searchCharSequences) {
1155        if (isEmpty(cs) || ArrayUtils.isEmpty(searchCharSequences)) {
1156            return false;
1157        }
1158        for (final CharSequence searchCharSequence : searchCharSequences) {
1159            if (contains(cs, searchCharSequence)) {
1160                return true;
1161            }
1162        }
1163        return false;
1164    }
1165
1166    /**
1167     * <p>Checks if CharSequence contains a search CharSequence irrespective of case,
1168     * handling {@code null}. Case-insensitivity is defined as by
1169     * {@link String#equalsIgnoreCase(String)}.
1170     *
1171     * <p>A {@code null} CharSequence will return {@code false}.</p>
1172     *
1173     * <pre>
1174     * StringUtils.containsIgnoreCase(null, *) = false
1175     * StringUtils.containsIgnoreCase(*, null) = false
1176     * StringUtils.containsIgnoreCase("", "") = true
1177     * StringUtils.containsIgnoreCase("abc", "") = true
1178     * StringUtils.containsIgnoreCase("abc", "a") = true
1179     * StringUtils.containsIgnoreCase("abc", "z") = false
1180     * StringUtils.containsIgnoreCase("abc", "A") = true
1181     * StringUtils.containsIgnoreCase("abc", "Z") = false
1182     * </pre>
1183     *
1184     * @param str  the CharSequence to check, may be null
1185     * @param searchStr  the CharSequence to find, may be null
1186     * @return true if the CharSequence contains the search CharSequence irrespective of
1187     * case or false if not or {@code null} string input
1188     * @since 3.0 Changed signature from containsIgnoreCase(String, String) to containsIgnoreCase(CharSequence, CharSequence)
1189     */
1190    public static boolean containsIgnoreCase(final CharSequence str, final CharSequence searchStr) {
1191        if (str == null || searchStr == null) {
1192            return false;
1193        }
1194        final int len = searchStr.length();
1195        final int max = str.length() - len;
1196        for (int i = 0; i <= max; i++) {
1197            if (CharSequenceUtils.regionMatches(str, true, i, searchStr, 0, len)) {
1198                return true;
1199            }
1200        }
1201        return false;
1202    }
1203
1204    // ContainsNone
1205    //-----------------------------------------------------------------------
1206    /**
1207     * <p>Checks that the CharSequence does not contain certain characters.</p>
1208     *
1209     * <p>A {@code null} CharSequence will return {@code true}.
1210     * A {@code null} invalid character array will return {@code true}.
1211     * An empty CharSequence (length()=0) always returns true.</p>
1212     *
1213     * <pre>
1214     * StringUtils.containsNone(null, *)       = true
1215     * StringUtils.containsNone(*, null)       = true
1216     * StringUtils.containsNone("", *)         = true
1217     * StringUtils.containsNone("ab", '')      = true
1218     * StringUtils.containsNone("abab", 'xyz') = true
1219     * StringUtils.containsNone("ab1", 'xyz')  = true
1220     * StringUtils.containsNone("abz", 'xyz')  = false
1221     * </pre>
1222     *
1223     * @param cs  the CharSequence to check, may be null
1224     * @param searchChars  an array of invalid chars, may be null
1225     * @return true if it contains none of the invalid chars, or is null
1226     * @since 2.0
1227     * @since 3.0 Changed signature from containsNone(String, char[]) to containsNone(CharSequence, char...)
1228     */
1229    public static boolean containsNone(final CharSequence cs, final char... searchChars) {
1230        if (cs == null || searchChars == null) {
1231            return true;
1232        }
1233        final int csLen = cs.length();
1234        final int csLast = csLen - 1;
1235        final int searchLen = searchChars.length;
1236        final int searchLast = searchLen - 1;
1237        for (int i = 0; i < csLen; i++) {
1238            final char ch = cs.charAt(i);
1239            for (int j = 0; j < searchLen; j++) {
1240                if (searchChars[j] == ch) {
1241                    if (Character.isHighSurrogate(ch)) {
1242                        if (j == searchLast) {
1243                            // missing low surrogate, fine, like String.indexOf(String)
1244                            return false;
1245                        }
1246                        if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
1247                            return false;
1248                        }
1249                    } else {
1250                        // ch is in the Basic Multilingual Plane
1251                        return false;
1252                    }
1253                }
1254            }
1255        }
1256        return true;
1257    }
1258
1259    /**
1260     * <p>Checks that the CharSequence does not contain certain characters.</p>
1261     *
1262     * <p>A {@code null} CharSequence will return {@code true}.
1263     * A {@code null} invalid character array will return {@code true}.
1264     * An empty String ("") always returns true.</p>
1265     *
1266     * <pre>
1267     * StringUtils.containsNone(null, *)       = true
1268     * StringUtils.containsNone(*, null)       = true
1269     * StringUtils.containsNone("", *)         = true
1270     * StringUtils.containsNone("ab", "")      = true
1271     * StringUtils.containsNone("abab", "xyz") = true
1272     * StringUtils.containsNone("ab1", "xyz")  = true
1273     * StringUtils.containsNone("abz", "xyz")  = false
1274     * </pre>
1275     *
1276     * @param cs  the CharSequence to check, may be null
1277     * @param invalidChars  a String of invalid chars, may be null
1278     * @return true if it contains none of the invalid chars, or is null
1279     * @since 2.0
1280     * @since 3.0 Changed signature from containsNone(String, String) to containsNone(CharSequence, String)
1281     */
1282    public static boolean containsNone(final CharSequence cs, final String invalidChars) {
1283        if (cs == null || invalidChars == null) {
1284            return true;
1285        }
1286        return containsNone(cs, invalidChars.toCharArray());
1287    }
1288
1289    // ContainsOnly
1290    //-----------------------------------------------------------------------
1291    /**
1292     * <p>Checks if the CharSequence contains only certain characters.</p>
1293     *
1294     * <p>A {@code null} CharSequence will return {@code false}.
1295     * A {@code null} valid character array will return {@code false}.
1296     * An empty CharSequence (length()=0) always returns {@code true}.</p>
1297     *
1298     * <pre>
1299     * StringUtils.containsOnly(null, *)       = false
1300     * StringUtils.containsOnly(*, null)       = false
1301     * StringUtils.containsOnly("", *)         = true
1302     * StringUtils.containsOnly("ab", '')      = false
1303     * StringUtils.containsOnly("abab", 'abc') = true
1304     * StringUtils.containsOnly("ab1", 'abc')  = false
1305     * StringUtils.containsOnly("abz", 'abc')  = false
1306     * </pre>
1307     *
1308     * @param cs  the String to check, may be null
1309     * @param valid  an array of valid chars, may be null
1310     * @return true if it only contains valid chars and is non-null
1311     * @since 3.0 Changed signature from containsOnly(String, char[]) to containsOnly(CharSequence, char...)
1312     */
1313    public static boolean containsOnly(final CharSequence cs, final char... valid) {
1314        // All these pre-checks are to maintain API with an older version
1315        if (valid == null || cs == null) {
1316            return false;
1317        }
1318        if (cs.length() == 0) {
1319            return true;
1320        }
1321        if (valid.length == 0) {
1322            return false;
1323        }
1324        return indexOfAnyBut(cs, valid) == INDEX_NOT_FOUND;
1325    }
1326
1327    /**
1328     * <p>Checks if the CharSequence contains only certain characters.</p>
1329     *
1330     * <p>A {@code null} CharSequence will return {@code false}.
1331     * A {@code null} valid character String will return {@code false}.
1332     * An empty String (length()=0) always returns {@code true}.</p>
1333     *
1334     * <pre>
1335     * StringUtils.containsOnly(null, *)       = false
1336     * StringUtils.containsOnly(*, null)       = false
1337     * StringUtils.containsOnly("", *)         = true
1338     * StringUtils.containsOnly("ab", "")      = false
1339     * StringUtils.containsOnly("abab", "abc") = true
1340     * StringUtils.containsOnly("ab1", "abc")  = false
1341     * StringUtils.containsOnly("abz", "abc")  = false
1342     * </pre>
1343     *
1344     * @param cs  the CharSequence to check, may be null
1345     * @param validChars  a String of valid chars, may be null
1346     * @return true if it only contains valid chars and is non-null
1347     * @since 2.0
1348     * @since 3.0 Changed signature from containsOnly(String, String) to containsOnly(CharSequence, String)
1349     */
1350    public static boolean containsOnly(final CharSequence cs, final String validChars) {
1351        if (cs == null || validChars == null) {
1352            return false;
1353        }
1354        return containsOnly(cs, validChars.toCharArray());
1355    }
1356
1357    /**
1358     * <p>Check whether the given CharSequence contains any whitespace characters.</p>
1359     *
1360     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
1361     *
1362     * @param seq the CharSequence to check (may be {@code null})
1363     * @return {@code true} if the CharSequence is not empty and
1364     * contains at least 1 (breaking) whitespace character
1365     * @since 3.0
1366     */
1367    // From org.springframework.util.StringUtils, under Apache License 2.0
1368    public static boolean containsWhitespace(final CharSequence seq) {
1369        if (isEmpty(seq)) {
1370            return false;
1371        }
1372        final int strLen = seq.length();
1373        for (int i = 0; i < strLen; i++) {
1374            if (Character.isWhitespace(seq.charAt(i))) {
1375                return true;
1376            }
1377        }
1378        return false;
1379    }
1380
1381    private static void convertRemainingAccentCharacters(final StringBuilder decomposed) {
1382        for (int i = 0; i < decomposed.length(); i++) {
1383            if (decomposed.charAt(i) == '\u0141') {
1384                decomposed.deleteCharAt(i);
1385                decomposed.insert(i, 'L');
1386            } else if (decomposed.charAt(i) == '\u0142') {
1387                decomposed.deleteCharAt(i);
1388                decomposed.insert(i, 'l');
1389            }
1390        }
1391    }
1392
1393    /**
1394     * <p>Counts how many times the char appears in the given string.</p>
1395     *
1396     * <p>A {@code null} or empty ("") String input returns {@code 0}.</p>
1397     *
1398     * <pre>
1399     * StringUtils.countMatches(null, *)       = 0
1400     * StringUtils.countMatches("", *)         = 0
1401     * StringUtils.countMatches("abba", 0)  = 0
1402     * StringUtils.countMatches("abba", 'a')   = 2
1403     * StringUtils.countMatches("abba", 'b')  = 2
1404     * StringUtils.countMatches("abba", 'x') = 0
1405     * </pre>
1406     *
1407     * @param str  the CharSequence to check, may be null
1408     * @param ch  the char to count
1409     * @return the number of occurrences, 0 if the CharSequence is {@code null}
1410     * @since 3.4
1411     */
1412    public static int countMatches(final CharSequence str, final char ch) {
1413        if (isEmpty(str)) {
1414            return 0;
1415        }
1416        int count = 0;
1417        // We could also call str.toCharArray() for faster look ups but that would generate more garbage.
1418        for (int i = 0; i < str.length(); i++) {
1419            if (ch == str.charAt(i)) {
1420                count++;
1421            }
1422        }
1423        return count;
1424    }
1425
1426    // Count matches
1427    //-----------------------------------------------------------------------
1428    /**
1429     * <p>Counts how many times the substring appears in the larger string.</p>
1430     *
1431     * <p>A {@code null} or empty ("") String input returns {@code 0}.</p>
1432     *
1433     * <pre>
1434     * StringUtils.countMatches(null, *)       = 0
1435     * StringUtils.countMatches("", *)         = 0
1436     * StringUtils.countMatches("abba", null)  = 0
1437     * StringUtils.countMatches("abba", "")    = 0
1438     * StringUtils.countMatches("abba", "a")   = 2
1439     * StringUtils.countMatches("abba", "ab")  = 1
1440     * StringUtils.countMatches("abba", "xxx") = 0
1441     * </pre>
1442     *
1443     * @param str  the CharSequence to check, may be null
1444     * @param sub  the substring to count, may be null
1445     * @return the number of occurrences, 0 if either CharSequence is {@code null}
1446     * @since 3.0 Changed signature from countMatches(String, String) to countMatches(CharSequence, CharSequence)
1447     */
1448    public static int countMatches(final CharSequence str, final CharSequence sub) {
1449        if (isEmpty(str) || isEmpty(sub)) {
1450            return 0;
1451        }
1452        int count = 0;
1453        int idx = 0;
1454        while ((idx = CharSequenceUtils.indexOf(str, sub, idx)) != INDEX_NOT_FOUND) {
1455            count++;
1456            idx += sub.length();
1457        }
1458        return count;
1459    }
1460
1461    /**
1462     * <p>Returns either the passed in CharSequence, or if the CharSequence is
1463     * whitespace, empty ("") or {@code null}, the value of {@code defaultStr}.</p>
1464     *
1465     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
1466     *
1467     * <pre>
1468     * StringUtils.defaultIfBlank(null, "NULL")  = "NULL"
1469     * StringUtils.defaultIfBlank("", "NULL")    = "NULL"
1470     * StringUtils.defaultIfBlank(" ", "NULL")   = "NULL"
1471     * StringUtils.defaultIfBlank("bat", "NULL") = "bat"
1472     * StringUtils.defaultIfBlank("", null)      = null
1473     * </pre>
1474     * @param <T> the specific kind of CharSequence
1475     * @param str the CharSequence to check, may be null
1476     * @param defaultStr  the default CharSequence to return
1477     *  if the input is whitespace, empty ("") or {@code null}, may be null
1478     * @return the passed in CharSequence, or the default
1479     * @see StringUtils#defaultString(String, String)
1480     */
1481    public static <T extends CharSequence> T defaultIfBlank(final T str, final T defaultStr) {
1482        return isBlank(str) ? defaultStr : str;
1483    }
1484
1485    /**
1486     * <p>Returns either the passed in CharSequence, or if the CharSequence is
1487     * empty or {@code null}, the value of {@code defaultStr}.</p>
1488     *
1489     * <pre>
1490     * StringUtils.defaultIfEmpty(null, "NULL")  = "NULL"
1491     * StringUtils.defaultIfEmpty("", "NULL")    = "NULL"
1492     * StringUtils.defaultIfEmpty(" ", "NULL")   = " "
1493     * StringUtils.defaultIfEmpty("bat", "NULL") = "bat"
1494     * StringUtils.defaultIfEmpty("", null)      = null
1495     * </pre>
1496     * @param <T> the specific kind of CharSequence
1497     * @param str  the CharSequence to check, may be null
1498     * @param defaultStr  the default CharSequence to return
1499     *  if the input is empty ("") or {@code null}, may be null
1500     * @return the passed in CharSequence, or the default
1501     * @see StringUtils#defaultString(String, String)
1502     */
1503    public static <T extends CharSequence> T defaultIfEmpty(final T str, final T defaultStr) {
1504        return isEmpty(str) ? defaultStr : str;
1505    }
1506
1507    /**
1508     * <p>Returns either the passed in String,
1509     * or if the String is {@code null}, an empty String ("").</p>
1510     *
1511     * <pre>
1512     * StringUtils.defaultString(null)  = ""
1513     * StringUtils.defaultString("")    = ""
1514     * StringUtils.defaultString("bat") = "bat"
1515     * </pre>
1516     *
1517     * @see ObjectUtils#toString(Object)
1518     * @see String#valueOf(Object)
1519     * @param str  the String to check, may be null
1520     * @return the passed in String, or the empty String if it
1521     *  was {@code null}
1522     */
1523    public static String defaultString(final String str) {
1524        return defaultString(str, EMPTY);
1525    }
1526
1527    /**
1528     * <p>Returns either the passed in String, or if the String is
1529     * {@code null}, the value of {@code defaultStr}.</p>
1530     *
1531     * <pre>
1532     * StringUtils.defaultString(null, "NULL")  = "NULL"
1533     * StringUtils.defaultString("", "NULL")    = ""
1534     * StringUtils.defaultString("bat", "NULL") = "bat"
1535     * </pre>
1536     *
1537     * @see ObjectUtils#toString(Object,String)
1538     * @see String#valueOf(Object)
1539     * @param str  the String to check, may be null
1540     * @param defaultStr  the default String to return
1541     *  if the input is {@code null}, may be null
1542     * @return the passed in String, or the default if it was {@code null}
1543     */
1544    public static String defaultString(final String str, final String defaultStr) {
1545        return str == null ? defaultStr : str;
1546    }
1547
1548    // Delete
1549    //-----------------------------------------------------------------------
1550    /**
1551     * <p>Deletes all whitespaces from a String as defined by
1552     * {@link Character#isWhitespace(char)}.</p>
1553     *
1554     * <pre>
1555     * StringUtils.deleteWhitespace(null)         = null
1556     * StringUtils.deleteWhitespace("")           = ""
1557     * StringUtils.deleteWhitespace("abc")        = "abc"
1558     * StringUtils.deleteWhitespace("   ab  c  ") = "abc"
1559     * </pre>
1560     *
1561     * @param str  the String to delete whitespace from, may be null
1562     * @return the String without whitespaces, {@code null} if null String input
1563     */
1564    public static String deleteWhitespace(final String str) {
1565        if (isEmpty(str)) {
1566            return str;
1567        }
1568        final int sz = str.length();
1569        final char[] chs = new char[sz];
1570        int count = 0;
1571        for (int i = 0; i < sz; i++) {
1572            if (!Character.isWhitespace(str.charAt(i))) {
1573                chs[count++] = str.charAt(i);
1574            }
1575        }
1576        if (count == sz) {
1577            return str;
1578        }
1579        return new String(chs, 0, count);
1580    }
1581
1582    // Difference
1583    //-----------------------------------------------------------------------
1584    /**
1585     * <p>Compares two Strings, and returns the portion where they differ.
1586     * More precisely, return the remainder of the second String,
1587     * starting from where it's different from the first. This means that
1588     * the difference between "abc" and "ab" is the empty String and not "c". </p>
1589     *
1590     * <p>For example,
1591     * {@code difference("i am a machine", "i am a robot") -> "robot"}.</p>
1592     *
1593     * <pre>
1594     * StringUtils.difference(null, null) = null
1595     * StringUtils.difference("", "") = ""
1596     * StringUtils.difference("", "abc") = "abc"
1597     * StringUtils.difference("abc", "") = ""
1598     * StringUtils.difference("abc", "abc") = ""
1599     * StringUtils.difference("abc", "ab") = ""
1600     * StringUtils.difference("ab", "abxyz") = "xyz"
1601     * StringUtils.difference("abcde", "abxyz") = "xyz"
1602     * StringUtils.difference("abcde", "xyz") = "xyz"
1603     * </pre>
1604     *
1605     * @param str1  the first String, may be null
1606     * @param str2  the second String, may be null
1607     * @return the portion of str2 where it differs from str1; returns the
1608     * empty String if they are equal
1609     * @see #indexOfDifference(CharSequence,CharSequence)
1610     * @since 2.0
1611     */
1612    public static String difference(final String str1, final String str2) {
1613        if (str1 == null) {
1614            return str2;
1615        }
1616        if (str2 == null) {
1617            return str1;
1618        }
1619        final int at = indexOfDifference(str1, str2);
1620        if (at == INDEX_NOT_FOUND) {
1621            return EMPTY;
1622        }
1623        return str2.substring(at);
1624    }
1625
1626    /**
1627     * <p>Check if a CharSequence ends with a specified suffix.</p>
1628     *
1629     * <p>{@code null}s are handled without exceptions. Two {@code null}
1630     * references are considered to be equal. The comparison is case sensitive.</p>
1631     *
1632     * <pre>
1633     * StringUtils.endsWith(null, null)      = true
1634     * StringUtils.endsWith(null, "def")     = false
1635     * StringUtils.endsWith("abcdef", null)  = false
1636     * StringUtils.endsWith("abcdef", "def") = true
1637     * StringUtils.endsWith("ABCDEF", "def") = false
1638     * StringUtils.endsWith("ABCDEF", "cde") = false
1639     * StringUtils.endsWith("ABCDEF", "")    = true
1640     * </pre>
1641     *
1642     * @see java.lang.String#endsWith(String)
1643     * @param str  the CharSequence to check, may be null
1644     * @param suffix the suffix to find, may be null
1645     * @return {@code true} if the CharSequence ends with the suffix, case sensitive, or
1646     *  both {@code null}
1647     * @since 2.4
1648     * @since 3.0 Changed signature from endsWith(String, String) to endsWith(CharSequence, CharSequence)
1649     */
1650    public static boolean endsWith(final CharSequence str, final CharSequence suffix) {
1651        return endsWith(str, suffix, false);
1652    }
1653
1654    /**
1655     * <p>Check if a CharSequence ends with a specified suffix (optionally case insensitive).</p>
1656     *
1657     * @see java.lang.String#endsWith(String)
1658     * @param str  the CharSequence to check, may be null
1659     * @param suffix the suffix to find, may be null
1660     * @param ignoreCase indicates whether the compare should ignore case
1661     *  (case insensitive) or not.
1662     * @return {@code true} if the CharSequence starts with the prefix or
1663     *  both {@code null}
1664     */
1665    private static boolean endsWith(final CharSequence str, final CharSequence suffix, final boolean ignoreCase) {
1666        if (str == null || suffix == null) {
1667            return str == suffix;
1668        }
1669        if (suffix.length() > str.length()) {
1670            return false;
1671        }
1672        final int strOffset = str.length() - suffix.length();
1673        return CharSequenceUtils.regionMatches(str, ignoreCase, strOffset, suffix, 0, suffix.length());
1674    }
1675
1676    /**
1677     * <p>Check if a CharSequence ends with any of the provided case-sensitive suffixes.</p>
1678     *
1679     * <pre>
1680     * StringUtils.endsWithAny(null, null)      = false
1681     * StringUtils.endsWithAny(null, new String[] {"abc"})  = false
1682     * StringUtils.endsWithAny("abcxyz", null)     = false
1683     * StringUtils.endsWithAny("abcxyz", new String[] {""}) = true
1684     * StringUtils.endsWithAny("abcxyz", new String[] {"xyz"}) = true
1685     * StringUtils.endsWithAny("abcxyz", new String[] {null, "xyz", "abc"}) = true
1686     * StringUtils.endsWithAny("abcXYZ", "def", "XYZ") = true
1687     * StringUtils.endsWithAny("abcXYZ", "def", "xyz") = false
1688     * </pre>
1689     *
1690     * @param sequence  the CharSequence to check, may be null
1691     * @param searchStrings the case-sensitive CharSequences to find, may be empty or contain {@code null}
1692     * @see StringUtils#endsWith(CharSequence, CharSequence)
1693     * @return {@code true} if the input {@code sequence} is {@code null} AND no {@code searchStrings} are provided, or
1694     *   the input {@code sequence} ends in any of the provided case-sensitive {@code searchStrings}.
1695     * @since 3.0
1696     */
1697    public static boolean endsWithAny(final CharSequence sequence, final CharSequence... searchStrings) {
1698        if (isEmpty(sequence) || ArrayUtils.isEmpty(searchStrings)) {
1699            return false;
1700        }
1701        for (final CharSequence searchString : searchStrings) {
1702            if (endsWith(sequence, searchString)) {
1703                return true;
1704            }
1705        }
1706        return false;
1707    }
1708
1709    /**
1710     * <p>Case insensitive check if a CharSequence ends with a specified suffix.</p>
1711     *
1712     * <p>{@code null}s are handled without exceptions. Two {@code null}
1713     * references are considered to be equal. The comparison is case insensitive.</p>
1714     *
1715     * <pre>
1716     * StringUtils.endsWithIgnoreCase(null, null)      = true
1717     * StringUtils.endsWithIgnoreCase(null, "def")     = false
1718     * StringUtils.endsWithIgnoreCase("abcdef", null)  = false
1719     * StringUtils.endsWithIgnoreCase("abcdef", "def") = true
1720     * StringUtils.endsWithIgnoreCase("ABCDEF", "def") = true
1721     * StringUtils.endsWithIgnoreCase("ABCDEF", "cde") = false
1722     * </pre>
1723     *
1724     * @see java.lang.String#endsWith(String)
1725     * @param str  the CharSequence to check, may be null
1726     * @param suffix the suffix to find, may be null
1727     * @return {@code true} if the CharSequence ends with the suffix, case insensitive, or
1728     *  both {@code null}
1729     * @since 2.4
1730     * @since 3.0 Changed signature from endsWithIgnoreCase(String, String) to endsWithIgnoreCase(CharSequence, CharSequence)
1731     */
1732    public static boolean endsWithIgnoreCase(final CharSequence str, final CharSequence suffix) {
1733        return endsWith(str, suffix, true);
1734    }
1735
1736    // Equals
1737    //-----------------------------------------------------------------------
1738    /**
1739     * <p>Compares two CharSequences, returning {@code true} if they represent
1740     * equal sequences of characters.</p>
1741     *
1742     * <p>{@code null}s are handled without exceptions. Two {@code null}
1743     * references are considered to be equal. The comparison is <strong>case sensitive</strong>.</p>
1744     *
1745     * <pre>
1746     * StringUtils.equals(null, null)   = true
1747     * StringUtils.equals(null, "abc")  = false
1748     * StringUtils.equals("abc", null)  = false
1749     * StringUtils.equals("abc", "abc") = true
1750     * StringUtils.equals("abc", "ABC") = false
1751     * </pre>
1752     *
1753     * @param cs1  the first CharSequence, may be {@code null}
1754     * @param cs2  the second CharSequence, may be {@code null}
1755     * @return {@code true} if the CharSequences are equal (case-sensitive), or both {@code null}
1756     * @since 3.0 Changed signature from equals(String, String) to equals(CharSequence, CharSequence)
1757     * @see Object#equals(Object)
1758     * @see #equalsIgnoreCase(CharSequence, CharSequence)
1759     */
1760    public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
1761        if (cs1 == cs2) {
1762            return true;
1763        }
1764        if (cs1 == null || cs2 == null) {
1765            return false;
1766        }
1767        if (cs1.length() != cs2.length()) {
1768            return false;
1769        }
1770        if (cs1 instanceof String && cs2 instanceof String) {
1771            return cs1.equals(cs2);
1772        }
1773        // Step-wise comparison
1774        final int length = cs1.length();
1775        for (int i = 0; i < length; i++) {
1776            if (cs1.charAt(i) != cs2.charAt(i)) {
1777                return false;
1778            }
1779        }
1780        return true;
1781    }
1782
1783    /**
1784     * <p>Compares given {@code string} to a CharSequences vararg of {@code searchStrings},
1785     * returning {@code true} if the {@code string} is equal to any of the {@code searchStrings}.</p>
1786     *
1787     * <pre>
1788     * StringUtils.equalsAny(null, (CharSequence[]) null) = false
1789     * StringUtils.equalsAny(null, null, null)    = true
1790     * StringUtils.equalsAny(null, "abc", "def")  = false
1791     * StringUtils.equalsAny("abc", null, "def")  = false
1792     * StringUtils.equalsAny("abc", "abc", "def") = true
1793     * StringUtils.equalsAny("abc", "ABC", "DEF") = false
1794     * </pre>
1795     *
1796     * @param string to compare, may be {@code null}.
1797     * @param searchStrings a vararg of strings, may be {@code null}.
1798     * @return {@code true} if the string is equal (case-sensitive) to any other element of {@code searchStrings};
1799     * {@code false} if {@code searchStrings} is null or contains no matches.
1800     * @since 3.5
1801     */
1802    public static boolean equalsAny(final CharSequence string, final CharSequence... searchStrings) {
1803        if (ArrayUtils.isNotEmpty(searchStrings)) {
1804            for (final CharSequence next : searchStrings) {
1805                if (equals(string, next)) {
1806                    return true;
1807                }
1808            }
1809        }
1810        return false;
1811    }
1812
1813    /**
1814     * <p>Compares given {@code string} to a CharSequences vararg of {@code searchStrings},
1815     * returning {@code true} if the {@code string} is equal to any of the {@code searchStrings}, ignoring case.</p>
1816     *
1817     * <pre>
1818     * StringUtils.equalsAnyIgnoreCase(null, (CharSequence[]) null) = false
1819     * StringUtils.equalsAnyIgnoreCase(null, null, null)    = true
1820     * StringUtils.equalsAnyIgnoreCase(null, "abc", "def")  = false
1821     * StringUtils.equalsAnyIgnoreCase("abc", null, "def")  = false
1822     * StringUtils.equalsAnyIgnoreCase("abc", "abc", "def") = true
1823     * StringUtils.equalsAnyIgnoreCase("abc", "ABC", "DEF") = true
1824     * </pre>
1825     *
1826     * @param string to compare, may be {@code null}.
1827     * @param searchStrings a vararg of strings, may be {@code null}.
1828     * @return {@code true} if the string is equal (case-insensitive) to any other element of {@code searchStrings};
1829     * {@code false} if {@code searchStrings} is null or contains no matches.
1830     * @since 3.5
1831     */
1832    public static boolean equalsAnyIgnoreCase(final CharSequence string, final CharSequence...searchStrings) {
1833        if (ArrayUtils.isNotEmpty(searchStrings)) {
1834            for (final CharSequence next : searchStrings) {
1835                if (equalsIgnoreCase(string, next)) {
1836                    return true;
1837                }
1838            }
1839        }
1840        return false;
1841    }
1842
1843    /**
1844     * <p>Compares two CharSequences, returning {@code true} if they represent
1845     * equal sequences of characters, ignoring case.</p>
1846     *
1847     * <p>{@code null}s are handled without exceptions. Two {@code null}
1848     * references are considered equal. The comparison is <strong>case insensitive</strong>.</p>
1849     *
1850     * <pre>
1851     * StringUtils.equalsIgnoreCase(null, null)   = true
1852     * StringUtils.equalsIgnoreCase(null, "abc")  = false
1853     * StringUtils.equalsIgnoreCase("abc", null)  = false
1854     * StringUtils.equalsIgnoreCase("abc", "abc") = true
1855     * StringUtils.equalsIgnoreCase("abc", "ABC") = true
1856     * </pre>
1857     *
1858     * @param cs1  the first CharSequence, may be {@code null}
1859     * @param cs2  the second CharSequence, may be {@code null}
1860     * @return {@code true} if the CharSequences are equal (case-insensitive), or both {@code null}
1861     * @since 3.0 Changed signature from equalsIgnoreCase(String, String) to equalsIgnoreCase(CharSequence, CharSequence)
1862     * @see #equals(CharSequence, CharSequence)
1863     */
1864    public static boolean equalsIgnoreCase(final CharSequence cs1, final CharSequence cs2) {
1865        if (cs1 == cs2) {
1866            return true;
1867        }
1868        if (cs1 == null || cs2 == null) {
1869            return false;
1870        }
1871        if (cs1.length() != cs2.length()) {
1872            return false;
1873        }
1874        return CharSequenceUtils.regionMatches(cs1, true, 0, cs2, 0, cs1.length());
1875    }
1876
1877    /**
1878     * <p>Returns the first value in the array which is not empty (""),
1879     * {@code null} or whitespace only.</p>
1880     *
1881     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
1882     *
1883     * <p>If all values are blank or the array is {@code null}
1884     * or empty then {@code null} is returned.</p>
1885     *
1886     * <pre>
1887     * StringUtils.firstNonBlank(null, null, null)     = null
1888     * StringUtils.firstNonBlank(null, "", " ")        = null
1889     * StringUtils.firstNonBlank("abc")                = "abc"
1890     * StringUtils.firstNonBlank(null, "xyz")          = "xyz"
1891     * StringUtils.firstNonBlank(null, "", " ", "xyz") = "xyz"
1892     * StringUtils.firstNonBlank(null, "xyz", "abc")   = "xyz"
1893     * StringUtils.firstNonBlank()                     = null
1894     * </pre>
1895     *
1896     * @param <T> the specific kind of CharSequence
1897     * @param values  the values to test, may be {@code null} or empty
1898     * @return the first value from {@code values} which is not blank,
1899     *  or {@code null} if there are no non-blank values
1900     * @since 3.8
1901     */
1902    @SafeVarargs
1903    public static <T extends CharSequence> T firstNonBlank(final T... values) {
1904        if (values != null) {
1905            for (final T val : values) {
1906                if (isNotBlank(val)) {
1907                    return val;
1908                }
1909            }
1910        }
1911        return null;
1912    }
1913
1914    /**
1915     * <p>Returns the first value in the array which is not empty.</p>
1916     *
1917     * <p>If all values are empty or the array is {@code null}
1918     * or empty then {@code null} is returned.</p>
1919     *
1920     * <pre>
1921     * StringUtils.firstNonEmpty(null, null, null)   = null
1922     * StringUtils.firstNonEmpty(null, null, "")     = null
1923     * StringUtils.firstNonEmpty(null, "", " ")      = " "
1924     * StringUtils.firstNonEmpty("abc")              = "abc"
1925     * StringUtils.firstNonEmpty(null, "xyz")        = "xyz"
1926     * StringUtils.firstNonEmpty("", "xyz")          = "xyz"
1927     * StringUtils.firstNonEmpty(null, "xyz", "abc") = "xyz"
1928     * StringUtils.firstNonEmpty()                   = null
1929     * </pre>
1930     *
1931     * @param <T> the specific kind of CharSequence
1932     * @param values  the values to test, may be {@code null} or empty
1933     * @return the first value from {@code values} which is not empty,
1934     *  or {@code null} if there are no non-empty values
1935     * @since 3.8
1936     */
1937    @SafeVarargs
1938    public static <T extends CharSequence> T firstNonEmpty(final T... values) {
1939        if (values != null) {
1940            for (final T val : values) {
1941                if (isNotEmpty(val)) {
1942                    return val;
1943                }
1944            }
1945        }
1946        return null;
1947    }
1948
1949    /**
1950     * Calls {@link String#getBytes(Charset)} in a null-safe manner.
1951     *
1952     * @param string input string
1953     * @param charset The {@link Charset} to encode the {@code String}. If null, then use the default Charset.
1954     * @return The empty byte[] if {@code string} is null, the result of {@link String#getBytes(Charset)} otherwise.
1955     * @see String#getBytes(Charset)
1956     * @since 3.10
1957     */
1958    public static byte[] getBytes(final String string, final Charset charset) {
1959        return string == null ? ArrayUtils.EMPTY_BYTE_ARRAY : string.getBytes(Charsets.toCharset(charset));
1960    }
1961
1962    /**
1963     * Calls {@link String#getBytes(String)} in a null-safe manner.
1964     *
1965     * @param string input string
1966     * @param charset The {@link Charset} name to encode the {@code String}. If null, then use the default Charset.
1967     * @return The empty byte[] if {@code string} is null, the result of {@link String#getBytes(String)} otherwise.
1968     * @throws UnsupportedEncodingException Thrown when the named charset is not supported.
1969     * @see String#getBytes(String)
1970     * @since 3.10
1971     */
1972    public static byte[] getBytes(final String string, final String charset) throws UnsupportedEncodingException {
1973        return string == null ? ArrayUtils.EMPTY_BYTE_ARRAY : string.getBytes(Charsets.toCharsetName(charset));
1974    }
1975
1976    /**
1977     * <p>Compares all Strings in an array and returns the initial sequence of
1978     * characters that is common to all of them.</p>
1979     *
1980     * <p>For example,
1981     * {@code getCommonPrefix(new String[] {"i am a machine", "i am a robot"}) -&gt; "i am a "}</p>
1982     *
1983     * <pre>
1984     * StringUtils.getCommonPrefix(null) = ""
1985     * StringUtils.getCommonPrefix(new String[] {}) = ""
1986     * StringUtils.getCommonPrefix(new String[] {"abc"}) = "abc"
1987     * StringUtils.getCommonPrefix(new String[] {null, null}) = ""
1988     * StringUtils.getCommonPrefix(new String[] {"", ""}) = ""
1989     * StringUtils.getCommonPrefix(new String[] {"", null}) = ""
1990     * StringUtils.getCommonPrefix(new String[] {"abc", null, null}) = ""
1991     * StringUtils.getCommonPrefix(new String[] {null, null, "abc"}) = ""
1992     * StringUtils.getCommonPrefix(new String[] {"", "abc"}) = ""
1993     * StringUtils.getCommonPrefix(new String[] {"abc", ""}) = ""
1994     * StringUtils.getCommonPrefix(new String[] {"abc", "abc"}) = "abc"
1995     * StringUtils.getCommonPrefix(new String[] {"abc", "a"}) = "a"
1996     * StringUtils.getCommonPrefix(new String[] {"ab", "abxyz"}) = "ab"
1997     * StringUtils.getCommonPrefix(new String[] {"abcde", "abxyz"}) = "ab"
1998     * StringUtils.getCommonPrefix(new String[] {"abcde", "xyz"}) = ""
1999     * StringUtils.getCommonPrefix(new String[] {"xyz", "abcde"}) = ""
2000     * StringUtils.getCommonPrefix(new String[] {"i am a machine", "i am a robot"}) = "i am a "
2001     * </pre>
2002     *
2003     * @param strs  array of String objects, entries may be null
2004     * @return the initial sequence of characters that are common to all Strings
2005     * in the array; empty String if the array is null, the elements are all null
2006     * or if there is no common prefix.
2007     * @since 2.4
2008     */
2009    public static String getCommonPrefix(final String... strs) {
2010        if (ArrayUtils.isEmpty(strs)) {
2011            return EMPTY;
2012        }
2013        final int smallestIndexOfDiff = indexOfDifference(strs);
2014        if (smallestIndexOfDiff == INDEX_NOT_FOUND) {
2015            // all strings were identical
2016            if (strs[0] == null) {
2017                return EMPTY;
2018            }
2019            return strs[0];
2020        } else if (smallestIndexOfDiff == 0) {
2021            // there were no common initial characters
2022            return EMPTY;
2023        } else {
2024            // we found a common initial character sequence
2025            return strs[0].substring(0, smallestIndexOfDiff);
2026        }
2027    }
2028
2029    /**
2030     * <p>Checks if a String {@code str} contains Unicode digits,
2031     * if yes then concatenate all the digits in {@code str} and return it as a String.</p>
2032     *
2033     * <p>An empty ("") String will be returned if no digits found in {@code str}.</p>
2034     *
2035     * <pre>
2036     * StringUtils.getDigits(null)  = null
2037     * StringUtils.getDigits("")    = ""
2038     * StringUtils.getDigits("abc") = ""
2039     * StringUtils.getDigits("1000$") = "1000"
2040     * StringUtils.getDigits("1123~45") = "112345"
2041     * StringUtils.getDigits("(541) 754-3010") = "5417543010"
2042     * StringUtils.getDigits("\u0967\u0968\u0969") = "\u0967\u0968\u0969"
2043     * </pre>
2044     *
2045     * @param str the String to extract digits from, may be null
2046     * @return String with only digits,
2047     *           or an empty ("") String if no digits found,
2048     *           or {@code null} String if {@code str} is null
2049     * @since 3.6
2050     */
2051    public static String getDigits(final String str) {
2052        if (isEmpty(str)) {
2053            return str;
2054        }
2055        final int sz = str.length();
2056        final StringBuilder strDigits = new StringBuilder(sz);
2057        for (int i = 0; i < sz; i++) {
2058            final char tempChar = str.charAt(i);
2059            if (Character.isDigit(tempChar)) {
2060                strDigits.append(tempChar);
2061            }
2062        }
2063        return strDigits.toString();
2064    }
2065
2066    /**
2067     * <p>Find the Fuzzy Distance which indicates the similarity score between two Strings.</p>
2068     *
2069     * <p>This string matching algorithm is similar to the algorithms of editors such as Sublime Text,
2070     * TextMate, Atom and others. One point is given for every matched character. Subsequent
2071     * matches yield two bonus points. A higher score indicates a higher similarity.</p>
2072     *
2073     * <pre>
2074     * StringUtils.getFuzzyDistance(null, null, null)                                    = IllegalArgumentException
2075     * StringUtils.getFuzzyDistance("", "", Locale.ENGLISH)                              = 0
2076     * StringUtils.getFuzzyDistance("Workshop", "b", Locale.ENGLISH)                     = 0
2077     * StringUtils.getFuzzyDistance("Room", "o", Locale.ENGLISH)                         = 1
2078     * StringUtils.getFuzzyDistance("Workshop", "w", Locale.ENGLISH)                     = 1
2079     * StringUtils.getFuzzyDistance("Workshop", "ws", Locale.ENGLISH)                    = 2
2080     * StringUtils.getFuzzyDistance("Workshop", "wo", Locale.ENGLISH)                    = 4
2081     * StringUtils.getFuzzyDistance("Apache Software Foundation", "asf", Locale.ENGLISH) = 3
2082     * </pre>
2083     *
2084     * @param term a full term that should be matched against, must not be null
2085     * @param query the query that will be matched against a term, must not be null
2086     * @param locale This string matching logic is case insensitive. A locale is necessary to normalize
2087     *  both Strings to lower case.
2088     * @return result score
2089     * @throws IllegalArgumentException if either String input {@code null} or Locale input {@code null}
2090     * @since 3.4
2091     * @deprecated as of 3.6, use commons-text
2092     * <a href="https://commons.apache.org/proper/commons-text/javadocs/api-release/org/apache/commons/text/similarity/FuzzyScore.html">
2093     * FuzzyScore</a> instead
2094     */
2095    @Deprecated
2096    public static int getFuzzyDistance(final CharSequence term, final CharSequence query, final Locale locale) {
2097        if (term == null || query == null) {
2098            throw new IllegalArgumentException("Strings must not be null");
2099        } else if (locale == null) {
2100            throw new IllegalArgumentException("Locale must not be null");
2101        }
2102
2103        // fuzzy logic is case insensitive. We normalize the Strings to lower
2104        // case right from the start. Turning characters to lower case
2105        // via Character.toLowerCase(char) is unfortunately insufficient
2106        // as it does not accept a locale.
2107        final String termLowerCase = term.toString().toLowerCase(locale);
2108        final String queryLowerCase = query.toString().toLowerCase(locale);
2109
2110        // the resulting score
2111        int score = 0;
2112
2113        // the position in the term which will be scanned next for potential
2114        // query character matches
2115        int termIndex = 0;
2116
2117        // index of the previously matched character in the term
2118        int previousMatchingCharacterIndex = Integer.MIN_VALUE;
2119
2120        for (int queryIndex = 0; queryIndex < queryLowerCase.length(); queryIndex++) {
2121            final char queryChar = queryLowerCase.charAt(queryIndex);
2122
2123            boolean termCharacterMatchFound = false;
2124            for (; termIndex < termLowerCase.length() && !termCharacterMatchFound; termIndex++) {
2125                final char termChar = termLowerCase.charAt(termIndex);
2126
2127                if (queryChar == termChar) {
2128                    // simple character matches result in one point
2129                    score++;
2130
2131                    // subsequent character matches further improve
2132                    // the score.
2133                    if (previousMatchingCharacterIndex + 1 == termIndex) {
2134                        score += 2;
2135                    }
2136
2137                    previousMatchingCharacterIndex = termIndex;
2138
2139                    // we can leave the nested loop. Every character in the
2140                    // query can match at most one character in the term.
2141                    termCharacterMatchFound = true;
2142                }
2143            }
2144        }
2145
2146        return score;
2147    }
2148
2149    /**
2150     * <p>Returns either the passed in CharSequence, or if the CharSequence is
2151     * whitespace, empty ("") or {@code null}, the value supplied by {@code defaultStrSupplier}.</p>
2152     *
2153     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
2154     *
2155     * <p>Caller responsible for thread-safety and exception handling of default value supplier</p>
2156     *
2157     * <pre>
2158     * {@code
2159     * StringUtils.getIfBlank(null, () -> "NULL")   = "NULL"
2160     * StringUtils.getIfBlank("", () -> "NULL")     = "NULL"
2161     * StringUtils.getIfBlank(" ", () -> "NULL")    = "NULL"
2162     * StringUtils.getIfBlank("bat", () -> "NULL")  = "bat"
2163     * StringUtils.getIfBlank("", () -> null)       = null
2164     * StringUtils.getIfBlank("", null)             = null
2165     * }</pre>
2166     * @param <T> the specific kind of CharSequence
2167     * @param str the CharSequence to check, may be null
2168     * @param defaultSupplier the supplier of default CharSequence to return
2169     *  if the input is whitespace, empty ("") or {@code null}, may be null
2170     * @return the passed in CharSequence, or the default
2171     * @see StringUtils#defaultString(String, String)
2172     * @since 3.10
2173     */
2174    public static <T extends CharSequence> T getIfBlank(final T str, final Supplier<T> defaultSupplier) {
2175        return isBlank(str) ? defaultSupplier == null ? null : defaultSupplier.get() : str;
2176    }
2177
2178    /**
2179     * <p>Returns either the passed in CharSequence, or if the CharSequence is
2180     * empty or {@code null}, the value supplied by {@code defaultStrSupplier}.</p>
2181     *
2182     * <p>Caller responsible for thread-safety and exception handling of default value supplier</p>
2183     *
2184     * <pre>
2185     * {@code
2186     * StringUtils.getIfEmpty(null, () -> "NULL")    = "NULL"
2187     * StringUtils.getIfEmpty("", () -> "NULL")      = "NULL"
2188     * StringUtils.getIfEmpty(" ", () -> "NULL")     = " "
2189     * StringUtils.getIfEmpty("bat", () -> "NULL")   = "bat"
2190     * StringUtils.getIfEmpty("", () -> null)        = null
2191     * StringUtils.getIfEmpty("", null)              = null
2192     * }
2193     * </pre>
2194     * @param <T> the specific kind of CharSequence
2195     * @param str  the CharSequence to check, may be null
2196     * @param defaultSupplier  the supplier of default CharSequence to return
2197     *  if the input is empty ("") or {@code null}, may be null
2198     * @return the passed in CharSequence, or the default
2199     * @see StringUtils#defaultString(String, String)
2200     * @since 3.10
2201     */
2202    public static <T extends CharSequence> T getIfEmpty(final T str, final Supplier<T> defaultSupplier) {
2203        return isEmpty(str) ? defaultSupplier == null ? null : defaultSupplier.get() : str;
2204    }
2205
2206    /**
2207     * <p>Find the Jaro Winkler Distance which indicates the similarity score between two Strings.</p>
2208     *
2209     * <p>The Jaro measure is the weighted sum of percentage of matched characters from each file and transposed characters.
2210     * Winkler increased this measure for matching initial characters.</p>
2211     *
2212     * <p>This implementation is based on the Jaro Winkler similarity algorithm
2213     * from <a href="http://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance">http://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance</a>.</p>
2214     *
2215     * <pre>
2216     * StringUtils.getJaroWinklerDistance(null, null)          = IllegalArgumentException
2217     * StringUtils.getJaroWinklerDistance("", "")              = 0.0
2218     * StringUtils.getJaroWinklerDistance("", "a")             = 0.0
2219     * StringUtils.getJaroWinklerDistance("aaapppp", "")       = 0.0
2220     * StringUtils.getJaroWinklerDistance("frog", "fog")       = 0.93
2221     * StringUtils.getJaroWinklerDistance("fly", "ant")        = 0.0
2222     * StringUtils.getJaroWinklerDistance("elephant", "hippo") = 0.44
2223     * StringUtils.getJaroWinklerDistance("hippo", "elephant") = 0.44
2224     * StringUtils.getJaroWinklerDistance("hippo", "zzzzzzzz") = 0.0
2225     * StringUtils.getJaroWinklerDistance("hello", "hallo")    = 0.88
2226     * StringUtils.getJaroWinklerDistance("ABC Corporation", "ABC Corp") = 0.93
2227     * StringUtils.getJaroWinklerDistance("D N H Enterprises Inc", "D &amp; H Enterprises, Inc.") = 0.95
2228     * StringUtils.getJaroWinklerDistance("My Gym Children's Fitness Center", "My Gym. Childrens Fitness") = 0.92
2229     * StringUtils.getJaroWinklerDistance("PENNSYLVANIA", "PENNCISYLVNIA") = 0.88
2230     * </pre>
2231     *
2232     * @param first the first String, must not be null
2233     * @param second the second String, must not be null
2234     * @return result distance
2235     * @throws IllegalArgumentException if either String input {@code null}
2236     * @since 3.3
2237     * @deprecated as of 3.6, use commons-text
2238     * <a href="https://commons.apache.org/proper/commons-text/javadocs/api-release/org/apache/commons/text/similarity/JaroWinklerDistance.html">
2239     * JaroWinklerDistance</a> instead
2240     */
2241    @Deprecated
2242    public static double getJaroWinklerDistance(final CharSequence first, final CharSequence second) {
2243        final double DEFAULT_SCALING_FACTOR = 0.1;
2244
2245        if (first == null || second == null) {
2246            throw new IllegalArgumentException("Strings must not be null");
2247        }
2248
2249        final int[] mtp = matches(first, second);
2250        final double m = mtp[0];
2251        if (m == 0) {
2252            return 0D;
2253        }
2254        final double j = ((m / first.length() + m / second.length() + (m - mtp[1]) / m)) / 3;
2255        final double jw = j < 0.7D ? j : j + Math.min(DEFAULT_SCALING_FACTOR, 1D / mtp[3]) * mtp[2] * (1D - j);
2256        return Math.round(jw * 100.0D) / 100.0D;
2257    }
2258
2259    // Misc
2260    //-----------------------------------------------------------------------
2261    /**
2262     * <p>Find the Levenshtein distance between two Strings.</p>
2263     *
2264     * <p>This is the number of changes needed to change one String into
2265     * another, where each change is a single character modification (deletion,
2266     * insertion or substitution).</p>
2267     *
2268     * <p>The implementation uses a single-dimensional array of length s.length() + 1. See
2269     * <a href="http://blog.softwx.net/2014/12/optimizing-levenshtein-algorithm-in-c.html">
2270     * http://blog.softwx.net/2014/12/optimizing-levenshtein-algorithm-in-c.html</a> for details.</p>
2271     *
2272     * <pre>
2273     * StringUtils.getLevenshteinDistance(null, *)             = IllegalArgumentException
2274     * StringUtils.getLevenshteinDistance(*, null)             = IllegalArgumentException
2275     * StringUtils.getLevenshteinDistance("", "")              = 0
2276     * StringUtils.getLevenshteinDistance("", "a")             = 1
2277     * StringUtils.getLevenshteinDistance("aaapppp", "")       = 7
2278     * StringUtils.getLevenshteinDistance("frog", "fog")       = 1
2279     * StringUtils.getLevenshteinDistance("fly", "ant")        = 3
2280     * StringUtils.getLevenshteinDistance("elephant", "hippo") = 7
2281     * StringUtils.getLevenshteinDistance("hippo", "elephant") = 7
2282     * StringUtils.getLevenshteinDistance("hippo", "zzzzzzzz") = 8
2283     * StringUtils.getLevenshteinDistance("hello", "hallo")    = 1
2284     * </pre>
2285     *
2286     * @param s  the first String, must not be null
2287     * @param t  the second String, must not be null
2288     * @return result distance
2289     * @throws IllegalArgumentException if either String input {@code null}
2290     * @since 3.0 Changed signature from getLevenshteinDistance(String, String) to
2291     * getLevenshteinDistance(CharSequence, CharSequence)
2292     * @deprecated as of 3.6, use commons-text
2293     * <a href="https://commons.apache.org/proper/commons-text/javadocs/api-release/org/apache/commons/text/similarity/LevenshteinDistance.html">
2294     * LevenshteinDistance</a> instead
2295     */
2296    @Deprecated
2297    public static int getLevenshteinDistance(CharSequence s, CharSequence t) {
2298        if (s == null || t == null) {
2299            throw new IllegalArgumentException("Strings must not be null");
2300        }
2301
2302        int n = s.length();
2303        int m = t.length();
2304
2305        if (n == 0) {
2306            return m;
2307        } else if (m == 0) {
2308            return n;
2309        }
2310
2311        if (n > m) {
2312            // swap the input strings to consume less memory
2313            final CharSequence tmp = s;
2314            s = t;
2315            t = tmp;
2316            n = m;
2317            m = t.length();
2318        }
2319
2320        final int p[] = new int[n + 1];
2321        // indexes into strings s and t
2322        int i; // iterates through s
2323        int j; // iterates through t
2324        int upper_left;
2325        int upper;
2326
2327        char t_j; // jth character of t
2328        int cost;
2329
2330        for (i = 0; i <= n; i++) {
2331            p[i] = i;
2332        }
2333
2334        for (j = 1; j <= m; j++) {
2335            upper_left = p[0];
2336            t_j = t.charAt(j - 1);
2337            p[0] = j;
2338
2339            for (i = 1; i <= n; i++) {
2340                upper = p[i];
2341                cost = s.charAt(i - 1) == t_j ? 0 : 1;
2342                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
2343                p[i] = Math.min(Math.min(p[i - 1] + 1, p[i] + 1), upper_left + cost);
2344                upper_left = upper;
2345            }
2346        }
2347
2348        return p[n];
2349    }
2350
2351    /**
2352     * <p>Find the Levenshtein distance between two Strings if it's less than or equal to a given
2353     * threshold.</p>
2354     *
2355     * <p>This is the number of changes needed to change one String into
2356     * another, where each change is a single character modification (deletion,
2357     * insertion or substitution).</p>
2358     *
2359     * <p>This implementation follows from Algorithms on Strings, Trees and Sequences by Dan Gusfield
2360     * and Chas Emerick's implementation of the Levenshtein distance algorithm from
2361     * <a href="http://www.merriampark.com/ld.htm">http://www.merriampark.com/ld.htm</a></p>
2362     *
2363     * <pre>
2364     * StringUtils.getLevenshteinDistance(null, *, *)             = IllegalArgumentException
2365     * StringUtils.getLevenshteinDistance(*, null, *)             = IllegalArgumentException
2366     * StringUtils.getLevenshteinDistance(*, *, -1)               = IllegalArgumentException
2367     * StringUtils.getLevenshteinDistance("", "", 0)              = 0
2368     * StringUtils.getLevenshteinDistance("aaapppp", "", 8)       = 7
2369     * StringUtils.getLevenshteinDistance("aaapppp", "", 7)       = 7
2370     * StringUtils.getLevenshteinDistance("aaapppp", "", 6))      = -1
2371     * StringUtils.getLevenshteinDistance("elephant", "hippo", 7) = 7
2372     * StringUtils.getLevenshteinDistance("elephant", "hippo", 6) = -1
2373     * StringUtils.getLevenshteinDistance("hippo", "elephant", 7) = 7
2374     * StringUtils.getLevenshteinDistance("hippo", "elephant", 6) = -1
2375     * </pre>
2376     *
2377     * @param s  the first String, must not be null
2378     * @param t  the second String, must not be null
2379     * @param threshold the target threshold, must not be negative
2380     * @return result distance, or {@code -1} if the distance would be greater than the threshold
2381     * @throws IllegalArgumentException if either String input {@code null} or negative threshold
2382     * @deprecated as of 3.6, use commons-text
2383     * <a href="https://commons.apache.org/proper/commons-text/javadocs/api-release/org/apache/commons/text/similarity/LevenshteinDistance.html">
2384     * LevenshteinDistance</a> instead
2385     */
2386    @Deprecated
2387    public static int getLevenshteinDistance(CharSequence s, CharSequence t, final int threshold) {
2388        if (s == null || t == null) {
2389            throw new IllegalArgumentException("Strings must not be null");
2390        }
2391        if (threshold < 0) {
2392            throw new IllegalArgumentException("Threshold must not be negative");
2393        }
2394
2395        /*
2396        This implementation only computes the distance if it's less than or equal to the
2397        threshold value, returning -1 if it's greater.  The advantage is performance: unbounded
2398        distance is O(nm), but a bound of k allows us to reduce it to O(km) time by only
2399        computing a diagonal stripe of width 2k + 1 of the cost table.
2400        It is also possible to use this to compute the unbounded Levenshtein distance by starting
2401        the threshold at 1 and doubling each time until the distance is found; this is O(dm), where
2402        d is the distance.
2403
2404        One subtlety comes from needing to ignore entries on the border of our stripe
2405        eg.
2406        p[] = |#|#|#|*
2407        d[] =  *|#|#|#|
2408        We must ignore the entry to the left of the leftmost member
2409        We must ignore the entry above the rightmost member
2410
2411        Another subtlety comes from our stripe running off the matrix if the strings aren't
2412        of the same size.  Since string s is always swapped to be the shorter of the two,
2413        the stripe will always run off to the upper right instead of the lower left of the matrix.
2414
2415        As a concrete example, suppose s is of length 5, t is of length 7, and our threshold is 1.
2416        In this case we're going to walk a stripe of length 3.  The matrix would look like so:
2417
2418           1 2 3 4 5
2419        1 |#|#| | | |
2420        2 |#|#|#| | |
2421        3 | |#|#|#| |
2422        4 | | |#|#|#|
2423        5 | | | |#|#|
2424        6 | | | | |#|
2425        7 | | | | | |
2426
2427        Note how the stripe leads off the table as there is no possible way to turn a string of length 5
2428        into one of length 7 in edit distance of 1.
2429
2430        Additionally, this implementation decreases memory usage by using two
2431        single-dimensional arrays and swapping them back and forth instead of allocating
2432        an entire n by m matrix.  This requires a few minor changes, such as immediately returning
2433        when it's detected that the stripe has run off the matrix and initially filling the arrays with
2434        large values so that entries we don't compute are ignored.
2435
2436        See Algorithms on Strings, Trees and Sequences by Dan Gusfield for some discussion.
2437         */
2438
2439        int n = s.length(); // length of s
2440        int m = t.length(); // length of t
2441
2442        // if one string is empty, the edit distance is necessarily the length of the other
2443        if (n == 0) {
2444            return m <= threshold ? m : -1;
2445        } else if (m == 0) {
2446            return n <= threshold ? n : -1;
2447        } else if (Math.abs(n - m) > threshold) {
2448            // no need to calculate the distance if the length difference is greater than the threshold
2449            return -1;
2450        }
2451
2452        if (n > m) {
2453            // swap the two strings to consume less memory
2454            final CharSequence tmp = s;
2455            s = t;
2456            t = tmp;
2457            n = m;
2458            m = t.length();
2459        }
2460
2461        int p[] = new int[n + 1]; // 'previous' cost array, horizontally
2462        int d[] = new int[n + 1]; // cost array, horizontally
2463        int _d[]; // placeholder to assist in swapping p and d
2464
2465        // fill in starting table values
2466        final int boundary = Math.min(n, threshold) + 1;
2467        for (int i = 0; i < boundary; i++) {
2468            p[i] = i;
2469        }
2470        // these fills ensure that the value above the rightmost entry of our
2471        // stripe will be ignored in following loop iterations
2472        Arrays.fill(p, boundary, p.length, Integer.MAX_VALUE);
2473        Arrays.fill(d, Integer.MAX_VALUE);
2474
2475        // iterates through t
2476        for (int j = 1; j <= m; j++) {
2477            final char t_j = t.charAt(j - 1); // jth character of t
2478            d[0] = j;
2479
2480            // compute stripe indices, constrain to array size
2481            final int min = Math.max(1, j - threshold);
2482            final int max = j > Integer.MAX_VALUE - threshold ? n : Math.min(n, j + threshold);
2483
2484            // the stripe may lead off of the table if s and t are of different sizes
2485            if (min > max) {
2486                return -1;
2487            }
2488
2489            // ignore entry left of leftmost
2490            if (min > 1) {
2491                d[min - 1] = Integer.MAX_VALUE;
2492            }
2493
2494            // iterates through [min, max] in s
2495            for (int i = min; i <= max; i++) {
2496                if (s.charAt(i - 1) == t_j) {
2497                    // diagonally left and up
2498                    d[i] = p[i - 1];
2499                } else {
2500                    // 1 + minimum of cell to the left, to the top, diagonally left and up
2501                    d[i] = 1 + Math.min(Math.min(d[i - 1], p[i]), p[i - 1]);
2502                }
2503            }
2504
2505            // copy current distance counts to 'previous row' distance counts
2506            _d = p;
2507            p = d;
2508            d = _d;
2509        }
2510
2511        // if p[n] is greater than the threshold, there's no guarantee on it being the correct
2512        // distance
2513        if (p[n] <= threshold) {
2514            return p[n];
2515        }
2516        return -1;
2517    }
2518
2519    /**
2520     * <p>Finds the first index within a CharSequence, handling {@code null}.
2521     * This method uses {@link String#indexOf(String, int)} if possible.</p>
2522     *
2523     * <p>A {@code null} CharSequence will return {@code -1}.</p>
2524     *
2525     * <pre>
2526     * StringUtils.indexOf(null, *)          = -1
2527     * StringUtils.indexOf(*, null)          = -1
2528     * StringUtils.indexOf("", "")           = 0
2529     * StringUtils.indexOf("", *)            = -1 (except when * = "")
2530     * StringUtils.indexOf("aabaabaa", "a")  = 0
2531     * StringUtils.indexOf("aabaabaa", "b")  = 2
2532     * StringUtils.indexOf("aabaabaa", "ab") = 1
2533     * StringUtils.indexOf("aabaabaa", "")   = 0
2534     * </pre>
2535     *
2536     * @param seq  the CharSequence to check, may be null
2537     * @param searchSeq  the CharSequence to find, may be null
2538     * @return the first index of the search CharSequence,
2539     *  -1 if no match or {@code null} string input
2540     * @since 2.0
2541     * @since 3.0 Changed signature from indexOf(String, String) to indexOf(CharSequence, CharSequence)
2542     */
2543    public static int indexOf(final CharSequence seq, final CharSequence searchSeq) {
2544        if (seq == null || searchSeq == null) {
2545            return INDEX_NOT_FOUND;
2546        }
2547        return CharSequenceUtils.indexOf(seq, searchSeq, 0);
2548    }
2549
2550    /**
2551     * <p>Finds the first index within a CharSequence, handling {@code null}.
2552     * This method uses {@link String#indexOf(String, int)} if possible.</p>
2553     *
2554     * <p>A {@code null} CharSequence will return {@code -1}.
2555     * A negative start position is treated as zero.
2556     * An empty ("") search CharSequence always matches.
2557     * A start position greater than the string length only matches
2558     * an empty search CharSequence.</p>
2559     *
2560     * <pre>
2561     * StringUtils.indexOf(null, *, *)          = -1
2562     * StringUtils.indexOf(*, null, *)          = -1
2563     * StringUtils.indexOf("", "", 0)           = 0
2564     * StringUtils.indexOf("", *, 0)            = -1 (except when * = "")
2565     * StringUtils.indexOf("aabaabaa", "a", 0)  = 0
2566     * StringUtils.indexOf("aabaabaa", "b", 0)  = 2
2567     * StringUtils.indexOf("aabaabaa", "ab", 0) = 1
2568     * StringUtils.indexOf("aabaabaa", "b", 3)  = 5
2569     * StringUtils.indexOf("aabaabaa", "b", 9)  = -1
2570     * StringUtils.indexOf("aabaabaa", "b", -1) = 2
2571     * StringUtils.indexOf("aabaabaa", "", 2)   = 2
2572     * StringUtils.indexOf("abc", "", 9)        = 3
2573     * </pre>
2574     *
2575     * @param seq  the CharSequence to check, may be null
2576     * @param searchSeq  the CharSequence to find, may be null
2577     * @param startPos  the start position, negative treated as zero
2578     * @return the first index of the search CharSequence (always &ge; startPos),
2579     *  -1 if no match or {@code null} string input
2580     * @since 2.0
2581     * @since 3.0 Changed signature from indexOf(String, String, int) to indexOf(CharSequence, CharSequence, int)
2582     */
2583    public static int indexOf(final CharSequence seq, final CharSequence searchSeq, final int startPos) {
2584        if (seq == null || searchSeq == null) {
2585            return INDEX_NOT_FOUND;
2586        }
2587        return CharSequenceUtils.indexOf(seq, searchSeq, startPos);
2588    }
2589
2590    // IndexOf
2591    //-----------------------------------------------------------------------
2592    /**
2593     * Returns the index within {@code seq} of the first occurrence of
2594     * the specified character. If a character with value
2595     * {@code searchChar} occurs in the character sequence represented by
2596     * {@code seq} {@code CharSequence} object, then the index (in Unicode
2597     * code units) of the first such occurrence is returned. For
2598     * values of {@code searchChar} in the range from 0 to 0xFFFF
2599     * (inclusive), this is the smallest value <i>k</i> such that:
2600     * <blockquote><pre>
2601     * this.charAt(<i>k</i>) == searchChar
2602     * </pre></blockquote>
2603     * is true. For other values of {@code searchChar}, it is the
2604     * smallest value <i>k</i> such that:
2605     * <blockquote><pre>
2606     * this.codePointAt(<i>k</i>) == searchChar
2607     * </pre></blockquote>
2608     * is true. In either case, if no such character occurs in {@code seq},
2609     * then {@code INDEX_NOT_FOUND (-1)} is returned.
2610     *
2611     * <p>Furthermore, a {@code null} or empty ("") CharSequence will
2612     * return {@code INDEX_NOT_FOUND (-1)}.</p>
2613     *
2614     * <pre>
2615     * StringUtils.indexOf(null, *)         = -1
2616     * StringUtils.indexOf("", *)           = -1
2617     * StringUtils.indexOf("aabaabaa", 'a') = 0
2618     * StringUtils.indexOf("aabaabaa", 'b') = 2
2619     * </pre>
2620     *
2621     * @param seq  the CharSequence to check, may be null
2622     * @param searchChar  the character to find
2623     * @return the first index of the search character,
2624     *  -1 if no match or {@code null} string input
2625     * @since 2.0
2626     * @since 3.0 Changed signature from indexOf(String, int) to indexOf(CharSequence, int)
2627     * @since 3.6 Updated {@link CharSequenceUtils} call to behave more like {@code String}
2628     */
2629    public static int indexOf(final CharSequence seq, final int searchChar) {
2630        if (isEmpty(seq)) {
2631            return INDEX_NOT_FOUND;
2632        }
2633        return CharSequenceUtils.indexOf(seq, searchChar, 0);
2634    }
2635
2636    /**
2637     *
2638     * Returns the index within {@code seq} of the first occurrence of the
2639     * specified character, starting the search at the specified index.
2640     * <p>
2641     * If a character with value {@code searchChar} occurs in the
2642     * character sequence represented by the {@code seq} {@code CharSequence}
2643     * object at an index no smaller than {@code startPos}, then
2644     * the index of the first such occurrence is returned. For values
2645     * of {@code searchChar} in the range from 0 to 0xFFFF (inclusive),
2646     * this is the smallest value <i>k</i> such that:
2647     * <blockquote><pre>
2648     * (this.charAt(<i>k</i>) == searchChar) &amp;&amp; (<i>k</i> &gt;= startPos)
2649     * </pre></blockquote>
2650     * is true. For other values of {@code searchChar}, it is the
2651     * smallest value <i>k</i> such that:
2652     * <blockquote><pre>
2653     * (this.codePointAt(<i>k</i>) == searchChar) &amp;&amp; (<i>k</i> &gt;= startPos)
2654     * </pre></blockquote>
2655     * is true. In either case, if no such character occurs in {@code seq}
2656     * at or after position {@code startPos}, then
2657     * {@code -1} is returned.
2658     *
2659     * <p>
2660     * There is no restriction on the value of {@code startPos}. If it
2661     * is negative, it has the same effect as if it were zero: this entire
2662     * string may be searched. If it is greater than the length of this
2663     * string, it has the same effect as if it were equal to the length of
2664     * this string: {@code (INDEX_NOT_FOUND) -1} is returned. Furthermore, a
2665     * {@code null} or empty ("") CharSequence will
2666     * return {@code (INDEX_NOT_FOUND) -1}.
2667     *
2668     * <p>All indices are specified in {@code char} values
2669     * (Unicode code units).
2670     *
2671     * <pre>
2672     * StringUtils.indexOf(null, *, *)          = -1
2673     * StringUtils.indexOf("", *, *)            = -1
2674     * StringUtils.indexOf("aabaabaa", 'b', 0)  = 2
2675     * StringUtils.indexOf("aabaabaa", 'b', 3)  = 5
2676     * StringUtils.indexOf("aabaabaa", 'b', 9)  = -1
2677     * StringUtils.indexOf("aabaabaa", 'b', -1) = 2
2678     * </pre>
2679     *
2680     * @param seq  the CharSequence to check, may be null
2681     * @param searchChar  the character to find
2682     * @param startPos  the start position, negative treated as zero
2683     * @return the first index of the search character (always &ge; startPos),
2684     *  -1 if no match or {@code null} string input
2685     * @since 2.0
2686     * @since 3.0 Changed signature from indexOf(String, int, int) to indexOf(CharSequence, int, int)
2687     * @since 3.6 Updated {@link CharSequenceUtils} call to behave more like {@code String}
2688     */
2689    public static int indexOf(final CharSequence seq, final int searchChar, final int startPos) {
2690        if (isEmpty(seq)) {
2691            return INDEX_NOT_FOUND;
2692        }
2693        return CharSequenceUtils.indexOf(seq, searchChar, startPos);
2694    }
2695
2696    // IndexOfAny chars
2697    //-----------------------------------------------------------------------
2698    /**
2699     * <p>Search a CharSequence to find the first index of any
2700     * character in the given set of characters.</p>
2701     *
2702     * <p>A {@code null} String will return {@code -1}.
2703     * A {@code null} or zero length search array will return {@code -1}.</p>
2704     *
2705     * <pre>
2706     * StringUtils.indexOfAny(null, *)                  = -1
2707     * StringUtils.indexOfAny("", *)                    = -1
2708     * StringUtils.indexOfAny(*, null)                  = -1
2709     * StringUtils.indexOfAny(*, [])                    = -1
2710     * StringUtils.indexOfAny("zzabyycdxx", ['z', 'a']) = 0
2711     * StringUtils.indexOfAny("zzabyycdxx", ['b', 'y']) = 3
2712     * StringUtils.indexOfAny("aba", ['z'])             = -1
2713     * </pre>
2714     *
2715     * @param cs  the CharSequence to check, may be null
2716     * @param searchChars  the chars to search for, may be null
2717     * @return the index of any of the chars, -1 if no match or null input
2718     * @since 2.0
2719     * @since 3.0 Changed signature from indexOfAny(String, char[]) to indexOfAny(CharSequence, char...)
2720     */
2721    public static int indexOfAny(final CharSequence cs, final char... searchChars) {
2722        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
2723            return INDEX_NOT_FOUND;
2724        }
2725        final int csLen = cs.length();
2726        final int csLast = csLen - 1;
2727        final int searchLen = searchChars.length;
2728        final int searchLast = searchLen - 1;
2729        for (int i = 0; i < csLen; i++) {
2730            final char ch = cs.charAt(i);
2731            for (int j = 0; j < searchLen; j++) {
2732                if (searchChars[j] == ch) {
2733                    if (i < csLast && j < searchLast && Character.isHighSurrogate(ch)) {
2734                        // ch is a supplementary character
2735                        if (searchChars[j + 1] == cs.charAt(i + 1)) {
2736                            return i;
2737                        }
2738                    } else {
2739                        return i;
2740                    }
2741                }
2742            }
2743        }
2744        return INDEX_NOT_FOUND;
2745    }
2746
2747    // IndexOfAny strings
2748    //-----------------------------------------------------------------------
2749    /**
2750     * <p>Find the first index of any of a set of potential substrings.</p>
2751     *
2752     * <p>A {@code null} CharSequence will return {@code -1}.
2753     * A {@code null} or zero length search array will return {@code -1}.
2754     * A {@code null} search array entry will be ignored, but a search
2755     * array containing "" will return {@code 0} if {@code str} is not
2756     * null. This method uses {@link String#indexOf(String)} if possible.</p>
2757     *
2758     * <pre>
2759     * StringUtils.indexOfAny(null, *)                      = -1
2760     * StringUtils.indexOfAny(*, null)                      = -1
2761     * StringUtils.indexOfAny(*, [])                        = -1
2762     * StringUtils.indexOfAny("zzabyycdxx", ["ab", "cd"])   = 2
2763     * StringUtils.indexOfAny("zzabyycdxx", ["cd", "ab"])   = 2
2764     * StringUtils.indexOfAny("zzabyycdxx", ["mn", "op"])   = -1
2765     * StringUtils.indexOfAny("zzabyycdxx", ["zab", "aby"]) = 1
2766     * StringUtils.indexOfAny("zzabyycdxx", [""])           = 0
2767     * StringUtils.indexOfAny("", [""])                     = 0
2768     * StringUtils.indexOfAny("", ["a"])                    = -1
2769     * </pre>
2770     *
2771     * @param str  the CharSequence to check, may be null
2772     * @param searchStrs  the CharSequences to search for, may be null
2773     * @return the first index of any of the searchStrs in str, -1 if no match
2774     * @since 3.0 Changed signature from indexOfAny(String, String[]) to indexOfAny(CharSequence, CharSequence...)
2775     */
2776    public static int indexOfAny(final CharSequence str, final CharSequence... searchStrs) {
2777        if (str == null || searchStrs == null) {
2778            return INDEX_NOT_FOUND;
2779        }
2780
2781        // String's can't have a MAX_VALUEth index.
2782        int ret = Integer.MAX_VALUE;
2783
2784        int tmp = 0;
2785        for (final CharSequence search : searchStrs) {
2786            if (search == null) {
2787                continue;
2788            }
2789            tmp = CharSequenceUtils.indexOf(str, search, 0);
2790            if (tmp == INDEX_NOT_FOUND) {
2791                continue;
2792            }
2793
2794            if (tmp < ret) {
2795                ret = tmp;
2796            }
2797        }
2798
2799        return ret == Integer.MAX_VALUE ? INDEX_NOT_FOUND : ret;
2800    }
2801
2802    /**
2803     * <p>Search a CharSequence to find the first index of any
2804     * character in the given set of characters.</p>
2805     *
2806     * <p>A {@code null} String will return {@code -1}.
2807     * A {@code null} search string will return {@code -1}.</p>
2808     *
2809     * <pre>
2810     * StringUtils.indexOfAny(null, *)            = -1
2811     * StringUtils.indexOfAny("", *)              = -1
2812     * StringUtils.indexOfAny(*, null)            = -1
2813     * StringUtils.indexOfAny(*, "")              = -1
2814     * StringUtils.indexOfAny("zzabyycdxx", "za") = 0
2815     * StringUtils.indexOfAny("zzabyycdxx", "by") = 3
2816     * StringUtils.indexOfAny("aba", "z")         = -1
2817     * </pre>
2818     *
2819     * @param cs  the CharSequence to check, may be null
2820     * @param searchChars  the chars to search for, may be null
2821     * @return the index of any of the chars, -1 if no match or null input
2822     * @since 2.0
2823     * @since 3.0 Changed signature from indexOfAny(String, String) to indexOfAny(CharSequence, String)
2824     */
2825    public static int indexOfAny(final CharSequence cs, final String searchChars) {
2826        if (isEmpty(cs) || isEmpty(searchChars)) {
2827            return INDEX_NOT_FOUND;
2828        }
2829        return indexOfAny(cs, searchChars.toCharArray());
2830    }
2831
2832    // IndexOfAnyBut chars
2833    //-----------------------------------------------------------------------
2834    /**
2835     * <p>Searches a CharSequence to find the first index of any
2836     * character not in the given set of characters.</p>
2837     *
2838     * <p>A {@code null} CharSequence will return {@code -1}.
2839     * A {@code null} or zero length search array will return {@code -1}.</p>
2840     *
2841     * <pre>
2842     * StringUtils.indexOfAnyBut(null, *)                              = -1
2843     * StringUtils.indexOfAnyBut("", *)                                = -1
2844     * StringUtils.indexOfAnyBut(*, null)                              = -1
2845     * StringUtils.indexOfAnyBut(*, [])                                = -1
2846     * StringUtils.indexOfAnyBut("zzabyycdxx", new char[] {'z', 'a'} ) = 3
2847     * StringUtils.indexOfAnyBut("aba", new char[] {'z'} )             = 0
2848     * StringUtils.indexOfAnyBut("aba", new char[] {'a', 'b'} )        = -1
2849
2850     * </pre>
2851     *
2852     * @param cs  the CharSequence to check, may be null
2853     * @param searchChars  the chars to search for, may be null
2854     * @return the index of any of the chars, -1 if no match or null input
2855     * @since 2.0
2856     * @since 3.0 Changed signature from indexOfAnyBut(String, char[]) to indexOfAnyBut(CharSequence, char...)
2857     */
2858    public static int indexOfAnyBut(final CharSequence cs, final char... searchChars) {
2859        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
2860            return INDEX_NOT_FOUND;
2861        }
2862        final int csLen = cs.length();
2863        final int csLast = csLen - 1;
2864        final int searchLen = searchChars.length;
2865        final int searchLast = searchLen - 1;
2866        outer:
2867        for (int i = 0; i < csLen; i++) {
2868            final char ch = cs.charAt(i);
2869            for (int j = 0; j < searchLen; j++) {
2870                if (searchChars[j] == ch) {
2871                    if (i < csLast && j < searchLast && Character.isHighSurrogate(ch)) {
2872                        if (searchChars[j + 1] == cs.charAt(i + 1)) {
2873                            continue outer;
2874                        }
2875                    } else {
2876                        continue outer;
2877                    }
2878                }
2879            }
2880            return i;
2881        }
2882        return INDEX_NOT_FOUND;
2883    }
2884
2885    /**
2886     * <p>Search a CharSequence to find the first index of any
2887     * character not in the given set of characters.</p>
2888     *
2889     * <p>A {@code null} CharSequence will return {@code -1}.
2890     * A {@code null} or empty search string will return {@code -1}.</p>
2891     *
2892     * <pre>
2893     * StringUtils.indexOfAnyBut(null, *)            = -1
2894     * StringUtils.indexOfAnyBut("", *)              = -1
2895     * StringUtils.indexOfAnyBut(*, null)            = -1
2896     * StringUtils.indexOfAnyBut(*, "")              = -1
2897     * StringUtils.indexOfAnyBut("zzabyycdxx", "za") = 3
2898     * StringUtils.indexOfAnyBut("zzabyycdxx", "")   = -1
2899     * StringUtils.indexOfAnyBut("aba", "ab")        = -1
2900     * </pre>
2901     *
2902     * @param seq  the CharSequence to check, may be null
2903     * @param searchChars  the chars to search for, may be null
2904     * @return the index of any of the chars, -1 if no match or null input
2905     * @since 2.0
2906     * @since 3.0 Changed signature from indexOfAnyBut(String, String) to indexOfAnyBut(CharSequence, CharSequence)
2907     */
2908    public static int indexOfAnyBut(final CharSequence seq, final CharSequence searchChars) {
2909        if (isEmpty(seq) || isEmpty(searchChars)) {
2910            return INDEX_NOT_FOUND;
2911        }
2912        final int strLen = seq.length();
2913        for (int i = 0; i < strLen; i++) {
2914            final char ch = seq.charAt(i);
2915            final boolean chFound = CharSequenceUtils.indexOf(searchChars, ch, 0) >= 0;
2916            if (i + 1 < strLen && Character.isHighSurrogate(ch)) {
2917                final char ch2 = seq.charAt(i + 1);
2918                if (chFound && CharSequenceUtils.indexOf(searchChars, ch2, 0) < 0) {
2919                    return i;
2920                }
2921            } else {
2922                if (!chFound) {
2923                    return i;
2924                }
2925            }
2926        }
2927        return INDEX_NOT_FOUND;
2928    }
2929
2930    /**
2931     * <p>Compares all CharSequences in an array and returns the index at which the
2932     * CharSequences begin to differ.</p>
2933     *
2934     * <p>For example,
2935     * {@code indexOfDifference(new String[] {"i am a machine", "i am a robot"}) -> 7}</p>
2936     *
2937     * <pre>
2938     * StringUtils.indexOfDifference(null) = -1
2939     * StringUtils.indexOfDifference(new String[] {}) = -1
2940     * StringUtils.indexOfDifference(new String[] {"abc"}) = -1
2941     * StringUtils.indexOfDifference(new String[] {null, null}) = -1
2942     * StringUtils.indexOfDifference(new String[] {"", ""}) = -1
2943     * StringUtils.indexOfDifference(new String[] {"", null}) = 0
2944     * StringUtils.indexOfDifference(new String[] {"abc", null, null}) = 0
2945     * StringUtils.indexOfDifference(new String[] {null, null, "abc"}) = 0
2946     * StringUtils.indexOfDifference(new String[] {"", "abc"}) = 0
2947     * StringUtils.indexOfDifference(new String[] {"abc", ""}) = 0
2948     * StringUtils.indexOfDifference(new String[] {"abc", "abc"}) = -1
2949     * StringUtils.indexOfDifference(new String[] {"abc", "a"}) = 1
2950     * StringUtils.indexOfDifference(new String[] {"ab", "abxyz"}) = 2
2951     * StringUtils.indexOfDifference(new String[] {"abcde", "abxyz"}) = 2
2952     * StringUtils.indexOfDifference(new String[] {"abcde", "xyz"}) = 0
2953     * StringUtils.indexOfDifference(new String[] {"xyz", "abcde"}) = 0
2954     * StringUtils.indexOfDifference(new String[] {"i am a machine", "i am a robot"}) = 7
2955     * </pre>
2956     *
2957     * @param css  array of CharSequences, entries may be null
2958     * @return the index where the strings begin to differ; -1 if they are all equal
2959     * @since 2.4
2960     * @since 3.0 Changed signature from indexOfDifference(String...) to indexOfDifference(CharSequence...)
2961     */
2962    public static int indexOfDifference(final CharSequence... css) {
2963        if (ArrayUtils.getLength(css) <= 1) {
2964            return INDEX_NOT_FOUND;
2965        }
2966        boolean anyStringNull = false;
2967        boolean allStringsNull = true;
2968        final int arrayLen = css.length;
2969        int shortestStrLen = Integer.MAX_VALUE;
2970        int longestStrLen = 0;
2971
2972        // find the min and max string lengths; this avoids checking to make
2973        // sure we are not exceeding the length of the string each time through
2974        // the bottom loop.
2975        for (final CharSequence cs : css) {
2976            if (cs == null) {
2977                anyStringNull = true;
2978                shortestStrLen = 0;
2979            } else {
2980                allStringsNull = false;
2981                shortestStrLen = Math.min(cs.length(), shortestStrLen);
2982                longestStrLen = Math.max(cs.length(), longestStrLen);
2983            }
2984        }
2985
2986        // handle lists containing all nulls or all empty strings
2987        if (allStringsNull || longestStrLen == 0 && !anyStringNull) {
2988            return INDEX_NOT_FOUND;
2989        }
2990
2991        // handle lists containing some nulls or some empty strings
2992        if (shortestStrLen == 0) {
2993            return 0;
2994        }
2995
2996        // find the position with the first difference across all strings
2997        int firstDiff = -1;
2998        for (int stringPos = 0; stringPos < shortestStrLen; stringPos++) {
2999            final char comparisonChar = css[0].charAt(stringPos);
3000            for (int arrayPos = 1; arrayPos < arrayLen; arrayPos++) {
3001                if (css[arrayPos].charAt(stringPos) != comparisonChar) {
3002                    firstDiff = stringPos;
3003                    break;
3004                }
3005            }
3006            if (firstDiff != -1) {
3007                break;
3008            }
3009        }
3010
3011        if (firstDiff == -1 && shortestStrLen != longestStrLen) {
3012            // we compared all of the characters up to the length of the
3013            // shortest string and didn't find a match, but the string lengths
3014            // vary, so return the length of the shortest string.
3015            return shortestStrLen;
3016        }
3017        return firstDiff;
3018    }
3019
3020    /**
3021     * <p>Compares two CharSequences, and returns the index at which the
3022     * CharSequences begin to differ.</p>
3023     *
3024     * <p>For example,
3025     * {@code indexOfDifference("i am a machine", "i am a robot") -> 7}</p>
3026     *
3027     * <pre>
3028     * StringUtils.indexOfDifference(null, null) = -1
3029     * StringUtils.indexOfDifference("", "") = -1
3030     * StringUtils.indexOfDifference("", "abc") = 0
3031     * StringUtils.indexOfDifference("abc", "") = 0
3032     * StringUtils.indexOfDifference("abc", "abc") = -1
3033     * StringUtils.indexOfDifference("ab", "abxyz") = 2
3034     * StringUtils.indexOfDifference("abcde", "abxyz") = 2
3035     * StringUtils.indexOfDifference("abcde", "xyz") = 0
3036     * </pre>
3037     *
3038     * @param cs1  the first CharSequence, may be null
3039     * @param cs2  the second CharSequence, may be null
3040     * @return the index where cs1 and cs2 begin to differ; -1 if they are equal
3041     * @since 2.0
3042     * @since 3.0 Changed signature from indexOfDifference(String, String) to
3043     * indexOfDifference(CharSequence, CharSequence)
3044     */
3045    public static int indexOfDifference(final CharSequence cs1, final CharSequence cs2) {
3046        if (cs1 == cs2) {
3047            return INDEX_NOT_FOUND;
3048        }
3049        if (cs1 == null || cs2 == null) {
3050            return 0;
3051        }
3052        int i;
3053        for (i = 0; i < cs1.length() && i < cs2.length(); ++i) {
3054            if (cs1.charAt(i) != cs2.charAt(i)) {
3055                break;
3056            }
3057        }
3058        if (i < cs2.length() || i < cs1.length()) {
3059            return i;
3060        }
3061        return INDEX_NOT_FOUND;
3062    }
3063
3064    /**
3065     * <p>Case in-sensitive find of the first index within a CharSequence.</p>
3066     *
3067     * <p>A {@code null} CharSequence will return {@code -1}.
3068     * A negative start position is treated as zero.
3069     * An empty ("") search CharSequence always matches.
3070     * A start position greater than the string length only matches
3071     * an empty search CharSequence.</p>
3072     *
3073     * <pre>
3074     * StringUtils.indexOfIgnoreCase(null, *)          = -1
3075     * StringUtils.indexOfIgnoreCase(*, null)          = -1
3076     * StringUtils.indexOfIgnoreCase("", "")           = 0
3077     * StringUtils.indexOfIgnoreCase("aabaabaa", "a")  = 0
3078     * StringUtils.indexOfIgnoreCase("aabaabaa", "b")  = 2
3079     * StringUtils.indexOfIgnoreCase("aabaabaa", "ab") = 1
3080     * </pre>
3081     *
3082     * @param str  the CharSequence to check, may be null
3083     * @param searchStr  the CharSequence to find, may be null
3084     * @return the first index of the search CharSequence,
3085     *  -1 if no match or {@code null} string input
3086     * @since 2.5
3087     * @since 3.0 Changed signature from indexOfIgnoreCase(String, String) to indexOfIgnoreCase(CharSequence, CharSequence)
3088     */
3089    public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr) {
3090        return indexOfIgnoreCase(str, searchStr, 0);
3091    }
3092
3093    /**
3094     * <p>Case in-sensitive find of the first index within a CharSequence
3095     * from the specified position.</p>
3096     *
3097     * <p>A {@code null} CharSequence will return {@code -1}.
3098     * A negative start position is treated as zero.
3099     * An empty ("") search CharSequence always matches.
3100     * A start position greater than the string length only matches
3101     * an empty search CharSequence.</p>
3102     *
3103     * <pre>
3104     * StringUtils.indexOfIgnoreCase(null, *, *)          = -1
3105     * StringUtils.indexOfIgnoreCase(*, null, *)          = -1
3106     * StringUtils.indexOfIgnoreCase("", "", 0)           = 0
3107     * StringUtils.indexOfIgnoreCase("aabaabaa", "A", 0)  = 0
3108     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 0)  = 2
3109     * StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 0) = 1
3110     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 3)  = 5
3111     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 9)  = -1
3112     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", -1) = 2
3113     * StringUtils.indexOfIgnoreCase("aabaabaa", "", 2)   = 2
3114     * StringUtils.indexOfIgnoreCase("abc", "", 9)        = -1
3115     * </pre>
3116     *
3117     * @param str  the CharSequence to check, may be null
3118     * @param searchStr  the CharSequence to find, may be null
3119     * @param startPos  the start position, negative treated as zero
3120     * @return the first index of the search CharSequence (always &ge; startPos),
3121     *  -1 if no match or {@code null} string input
3122     * @since 2.5
3123     * @since 3.0 Changed signature from indexOfIgnoreCase(String, String, int) to indexOfIgnoreCase(CharSequence, CharSequence, int)
3124     */
3125    public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr, int startPos) {
3126        if (str == null || searchStr == null) {
3127            return INDEX_NOT_FOUND;
3128        }
3129        if (startPos < 0) {
3130            startPos = 0;
3131        }
3132        final int endLimit = str.length() - searchStr.length() + 1;
3133        if (startPos > endLimit) {
3134            return INDEX_NOT_FOUND;
3135        }
3136        if (searchStr.length() == 0) {
3137            return startPos;
3138        }
3139        for (int i = startPos; i < endLimit; i++) {
3140            if (CharSequenceUtils.regionMatches(str, true, i, searchStr, 0, searchStr.length())) {
3141                return i;
3142            }
3143        }
3144        return INDEX_NOT_FOUND;
3145    }
3146
3147    /**
3148     * <p>Checks if all of the CharSequences are empty (""), null or whitespace only.</p>
3149     *
3150     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
3151     *
3152     * <pre>
3153     * StringUtils.isAllBlank(null)             = true
3154     * StringUtils.isAllBlank(null, "foo")      = false
3155     * StringUtils.isAllBlank(null, null)       = true
3156     * StringUtils.isAllBlank("", "bar")        = false
3157     * StringUtils.isAllBlank("bob", "")        = false
3158     * StringUtils.isAllBlank("  bob  ", null)  = false
3159     * StringUtils.isAllBlank(" ", "bar")       = false
3160     * StringUtils.isAllBlank("foo", "bar")     = false
3161     * StringUtils.isAllBlank(new String[] {})  = true
3162     * </pre>
3163     *
3164     * @param css  the CharSequences to check, may be null or empty
3165     * @return {@code true} if all of the CharSequences are empty or null or whitespace only
3166     * @since 3.6
3167     */
3168    public static boolean isAllBlank(final CharSequence... css) {
3169        if (ArrayUtils.isEmpty(css)) {
3170            return true;
3171        }
3172        for (final CharSequence cs : css) {
3173            if (isNotBlank(cs)) {
3174               return false;
3175            }
3176        }
3177        return true;
3178    }
3179
3180    /**
3181     * <p>Checks if all of the CharSequences are empty ("") or null.</p>
3182     *
3183     * <pre>
3184     * StringUtils.isAllEmpty(null)             = true
3185     * StringUtils.isAllEmpty(null, "")         = true
3186     * StringUtils.isAllEmpty(new String[] {})  = true
3187     * StringUtils.isAllEmpty(null, "foo")      = false
3188     * StringUtils.isAllEmpty("", "bar")        = false
3189     * StringUtils.isAllEmpty("bob", "")        = false
3190     * StringUtils.isAllEmpty("  bob  ", null)  = false
3191     * StringUtils.isAllEmpty(" ", "bar")       = false
3192     * StringUtils.isAllEmpty("foo", "bar")     = false
3193     * </pre>
3194     *
3195     * @param css  the CharSequences to check, may be null or empty
3196     * @return {@code true} if all of the CharSequences are empty or null
3197     * @since 3.6
3198     */
3199    public static boolean isAllEmpty(final CharSequence... css) {
3200        if (ArrayUtils.isEmpty(css)) {
3201            return true;
3202        }
3203        for (final CharSequence cs : css) {
3204            if (isNotEmpty(cs)) {
3205                return false;
3206            }
3207        }
3208        return true;
3209    }
3210
3211    /**
3212     * <p>Checks if the CharSequence contains only lowercase characters.</p>
3213     *
3214     * <p>{@code null} will return {@code false}.
3215     * An empty CharSequence (length()=0) will return {@code false}.</p>
3216     *
3217     * <pre>
3218     * StringUtils.isAllLowerCase(null)   = false
3219     * StringUtils.isAllLowerCase("")     = false
3220     * StringUtils.isAllLowerCase("  ")   = false
3221     * StringUtils.isAllLowerCase("abc")  = true
3222     * StringUtils.isAllLowerCase("abC")  = false
3223     * StringUtils.isAllLowerCase("ab c") = false
3224     * StringUtils.isAllLowerCase("ab1c") = false
3225     * StringUtils.isAllLowerCase("ab/c") = false
3226     * </pre>
3227     *
3228     * @param cs  the CharSequence to check, may be null
3229     * @return {@code true} if only contains lowercase characters, and is non-null
3230     * @since 2.5
3231     * @since 3.0 Changed signature from isAllLowerCase(String) to isAllLowerCase(CharSequence)
3232     */
3233    public static boolean isAllLowerCase(final CharSequence cs) {
3234        if (isEmpty(cs)) {
3235            return false;
3236        }
3237        final int sz = cs.length();
3238        for (int i = 0; i < sz; i++) {
3239            if (!Character.isLowerCase(cs.charAt(i))) {
3240                return false;
3241            }
3242        }
3243        return true;
3244    }
3245
3246    /**
3247     * <p>Checks if the CharSequence contains only uppercase characters.</p>
3248     *
3249     * <p>{@code null} will return {@code false}.
3250     * An empty String (length()=0) will return {@code false}.</p>
3251     *
3252     * <pre>
3253     * StringUtils.isAllUpperCase(null)   = false
3254     * StringUtils.isAllUpperCase("")     = false
3255     * StringUtils.isAllUpperCase("  ")   = false
3256     * StringUtils.isAllUpperCase("ABC")  = true
3257     * StringUtils.isAllUpperCase("aBC")  = false
3258     * StringUtils.isAllUpperCase("A C")  = false
3259     * StringUtils.isAllUpperCase("A1C")  = false
3260     * StringUtils.isAllUpperCase("A/C")  = false
3261     * </pre>
3262     *
3263     * @param cs the CharSequence to check, may be null
3264     * @return {@code true} if only contains uppercase characters, and is non-null
3265     * @since 2.5
3266     * @since 3.0 Changed signature from isAllUpperCase(String) to isAllUpperCase(CharSequence)
3267     */
3268    public static boolean isAllUpperCase(final CharSequence cs) {
3269        if (isEmpty(cs)) {
3270            return false;
3271        }
3272        final int sz = cs.length();
3273        for (int i = 0; i < sz; i++) {
3274            if (!Character.isUpperCase(cs.charAt(i))) {
3275                return false;
3276            }
3277        }
3278        return true;
3279    }
3280
3281    // Character Tests
3282    //-----------------------------------------------------------------------
3283    /**
3284     * <p>Checks if the CharSequence contains only Unicode letters.</p>
3285     *
3286     * <p>{@code null} will return {@code false}.
3287     * An empty CharSequence (length()=0) will return {@code false}.</p>
3288     *
3289     * <pre>
3290     * StringUtils.isAlpha(null)   = false
3291     * StringUtils.isAlpha("")     = false
3292     * StringUtils.isAlpha("  ")   = false
3293     * StringUtils.isAlpha("abc")  = true
3294     * StringUtils.isAlpha("ab2c") = false
3295     * StringUtils.isAlpha("ab-c") = false
3296     * </pre>
3297     *
3298     * @param cs  the CharSequence to check, may be null
3299     * @return {@code true} if only contains letters, and is non-null
3300     * @since 3.0 Changed signature from isAlpha(String) to isAlpha(CharSequence)
3301     * @since 3.0 Changed "" to return false and not true
3302     */
3303    public static boolean isAlpha(final CharSequence cs) {
3304        if (isEmpty(cs)) {
3305            return false;
3306        }
3307        final int sz = cs.length();
3308        for (int i = 0; i < sz; i++) {
3309            if (!Character.isLetter(cs.charAt(i))) {
3310                return false;
3311            }
3312        }
3313        return true;
3314    }
3315
3316    /**
3317     * <p>Checks if the CharSequence contains only Unicode letters or digits.</p>
3318     *
3319     * <p>{@code null} will return {@code false}.
3320     * An empty CharSequence (length()=0) will return {@code false}.</p>
3321     *
3322     * <pre>
3323     * StringUtils.isAlphanumeric(null)   = false
3324     * StringUtils.isAlphanumeric("")     = false
3325     * StringUtils.isAlphanumeric("  ")   = false
3326     * StringUtils.isAlphanumeric("abc")  = true
3327     * StringUtils.isAlphanumeric("ab c") = false
3328     * StringUtils.isAlphanumeric("ab2c") = true
3329     * StringUtils.isAlphanumeric("ab-c") = false
3330     * </pre>
3331     *
3332     * @param cs  the CharSequence to check, may be null
3333     * @return {@code true} if only contains letters or digits,
3334     *  and is non-null
3335     * @since 3.0 Changed signature from isAlphanumeric(String) to isAlphanumeric(CharSequence)
3336     * @since 3.0 Changed "" to return false and not true
3337     */
3338    public static boolean isAlphanumeric(final CharSequence cs) {
3339        if (isEmpty(cs)) {
3340            return false;
3341        }
3342        final int sz = cs.length();
3343        for (int i = 0; i < sz; i++) {
3344            if (!Character.isLetterOrDigit(cs.charAt(i))) {
3345                return false;
3346            }
3347        }
3348        return true;
3349    }
3350
3351    /**
3352     * <p>Checks if the CharSequence contains only Unicode letters, digits
3353     * or space ({@code ' '}).</p>
3354     *
3355     * <p>{@code null} will return {@code false}.
3356     * An empty CharSequence (length()=0) will return {@code true}.</p>
3357     *
3358     * <pre>
3359     * StringUtils.isAlphanumericSpace(null)   = false
3360     * StringUtils.isAlphanumericSpace("")     = true
3361     * StringUtils.isAlphanumericSpace("  ")   = true
3362     * StringUtils.isAlphanumericSpace("abc")  = true
3363     * StringUtils.isAlphanumericSpace("ab c") = true
3364     * StringUtils.isAlphanumericSpace("ab2c") = true
3365     * StringUtils.isAlphanumericSpace("ab-c") = false
3366     * </pre>
3367     *
3368     * @param cs  the CharSequence to check, may be null
3369     * @return {@code true} if only contains letters, digits or space,
3370     *  and is non-null
3371     * @since 3.0 Changed signature from isAlphanumericSpace(String) to isAlphanumericSpace(CharSequence)
3372     */
3373    public static boolean isAlphanumericSpace(final CharSequence cs) {
3374        if (cs == null) {
3375            return false;
3376        }
3377        final int sz = cs.length();
3378        for (int i = 0; i < sz; i++) {
3379            if (!Character.isLetterOrDigit(cs.charAt(i)) && cs.charAt(i) != ' ') {
3380                return false;
3381            }
3382        }
3383        return true;
3384    }
3385
3386    /**
3387     * <p>Checks if the CharSequence contains only Unicode letters and
3388     * space (' ').</p>
3389     *
3390     * <p>{@code null} will return {@code false}
3391     * An empty CharSequence (length()=0) will return {@code true}.</p>
3392     *
3393     * <pre>
3394     * StringUtils.isAlphaSpace(null)   = false
3395     * StringUtils.isAlphaSpace("")     = true
3396     * StringUtils.isAlphaSpace("  ")   = true
3397     * StringUtils.isAlphaSpace("abc")  = true
3398     * StringUtils.isAlphaSpace("ab c") = true
3399     * StringUtils.isAlphaSpace("ab2c") = false
3400     * StringUtils.isAlphaSpace("ab-c") = false
3401     * </pre>
3402     *
3403     * @param cs  the CharSequence to check, may be null
3404     * @return {@code true} if only contains letters and space,
3405     *  and is non-null
3406     * @since 3.0 Changed signature from isAlphaSpace(String) to isAlphaSpace(CharSequence)
3407     */
3408    public static boolean isAlphaSpace(final CharSequence cs) {
3409        if (cs == null) {
3410            return false;
3411        }
3412        final int sz = cs.length();
3413        for (int i = 0; i < sz; i++) {
3414            if (!Character.isLetter(cs.charAt(i)) && cs.charAt(i) != ' ') {
3415                return false;
3416            }
3417        }
3418        return true;
3419    }
3420
3421    /**
3422     * <p>Checks if any of the CharSequences are empty ("") or null or whitespace only.</p>
3423     *
3424     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
3425     *
3426     * <pre>
3427     * StringUtils.isAnyBlank((String) null)    = true
3428     * StringUtils.isAnyBlank((String[]) null)  = false
3429     * StringUtils.isAnyBlank(null, "foo")      = true
3430     * StringUtils.isAnyBlank(null, null)       = true
3431     * StringUtils.isAnyBlank("", "bar")        = true
3432     * StringUtils.isAnyBlank("bob", "")        = true
3433     * StringUtils.isAnyBlank("  bob  ", null)  = true
3434     * StringUtils.isAnyBlank(" ", "bar")       = true
3435     * StringUtils.isAnyBlank(new String[] {})  = false
3436     * StringUtils.isAnyBlank(new String[]{""}) = true
3437     * StringUtils.isAnyBlank("foo", "bar")     = false
3438     * </pre>
3439     *
3440     * @param css  the CharSequences to check, may be null or empty
3441     * @return {@code true} if any of the CharSequences are empty or null or whitespace only
3442     * @since 3.2
3443     */
3444    public static boolean isAnyBlank(final CharSequence... css) {
3445      if (ArrayUtils.isEmpty(css)) {
3446        return false;
3447      }
3448      for (final CharSequence cs : css) {
3449        if (isBlank(cs)) {
3450          return true;
3451        }
3452      }
3453      return false;
3454    }
3455
3456    /**
3457     * <p>Checks if any of the CharSequences are empty ("") or null.</p>
3458     *
3459     * <pre>
3460     * StringUtils.isAnyEmpty((String) null)    = true
3461     * StringUtils.isAnyEmpty((String[]) null)  = false
3462     * StringUtils.isAnyEmpty(null, "foo")      = true
3463     * StringUtils.isAnyEmpty("", "bar")        = true
3464     * StringUtils.isAnyEmpty("bob", "")        = true
3465     * StringUtils.isAnyEmpty("  bob  ", null)  = true
3466     * StringUtils.isAnyEmpty(" ", "bar")       = false
3467     * StringUtils.isAnyEmpty("foo", "bar")     = false
3468     * StringUtils.isAnyEmpty(new String[]{})   = false
3469     * StringUtils.isAnyEmpty(new String[]{""}) = true
3470     * </pre>
3471     *
3472     * @param css  the CharSequences to check, may be null or empty
3473     * @return {@code true} if any of the CharSequences are empty or null
3474     * @since 3.2
3475     */
3476    public static boolean isAnyEmpty(final CharSequence... css) {
3477      if (ArrayUtils.isEmpty(css)) {
3478        return false;
3479      }
3480      for (final CharSequence cs : css) {
3481        if (isEmpty(cs)) {
3482          return true;
3483        }
3484      }
3485      return false;
3486    }
3487
3488    /**
3489     * <p>Checks if the CharSequence contains only ASCII printable characters.</p>
3490     *
3491     * <p>{@code null} will return {@code false}.
3492     * An empty CharSequence (length()=0) will return {@code true}.</p>
3493     *
3494     * <pre>
3495     * StringUtils.isAsciiPrintable(null)     = false
3496     * StringUtils.isAsciiPrintable("")       = true
3497     * StringUtils.isAsciiPrintable(" ")      = true
3498     * StringUtils.isAsciiPrintable("Ceki")   = true
3499     * StringUtils.isAsciiPrintable("ab2c")   = true
3500     * StringUtils.isAsciiPrintable("!ab-c~") = true
3501     * StringUtils.isAsciiPrintable("\u0020") = true
3502     * StringUtils.isAsciiPrintable("\u0021") = true
3503     * StringUtils.isAsciiPrintable("\u007e") = true
3504     * StringUtils.isAsciiPrintable("\u007f") = false
3505     * StringUtils.isAsciiPrintable("Ceki G\u00fclc\u00fc") = false
3506     * </pre>
3507     *
3508     * @param cs the CharSequence to check, may be null
3509     * @return {@code true} if every character is in the range
3510     *  32 thru 126
3511     * @since 2.1
3512     * @since 3.0 Changed signature from isAsciiPrintable(String) to isAsciiPrintable(CharSequence)
3513     */
3514    public static boolean isAsciiPrintable(final CharSequence cs) {
3515        if (cs == null) {
3516            return false;
3517        }
3518        final int sz = cs.length();
3519        for (int i = 0; i < sz; i++) {
3520            if (!CharUtils.isAsciiPrintable(cs.charAt(i))) {
3521                return false;
3522            }
3523        }
3524        return true;
3525    }
3526
3527    // Nested extraction
3528    //-----------------------------------------------------------------------
3529
3530    /**
3531     * <p>Checks if a CharSequence is empty (""), null or whitespace only.</p>
3532     *
3533     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
3534     *
3535     * <pre>
3536     * StringUtils.isBlank(null)      = true
3537     * StringUtils.isBlank("")        = true
3538     * StringUtils.isBlank(" ")       = true
3539     * StringUtils.isBlank("bob")     = false
3540     * StringUtils.isBlank("  bob  ") = false
3541     * </pre>
3542     *
3543     * @param cs  the CharSequence to check, may be null
3544     * @return {@code true} if the CharSequence is null, empty or whitespace only
3545     * @since 2.0
3546     * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
3547     */
3548    public static boolean isBlank(final CharSequence cs) {
3549        final int strLen = length(cs);
3550        if (strLen == 0) {
3551            return true;
3552        }
3553        for (int i = 0; i < strLen; i++) {
3554            if (!Character.isWhitespace(cs.charAt(i))) {
3555                return false;
3556            }
3557        }
3558        return true;
3559    }
3560
3561    // Empty checks
3562    //-----------------------------------------------------------------------
3563    /**
3564     * <p>Checks if a CharSequence is empty ("") or null.</p>
3565     *
3566     * <pre>
3567     * StringUtils.isEmpty(null)      = true
3568     * StringUtils.isEmpty("")        = true
3569     * StringUtils.isEmpty(" ")       = false
3570     * StringUtils.isEmpty("bob")     = false
3571     * StringUtils.isEmpty("  bob  ") = false
3572     * </pre>
3573     *
3574     * <p>NOTE: This method changed in Lang version 2.0.
3575     * It no longer trims the CharSequence.
3576     * That functionality is available in isBlank().</p>
3577     *
3578     * @param cs  the CharSequence to check, may be null
3579     * @return {@code true} if the CharSequence is empty or null
3580     * @since 3.0 Changed signature from isEmpty(String) to isEmpty(CharSequence)
3581     */
3582    public static boolean isEmpty(final CharSequence cs) {
3583        return cs == null || cs.length() == 0;
3584    }
3585
3586    /**
3587     * <p>Checks if the CharSequence contains mixed casing of both uppercase and lowercase characters.</p>
3588     *
3589     * <p>{@code null} will return {@code false}. An empty CharSequence ({@code length()=0}) will return
3590     * {@code false}.</p>
3591     *
3592     * <pre>
3593     * StringUtils.isMixedCase(null)    = false
3594     * StringUtils.isMixedCase("")      = false
3595     * StringUtils.isMixedCase("ABC")   = false
3596     * StringUtils.isMixedCase("abc")   = false
3597     * StringUtils.isMixedCase("aBc")   = true
3598     * StringUtils.isMixedCase("A c")   = true
3599     * StringUtils.isMixedCase("A1c")   = true
3600     * StringUtils.isMixedCase("a/C")   = true
3601     * StringUtils.isMixedCase("aC\t")  = true
3602     * </pre>
3603     *
3604     * @param cs the CharSequence to check, may be null
3605     * @return {@code true} if the CharSequence contains both uppercase and lowercase characters
3606     * @since 3.5
3607     */
3608    public static boolean isMixedCase(final CharSequence cs) {
3609        if (isEmpty(cs) || cs.length() == 1) {
3610            return false;
3611        }
3612        boolean containsUppercase = false;
3613        boolean containsLowercase = false;
3614        final int sz = cs.length();
3615        for (int i = 0; i < sz; i++) {
3616            if (containsUppercase && containsLowercase) {
3617                return true;
3618            } else if (Character.isUpperCase(cs.charAt(i))) {
3619                containsUppercase = true;
3620            } else if (Character.isLowerCase(cs.charAt(i))) {
3621                containsLowercase = true;
3622            }
3623        }
3624        return containsUppercase && containsLowercase;
3625    }
3626
3627    /**
3628     * <p>Checks if none of the CharSequences are empty (""), null or whitespace only.</p>
3629     *
3630     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
3631     *
3632     * <pre>
3633     * StringUtils.isNoneBlank((String) null)    = false
3634     * StringUtils.isNoneBlank((String[]) null)  = true
3635     * StringUtils.isNoneBlank(null, "foo")      = false
3636     * StringUtils.isNoneBlank(null, null)       = false
3637     * StringUtils.isNoneBlank("", "bar")        = false
3638     * StringUtils.isNoneBlank("bob", "")        = false
3639     * StringUtils.isNoneBlank("  bob  ", null)  = false
3640     * StringUtils.isNoneBlank(" ", "bar")       = false
3641     * StringUtils.isNoneBlank(new String[] {})  = true
3642     * StringUtils.isNoneBlank(new String[]{""}) = false
3643     * StringUtils.isNoneBlank("foo", "bar")     = true
3644     * </pre>
3645     *
3646     * @param css  the CharSequences to check, may be null or empty
3647     * @return {@code true} if none of the CharSequences are empty or null or whitespace only
3648     * @since 3.2
3649     */
3650    public static boolean isNoneBlank(final CharSequence... css) {
3651      return !isAnyBlank(css);
3652    }
3653
3654    /**
3655     * <p>Checks if none of the CharSequences are empty ("") or null.</p>
3656     *
3657     * <pre>
3658     * StringUtils.isNoneEmpty((String) null)    = false
3659     * StringUtils.isNoneEmpty((String[]) null)  = true
3660     * StringUtils.isNoneEmpty(null, "foo")      = false
3661     * StringUtils.isNoneEmpty("", "bar")        = false
3662     * StringUtils.isNoneEmpty("bob", "")        = false
3663     * StringUtils.isNoneEmpty("  bob  ", null)  = false
3664     * StringUtils.isNoneEmpty(new String[] {})  = true
3665     * StringUtils.isNoneEmpty(new String[]{""}) = false
3666     * StringUtils.isNoneEmpty(" ", "bar")       = true
3667     * StringUtils.isNoneEmpty("foo", "bar")     = true
3668     * </pre>
3669     *
3670     * @param css  the CharSequences to check, may be null or empty
3671     * @return {@code true} if none of the CharSequences are empty or null
3672     * @since 3.2
3673     */
3674    public static boolean isNoneEmpty(final CharSequence... css) {
3675      return !isAnyEmpty(css);
3676    }
3677
3678    /**
3679     * <p>Checks if a CharSequence is not empty (""), not null and not whitespace only.</p>
3680     *
3681     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
3682     *
3683     * <pre>
3684     * StringUtils.isNotBlank(null)      = false
3685     * StringUtils.isNotBlank("")        = false
3686     * StringUtils.isNotBlank(" ")       = false
3687     * StringUtils.isNotBlank("bob")     = true
3688     * StringUtils.isNotBlank("  bob  ") = true
3689     * </pre>
3690     *
3691     * @param cs  the CharSequence to check, may be null
3692     * @return {@code true} if the CharSequence is
3693     *  not empty and not null and not whitespace only
3694     * @since 2.0
3695     * @since 3.0 Changed signature from isNotBlank(String) to isNotBlank(CharSequence)
3696     */
3697    public static boolean isNotBlank(final CharSequence cs) {
3698        return !isBlank(cs);
3699    }
3700
3701    /**
3702     * <p>Checks if a CharSequence is not empty ("") and not null.</p>
3703     *
3704     * <pre>
3705     * StringUtils.isNotEmpty(null)      = false
3706     * StringUtils.isNotEmpty("")        = false
3707     * StringUtils.isNotEmpty(" ")       = true
3708     * StringUtils.isNotEmpty("bob")     = true
3709     * StringUtils.isNotEmpty("  bob  ") = true
3710     * </pre>
3711     *
3712     * @param cs  the CharSequence to check, may be null
3713     * @return {@code true} if the CharSequence is not empty and not null
3714     * @since 3.0 Changed signature from isNotEmpty(String) to isNotEmpty(CharSequence)
3715     */
3716    public static boolean isNotEmpty(final CharSequence cs) {
3717        return !isEmpty(cs);
3718    }
3719
3720    /**
3721     * <p>Checks if the CharSequence contains only Unicode digits.
3722     * A decimal point is not a Unicode digit and returns false.</p>
3723     *
3724     * <p>{@code null} will return {@code false}.
3725     * An empty CharSequence (length()=0) will return {@code false}.</p>
3726     *
3727     * <p>Note that the method does not allow for a leading sign, either positive or negative.
3728     * Also, if a String passes the numeric test, it may still generate a NumberFormatException
3729     * when parsed by Integer.parseInt or Long.parseLong, e.g. if the value is outside the range
3730     * for int or long respectively.</p>
3731     *
3732     * <pre>
3733     * StringUtils.isNumeric(null)   = false
3734     * StringUtils.isNumeric("")     = false
3735     * StringUtils.isNumeric("  ")   = false
3736     * StringUtils.isNumeric("123")  = true
3737     * StringUtils.isNumeric("\u0967\u0968\u0969")  = true
3738     * StringUtils.isNumeric("12 3") = false
3739     * StringUtils.isNumeric("ab2c") = false
3740     * StringUtils.isNumeric("12-3") = false
3741     * StringUtils.isNumeric("12.3") = false
3742     * StringUtils.isNumeric("-123") = false
3743     * StringUtils.isNumeric("+123") = false
3744     * </pre>
3745     *
3746     * @param cs  the CharSequence to check, may be null
3747     * @return {@code true} if only contains digits, and is non-null
3748     * @since 3.0 Changed signature from isNumeric(String) to isNumeric(CharSequence)
3749     * @since 3.0 Changed "" to return false and not true
3750     */
3751    public static boolean isNumeric(final CharSequence cs) {
3752        if (isEmpty(cs)) {
3753            return false;
3754        }
3755        final int sz = cs.length();
3756        for (int i = 0; i < sz; i++) {
3757            if (!Character.isDigit(cs.charAt(i))) {
3758                return false;
3759            }
3760        }
3761        return true;
3762    }
3763
3764    /**
3765     * <p>Checks if the CharSequence contains only Unicode digits or space
3766     * ({@code ' '}).
3767     * A decimal point is not a Unicode digit and returns false.</p>
3768     *
3769     * <p>{@code null} will return {@code false}.
3770     * An empty CharSequence (length()=0) will return {@code true}.</p>
3771     *
3772     * <pre>
3773     * StringUtils.isNumericSpace(null)   = false
3774     * StringUtils.isNumericSpace("")     = true
3775     * StringUtils.isNumericSpace("  ")   = true
3776     * StringUtils.isNumericSpace("123")  = true
3777     * StringUtils.isNumericSpace("12 3") = true
3778     * StringUtils.isNumeric("\u0967\u0968\u0969")  = true
3779     * StringUtils.isNumeric("\u0967\u0968 \u0969")  = true
3780     * StringUtils.isNumericSpace("ab2c") = false
3781     * StringUtils.isNumericSpace("12-3") = false
3782     * StringUtils.isNumericSpace("12.3") = false
3783     * </pre>
3784     *
3785     * @param cs  the CharSequence to check, may be null
3786     * @return {@code true} if only contains digits or space,
3787     *  and is non-null
3788     * @since 3.0 Changed signature from isNumericSpace(String) to isNumericSpace(CharSequence)
3789     */
3790    public static boolean isNumericSpace(final CharSequence cs) {
3791        if (cs == null) {
3792            return false;
3793        }
3794        final int sz = cs.length();
3795        for (int i = 0; i < sz; i++) {
3796            if (!Character.isDigit(cs.charAt(i)) && cs.charAt(i) != ' ') {
3797                return false;
3798            }
3799        }
3800        return true;
3801    }
3802
3803    /**
3804     * <p>Checks if the CharSequence contains only whitespace.</p>
3805     *
3806     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
3807     *
3808     * <p>{@code null} will return {@code false}.
3809     * An empty CharSequence (length()=0) will return {@code true}.</p>
3810     *
3811     * <pre>
3812     * StringUtils.isWhitespace(null)   = false
3813     * StringUtils.isWhitespace("")     = true
3814     * StringUtils.isWhitespace("  ")   = true
3815     * StringUtils.isWhitespace("abc")  = false
3816     * StringUtils.isWhitespace("ab2c") = false
3817     * StringUtils.isWhitespace("ab-c") = false
3818     * </pre>
3819     *
3820     * @param cs  the CharSequence to check, may be null
3821     * @return {@code true} if only contains whitespace, and is non-null
3822     * @since 2.0
3823     * @since 3.0 Changed signature from isWhitespace(String) to isWhitespace(CharSequence)
3824     */
3825    public static boolean isWhitespace(final CharSequence cs) {
3826        if (cs == null) {
3827            return false;
3828        }
3829        final int sz = cs.length();
3830        for (int i = 0; i < sz; i++) {
3831            if (!Character.isWhitespace(cs.charAt(i))) {
3832                return false;
3833            }
3834        }
3835        return true;
3836    }
3837
3838    /**
3839     * <p>
3840     * Joins the elements of the provided array into a single String containing the provided list of elements.
3841     * </p>
3842     *
3843     * <p>
3844     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
3845     * by empty strings.
3846     * </p>
3847     *
3848     * <pre>
3849     * StringUtils.join(null, *)               = null
3850     * StringUtils.join([], *)                 = ""
3851     * StringUtils.join([null], *)             = ""
3852     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
3853     * StringUtils.join([1, 2, 3], null) = "123"
3854     * </pre>
3855     *
3856     * @param array
3857     *            the array of values to join together, may be null
3858     * @param separator
3859     *            the separator character to use
3860     * @return the joined String, {@code null} if null array input
3861     * @since 3.2
3862     */
3863    public static String join(final byte[] array, final char separator) {
3864        if (array == null) {
3865            return null;
3866        }
3867        return join(array, separator, 0, array.length);
3868    }
3869
3870    /**
3871     * <p>
3872     * Joins the elements of the provided array into a single String containing the provided list of elements.
3873     * </p>
3874     *
3875     * <p>
3876     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
3877     * by empty strings.
3878     * </p>
3879     *
3880     * <pre>
3881     * StringUtils.join(null, *)               = null
3882     * StringUtils.join([], *)                 = ""
3883     * StringUtils.join([null], *)             = ""
3884     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
3885     * StringUtils.join([1, 2, 3], null) = "123"
3886     * </pre>
3887     *
3888     * @param array
3889     *            the array of values to join together, may be null
3890     * @param separator
3891     *            the separator character to use
3892     * @param startIndex
3893     *            the first index to start joining from. It is an error to pass in a start index past the end of the
3894     *            array
3895     * @param endIndex
3896     *            the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
3897     *            the array
3898     * @return the joined String, {@code null} if null array input
3899     * @since 3.2
3900     */
3901    public static String join(final byte[] array, final char separator, final int startIndex, final int endIndex) {
3902        if (array == null) {
3903            return null;
3904        }
3905        final int noOfItems = endIndex - startIndex;
3906        if (noOfItems <= 0) {
3907            return EMPTY;
3908        }
3909        final StringBuilder buf = newStringBuilder(noOfItems);
3910        buf.append(array[startIndex]);
3911        for (int i = startIndex + 1; i < endIndex; i++) {
3912            buf.append(separator);
3913            buf.append(array[i]);
3914        }
3915        return buf.toString();
3916    }
3917
3918    /**
3919     * <p>
3920     * Joins the elements of the provided array into a single String containing the provided list of elements.
3921     * </p>
3922     *
3923     * <p>
3924     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
3925     * by empty strings.
3926     * </p>
3927     *
3928     * <pre>
3929     * StringUtils.join(null, *)               = null
3930     * StringUtils.join([], *)                 = ""
3931     * StringUtils.join([null], *)             = ""
3932     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
3933     * StringUtils.join([1, 2, 3], null) = "123"
3934     * </pre>
3935     *
3936     * @param array
3937     *            the array of values to join together, may be null
3938     * @param separator
3939     *            the separator character to use
3940     * @return the joined String, {@code null} if null array input
3941     * @since 3.2
3942     */
3943    public static String join(final char[] array, final char separator) {
3944        if (array == null) {
3945            return null;
3946        }
3947        return join(array, separator, 0, array.length);
3948    }
3949
3950    /**
3951     * <p>
3952     * Joins the elements of the provided array into a single String containing the provided list of elements.
3953     * </p>
3954     *
3955     * <p>
3956     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
3957     * by empty strings.
3958     * </p>
3959     *
3960     * <pre>
3961     * StringUtils.join(null, *)               = null
3962     * StringUtils.join([], *)                 = ""
3963     * StringUtils.join([null], *)             = ""
3964     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
3965     * StringUtils.join([1, 2, 3], null) = "123"
3966     * </pre>
3967     *
3968     * @param array
3969     *            the array of values to join together, may be null
3970     * @param separator
3971     *            the separator character to use
3972     * @param startIndex
3973     *            the first index to start joining from. It is an error to pass in a start index past the end of the
3974     *            array
3975     * @param endIndex
3976     *            the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
3977     *            the array
3978     * @return the joined String, {@code null} if null array input
3979     * @since 3.2
3980     */
3981    public static String join(final char[] array, final char separator, final int startIndex, final int endIndex) {
3982        if (array == null) {
3983            return null;
3984        }
3985        final int noOfItems = endIndex - startIndex;
3986        if (noOfItems <= 0) {
3987            return EMPTY;
3988        }
3989        final StringBuilder buf = newStringBuilder(noOfItems);
3990        buf.append(array[startIndex]);
3991        for (int i = startIndex + 1; i < endIndex; i++) {
3992            buf.append(separator);
3993            buf.append(array[i]);
3994        }
3995        return buf.toString();
3996    }
3997
3998    /**
3999     * <p>
4000     * Joins the elements of the provided array into a single String containing the provided list of elements.
4001     * </p>
4002     *
4003     * <p>
4004     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
4005     * by empty strings.
4006     * </p>
4007     *
4008     * <pre>
4009     * StringUtils.join(null, *)               = null
4010     * StringUtils.join([], *)                 = ""
4011     * StringUtils.join([null], *)             = ""
4012     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
4013     * StringUtils.join([1, 2, 3], null) = "123"
4014     * </pre>
4015     *
4016     * @param array
4017     *            the array of values to join together, may be null
4018     * @param separator
4019     *            the separator character to use
4020     * @return the joined String, {@code null} if null array input
4021     * @since 3.2
4022     */
4023    public static String join(final double[] array, final char separator) {
4024        if (array == null) {
4025            return null;
4026        }
4027        return join(array, separator, 0, array.length);
4028    }
4029
4030    /**
4031     * <p>
4032     * Joins the elements of the provided array into a single String containing the provided list of elements.
4033     * </p>
4034     *
4035     * <p>
4036     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
4037     * by empty strings.
4038     * </p>
4039     *
4040     * <pre>
4041     * StringUtils.join(null, *)               = null
4042     * StringUtils.join([], *)                 = ""
4043     * StringUtils.join([null], *)             = ""
4044     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
4045     * StringUtils.join([1, 2, 3], null) = "123"
4046     * </pre>
4047     *
4048     * @param array
4049     *            the array of values to join together, may be null
4050     * @param separator
4051     *            the separator character to use
4052     * @param startIndex
4053     *            the first index to start joining from. It is an error to pass in a start index past the end of the
4054     *            array
4055     * @param endIndex
4056     *            the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
4057     *            the array
4058     * @return the joined String, {@code null} if null array input
4059     * @since 3.2
4060     */
4061    public static String join(final double[] array, final char separator, final int startIndex, final int endIndex) {
4062        if (array == null) {
4063            return null;
4064        }
4065        final int noOfItems = endIndex - startIndex;
4066        if (noOfItems <= 0) {
4067            return EMPTY;
4068        }
4069        final StringBuilder buf = newStringBuilder(noOfItems);
4070        buf.append(array[startIndex]);
4071        for (int i = startIndex + 1; i < endIndex; i++) {
4072            buf.append(separator);
4073            buf.append(array[i]);
4074        }
4075        return buf.toString();
4076    }
4077
4078    /**
4079     * <p>
4080     * Joins the elements of the provided array into a single String containing the provided list of elements.
4081     * </p>
4082     *
4083     * <p>
4084     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
4085     * by empty strings.
4086     * </p>
4087     *
4088     * <pre>
4089     * StringUtils.join(null, *)               = null
4090     * StringUtils.join([], *)                 = ""
4091     * StringUtils.join([null], *)             = ""
4092     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
4093     * StringUtils.join([1, 2, 3], null) = "123"
4094     * </pre>
4095     *
4096     * @param array
4097     *            the array of values to join together, may be null
4098     * @param separator
4099     *            the separator character to use
4100     * @return the joined String, {@code null} if null array input
4101     * @since 3.2
4102     */
4103    public static String join(final float[] array, final char separator) {
4104        if (array == null) {
4105            return null;
4106        }
4107        return join(array, separator, 0, array.length);
4108    }
4109
4110    /**
4111     * <p>
4112     * Joins the elements of the provided array into a single String containing the provided list of elements.
4113     * </p>
4114     *
4115     * <p>
4116     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
4117     * by empty strings.
4118     * </p>
4119     *
4120     * <pre>
4121     * StringUtils.join(null, *)               = null
4122     * StringUtils.join([], *)                 = ""
4123     * StringUtils.join([null], *)             = ""
4124     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
4125     * StringUtils.join([1, 2, 3], null) = "123"
4126     * </pre>
4127     *
4128     * @param array
4129     *            the array of values to join together, may be null
4130     * @param separator
4131     *            the separator character to use
4132     * @param startIndex
4133     *            the first index to start joining from. It is an error to pass in a start index past the end of the
4134     *            array
4135     * @param endIndex
4136     *            the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
4137     *            the array
4138     * @return the joined String, {@code null} if null array input
4139     * @since 3.2
4140     */
4141    public static String join(final float[] array, final char separator, final int startIndex, final int endIndex) {
4142        if (array == null) {
4143            return null;
4144        }
4145        final int noOfItems = endIndex - startIndex;
4146        if (noOfItems <= 0) {
4147            return EMPTY;
4148        }
4149        final StringBuilder buf = newStringBuilder(noOfItems);
4150        buf.append(array[startIndex]);
4151        for (int i = startIndex + 1; i < endIndex; i++) {
4152            buf.append(separator);
4153            buf.append(array[i]);
4154        }
4155        return buf.toString();
4156    }
4157
4158    /**
4159     * <p>
4160     * Joins the elements of the provided array into a single String containing the provided list of elements.
4161     * </p>
4162     *
4163     * <p>
4164     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
4165     * by empty strings.
4166     * </p>
4167     *
4168     * <pre>
4169     * StringUtils.join(null, *)               = null
4170     * StringUtils.join([], *)                 = ""
4171     * StringUtils.join([null], *)             = ""
4172     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
4173     * StringUtils.join([1, 2, 3], null) = "123"
4174     * </pre>
4175     *
4176     * @param array
4177     *            the array of values to join together, may be null
4178     * @param separator
4179     *            the separator character to use
4180     * @return the joined String, {@code null} if null array input
4181     * @since 3.2
4182     */
4183    public static String join(final int[] array, final char separator) {
4184        if (array == null) {
4185            return null;
4186        }
4187        return join(array, separator, 0, array.length);
4188    }
4189
4190    /**
4191     * <p>
4192     * Joins the elements of the provided array into a single String containing the provided list of elements.
4193     * </p>
4194     *
4195     * <p>
4196     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
4197     * by empty strings.
4198     * </p>
4199     *
4200     * <pre>
4201     * StringUtils.join(null, *)               = null
4202     * StringUtils.join([], *)                 = ""
4203     * StringUtils.join([null], *)             = ""
4204     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
4205     * StringUtils.join([1, 2, 3], null) = "123"
4206     * </pre>
4207     *
4208     * @param array
4209     *            the array of values to join together, may be null
4210     * @param separator
4211     *            the separator character to use
4212     * @param startIndex
4213     *            the first index to start joining from. It is an error to pass in a start index past the end of the
4214     *            array
4215     * @param endIndex
4216     *            the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
4217     *            the array
4218     * @return the joined String, {@code null} if null array input
4219     * @since 3.2
4220     */
4221    public static String join(final int[] array, final char separator, final int startIndex, final int endIndex) {
4222        if (array == null) {
4223            return null;
4224        }
4225        final int noOfItems = endIndex - startIndex;
4226        if (noOfItems <= 0) {
4227            return EMPTY;
4228        }
4229        final StringBuilder buf = newStringBuilder(noOfItems);
4230        buf.append(array[startIndex]);
4231        for (int i = startIndex + 1; i < endIndex; i++) {
4232            buf.append(separator);
4233            buf.append(array[i]);
4234        }
4235        return buf.toString();
4236    }
4237
4238    /**
4239     * <p>Joins the elements of the provided {@code Iterable} into
4240     * a single String containing the provided elements.</p>
4241     *
4242     * <p>No delimiter is added before or after the list. Null objects or empty
4243     * strings within the iteration are represented by empty strings.</p>
4244     *
4245     * <p>See the examples here: {@link #join(Object[],char)}. </p>
4246     *
4247     * @param iterable  the {@code Iterable} providing the values to join together, may be null
4248     * @param separator  the separator character to use
4249     * @return the joined String, {@code null} if null iterator input
4250     * @since 2.3
4251     */
4252    public static String join(final Iterable<?> iterable, final char separator) {
4253        if (iterable == null) {
4254            return null;
4255        }
4256        return join(iterable.iterator(), separator);
4257    }
4258
4259    /**
4260     * <p>Joins the elements of the provided {@code Iterable} into
4261     * a single String containing the provided elements.</p>
4262     *
4263     * <p>No delimiter is added before or after the list.
4264     * A {@code null} separator is the same as an empty String ("").</p>
4265     *
4266     * <p>See the examples here: {@link #join(Object[],String)}. </p>
4267     *
4268     * @param iterable  the {@code Iterable} providing the values to join together, may be null
4269     * @param separator  the separator character to use, null treated as ""
4270     * @return the joined String, {@code null} if null iterator input
4271     * @since 2.3
4272     */
4273    public static String join(final Iterable<?> iterable, final String separator) {
4274        if (iterable == null) {
4275            return null;
4276        }
4277        return join(iterable.iterator(), separator);
4278    }
4279
4280    /**
4281     * <p>Joins the elements of the provided {@code Iterator} into
4282     * a single String containing the provided elements.</p>
4283     *
4284     * <p>No delimiter is added before or after the list. Null objects or empty
4285     * strings within the iteration are represented by empty strings.</p>
4286     *
4287     * <p>See the examples here: {@link #join(Object[],char)}. </p>
4288     *
4289     * @param iterator  the {@code Iterator} of values to join together, may be null
4290     * @param separator  the separator character to use
4291     * @return the joined String, {@code null} if null iterator input
4292     * @since 2.0
4293     */
4294    public static String join(final Iterator<?> iterator, final char separator) {
4295
4296        // handle null, zero and one elements before building a buffer
4297        if (iterator == null) {
4298            return null;
4299        }
4300        if (!iterator.hasNext()) {
4301            return EMPTY;
4302        }
4303        final Object first = iterator.next();
4304        if (!iterator.hasNext()) {
4305            return Objects.toString(first, EMPTY);
4306        }
4307
4308        // two or more elements
4309        final StringBuilder buf = new StringBuilder(STRING_BUILDER_SIZE); // Java default is 16, probably too small
4310        if (first != null) {
4311            buf.append(first);
4312        }
4313
4314        while (iterator.hasNext()) {
4315            buf.append(separator);
4316            final Object obj = iterator.next();
4317            if (obj != null) {
4318                buf.append(obj);
4319            }
4320        }
4321
4322        return buf.toString();
4323    }
4324
4325    /**
4326     * <p>Joins the elements of the provided {@code Iterator} into
4327     * a single String containing the provided elements.</p>
4328     *
4329     * <p>No delimiter is added before or after the list.
4330     * A {@code null} separator is the same as an empty String ("").</p>
4331     *
4332     * <p>See the examples here: {@link #join(Object[],String)}. </p>
4333     *
4334     * @param iterator  the {@code Iterator} of values to join together, may be null
4335     * @param separator  the separator character to use, null treated as ""
4336     * @return the joined String, {@code null} if null iterator input
4337     */
4338    public static String join(final Iterator<?> iterator, final String separator) {
4339
4340        // handle null, zero and one elements before building a buffer
4341        if (iterator == null) {
4342            return null;
4343        }
4344        if (!iterator.hasNext()) {
4345            return EMPTY;
4346        }
4347        final Object first = iterator.next();
4348        if (!iterator.hasNext()) {
4349            return Objects.toString(first, "");
4350        }
4351
4352        // two or more elements
4353        final StringBuilder buf = new StringBuilder(STRING_BUILDER_SIZE); // Java default is 16, probably too small
4354        if (first != null) {
4355            buf.append(first);
4356        }
4357
4358        while (iterator.hasNext()) {
4359            if (separator != null) {
4360                buf.append(separator);
4361            }
4362            final Object obj = iterator.next();
4363            if (obj != null) {
4364                buf.append(obj);
4365            }
4366        }
4367        return buf.toString();
4368    }
4369
4370    /**
4371     * <p>Joins the elements of the provided {@code List} into a single String
4372     * containing the provided list of elements.</p>
4373     *
4374     * <p>No delimiter is added before or after the list.
4375     * Null objects or empty strings within the array are represented by
4376     * empty strings.</p>
4377     *
4378     * <pre>
4379     * StringUtils.join(null, *)               = null
4380     * StringUtils.join([], *)                 = ""
4381     * StringUtils.join([null], *)             = ""
4382     * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
4383     * StringUtils.join(["a", "b", "c"], null) = "abc"
4384     * StringUtils.join([null, "", "a"], ';')  = ";;a"
4385     * </pre>
4386     *
4387     * @param list  the {@code List} of values to join together, may be null
4388     * @param separator  the separator character to use
4389     * @param startIndex the first index to start joining from.  It is
4390     * an error to pass in a start index past the end of the list
4391     * @param endIndex the index to stop joining from (exclusive). It is
4392     * an error to pass in an end index past the end of the list
4393     * @return the joined String, {@code null} if null list input
4394     * @since 3.8
4395     */
4396    public static String join(final List<?> list, final char separator, final int startIndex, final int endIndex) {
4397        if (list == null) {
4398            return null;
4399        }
4400        final int noOfItems = endIndex - startIndex;
4401        if (noOfItems <= 0) {
4402            return EMPTY;
4403        }
4404        final List<?> subList = list.subList(startIndex, endIndex);
4405        return join(subList.iterator(), separator);
4406    }
4407
4408    /**
4409     * <p>Joins the elements of the provided {@code List} into a single String
4410     * containing the provided list of elements.</p>
4411     *
4412     * <p>No delimiter is added before or after the list.
4413     * Null objects or empty strings within the array are represented by
4414     * empty strings.</p>
4415     *
4416     * <pre>
4417     * StringUtils.join(null, *)               = null
4418     * StringUtils.join([], *)                 = ""
4419     * StringUtils.join([null], *)             = ""
4420     * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
4421     * StringUtils.join(["a", "b", "c"], null) = "abc"
4422     * StringUtils.join([null, "", "a"], ';')  = ";;a"
4423     * </pre>
4424     *
4425     * @param list  the {@code List} of values to join together, may be null
4426     * @param separator  the separator character to use
4427     * @param startIndex the first index to start joining from.  It is
4428     * an error to pass in a start index past the end of the list
4429     * @param endIndex the index to stop joining from (exclusive). It is
4430     * an error to pass in an end index past the end of the list
4431     * @return the joined String, {@code null} if null list input
4432     * @since 3.8
4433     */
4434    public static String join(final List<?> list, final String separator, final int startIndex, final int endIndex) {
4435        if (list == null) {
4436            return null;
4437        }
4438        final int noOfItems = endIndex - startIndex;
4439        if (noOfItems <= 0) {
4440            return EMPTY;
4441        }
4442        final List<?> subList = list.subList(startIndex, endIndex);
4443        return join(subList.iterator(), separator);
4444    }
4445
4446
4447    /**
4448     * <p>
4449     * Joins the elements of the provided array into a single String containing the provided list of elements.
4450     * </p>
4451     *
4452     * <p>
4453     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
4454     * by empty strings.
4455     * </p>
4456     *
4457     * <pre>
4458     * StringUtils.join(null, *)               = null
4459     * StringUtils.join([], *)                 = ""
4460     * StringUtils.join([null], *)             = ""
4461     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
4462     * StringUtils.join([1, 2, 3], null) = "123"
4463     * </pre>
4464     *
4465     * @param array
4466     *            the array of values to join together, may be null
4467     * @param separator
4468     *            the separator character to use
4469     * @return the joined String, {@code null} if null array input
4470     * @since 3.2
4471     */
4472    public static String join(final long[] array, final char separator) {
4473        if (array == null) {
4474            return null;
4475        }
4476        return join(array, separator, 0, array.length);
4477    }
4478
4479    /**
4480     * <p>
4481     * Joins the elements of the provided array into a single String containing the provided list of elements.
4482     * </p>
4483     *
4484     * <p>
4485     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
4486     * by empty strings.
4487     * </p>
4488     *
4489     * <pre>
4490     * StringUtils.join(null, *)               = null
4491     * StringUtils.join([], *)                 = ""
4492     * StringUtils.join([null], *)             = ""
4493     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
4494     * StringUtils.join([1, 2, 3], null) = "123"
4495     * </pre>
4496     *
4497     * @param array
4498     *            the array of values to join together, may be null
4499     * @param separator
4500     *            the separator character to use
4501     * @param startIndex
4502     *            the first index to start joining from. It is an error to pass in a start index past the end of the
4503     *            array
4504     * @param endIndex
4505     *            the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
4506     *            the array
4507     * @return the joined String, {@code null} if null array input
4508     * @since 3.2
4509     */
4510    public static String join(final long[] array, final char separator, final int startIndex, final int endIndex) {
4511        if (array == null) {
4512            return null;
4513        }
4514        final int noOfItems = endIndex - startIndex;
4515        if (noOfItems <= 0) {
4516            return EMPTY;
4517        }
4518        final StringBuilder buf = newStringBuilder(noOfItems);
4519        buf.append(array[startIndex]);
4520        for (int i = startIndex + 1; i < endIndex; i++) {
4521            buf.append(separator);
4522            buf.append(array[i]);
4523        }
4524        return buf.toString();
4525    }
4526
4527    /**
4528     * <p>Joins the elements of the provided array into a single String
4529     * containing the provided list of elements.</p>
4530     *
4531     * <p>No delimiter is added before or after the list.
4532     * Null objects or empty strings within the array are represented by
4533     * empty strings.</p>
4534     *
4535     * <pre>
4536     * StringUtils.join(null, *)               = null
4537     * StringUtils.join([], *)                 = ""
4538     * StringUtils.join([null], *)             = ""
4539     * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
4540     * StringUtils.join(["a", "b", "c"], null) = "abc"
4541     * StringUtils.join([null, "", "a"], ';')  = ";;a"
4542     * </pre>
4543     *
4544     * @param array  the array of values to join together, may be null
4545     * @param separator  the separator character to use
4546     * @return the joined String, {@code null} if null array input
4547     * @since 2.0
4548     */
4549    public static String join(final Object[] array, final char separator) {
4550        if (array == null) {
4551            return null;
4552        }
4553        return join(array, separator, 0, array.length);
4554    }
4555
4556    /**
4557     * <p>Joins the elements of the provided array into a single String
4558     * containing the provided list of elements.</p>
4559     *
4560     * <p>No delimiter is added before or after the list.
4561     * Null objects or empty strings within the array are represented by
4562     * empty strings.</p>
4563     *
4564     * <pre>
4565     * StringUtils.join(null, *)               = null
4566     * StringUtils.join([], *)                 = ""
4567     * StringUtils.join([null], *)             = ""
4568     * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
4569     * StringUtils.join(["a", "b", "c"], null) = "abc"
4570     * StringUtils.join([null, "", "a"], ';')  = ";;a"
4571     * </pre>
4572     *
4573     * @param array  the array of values to join together, may be null
4574     * @param separator  the separator character to use
4575     * @param startIndex the first index to start joining from.  It is
4576     * an error to pass in a start index past the end of the array
4577     * @param endIndex the index to stop joining from (exclusive). It is
4578     * an error to pass in an end index past the end of the array
4579     * @return the joined String, {@code null} if null array input
4580     * @since 2.0
4581     */
4582    public static String join(final Object[] array, final char separator, final int startIndex, final int endIndex) {
4583        if (array == null) {
4584            return null;
4585        }
4586        final int noOfItems = endIndex - startIndex;
4587        if (noOfItems <= 0) {
4588            return EMPTY;
4589        }
4590        final StringBuilder buf = newStringBuilder(noOfItems);
4591        if (array[startIndex] != null) {
4592            buf.append(array[startIndex]);
4593        }
4594        for (int i = startIndex + 1; i < endIndex; i++) {
4595            buf.append(separator);
4596            if (array[i] != null) {
4597                buf.append(array[i]);
4598            }
4599        }
4600        return buf.toString();
4601    }
4602
4603    /**
4604     * <p>Joins the elements of the provided array into a single String
4605     * containing the provided list of elements.</p>
4606     *
4607     * <p>No delimiter is added before or after the list.
4608     * A {@code null} separator is the same as an empty String ("").
4609     * Null objects or empty strings within the array are represented by
4610     * empty strings.</p>
4611     *
4612     * <pre>
4613     * StringUtils.join(null, *)                = null
4614     * StringUtils.join([], *)                  = ""
4615     * StringUtils.join([null], *)              = ""
4616     * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
4617     * StringUtils.join(["a", "b", "c"], null)  = "abc"
4618     * StringUtils.join(["a", "b", "c"], "")    = "abc"
4619     * StringUtils.join([null, "", "a"], ',')   = ",,a"
4620     * </pre>
4621     *
4622     * @param array  the array of values to join together, may be null
4623     * @param separator  the separator character to use, null treated as ""
4624     * @return the joined String, {@code null} if null array input
4625     */
4626    public static String join(final Object[] array, final String separator) {
4627        if (array == null) {
4628            return null;
4629        }
4630        return join(array, separator, 0, array.length);
4631    }
4632
4633    /**
4634     * <p>Joins the elements of the provided array into a single String
4635     * containing the provided list of elements.</p>
4636     *
4637     * <p>No delimiter is added before or after the list.
4638     * A {@code null} separator is the same as an empty String ("").
4639     * Null objects or empty strings within the array are represented by
4640     * empty strings.</p>
4641     *
4642     * <pre>
4643     * StringUtils.join(null, *, *, *)                = null
4644     * StringUtils.join([], *, *, *)                  = ""
4645     * StringUtils.join([null], *, *, *)              = ""
4646     * StringUtils.join(["a", "b", "c"], "--", 0, 3)  = "a--b--c"
4647     * StringUtils.join(["a", "b", "c"], "--", 1, 3)  = "b--c"
4648     * StringUtils.join(["a", "b", "c"], "--", 2, 3)  = "c"
4649     * StringUtils.join(["a", "b", "c"], "--", 2, 2)  = ""
4650     * StringUtils.join(["a", "b", "c"], null, 0, 3)  = "abc"
4651     * StringUtils.join(["a", "b", "c"], "", 0, 3)    = "abc"
4652     * StringUtils.join([null, "", "a"], ',', 0, 3)   = ",,a"
4653     * </pre>
4654     *
4655     * @param array  the array of values to join together, may be null
4656     * @param separator  the separator character to use, null treated as ""
4657     * @param startIndex the first index to start joining from.
4658     * @param endIndex the index to stop joining from (exclusive).
4659     * @return the joined String, {@code null} if null array input; or the empty string
4660     * if {@code endIndex - startIndex <= 0}. The number of joined entries is given by
4661     * {@code endIndex - startIndex}
4662     * @throws ArrayIndexOutOfBoundsException ife<br>
4663     * {@code startIndex < 0} or <br>
4664     * {@code startIndex >= array.length()} or <br>
4665     * {@code endIndex < 0} or <br>
4666     * {@code endIndex > array.length()}
4667     */
4668    public static String join(final Object[] array, String separator, final int startIndex, final int endIndex) {
4669        if (array == null) {
4670            return null;
4671        }
4672        if (separator == null) {
4673            separator = EMPTY;
4674        }
4675
4676        // endIndex - startIndex > 0:   Len = NofStrings *(len(firstString) + len(separator))
4677        //           (Assuming that all Strings are roughly equally long)
4678        final int noOfItems = endIndex - startIndex;
4679        if (noOfItems <= 0) {
4680            return EMPTY;
4681        }
4682
4683        final StringBuilder buf = newStringBuilder(noOfItems);
4684
4685        if (array[startIndex] != null) {
4686            buf.append(array[startIndex]);
4687        }
4688
4689        for (int i = startIndex + 1; i < endIndex; i++) {
4690            buf.append(separator);
4691
4692            if (array[i] != null) {
4693                buf.append(array[i]);
4694            }
4695        }
4696        return buf.toString();
4697    }
4698
4699    /**
4700     * <p>
4701     * Joins the elements of the provided array into a single String containing the provided list of elements.
4702     * </p>
4703     *
4704     * <p>
4705     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
4706     * by empty strings.
4707     * </p>
4708     *
4709     * <pre>
4710     * StringUtils.join(null, *)               = null
4711     * StringUtils.join([], *)                 = ""
4712     * StringUtils.join([null], *)             = ""
4713     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
4714     * StringUtils.join([1, 2, 3], null) = "123"
4715     * </pre>
4716     *
4717     * @param array
4718     *            the array of values to join together, may be null
4719     * @param separator
4720     *            the separator character to use
4721     * @return the joined String, {@code null} if null array input
4722     * @since 3.2
4723     */
4724    public static String join(final short[] array, final char separator) {
4725        if (array == null) {
4726            return null;
4727        }
4728        return join(array, separator, 0, array.length);
4729    }
4730
4731    /**
4732     * <p>
4733     * Joins the elements of the provided array into a single String containing the provided list of elements.
4734     * </p>
4735     *
4736     * <p>
4737     * No delimiter is added before or after the list. Null objects or empty strings within the array are represented
4738     * by empty strings.
4739     * </p>
4740     *
4741     * <pre>
4742     * StringUtils.join(null, *)               = null
4743     * StringUtils.join([], *)                 = ""
4744     * StringUtils.join([null], *)             = ""
4745     * StringUtils.join([1, 2, 3], ';')  = "1;2;3"
4746     * StringUtils.join([1, 2, 3], null) = "123"
4747     * </pre>
4748     *
4749     * @param array
4750     *            the array of values to join together, may be null
4751     * @param separator
4752     *            the separator character to use
4753     * @param startIndex
4754     *            the first index to start joining from. It is an error to pass in a start index past the end of the
4755     *            array
4756     * @param endIndex
4757     *            the index to stop joining from (exclusive). It is an error to pass in an end index past the end of
4758     *            the array
4759     * @return the joined String, {@code null} if null array input
4760     * @since 3.2
4761     */
4762    public static String join(final short[] array, final char separator, final int startIndex, final int endIndex) {
4763        if (array == null) {
4764            return null;
4765        }
4766        final int noOfItems = endIndex - startIndex;
4767        if (noOfItems <= 0) {
4768            return EMPTY;
4769        }
4770        final StringBuilder buf = newStringBuilder(noOfItems);
4771        buf.append(array[startIndex]);
4772        for (int i = startIndex + 1; i < endIndex; i++) {
4773            buf.append(separator);
4774            buf.append(array[i]);
4775        }
4776        return buf.toString();
4777    }
4778
4779
4780    // Joining
4781    //-----------------------------------------------------------------------
4782    /**
4783     * <p>Joins the elements of the provided array into a single String
4784     * containing the provided list of elements.</p>
4785     *
4786     * <p>No separator is added to the joined String.
4787     * Null objects or empty strings within the array are represented by
4788     * empty strings.</p>
4789     *
4790     * <pre>
4791     * StringUtils.join(null)            = null
4792     * StringUtils.join([])              = ""
4793     * StringUtils.join([null])          = ""
4794     * StringUtils.join(["a", "b", "c"]) = "abc"
4795     * StringUtils.join([null, "", "a"]) = "a"
4796     * </pre>
4797     *
4798     * @param <T> the specific type of values to join together
4799     * @param elements  the values to join together, may be null
4800     * @return the joined String, {@code null} if null array input
4801     * @since 2.0
4802     * @since 3.0 Changed signature to use varargs
4803     */
4804    @SafeVarargs
4805    public static <T> String join(final T... elements) {
4806        return join(elements, null);
4807    }
4808
4809    /**
4810     * <p>Joins the elements of the provided varargs into a
4811     * single String containing the provided elements.</p>
4812     *
4813     * <p>No delimiter is added before or after the list.
4814     * {@code null} elements and separator are treated as empty Strings ("").</p>
4815     *
4816     * <pre>
4817     * StringUtils.joinWith(",", {"a", "b"})        = "a,b"
4818     * StringUtils.joinWith(",", {"a", "b",""})     = "a,b,"
4819     * StringUtils.joinWith(",", {"a", null, "b"})  = "a,,b"
4820     * StringUtils.joinWith(null, {"a", "b"})       = "ab"
4821     * </pre>
4822     *
4823     * @param separator the separator character to use, null treated as ""
4824     * @param objects the varargs providing the values to join together. {@code null} elements are treated as ""
4825     * @return the joined String.
4826     * @throws java.lang.IllegalArgumentException if a null varargs is provided
4827     * @since 3.5
4828     */
4829    public static String joinWith(final String separator, final Object... objects) {
4830        if (objects == null) {
4831            throw new IllegalArgumentException("Object varargs must not be null");
4832        }
4833
4834        final String sanitizedSeparator = defaultString(separator);
4835
4836        final StringBuilder result = new StringBuilder();
4837
4838        final Iterator<Object> iterator = Arrays.asList(objects).iterator();
4839        while (iterator.hasNext()) {
4840            final String value = Objects.toString(iterator.next(), "");
4841            result.append(value);
4842
4843            if (iterator.hasNext()) {
4844                result.append(sanitizedSeparator);
4845            }
4846        }
4847
4848        return result.toString();
4849    }
4850
4851    /**
4852     * <p>Finds the last index within a CharSequence, handling {@code null}.
4853     * This method uses {@link String#lastIndexOf(String)} if possible.</p>
4854     *
4855     * <p>A {@code null} CharSequence will return {@code -1}.</p>
4856     *
4857     * <pre>
4858     * StringUtils.lastIndexOf(null, *)          = -1
4859     * StringUtils.lastIndexOf(*, null)          = -1
4860     * StringUtils.lastIndexOf("", "")           = 0
4861     * StringUtils.lastIndexOf("aabaabaa", "a")  = 7
4862     * StringUtils.lastIndexOf("aabaabaa", "b")  = 5
4863     * StringUtils.lastIndexOf("aabaabaa", "ab") = 4
4864     * StringUtils.lastIndexOf("aabaabaa", "")   = 8
4865     * </pre>
4866     *
4867     * @param seq  the CharSequence to check, may be null
4868     * @param searchSeq  the CharSequence to find, may be null
4869     * @return the last index of the search String,
4870     *  -1 if no match or {@code null} string input
4871     * @since 2.0
4872     * @since 3.0 Changed signature from lastIndexOf(String, String) to lastIndexOf(CharSequence, CharSequence)
4873     */
4874    public static int lastIndexOf(final CharSequence seq, final CharSequence searchSeq) {
4875        if (seq == null || searchSeq == null) {
4876            return INDEX_NOT_FOUND;
4877        }
4878        return CharSequenceUtils.lastIndexOf(seq, searchSeq, seq.length());
4879    }
4880
4881    /**
4882     * <p>Finds the last index within a CharSequence, handling {@code null}.
4883     * This method uses {@link String#lastIndexOf(String, int)} if possible.</p>
4884     *
4885     * <p>A {@code null} CharSequence will return {@code -1}.
4886     * A negative start position returns {@code -1}.
4887     * An empty ("") search CharSequence always matches unless the start position is negative.
4888     * A start position greater than the string length searches the whole string.
4889     * The search starts at the startPos and works backwards; matches starting after the start
4890     * position are ignored.
4891     * </p>
4892     *
4893     * <pre>
4894     * StringUtils.lastIndexOf(null, *, *)          = -1
4895     * StringUtils.lastIndexOf(*, null, *)          = -1
4896     * StringUtils.lastIndexOf("aabaabaa", "a", 8)  = 7
4897     * StringUtils.lastIndexOf("aabaabaa", "b", 8)  = 5
4898     * StringUtils.lastIndexOf("aabaabaa", "ab", 8) = 4
4899     * StringUtils.lastIndexOf("aabaabaa", "b", 9)  = 5
4900     * StringUtils.lastIndexOf("aabaabaa", "b", -1) = -1
4901     * StringUtils.lastIndexOf("aabaabaa", "a", 0)  = 0
4902     * StringUtils.lastIndexOf("aabaabaa", "b", 0)  = -1
4903     * StringUtils.lastIndexOf("aabaabaa", "b", 1)  = -1
4904     * StringUtils.lastIndexOf("aabaabaa", "b", 2)  = 2
4905     * StringUtils.lastIndexOf("aabaabaa", "ba", 2)  = 2
4906     * </pre>
4907     *
4908     * @param seq  the CharSequence to check, may be null
4909     * @param searchSeq  the CharSequence to find, may be null
4910     * @param startPos  the start position, negative treated as zero
4911     * @return the last index of the search CharSequence (always &le; startPos),
4912     *  -1 if no match or {@code null} string input
4913     * @since 2.0
4914     * @since 3.0 Changed signature from lastIndexOf(String, String, int) to lastIndexOf(CharSequence, CharSequence, int)
4915     */
4916    public static int lastIndexOf(final CharSequence seq, final CharSequence searchSeq, final int startPos) {
4917        if (seq == null || searchSeq == null) {
4918            return INDEX_NOT_FOUND;
4919        }
4920        return CharSequenceUtils.lastIndexOf(seq, searchSeq, startPos);
4921    }
4922
4923    // LastIndexOf
4924    //-----------------------------------------------------------------------
4925    /**
4926     * Returns the index within {@code seq} of the last occurrence of
4927     * the specified character. For values of {@code searchChar} in the
4928     * range from 0 to 0xFFFF (inclusive), the index (in Unicode code
4929     * units) returned is the largest value <i>k</i> such that:
4930     * <blockquote><pre>
4931     * this.charAt(<i>k</i>) == searchChar
4932     * </pre></blockquote>
4933     * is true. For other values of {@code searchChar}, it is the
4934     * largest value <i>k</i> such that:
4935     * <blockquote><pre>
4936     * this.codePointAt(<i>k</i>) == searchChar
4937     * </pre></blockquote>
4938     * is true.  In either case, if no such character occurs in this
4939     * string, then {@code -1} is returned. Furthermore, a {@code null} or empty ("")
4940     * {@code CharSequence} will return {@code -1}. The
4941     * {@code seq} {@code CharSequence} object is searched backwards
4942     * starting at the last character.
4943     *
4944     * <pre>
4945     * StringUtils.lastIndexOf(null, *)         = -1
4946     * StringUtils.lastIndexOf("", *)           = -1
4947     * StringUtils.lastIndexOf("aabaabaa", 'a') = 7
4948     * StringUtils.lastIndexOf("aabaabaa", 'b') = 5
4949     * </pre>
4950     *
4951     * @param seq  the {@code CharSequence} to check, may be null
4952     * @param searchChar  the character to find
4953     * @return the last index of the search character,
4954     *  -1 if no match or {@code null} string input
4955     * @since 2.0
4956     * @since 3.0 Changed signature from lastIndexOf(String, int) to lastIndexOf(CharSequence, int)
4957     * @since 3.6 Updated {@link CharSequenceUtils} call to behave more like {@code String}
4958     */
4959    public static int lastIndexOf(final CharSequence seq, final int searchChar) {
4960        if (isEmpty(seq)) {
4961            return INDEX_NOT_FOUND;
4962        }
4963        return CharSequenceUtils.lastIndexOf(seq, searchChar, seq.length());
4964    }
4965
4966    /**
4967     * Returns the index within {@code seq} of the last occurrence of
4968     * the specified character, searching backward starting at the
4969     * specified index. For values of {@code searchChar} in the range
4970     * from 0 to 0xFFFF (inclusive), the index returned is the largest
4971     * value <i>k</i> such that:
4972     * <blockquote><pre>
4973     * (this.charAt(<i>k</i>) == searchChar) &amp;&amp; (<i>k</i> &lt;= startPos)
4974     * </pre></blockquote>
4975     * is true. For other values of {@code searchChar}, it is the
4976     * largest value <i>k</i> such that:
4977     * <blockquote><pre>
4978     * (this.codePointAt(<i>k</i>) == searchChar) &amp;&amp; (<i>k</i> &lt;= startPos)
4979     * </pre></blockquote>
4980     * is true. In either case, if no such character occurs in {@code seq}
4981     * at or before position {@code startPos}, then
4982     * {@code -1} is returned. Furthermore, a {@code null} or empty ("")
4983     * {@code CharSequence} will return {@code -1}. A start position greater
4984     * than the string length searches the whole string.
4985     * The search starts at the {@code startPos} and works backwards;
4986     * matches starting after the start position are ignored.
4987     *
4988     * <p>All indices are specified in {@code char} values
4989     * (Unicode code units).
4990     *
4991     * <pre>
4992     * StringUtils.lastIndexOf(null, *, *)          = -1
4993     * StringUtils.lastIndexOf("", *,  *)           = -1
4994     * StringUtils.lastIndexOf("aabaabaa", 'b', 8)  = 5
4995     * StringUtils.lastIndexOf("aabaabaa", 'b', 4)  = 2
4996     * StringUtils.lastIndexOf("aabaabaa", 'b', 0)  = -1
4997     * StringUtils.lastIndexOf("aabaabaa", 'b', 9)  = 5
4998     * StringUtils.lastIndexOf("aabaabaa", 'b', -1) = -1
4999     * StringUtils.lastIndexOf("aabaabaa", 'a', 0)  = 0
5000     * </pre>
5001     *
5002     * @param seq  the CharSequence to check, may be null
5003     * @param searchChar  the character to find
5004     * @param startPos  the start position
5005     * @return the last index of the search character (always &le; startPos),
5006     *  -1 if no match or {@code null} string input
5007     * @since 2.0
5008     * @since 3.0 Changed signature from lastIndexOf(String, int, int) to lastIndexOf(CharSequence, int, int)
5009     */
5010    public static int lastIndexOf(final CharSequence seq, final int searchChar, final int startPos) {
5011        if (isEmpty(seq)) {
5012            return INDEX_NOT_FOUND;
5013        }
5014        return CharSequenceUtils.lastIndexOf(seq, searchChar, startPos);
5015    }
5016
5017    /**
5018     * <p>Find the latest index of any of a set of potential substrings.</p>
5019     *
5020     * <p>A {@code null} CharSequence will return {@code -1}.
5021     * A {@code null} search array will return {@code -1}.
5022     * A {@code null} or zero length search array entry will be ignored,
5023     * but a search array containing "" will return the length of {@code str}
5024     * if {@code str} is not null. This method uses {@link String#indexOf(String)} if possible</p>
5025     *
5026     * <pre>
5027     * StringUtils.lastIndexOfAny(null, *)                    = -1
5028     * StringUtils.lastIndexOfAny(*, null)                    = -1
5029     * StringUtils.lastIndexOfAny(*, [])                      = -1
5030     * StringUtils.lastIndexOfAny(*, [null])                  = -1
5031     * StringUtils.lastIndexOfAny("zzabyycdxx", ["ab", "cd"]) = 6
5032     * StringUtils.lastIndexOfAny("zzabyycdxx", ["cd", "ab"]) = 6
5033     * StringUtils.lastIndexOfAny("zzabyycdxx", ["mn", "op"]) = -1
5034     * StringUtils.lastIndexOfAny("zzabyycdxx", ["mn", "op"]) = -1
5035     * StringUtils.lastIndexOfAny("zzabyycdxx", ["mn", ""])   = 10
5036     * </pre>
5037     *
5038     * @param str  the CharSequence to check, may be null
5039     * @param searchStrs  the CharSequences to search for, may be null
5040     * @return the last index of any of the CharSequences, -1 if no match
5041     * @since 3.0 Changed signature from lastIndexOfAny(String, String[]) to lastIndexOfAny(CharSequence, CharSequence)
5042     */
5043    public static int lastIndexOfAny(final CharSequence str, final CharSequence... searchStrs) {
5044        if (str == null || searchStrs == null) {
5045            return INDEX_NOT_FOUND;
5046        }
5047        int ret = INDEX_NOT_FOUND;
5048        int tmp = 0;
5049        for (final CharSequence search : searchStrs) {
5050            if (search == null) {
5051                continue;
5052            }
5053            tmp = CharSequenceUtils.lastIndexOf(str, search, str.length());
5054            if (tmp > ret) {
5055                ret = tmp;
5056            }
5057        }
5058        return ret;
5059    }
5060
5061    /**
5062     * <p>Case in-sensitive find of the last index within a CharSequence.</p>
5063     *
5064     * <p>A {@code null} CharSequence will return {@code -1}.
5065     * A negative start position returns {@code -1}.
5066     * An empty ("") search CharSequence always matches unless the start position is negative.
5067     * A start position greater than the string length searches the whole string.</p>
5068     *
5069     * <pre>
5070     * StringUtils.lastIndexOfIgnoreCase(null, *)          = -1
5071     * StringUtils.lastIndexOfIgnoreCase(*, null)          = -1
5072     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A")  = 7
5073     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B")  = 5
5074     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "AB") = 4
5075     * </pre>
5076     *
5077     * @param str  the CharSequence to check, may be null
5078     * @param searchStr  the CharSequence to find, may be null
5079     * @return the first index of the search CharSequence,
5080     *  -1 if no match or {@code null} string input
5081     * @since 2.5
5082     * @since 3.0 Changed signature from lastIndexOfIgnoreCase(String, String) to lastIndexOfIgnoreCase(CharSequence, CharSequence)
5083     */
5084    public static int lastIndexOfIgnoreCase(final CharSequence str, final CharSequence searchStr) {
5085        if (str == null || searchStr == null) {
5086            return INDEX_NOT_FOUND;
5087        }
5088        return lastIndexOfIgnoreCase(str, searchStr, str.length());
5089    }
5090
5091    /**
5092     * <p>Case in-sensitive find of the last index within a CharSequence
5093     * from the specified position.</p>
5094     *
5095     * <p>A {@code null} CharSequence will return {@code -1}.
5096     * A negative start position returns {@code -1}.
5097     * An empty ("") search CharSequence always matches unless the start position is negative.
5098     * A start position greater than the string length searches the whole string.
5099     * The search starts at the startPos and works backwards; matches starting after the start
5100     * position are ignored.
5101     * </p>
5102     *
5103     * <pre>
5104     * StringUtils.lastIndexOfIgnoreCase(null, *, *)          = -1
5105     * StringUtils.lastIndexOfIgnoreCase(*, null, *)          = -1
5106     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 8)  = 7
5107     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 8)  = 5
5108     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "AB", 8) = 4
5109     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 9)  = 5
5110     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", -1) = -1
5111     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 0)  = 0
5112     * StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 0)  = -1
5113     * </pre>
5114     *
5115     * @param str  the CharSequence to check, may be null
5116     * @param searchStr  the CharSequence to find, may be null
5117     * @param startPos  the start position
5118     * @return the last index of the search CharSequence (always &le; startPos),
5119     *  -1 if no match or {@code null} input
5120     * @since 2.5
5121     * @since 3.0 Changed signature from lastIndexOfIgnoreCase(String, String, int) to lastIndexOfIgnoreCase(CharSequence, CharSequence, int)
5122     */
5123    public static int lastIndexOfIgnoreCase(final CharSequence str, final CharSequence searchStr, int startPos) {
5124        if (str == null || searchStr == null) {
5125            return INDEX_NOT_FOUND;
5126        }
5127        if (startPos > str.length() - searchStr.length()) {
5128            startPos = str.length() - searchStr.length();
5129        }
5130        if (startPos < 0) {
5131            return INDEX_NOT_FOUND;
5132        }
5133        if (searchStr.length() == 0) {
5134            return startPos;
5135        }
5136
5137        for (int i = startPos; i >= 0; i--) {
5138            if (CharSequenceUtils.regionMatches(str, true, i, searchStr, 0, searchStr.length())) {
5139                return i;
5140            }
5141        }
5142        return INDEX_NOT_FOUND;
5143    }
5144
5145    /**
5146     * <p>Finds the n-th last index within a String, handling {@code null}.
5147     * This method uses {@link String#lastIndexOf(String)}.</p>
5148     *
5149     * <p>A {@code null} String will return {@code -1}.</p>
5150     *
5151     * <pre>
5152     * StringUtils.lastOrdinalIndexOf(null, *, *)          = -1
5153     * StringUtils.lastOrdinalIndexOf(*, null, *)          = -1
5154     * StringUtils.lastOrdinalIndexOf("", "", *)           = 0
5155     * StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 1)  = 7
5156     * StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 2)  = 6
5157     * StringUtils.lastOrdinalIndexOf("aabaabaa", "b", 1)  = 5
5158     * StringUtils.lastOrdinalIndexOf("aabaabaa", "b", 2)  = 2
5159     * StringUtils.lastOrdinalIndexOf("aabaabaa", "ab", 1) = 4
5160     * StringUtils.lastOrdinalIndexOf("aabaabaa", "ab", 2) = 1
5161     * StringUtils.lastOrdinalIndexOf("aabaabaa", "", 1)   = 8
5162     * StringUtils.lastOrdinalIndexOf("aabaabaa", "", 2)   = 8
5163     * </pre>
5164     *
5165     * <p>Note that 'tail(CharSequence str, int n)' may be implemented as: </p>
5166     *
5167     * <pre>
5168     *   str.substring(lastOrdinalIndexOf(str, "\n", n) + 1)
5169     * </pre>
5170     *
5171     * @param str  the CharSequence to check, may be null
5172     * @param searchStr  the CharSequence to find, may be null
5173     * @param ordinal  the n-th last {@code searchStr} to find
5174     * @return the n-th last index of the search CharSequence,
5175     *  {@code -1} ({@code INDEX_NOT_FOUND}) if no match or {@code null} string input
5176     * @since 2.5
5177     * @since 3.0 Changed signature from lastOrdinalIndexOf(String, String, int) to lastOrdinalIndexOf(CharSequence, CharSequence, int)
5178     */
5179    public static int lastOrdinalIndexOf(final CharSequence str, final CharSequence searchStr, final int ordinal) {
5180        return ordinalIndexOf(str, searchStr, ordinal, true);
5181    }
5182
5183    // Left/Right/Mid
5184    //-----------------------------------------------------------------------
5185    /**
5186     * <p>Gets the leftmost {@code len} characters of a String.</p>
5187     *
5188     * <p>If {@code len} characters are not available, or the
5189     * String is {@code null}, the String will be returned without
5190     * an exception. An empty String is returned if len is negative.</p>
5191     *
5192     * <pre>
5193     * StringUtils.left(null, *)    = null
5194     * StringUtils.left(*, -ve)     = ""
5195     * StringUtils.left("", *)      = ""
5196     * StringUtils.left("abc", 0)   = ""
5197     * StringUtils.left("abc", 2)   = "ab"
5198     * StringUtils.left("abc", 4)   = "abc"
5199     * </pre>
5200     *
5201     * @param str  the String to get the leftmost characters from, may be null
5202     * @param len  the length of the required String
5203     * @return the leftmost characters, {@code null} if null String input
5204     */
5205    public static String left(final String str, final int len) {
5206        if (str == null) {
5207            return null;
5208        }
5209        if (len < 0) {
5210            return EMPTY;
5211        }
5212        if (str.length() <= len) {
5213            return str;
5214        }
5215        return str.substring(0, len);
5216    }
5217
5218    /**
5219     * <p>Left pad a String with spaces (' ').</p>
5220     *
5221     * <p>The String is padded to the size of {@code size}.</p>
5222     *
5223     * <pre>
5224     * StringUtils.leftPad(null, *)   = null
5225     * StringUtils.leftPad("", 3)     = "   "
5226     * StringUtils.leftPad("bat", 3)  = "bat"
5227     * StringUtils.leftPad("bat", 5)  = "  bat"
5228     * StringUtils.leftPad("bat", 1)  = "bat"
5229     * StringUtils.leftPad("bat", -1) = "bat"
5230     * </pre>
5231     *
5232     * @param str  the String to pad out, may be null
5233     * @param size  the size to pad to
5234     * @return left padded String or original String if no padding is necessary,
5235     *  {@code null} if null String input
5236     */
5237    public static String leftPad(final String str, final int size) {
5238        return leftPad(str, size, ' ');
5239    }
5240
5241    /**
5242     * <p>Left pad a String with a specified character.</p>
5243     *
5244     * <p>Pad to a size of {@code size}.</p>
5245     *
5246     * <pre>
5247     * StringUtils.leftPad(null, *, *)     = null
5248     * StringUtils.leftPad("", 3, 'z')     = "zzz"
5249     * StringUtils.leftPad("bat", 3, 'z')  = "bat"
5250     * StringUtils.leftPad("bat", 5, 'z')  = "zzbat"
5251     * StringUtils.leftPad("bat", 1, 'z')  = "bat"
5252     * StringUtils.leftPad("bat", -1, 'z') = "bat"
5253     * </pre>
5254     *
5255     * @param str  the String to pad out, may be null
5256     * @param size  the size to pad to
5257     * @param padChar  the character to pad with
5258     * @return left padded String or original String if no padding is necessary,
5259     *  {@code null} if null String input
5260     * @since 2.0
5261     */
5262    public static String leftPad(final String str, final int size, final char padChar) {
5263        if (str == null) {
5264            return null;
5265        }
5266        final int pads = size - str.length();
5267        if (pads <= 0) {
5268            return str; // returns original String when possible
5269        }
5270        if (pads > PAD_LIMIT) {
5271            return leftPad(str, size, String.valueOf(padChar));
5272        }
5273        return repeat(padChar, pads).concat(str);
5274    }
5275
5276    /**
5277     * <p>Left pad a String with a specified String.</p>
5278     *
5279     * <p>Pad to a size of {@code size}.</p>
5280     *
5281     * <pre>
5282     * StringUtils.leftPad(null, *, *)      = null
5283     * StringUtils.leftPad("", 3, "z")      = "zzz"
5284     * StringUtils.leftPad("bat", 3, "yz")  = "bat"
5285     * StringUtils.leftPad("bat", 5, "yz")  = "yzbat"
5286     * StringUtils.leftPad("bat", 8, "yz")  = "yzyzybat"
5287     * StringUtils.leftPad("bat", 1, "yz")  = "bat"
5288     * StringUtils.leftPad("bat", -1, "yz") = "bat"
5289     * StringUtils.leftPad("bat", 5, null)  = "  bat"
5290     * StringUtils.leftPad("bat", 5, "")    = "  bat"
5291     * </pre>
5292     *
5293     * @param str  the String to pad out, may be null
5294     * @param size  the size to pad to
5295     * @param padStr  the String to pad with, null or empty treated as single space
5296     * @return left padded String or original String if no padding is necessary,
5297     *  {@code null} if null String input
5298     */
5299    public static String leftPad(final String str, final int size, String padStr) {
5300        if (str == null) {
5301            return null;
5302        }
5303        if (isEmpty(padStr)) {
5304            padStr = SPACE;
5305        }
5306        final int padLen = padStr.length();
5307        final int strLen = str.length();
5308        final int pads = size - strLen;
5309        if (pads <= 0) {
5310            return str; // returns original String when possible
5311        }
5312        if (padLen == 1 && pads <= PAD_LIMIT) {
5313            return leftPad(str, size, padStr.charAt(0));
5314        }
5315
5316        if (pads == padLen) {
5317            return padStr.concat(str);
5318        } else if (pads < padLen) {
5319            return padStr.substring(0, pads).concat(str);
5320        } else {
5321            final char[] padding = new char[pads];
5322            final char[] padChars = padStr.toCharArray();
5323            for (int i = 0; i < pads; i++) {
5324                padding[i] = padChars[i % padLen];
5325            }
5326            return new String(padding).concat(str);
5327        }
5328    }
5329
5330    /**
5331     * Gets a CharSequence length or {@code 0} if the CharSequence is
5332     * {@code null}.
5333     *
5334     * @param cs
5335     *            a CharSequence or {@code null}
5336     * @return CharSequence length or {@code 0} if the CharSequence is
5337     *         {@code null}.
5338     * @since 2.4
5339     * @since 3.0 Changed signature from length(String) to length(CharSequence)
5340     */
5341    public static int length(final CharSequence cs) {
5342        return cs == null ? 0 : cs.length();
5343    }
5344
5345    /**
5346     * <p>Converts a String to lower case as per {@link String#toLowerCase()}.</p>
5347     *
5348     * <p>A {@code null} input String returns {@code null}.</p>
5349     *
5350     * <pre>
5351     * StringUtils.lowerCase(null)  = null
5352     * StringUtils.lowerCase("")    = ""
5353     * StringUtils.lowerCase("aBc") = "abc"
5354     * </pre>
5355     *
5356     * <p><strong>Note:</strong> As described in the documentation for {@link String#toLowerCase()},
5357     * the result of this method is affected by the current locale.
5358     * For platform-independent case transformations, the method {@link #lowerCase(String, Locale)}
5359     * should be used with a specific locale (e.g. {@link Locale#ENGLISH}).</p>
5360     *
5361     * @param str  the String to lower case, may be null
5362     * @return the lower cased String, {@code null} if null String input
5363     */
5364    public static String lowerCase(final String str) {
5365        if (str == null) {
5366            return null;
5367        }
5368        return str.toLowerCase();
5369    }
5370
5371    /**
5372     * <p>Converts a String to lower case as per {@link String#toLowerCase(Locale)}.</p>
5373     *
5374     * <p>A {@code null} input String returns {@code null}.</p>
5375     *
5376     * <pre>
5377     * StringUtils.lowerCase(null, Locale.ENGLISH)  = null
5378     * StringUtils.lowerCase("", Locale.ENGLISH)    = ""
5379     * StringUtils.lowerCase("aBc", Locale.ENGLISH) = "abc"
5380     * </pre>
5381     *
5382     * @param str  the String to lower case, may be null
5383     * @param locale  the locale that defines the case transformation rules, must not be null
5384     * @return the lower cased String, {@code null} if null String input
5385     * @since 2.5
5386     */
5387    public static String lowerCase(final String str, final Locale locale) {
5388        if (str == null) {
5389            return null;
5390        }
5391        return str.toLowerCase(locale);
5392    }
5393
5394    private static int[] matches(final CharSequence first, final CharSequence second) {
5395        CharSequence max, min;
5396        if (first.length() > second.length()) {
5397            max = first;
5398            min = second;
5399        } else {
5400            max = second;
5401            min = first;
5402        }
5403        final int range = Math.max(max.length() / 2 - 1, 0);
5404        final int[] matchIndexes = new int[min.length()];
5405        Arrays.fill(matchIndexes, -1);
5406        final boolean[] matchFlags = new boolean[max.length()];
5407        int matches = 0;
5408        for (int mi = 0; mi < min.length(); mi++) {
5409            final char c1 = min.charAt(mi);
5410            for (int xi = Math.max(mi - range, 0), xn = Math.min(mi + range + 1, max.length()); xi < xn; xi++) {
5411                if (!matchFlags[xi] && c1 == max.charAt(xi)) {
5412                    matchIndexes[mi] = xi;
5413                    matchFlags[xi] = true;
5414                    matches++;
5415                    break;
5416                }
5417            }
5418        }
5419        final char[] ms1 = new char[matches];
5420        final char[] ms2 = new char[matches];
5421        for (int i = 0, si = 0; i < min.length(); i++) {
5422            if (matchIndexes[i] != -1) {
5423                ms1[si] = min.charAt(i);
5424                si++;
5425            }
5426        }
5427        for (int i = 0, si = 0; i < max.length(); i++) {
5428            if (matchFlags[i]) {
5429                ms2[si] = max.charAt(i);
5430                si++;
5431            }
5432        }
5433        int transpositions = 0;
5434        for (int mi = 0; mi < ms1.length; mi++) {
5435            if (ms1[mi] != ms2[mi]) {
5436                transpositions++;
5437            }
5438        }
5439        int prefix = 0;
5440        for (int mi = 0; mi < min.length(); mi++) {
5441            if (first.charAt(mi) == second.charAt(mi)) {
5442                prefix++;
5443            } else {
5444                break;
5445            }
5446        }
5447        return new int[] { matches, transpositions / 2, prefix, max.length() };
5448    }
5449
5450    /**
5451     * <p>Gets {@code len} characters from the middle of a String.</p>
5452     *
5453     * <p>If {@code len} characters are not available, the remainder
5454     * of the String will be returned without an exception. If the
5455     * String is {@code null}, {@code null} will be returned.
5456     * An empty String is returned if len is negative or exceeds the
5457     * length of {@code str}.</p>
5458     *
5459     * <pre>
5460     * StringUtils.mid(null, *, *)    = null
5461     * StringUtils.mid(*, *, -ve)     = ""
5462     * StringUtils.mid("", 0, *)      = ""
5463     * StringUtils.mid("abc", 0, 2)   = "ab"
5464     * StringUtils.mid("abc", 0, 4)   = "abc"
5465     * StringUtils.mid("abc", 2, 4)   = "c"
5466     * StringUtils.mid("abc", 4, 2)   = ""
5467     * StringUtils.mid("abc", -2, 2)  = "ab"
5468     * </pre>
5469     *
5470     * @param str  the String to get the characters from, may be null
5471     * @param pos  the position to start from, negative treated as zero
5472     * @param len  the length of the required String
5473     * @return the middle characters, {@code null} if null String input
5474     */
5475    public static String mid(final String str, int pos, final int len) {
5476        if (str == null) {
5477            return null;
5478        }
5479        if (len < 0 || pos > str.length()) {
5480            return EMPTY;
5481        }
5482        if (pos < 0) {
5483            pos = 0;
5484        }
5485        if (str.length() <= pos + len) {
5486            return str.substring(pos);
5487        }
5488        return str.substring(pos, pos + len);
5489    }
5490
5491    private static StringBuilder newStringBuilder(final int noOfItems) {
5492        return new StringBuilder(noOfItems * 16);
5493    }
5494
5495    /**
5496     * <p>
5497     * Similar to <a
5498     * href="http://www.w3.org/TR/xpath/#function-normalize-space">http://www.w3.org/TR/xpath/#function-normalize
5499     * -space</a>
5500     * </p>
5501     * <p>
5502     * The function returns the argument string with whitespace normalized by using
5503     * {@code {@link #trim(String)}} to remove leading and trailing whitespace
5504     * and then replacing sequences of whitespace characters by a single space.
5505     * </p>
5506     * In XML Whitespace characters are the same as those allowed by the <a
5507     * href="http://www.w3.org/TR/REC-xml/#NT-S">S</a> production, which is S ::= (#x20 | #x9 | #xD | #xA)+
5508     * <p>
5509     * Java's regexp pattern \s defines whitespace as [ \t\n\x0B\f\r]
5510     *
5511     * <p>For reference:</p>
5512     * <ul>
5513     * <li>\x0B = vertical tab</li>
5514     * <li>\f = #xC = form feed</li>
5515     * <li>#x20 = space</li>
5516     * <li>#x9 = \t</li>
5517     * <li>#xA = \n</li>
5518     * <li>#xD = \r</li>
5519     * </ul>
5520     *
5521     * <p>
5522     * The difference is that Java's whitespace includes vertical tab and form feed, which this functional will also
5523     * normalize. Additionally {@code {@link #trim(String)}} removes control characters (char &lt;= 32) from both
5524     * ends of this String.
5525     * </p>
5526     *
5527     * @see Pattern
5528     * @see #trim(String)
5529     * @see <a
5530     *      href="http://www.w3.org/TR/xpath/#function-normalize-space">http://www.w3.org/TR/xpath/#function-normalize-space</a>
5531     * @param str the source String to normalize whitespaces from, may be null
5532     * @return the modified string with whitespace normalized, {@code null} if null String input
5533     *
5534     * @since 3.0
5535     */
5536    public static String normalizeSpace(final String str) {
5537        // LANG-1020: Improved performance significantly by normalizing manually instead of using regex
5538        // See https://github.com/librucha/commons-lang-normalizespaces-benchmark for performance test
5539        if (isEmpty(str)) {
5540            return str;
5541        }
5542        final int size = str.length();
5543        final char[] newChars = new char[size];
5544        int count = 0;
5545        int whitespacesCount = 0;
5546        boolean startWhitespaces = true;
5547        for (int i = 0; i < size; i++) {
5548            final char actualChar = str.charAt(i);
5549            final boolean isWhitespace = Character.isWhitespace(actualChar);
5550            if (isWhitespace) {
5551                if (whitespacesCount == 0 && !startWhitespaces) {
5552                    newChars[count++] = SPACE.charAt(0);
5553                }
5554                whitespacesCount++;
5555            } else {
5556                startWhitespaces = false;
5557                newChars[count++] = (actualChar == 160 ? 32 : actualChar);
5558                whitespacesCount = 0;
5559            }
5560        }
5561        if (startWhitespaces) {
5562            return EMPTY;
5563        }
5564        return new String(newChars, 0, count - (whitespacesCount > 0 ? 1 : 0)).trim();
5565    }
5566
5567    /**
5568     * <p>Finds the n-th index within a CharSequence, handling {@code null}.
5569     * This method uses {@link String#indexOf(String)} if possible.</p>
5570     * <p><b>Note:</b> The code starts looking for a match at the start of the target,
5571     * incrementing the starting index by one after each successful match
5572     * (unless {@code searchStr} is an empty string in which case the position
5573     * is never incremented and {@code 0} is returned immediately).
5574     * This means that matches may overlap.</p>
5575     * <p>A {@code null} CharSequence will return {@code -1}.</p>
5576     *
5577     * <pre>
5578     * StringUtils.ordinalIndexOf(null, *, *)          = -1
5579     * StringUtils.ordinalIndexOf(*, null, *)          = -1
5580     * StringUtils.ordinalIndexOf("", "", *)           = 0
5581     * StringUtils.ordinalIndexOf("aabaabaa", "a", 1)  = 0
5582     * StringUtils.ordinalIndexOf("aabaabaa", "a", 2)  = 1
5583     * StringUtils.ordinalIndexOf("aabaabaa", "b", 1)  = 2
5584     * StringUtils.ordinalIndexOf("aabaabaa", "b", 2)  = 5
5585     * StringUtils.ordinalIndexOf("aabaabaa", "ab", 1) = 1
5586     * StringUtils.ordinalIndexOf("aabaabaa", "ab", 2) = 4
5587     * StringUtils.ordinalIndexOf("aabaabaa", "", 1)   = 0
5588     * StringUtils.ordinalIndexOf("aabaabaa", "", 2)   = 0
5589     * </pre>
5590     *
5591     * <p>Matches may overlap:</p>
5592     * <pre>
5593     * StringUtils.ordinalIndexOf("ababab", "aba", 1)   = 0
5594     * StringUtils.ordinalIndexOf("ababab", "aba", 2)   = 2
5595     * StringUtils.ordinalIndexOf("ababab", "aba", 3)   = -1
5596     *
5597     * StringUtils.ordinalIndexOf("abababab", "abab", 1) = 0
5598     * StringUtils.ordinalIndexOf("abababab", "abab", 2) = 2
5599     * StringUtils.ordinalIndexOf("abababab", "abab", 3) = 4
5600     * StringUtils.ordinalIndexOf("abababab", "abab", 4) = -1
5601     * </pre>
5602     *
5603     * <p>Note that 'head(CharSequence str, int n)' may be implemented as: </p>
5604     *
5605     * <pre>
5606     *   str.substring(0, lastOrdinalIndexOf(str, "\n", n))
5607     * </pre>
5608     *
5609     * @param str  the CharSequence to check, may be null
5610     * @param searchStr  the CharSequence to find, may be null
5611     * @param ordinal  the n-th {@code searchStr} to find
5612     * @return the n-th index of the search CharSequence,
5613     *  {@code -1} ({@code INDEX_NOT_FOUND}) if no match or {@code null} string input
5614     * @since 2.1
5615     * @since 3.0 Changed signature from ordinalIndexOf(String, String, int) to ordinalIndexOf(CharSequence, CharSequence, int)
5616     */
5617    public static int ordinalIndexOf(final CharSequence str, final CharSequence searchStr, final int ordinal) {
5618        return ordinalIndexOf(str, searchStr, ordinal, false);
5619    }
5620
5621    /**
5622     * <p>Finds the n-th index within a String, handling {@code null}.
5623     * This method uses {@link String#indexOf(String)} if possible.</p>
5624     * <p>Note that matches may overlap<p>
5625     *
5626     * <p>A {@code null} CharSequence will return {@code -1}.</p>
5627     *
5628     * @param str  the CharSequence to check, may be null
5629     * @param searchStr  the CharSequence to find, may be null
5630     * @param ordinal  the n-th {@code searchStr} to find, overlapping matches are allowed.
5631     * @param lastIndex true if lastOrdinalIndexOf() otherwise false if ordinalIndexOf()
5632     * @return the n-th index of the search CharSequence,
5633     *  {@code -1} ({@code INDEX_NOT_FOUND}) if no match or {@code null} string input
5634     */
5635    // Shared code between ordinalIndexOf(String, String, int) and lastOrdinalIndexOf(String, String, int)
5636    private static int ordinalIndexOf(final CharSequence str, final CharSequence searchStr, final int ordinal, final boolean lastIndex) {
5637        if (str == null || searchStr == null || ordinal <= 0) {
5638            return INDEX_NOT_FOUND;
5639        }
5640        if (searchStr.length() == 0) {
5641            return lastIndex ? str.length() : 0;
5642        }
5643        int found = 0;
5644        // set the initial index beyond the end of the string
5645        // this is to allow for the initial index decrement/increment
5646        int index = lastIndex ? str.length() : INDEX_NOT_FOUND;
5647        do {
5648            if (lastIndex) {
5649                index = CharSequenceUtils.lastIndexOf(str, searchStr, index - 1); // step backwards thru string
5650            } else {
5651                index = CharSequenceUtils.indexOf(str, searchStr, index + 1); // step forwards through string
5652            }
5653            if (index < 0) {
5654                return index;
5655            }
5656            found++;
5657        } while (found < ordinal);
5658        return index;
5659    }
5660
5661    // Overlay
5662    //-----------------------------------------------------------------------
5663    /**
5664     * <p>Overlays part of a String with another String.</p>
5665     *
5666     * <p>A {@code null} string input returns {@code null}.
5667     * A negative index is treated as zero.
5668     * An index greater than the string length is treated as the string length.
5669     * The start index is always the smaller of the two indices.</p>
5670     *
5671     * <pre>
5672     * StringUtils.overlay(null, *, *, *)            = null
5673     * StringUtils.overlay("", "abc", 0, 0)          = "abc"
5674     * StringUtils.overlay("abcdef", null, 2, 4)     = "abef"
5675     * StringUtils.overlay("abcdef", "", 2, 4)       = "abef"
5676     * StringUtils.overlay("abcdef", "", 4, 2)       = "abef"
5677     * StringUtils.overlay("abcdef", "zzzz", 2, 4)   = "abzzzzef"
5678     * StringUtils.overlay("abcdef", "zzzz", 4, 2)   = "abzzzzef"
5679     * StringUtils.overlay("abcdef", "zzzz", -1, 4)  = "zzzzef"
5680     * StringUtils.overlay("abcdef", "zzzz", 2, 8)   = "abzzzz"
5681     * StringUtils.overlay("abcdef", "zzzz", -2, -3) = "zzzzabcdef"
5682     * StringUtils.overlay("abcdef", "zzzz", 8, 10)  = "abcdefzzzz"
5683     * </pre>
5684     *
5685     * @param str  the String to do overlaying in, may be null
5686     * @param overlay  the String to overlay, may be null
5687     * @param start  the position to start overlaying at
5688     * @param end  the position to stop overlaying before
5689     * @return overlayed String, {@code null} if null String input
5690     * @since 2.0
5691     */
5692    public static String overlay(final String str, String overlay, int start, int end) {
5693        if (str == null) {
5694            return null;
5695        }
5696        if (overlay == null) {
5697            overlay = EMPTY;
5698        }
5699        final int len = str.length();
5700        if (start < 0) {
5701            start = 0;
5702        }
5703        if (start > len) {
5704            start = len;
5705        }
5706        if (end < 0) {
5707            end = 0;
5708        }
5709        if (end > len) {
5710            end = len;
5711        }
5712        if (start > end) {
5713            final int temp = start;
5714            start = end;
5715            end = temp;
5716        }
5717        return str.substring(0, start) +
5718            overlay +
5719            str.substring(end);
5720    }
5721
5722    /**
5723     * Prepends the prefix to the start of the string if the string does not
5724     * already start with any of the prefixes.
5725     *
5726     * @param str The string.
5727     * @param prefix The prefix to prepend to the start of the string.
5728     * @param ignoreCase Indicates whether the compare should ignore case.
5729     * @param prefixes Additional prefixes that are valid (optional).
5730     *
5731     * @return A new String if prefix was prepended, the same string otherwise.
5732     */
5733    private static String prependIfMissing(final String str, final CharSequence prefix, final boolean ignoreCase, final CharSequence... prefixes) {
5734        if (str == null || isEmpty(prefix) || startsWith(str, prefix, ignoreCase)) {
5735            return str;
5736        }
5737        if (ArrayUtils.isNotEmpty(prefixes)) {
5738            for (final CharSequence p : prefixes) {
5739                if (startsWith(str, p, ignoreCase)) {
5740                    return str;
5741                }
5742            }
5743        }
5744        return prefix.toString() + str;
5745    }
5746
5747    /**
5748     * Prepends the prefix to the start of the string if the string does not
5749     * already start with any of the prefixes.
5750     *
5751     * <pre>
5752     * StringUtils.prependIfMissing(null, null) = null
5753     * StringUtils.prependIfMissing("abc", null) = "abc"
5754     * StringUtils.prependIfMissing("", "xyz") = "xyz"
5755     * StringUtils.prependIfMissing("abc", "xyz") = "xyzabc"
5756     * StringUtils.prependIfMissing("xyzabc", "xyz") = "xyzabc"
5757     * StringUtils.prependIfMissing("XYZabc", "xyz") = "xyzXYZabc"
5758     * </pre>
5759     * <p>With additional prefixes,</p>
5760     * <pre>
5761     * StringUtils.prependIfMissing(null, null, null) = null
5762     * StringUtils.prependIfMissing("abc", null, null) = "abc"
5763     * StringUtils.prependIfMissing("", "xyz", null) = "xyz"
5764     * StringUtils.prependIfMissing("abc", "xyz", new CharSequence[]{null}) = "xyzabc"
5765     * StringUtils.prependIfMissing("abc", "xyz", "") = "abc"
5766     * StringUtils.prependIfMissing("abc", "xyz", "mno") = "xyzabc"
5767     * StringUtils.prependIfMissing("xyzabc", "xyz", "mno") = "xyzabc"
5768     * StringUtils.prependIfMissing("mnoabc", "xyz", "mno") = "mnoabc"
5769     * StringUtils.prependIfMissing("XYZabc", "xyz", "mno") = "xyzXYZabc"
5770     * StringUtils.prependIfMissing("MNOabc", "xyz", "mno") = "xyzMNOabc"
5771     * </pre>
5772     *
5773     * @param str The string.
5774     * @param prefix The prefix to prepend to the start of the string.
5775     * @param prefixes Additional prefixes that are valid.
5776     *
5777     * @return A new String if prefix was prepended, the same string otherwise.
5778     *
5779     * @since 3.2
5780     */
5781    public static String prependIfMissing(final String str, final CharSequence prefix, final CharSequence... prefixes) {
5782        return prependIfMissing(str, prefix, false, prefixes);
5783    }
5784
5785    /**
5786     * Prepends the prefix to the start of the string if the string does not
5787     * already start, case insensitive, with any of the prefixes.
5788     *
5789     * <pre>
5790     * StringUtils.prependIfMissingIgnoreCase(null, null) = null
5791     * StringUtils.prependIfMissingIgnoreCase("abc", null) = "abc"
5792     * StringUtils.prependIfMissingIgnoreCase("", "xyz") = "xyz"
5793     * StringUtils.prependIfMissingIgnoreCase("abc", "xyz") = "xyzabc"
5794     * StringUtils.prependIfMissingIgnoreCase("xyzabc", "xyz") = "xyzabc"
5795     * StringUtils.prependIfMissingIgnoreCase("XYZabc", "xyz") = "XYZabc"
5796     * </pre>
5797     * <p>With additional prefixes,</p>
5798     * <pre>
5799     * StringUtils.prependIfMissingIgnoreCase(null, null, null) = null
5800     * StringUtils.prependIfMissingIgnoreCase("abc", null, null) = "abc"
5801     * StringUtils.prependIfMissingIgnoreCase("", "xyz", null) = "xyz"
5802     * StringUtils.prependIfMissingIgnoreCase("abc", "xyz", new CharSequence[]{null}) = "xyzabc"
5803     * StringUtils.prependIfMissingIgnoreCase("abc", "xyz", "") = "abc"
5804     * StringUtils.prependIfMissingIgnoreCase("abc", "xyz", "mno") = "xyzabc"
5805     * StringUtils.prependIfMissingIgnoreCase("xyzabc", "xyz", "mno") = "xyzabc"
5806     * StringUtils.prependIfMissingIgnoreCase("mnoabc", "xyz", "mno") = "mnoabc"
5807     * StringUtils.prependIfMissingIgnoreCase("XYZabc", "xyz", "mno") = "XYZabc"
5808     * StringUtils.prependIfMissingIgnoreCase("MNOabc", "xyz", "mno") = "MNOabc"
5809     * </pre>
5810     *
5811     * @param str The string.
5812     * @param prefix The prefix to prepend to the start of the string.
5813     * @param prefixes Additional prefixes that are valid (optional).
5814     *
5815     * @return A new String if prefix was prepended, the same string otherwise.
5816     *
5817     * @since 3.2
5818     */
5819    public static String prependIfMissingIgnoreCase(final String str, final CharSequence prefix, final CharSequence... prefixes) {
5820        return prependIfMissing(str, prefix, true, prefixes);
5821    }
5822
5823    /**
5824     * <p>Removes all occurrences of a character from within the source string.</p>
5825     *
5826     * <p>A {@code null} source string will return {@code null}.
5827     * An empty ("") source string will return the empty string.</p>
5828     *
5829     * <pre>
5830     * StringUtils.remove(null, *)       = null
5831     * StringUtils.remove("", *)         = ""
5832     * StringUtils.remove("queued", 'u') = "qeed"
5833     * StringUtils.remove("queued", 'z') = "queued"
5834     * </pre>
5835     *
5836     * @param str  the source String to search, may be null
5837     * @param remove  the char to search for and remove, may be null
5838     * @return the substring with the char removed if found,
5839     *  {@code null} if null String input
5840     * @since 2.1
5841     */
5842    public static String remove(final String str, final char remove) {
5843        if (isEmpty(str) || str.indexOf(remove) == INDEX_NOT_FOUND) {
5844            return str;
5845        }
5846        final char[] chars = str.toCharArray();
5847        int pos = 0;
5848        for (int i = 0; i < chars.length; i++) {
5849            if (chars[i] != remove) {
5850                chars[pos++] = chars[i];
5851            }
5852        }
5853        return new String(chars, 0, pos);
5854    }
5855
5856    /**
5857     * <p>Removes all occurrences of a substring from within the source string.</p>
5858     *
5859     * <p>A {@code null} source string will return {@code null}.
5860     * An empty ("") source string will return the empty string.
5861     * A {@code null} remove string will return the source string.
5862     * An empty ("") remove string will return the source string.</p>
5863     *
5864     * <pre>
5865     * StringUtils.remove(null, *)        = null
5866     * StringUtils.remove("", *)          = ""
5867     * StringUtils.remove(*, null)        = *
5868     * StringUtils.remove(*, "")          = *
5869     * StringUtils.remove("queued", "ue") = "qd"
5870     * StringUtils.remove("queued", "zz") = "queued"
5871     * </pre>
5872     *
5873     * @param str  the source String to search, may be null
5874     * @param remove  the String to search for and remove, may be null
5875     * @return the substring with the string removed if found,
5876     *  {@code null} if null String input
5877     * @since 2.1
5878     */
5879    public static String remove(final String str, final String remove) {
5880        if (isEmpty(str) || isEmpty(remove)) {
5881            return str;
5882        }
5883        return replace(str, remove, EMPTY, -1);
5884    }
5885
5886    /**
5887     * <p>Removes each substring of the text String that matches the given regular expression.</p>
5888     *
5889     * This method is a {@code null} safe equivalent to:
5890     * <ul>
5891     *  <li>{@code text.replaceAll(regex, StringUtils.EMPTY)}</li>
5892     *  <li>{@code Pattern.compile(regex).matcher(text).replaceAll(StringUtils.EMPTY)}</li>
5893     * </ul>
5894     *
5895     * <p>A {@code null} reference passed to this method is a no-op.</p>
5896     *
5897     * <p>Unlike in the {@link #removePattern(String, String)} method, the {@link Pattern#DOTALL} option
5898     * is NOT automatically added.
5899     * To use the DOTALL option prepend {@code "(?s)"} to the regex.
5900     * DOTALL is also known as single-line mode in Perl.</p>
5901     *
5902     * <pre>
5903     * StringUtils.removeAll(null, *)      = null
5904     * StringUtils.removeAll("any", (String) null)  = "any"
5905     * StringUtils.removeAll("any", "")    = "any"
5906     * StringUtils.removeAll("any", ".*")  = ""
5907     * StringUtils.removeAll("any", ".+")  = ""
5908     * StringUtils.removeAll("abc", ".?")  = ""
5909     * StringUtils.removeAll("A&lt;__&gt;\n&lt;__&gt;B", "&lt;.*&gt;")      = "A\nB"
5910     * StringUtils.removeAll("A&lt;__&gt;\n&lt;__&gt;B", "(?s)&lt;.*&gt;")  = "AB"
5911     * StringUtils.removeAll("ABCabc123abc", "[a-z]")     = "ABC123"
5912     * </pre>
5913     *
5914     * @param text  text to remove from, may be null
5915     * @param regex  the regular expression to which this string is to be matched
5916     * @return  the text with any removes processed,
5917     *              {@code null} if null String input
5918     *
5919     * @throws  java.util.regex.PatternSyntaxException
5920     *              if the regular expression's syntax is invalid
5921     *
5922     * @see #replaceAll(String, String, String)
5923     * @see #removePattern(String, String)
5924     * @see String#replaceAll(String, String)
5925     * @see java.util.regex.Pattern
5926     * @see java.util.regex.Pattern#DOTALL
5927     * @since 3.5
5928     *
5929     * @deprecated Moved to RegExUtils.
5930     */
5931    @Deprecated
5932    public static String removeAll(final String text, final String regex) {
5933        return RegExUtils.removeAll(text, regex);
5934    }
5935
5936    /**
5937     * <p>Removes a substring only if it is at the end of a source string,
5938     * otherwise returns the source string.</p>
5939     *
5940     * <p>A {@code null} source string will return {@code null}.
5941     * An empty ("") source string will return the empty string.
5942     * A {@code null} search string will return the source string.</p>
5943     *
5944     * <pre>
5945     * StringUtils.removeEnd(null, *)      = null
5946     * StringUtils.removeEnd("", *)        = ""
5947     * StringUtils.removeEnd(*, null)      = *
5948     * StringUtils.removeEnd("www.domain.com", ".com.")  = "www.domain.com"
5949     * StringUtils.removeEnd("www.domain.com", ".com")   = "www.domain"
5950     * StringUtils.removeEnd("www.domain.com", "domain") = "www.domain.com"
5951     * StringUtils.removeEnd("abc", "")    = "abc"
5952     * </pre>
5953     *
5954     * @param str  the source String to search, may be null
5955     * @param remove  the String to search for and remove, may be null
5956     * @return the substring with the string removed if found,
5957     *  {@code null} if null String input
5958     * @since 2.1
5959     */
5960    public static String removeEnd(final String str, final String remove) {
5961        if (isEmpty(str) || isEmpty(remove)) {
5962            return str;
5963        }
5964        if (str.endsWith(remove)) {
5965            return str.substring(0, str.length() - remove.length());
5966        }
5967        return str;
5968    }
5969
5970    /**
5971     * <p>Case insensitive removal of a substring if it is at the end of a source string,
5972     * otherwise returns the source string.</p>
5973     *
5974     * <p>A {@code null} source string will return {@code null}.
5975     * An empty ("") source string will return the empty string.
5976     * A {@code null} search string will return the source string.</p>
5977     *
5978     * <pre>
5979     * StringUtils.removeEndIgnoreCase(null, *)      = null
5980     * StringUtils.removeEndIgnoreCase("", *)        = ""
5981     * StringUtils.removeEndIgnoreCase(*, null)      = *
5982     * StringUtils.removeEndIgnoreCase("www.domain.com", ".com.")  = "www.domain.com"
5983     * StringUtils.removeEndIgnoreCase("www.domain.com", ".com")   = "www.domain"
5984     * StringUtils.removeEndIgnoreCase("www.domain.com", "domain") = "www.domain.com"
5985     * StringUtils.removeEndIgnoreCase("abc", "")    = "abc"
5986     * StringUtils.removeEndIgnoreCase("www.domain.com", ".COM") = "www.domain")
5987     * StringUtils.removeEndIgnoreCase("www.domain.COM", ".com") = "www.domain")
5988     * </pre>
5989     *
5990     * @param str  the source String to search, may be null
5991     * @param remove  the String to search for (case insensitive) and remove, may be null
5992     * @return the substring with the string removed if found,
5993     *  {@code null} if null String input
5994     * @since 2.4
5995     */
5996    public static String removeEndIgnoreCase(final String str, final String remove) {
5997        if (isEmpty(str) || isEmpty(remove)) {
5998            return str;
5999        }
6000        if (endsWithIgnoreCase(str, remove)) {
6001            return str.substring(0, str.length() - remove.length());
6002        }
6003        return str;
6004    }
6005
6006    /**
6007     * <p>Removes the first substring of the text string that matches the given regular expression.</p>
6008     *
6009     * This method is a {@code null} safe equivalent to:
6010     * <ul>
6011     *  <li>{@code text.replaceFirst(regex, StringUtils.EMPTY)}</li>
6012     *  <li>{@code Pattern.compile(regex).matcher(text).replaceFirst(StringUtils.EMPTY)}</li>
6013     * </ul>
6014     *
6015     * <p>A {@code null} reference passed to this method is a no-op.</p>
6016     *
6017     * <p>The {@link Pattern#DOTALL} option is NOT automatically added.
6018     * To use the DOTALL option prepend {@code "(?s)"} to the regex.
6019     * DOTALL is also known as single-line mode in Perl.</p>
6020     *
6021     * <pre>
6022     * StringUtils.removeFirst(null, *)      = null
6023     * StringUtils.removeFirst("any", (String) null)  = "any"
6024     * StringUtils.removeFirst("any", "")    = "any"
6025     * StringUtils.removeFirst("any", ".*")  = ""
6026     * StringUtils.removeFirst("any", ".+")  = ""
6027     * StringUtils.removeFirst("abc", ".?")  = "bc"
6028     * StringUtils.removeFirst("A&lt;__&gt;\n&lt;__&gt;B", "&lt;.*&gt;")      = "A\n&lt;__&gt;B"
6029     * StringUtils.removeFirst("A&lt;__&gt;\n&lt;__&gt;B", "(?s)&lt;.*&gt;")  = "AB"
6030     * StringUtils.removeFirst("ABCabc123", "[a-z]")          = "ABCbc123"
6031     * StringUtils.removeFirst("ABCabc123abc", "[a-z]+")      = "ABC123abc"
6032     * </pre>
6033     *
6034     * @param text  text to remove from, may be null
6035     * @param regex  the regular expression to which this string is to be matched
6036     * @return  the text with the first replacement processed,
6037     *              {@code null} if null String input
6038     *
6039     * @throws  java.util.regex.PatternSyntaxException
6040     *              if the regular expression's syntax is invalid
6041     *
6042     * @see #replaceFirst(String, String, String)
6043     * @see String#replaceFirst(String, String)
6044     * @see java.util.regex.Pattern
6045     * @see java.util.regex.Pattern#DOTALL
6046     * @since 3.5
6047     *
6048     * @deprecated Moved to RegExUtils.
6049     */
6050    @Deprecated
6051    public static String removeFirst(final String text, final String regex) {
6052        return replaceFirst(text, regex, EMPTY);
6053    }
6054
6055    /**
6056     * <p>
6057     * Case insensitive removal of all occurrences of a substring from within
6058     * the source string.
6059     * </p>
6060     *
6061     * <p>
6062     * A {@code null} source string will return {@code null}. An empty ("")
6063     * source string will return the empty string. A {@code null} remove string
6064     * will return the source string. An empty ("") remove string will return
6065     * the source string.
6066     * </p>
6067     *
6068     * <pre>
6069     * StringUtils.removeIgnoreCase(null, *)        = null
6070     * StringUtils.removeIgnoreCase("", *)          = ""
6071     * StringUtils.removeIgnoreCase(*, null)        = *
6072     * StringUtils.removeIgnoreCase(*, "")          = *
6073     * StringUtils.removeIgnoreCase("queued", "ue") = "qd"
6074     * StringUtils.removeIgnoreCase("queued", "zz") = "queued"
6075     * StringUtils.removeIgnoreCase("quEUed", "UE") = "qd"
6076     * StringUtils.removeIgnoreCase("queued", "zZ") = "queued"
6077     * </pre>
6078     *
6079     * @param str
6080     *            the source String to search, may be null
6081     * @param remove
6082     *            the String to search for (case insensitive) and remove, may be
6083     *            null
6084     * @return the substring with the string removed if found, {@code null} if
6085     *         null String input
6086     * @since 3.5
6087     */
6088    public static String removeIgnoreCase(final String str, final String remove) {
6089        if (isEmpty(str) || isEmpty(remove)) {
6090            return str;
6091        }
6092        return replaceIgnoreCase(str, remove, EMPTY, -1);
6093    }
6094
6095  }