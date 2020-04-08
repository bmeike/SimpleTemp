/*
   Copyright 2020, G. Blake Meike
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package net.callmeike.android.simpletemp.crypto

import net.callmeike.android.simpletemp.model.App
import java.util.Locale
import java.util.Random


interface Auth {
    fun getHashedAppId(): String

    suspend fun init(password: CharArray): App
    suspend fun validate(app: App, password: CharArray): Boolean

    suspend fun hash(text: String): String
}

object AuthUtil {
    const val ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val NUMERIC = "0123456789"

    val alphameric = NUMERIC + ALPHA + ALPHA.toLowerCase(Locale.ROOT)

    val random = Random()

    private val chars = alphameric.toCharArray()

    fun randomString(len: Int) = String(
        randomChars(len)
    )

    fun randomChars(len: Int): CharArray {
        val buf = CharArray(len)
        for (idx in buf.indices) {
            buf[idx] = chars[random.nextInt(chars.size)]
        }
        return buf
    }
}
