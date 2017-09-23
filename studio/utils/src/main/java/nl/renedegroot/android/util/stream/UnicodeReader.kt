/* Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.renedegroot.android.util.stream

import java.io.*

private const val BOM_SIZE = 4

/**
 * Construct UnicodeReader
 *
 * @param inputStream            Input stream.
 * @param defaultEncoding Default encoding to be used if BOM is not found, or `null` to use system
 * default encoding.
 * @throws IOException If an I/O error occurs.
 */
class UnicodeReader(inputStream: InputStream, defaultEncoding: String? = null) : Reader() {

    private val reader: InputStreamReader

    init {
        val bom = ByteArray(BOM_SIZE)
        val encoding: String?
        val unread: Int
        val pushbackStream = PushbackInputStream(inputStream, BOM_SIZE)
        val n = pushbackStream.read(bom, 0, bom.size)

        // Read ahead four bytes and check for BOM marks.
        if (bom[0] == 0xEF.toByte() && bom[1] == 0xBB.toByte() && bom[2] == 0xBF.toByte()) {
            encoding = "UTF-8"
            unread = n - 3
        } else if (bom[0] == 0xFE.toByte() && bom[1] == 0xFF.toByte()) {
            encoding = "UTF-16BE"
            unread = n - 2
        } else if (bom[0] == 0xFF.toByte() && bom[1] == 0xFE.toByte()) {
            encoding = "UTF-16LE"
            unread = n - 2
        } else if (bom[0] == 0x00.toByte() && bom[1] == 0x00.toByte() && bom[2] == 0xFE.toByte() && bom[3] == 0xFF.toByte()) {
            encoding = "UTF-32BE"
            unread = n - 4
        } else if (bom[0] == 0xFF.toByte() && bom[1] == 0xFE.toByte() && bom[2] == 0x00.toByte() && bom[3] == 0x00.toByte()) {
            encoding = "UTF-32LE"
            unread = n - 4
        } else {
            encoding = defaultEncoding
            unread = n
        }

        // Unread bytes if necessary and skip BOM marks.
        if (unread > 0) {
            pushbackStream.unread(bom, n - unread, unread)
        } else if (unread < -1) {
            pushbackStream.unread(bom, 0, 0)
        }

        // Use given encoding.
        if (encoding == null) {
            reader = InputStreamReader(pushbackStream)
        } else {
            reader = InputStreamReader(pushbackStream, encoding)
        }
    }

    @Throws(IOException::class)
    override fun close() {
        reader.close()
    }

    @Throws(IOException::class)
    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        return reader.read(cbuf, off, len)
    }
}

