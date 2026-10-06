package org.connecttag.lib.securestorage

import org.connecttag.lib.error.DataFailure
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SecureCredentialContractsTest {
    @Test
    fun tokenVaultReadsAndWritesThroughPortableContract() {
        val storage = FakeSecureStringVault()
        val vault = StringVaultTokenVault(
            vault = storage,
            accessTokenKey = CredentialKey("access"),
            refreshTokenKey = CredentialKey("refresh"),
        )

        assertTrue(vault.writeTokens(TokenWriteRequest("a", "r")).isSuccess)
        assertEquals(TokenSnapshot("a", "r"), vault.readTokens().getOrThrow())
        assertTrue(vault.clearTokens().isSuccess)
        assertEquals(TokenSnapshot(), vault.readTokens().getOrThrow())
    }

    @Test
    fun platformFailureIsReturnedAsDataFailureWithoutPlatformCause() {
        val failure = DataFailure.LocalStorage("SECURE_STORAGE_WRITE_FAILED")
        val storage = FakeSecureStringVault(writeFailure = failure)
        val vault = StringVaultTokenVault(
            vault = storage,
            accessTokenKey = CredentialKey("access"),
            refreshTokenKey = CredentialKey("refresh"),
        )

        val result = vault.writeTokens(TokenWriteRequest("a"))

        assertIs<DataFailure.LocalStorage>(result.exceptionOrNull())
        assertEquals("SECURE_STORAGE_WRITE_FAILED", (result.exceptionOrNull() as DataFailure).failureCode)
        assertEquals(null, result.exceptionOrNull()?.cause)
    }

    @Test
    fun credentialStoreKeepsKeysInsideItsNamespace() {
        val storage = FakeSecureStringVault()
        val account = StringVaultCredentialStore(storage, "account.")
        val other = StringVaultCredentialStore(storage, "other.")
        val key = CredentialKey("pin")

        assertTrue(account.write(key, "1234").isSuccess)
        assertTrue(other.write(key, "5678").isSuccess)
        assertEquals("1234", account.read(key).getOrThrow())
        assertEquals("5678", other.read(key).getOrThrow())

        assertTrue(account.clearNamespace().isSuccess)
        assertEquals(null, account.read(key).getOrThrow())
        assertEquals("5678", other.read(key).getOrThrow())
    }

    private class FakeSecureStringVault(
        private val writeFailure: DataFailure? = null,
    ) : PlatformSecureStringVault {
        private val values = mutableMapOf<String, String>()

        override fun readString(key: String): Result<String?> = Result.success(values[key])

        override fun writeString(key: String, value: String): Result<Unit> =
            writeStrings(mapOf(key to value))

        override fun writeStrings(values: Map<String, String>, removeKeys: Set<String>): Result<Unit> {
            writeFailure?.let { return Result.failure(it) }
            removeKeys.forEach(this.values::remove)
            this.values.putAll(values)
            return Result.success(Unit)
        }

        override fun removeString(key: String): Result<Unit> {
            values.remove(key)
            return Result.success(Unit)
        }

        override fun clearByPrefix(keyPrefix: String): Result<Unit> {
            values.keys.filter { it.startsWith(keyPrefix) }.forEach(values::remove)
            return Result.success(Unit)
        }
    }
}
