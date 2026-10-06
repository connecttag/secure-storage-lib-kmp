package org.connecttag.lib.securestorage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.connecttag.lib.error.DataFailure
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

public class AndroidKeystoreSecureStringVault(
    context: Context,
    preferencesName: String,
    private val keyAlias: String,
) : PlatformSecureStringVault {
    private val sharedPreferences = context.applicationContext
        .getSharedPreferences(requireIdentifier(preferencesName, "preferencesName"), Context.MODE_PRIVATE)

    init {
        requireIdentifier(keyAlias, "keyAlias")
    }

    override fun readString(key: String): Result<String?> {
        return runCatching {
            val encryptedValue = sharedPreferences.getString(requireIdentifier(key, "key"), null)
                ?: return@runCatching null
            decrypt(encryptedValue)
        }.mapSecureStorageFailure("SECURE_STORAGE_READ_FAILED")
    }

    override fun writeString(key: String, value: String): Result<Unit> {
        return writeStrings(values = mapOf(key to value))
    }

    override fun writeStrings(values: Map<String, String>, removeKeys: Set<String>): Result<Unit> {
        return runCatching {
            val encryptedValues = values.mapKeys { (key, _) ->
                requireIdentifier(key, "key")
            }.mapValues { (_, value) ->
                encrypt(value)
            }
            val normalizedRemoveKeys = removeKeys.map { key ->
                requireIdentifier(key, "key")
            }
            val committed = sharedPreferences.edit().apply {
                normalizedRemoveKeys.forEach(::remove)
                encryptedValues.forEach { (key, encryptedValue) ->
                    putString(key, encryptedValue)
                }
            }.commit()
            check(committed) { "Secure storage commit failed" }
        }.mapSecureStorageFailure("SECURE_STORAGE_WRITE_FAILED")
    }

    override fun removeString(key: String): Result<Unit> = runCatching {
        val committed = sharedPreferences.edit()
            .remove(requireIdentifier(key, "key"))
            .commit()
        check(committed) { "Secure storage remove failed" }
    }.mapSecureStorageFailure("SECURE_STORAGE_REMOVE_FAILED")

    override fun clearByPrefix(keyPrefix: String): Result<Unit> = runCatching {
        val normalizedPrefix = requireIdentifier(keyPrefix, "keyPrefix")
        val committed = sharedPreferences.edit().apply {
            sharedPreferences.all.keys
                .filter { key -> key.startsWith(normalizedPrefix) }
                .forEach { key -> remove(key) }
        }.commit()
        check(committed) { "Secure storage namespace clear failed" }
    }.mapSecureStorageFailure("SECURE_STORAGE_CLEAR_FAILED")

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val encryptedBytes = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return "${cipher.iv.encodeBase64()}:${encryptedBytes.encodeBase64()}"
    }

    private fun decrypt(value: String): String? {
        val parts = value.split(":")
        if (parts.size != 2) return null

        val iv = parts[0].decodeBase64()
        val encryptedBytes = parts[1].decodeBase64()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(TAG_LENGTH_BITS, iv))
        return cipher.doFinal(encryptedBytes).toString(Charsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
        val existingKey = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
        if (existingKey != null) return existingKey.secretKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keySpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private fun ByteArray.encodeBase64(): String {
        return Base64.encodeToString(this, Base64.NO_WRAP)
    }

    private fun String.decodeBase64(): ByteArray {
        return Base64.decode(this, Base64.NO_WRAP)
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_LENGTH_BITS = 128
    }
}

private fun requireIdentifier(value: String, parameterName: String): String {
    require(value.isNotBlank()) { "$parameterName must not be blank." }
    return value
}

private fun <T> Result<T>.mapSecureStorageFailure(code: String): Result<T> = fold(
    onSuccess = { Result.success(it) },
    onFailure = { Result.failure(DataFailure.LocalStorage(code)) },
)
