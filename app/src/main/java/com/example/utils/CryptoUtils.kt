package com.example.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private const val ALGORITHM = "AES"
    private val KEY = "NyAsUpAsHtHiTi12".toByteArray(Charsets.UTF_8) // 16 bytes key

    fun encrypt(data: String): String {
        return try {
            val keySpec = SecretKeySpec(KEY, ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP).trim()
        } catch (e: Exception) {
            e.printStackTrace()
            Base64.encodeToString(data.toByteArray(Charsets.UTF_8), Base64.NO_WRAP).trim()
        }
    }

    fun decrypt(encryptedData: String): String {
        return try {
            val keySpec = SecretKeySpec(KEY, ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(encryptedData, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                String(Base64.decode(encryptedData, Base64.NO_WRAP), Charsets.UTF_8)
            } catch (ex: Exception) {
                encryptedData
            }
        }
    }
}
