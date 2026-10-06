package org.connecttag.lib.securestorage

import kotlin.jvm.JvmInline

@JvmInline
value class CredentialKey(val value: String) {
    init {
        require(value.isNotBlank()) { "Credential key must not be blank" }
    }
}

data class TokenSnapshot(
    val accessToken: String? = null,
    val refreshToken: String? = null,
)

data class TokenWriteRequest(
    val accessToken: String,
    val refreshToken: String? = null,
    val replaceRefreshToken: Boolean = refreshToken != null,
)

interface TokenVault {
    fun readTokens(): Result<TokenSnapshot>
    fun writeTokens(request: TokenWriteRequest): Result<Unit>
    fun clearTokens(): Result<Unit>
}

interface CredentialStore {
    fun read(key: CredentialKey): Result<String?>
    fun write(key: CredentialKey, value: String): Result<Unit>
    fun remove(key: CredentialKey): Result<Unit>
    fun clearNamespace(): Result<Unit>
}

class StringVaultTokenVault(
    private val vault: PlatformSecureStringVault,
    private val accessTokenKey: CredentialKey,
    private val refreshTokenKey: CredentialKey,
) : TokenVault {
    override fun readTokens(): Result<TokenSnapshot> = vault.readString(accessTokenKey.value).fold(
        onSuccess = { accessToken ->
            vault.readString(refreshTokenKey.value).map { refreshToken ->
                TokenSnapshot(accessToken = accessToken, refreshToken = refreshToken)
            }
        },
        onFailure = { Result.failure(it) },
    )

    override fun writeTokens(request: TokenWriteRequest): Result<Unit> {
        val values = buildMap {
            if (request.accessToken.isNotBlank()) put(accessTokenKey.value, request.accessToken)
            if (request.replaceRefreshToken && !request.refreshToken.isNullOrBlank()) {
                put(refreshTokenKey.value, request.refreshToken)
            }
        }
        val removeKeys = buildSet {
            if (request.accessToken.isBlank()) add(accessTokenKey.value)
            if (request.replaceRefreshToken && request.refreshToken.isNullOrBlank()) add(refreshTokenKey.value)
        }
        return vault.writeStrings(values, removeKeys)
    }

    override fun clearTokens(): Result<Unit> {
        return vault.writeStrings(emptyMap(), setOf(accessTokenKey.value, refreshTokenKey.value))
    }
}

class StringVaultCredentialStore(
    private val vault: PlatformSecureStringVault,
    private val keyPrefix: String,
) : CredentialStore {
    override fun read(key: CredentialKey): Result<String?> = vault.readString(namespaced(key))

    override fun write(key: CredentialKey, value: String): Result<Unit> = vault.writeString(namespaced(key), value)

    override fun remove(key: CredentialKey): Result<Unit> = vault.removeString(namespaced(key))

    override fun clearNamespace(): Result<Unit> = vault.clearByPrefix(keyPrefix)

    private fun namespaced(key: CredentialKey): String = "$keyPrefix${key.value}"
}
