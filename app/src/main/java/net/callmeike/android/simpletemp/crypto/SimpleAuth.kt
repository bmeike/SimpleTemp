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

import android.util.Base64
import net.callmeike.android.simpletemp.model.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.CharBuffer
import java.security.GeneralSecurityException
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

// extension function to convert a CharArray to a ByteArray
fun CharArray.toByteArray(): ByteArray {
    val byteBuffer = Charsets.UTF_8.encode(CharBuffer.wrap(this))
    return Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit())
}


/**
 * This code attempts to anonymize the data shared by this app
 * I am not a security guy and cannot attest to their trustworthiness.
 */
@Singleton
class SimpleAuth @Inject constructor() : Auth {
    companion object {
        private const val TAG = "AUTH"

        private const val ID_LEN = 64
        private const val SALT_LEN = 16
        private const val KEY_LEN = 128

        private const val ITERATIONS = 64
        private const val ENCRYPT_ALGO = "AES/CBC/PKCS5PADDING"
        private const val KEY_SPEC_ALGO = "PBKDF2withHmacSHA1"
        private const val KEY_ALGO = "AES"
    }

    private lateinit var appKey: SecretKeySpec
    private lateinit var appInitVec: IvParameterSpec
    private lateinit var hashedApplicationId: String

    override fun getHashedAppId(): String = hashedApplicationId

    override suspend fun init(password: CharArray) = withContext(Dispatchers.Default) {
        val salt = ByteArray(SALT_LEN)
        AuthUtil.random.nextBytes(salt)

        val iv = ByteArray(16)
        AuthUtil.random.nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        val key = createKey(password, salt)

        val encryptedPwd = encrypt(password, key, ivSpec)
        Arrays.fill(password, ' ')

        val id = AuthUtil.randomChars(ID_LEN)

        val app = App(salt, iv, String(id), encryptedPwd)

        appKey = key
        appInitVec = ivSpec
        hashedApplicationId = encrypt(id, key, ivSpec)

        return@withContext app
    }

    override suspend fun validate(app: App, password: CharArray): Boolean = withContext(Dispatchers.Default) {
        val key = createKey(password, app.salt)

        val ivSpec = IvParameterSpec(app.iv)

        val pwd: CharArray? = try {
            decrypt(app.password, key, ivSpec).toCharArray()
        } catch (_: GeneralSecurityException) {
            null
        }

        if (pwd?.contentEquals(password) != true) {
            return@withContext false
        }
        Arrays.fill(password, ' ')

        appKey = key
        appInitVec = ivSpec
        hashedApplicationId = encrypt(app.id.toCharArray(), key, ivSpec)

        return@withContext true
    }

    override suspend fun hash(text: String) = encrypt(text.toCharArray(), appKey, appInitVec)

    private fun encrypt(text: CharArray, key: SecretKeySpec, ivSpec: IvParameterSpec): String {
        val cipher = Cipher.getInstance(ENCRYPT_ALGO)
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
        return Base64.encodeToString(cipher.doFinal(text.toByteArray()), Base64.DEFAULT)
    }

    private fun decrypt(cyphertext: String, key: SecretKeySpec, ivSpec: IvParameterSpec): String {
        val cipher = Cipher.getInstance(ENCRYPT_ALGO)
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        return cipher.doFinal(Base64.decode(cyphertext, Base64.DEFAULT)).toString(Charsets.UTF_8)
    }

    private fun createKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val pbKeySpec = PBEKeySpec(password, salt, ITERATIONS, KEY_LEN)
        val keyFactory = SecretKeyFactory.getInstance(KEY_SPEC_ALGO)
        val key = keyFactory.generateSecret(pbKeySpec).encoded
        return SecretKeySpec(key, KEY_ALGO)
    }
}
