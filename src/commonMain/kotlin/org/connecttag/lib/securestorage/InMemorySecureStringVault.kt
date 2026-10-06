package org.connecttag.lib.securestorage

/**
 * An in-memory implementation of [PlatformSecureStringVault] suitable for
 * testing, previews, and environments without hardware keystore support.
 */
class InMemorySecureStringVault : PlatformSecureStringVault {
    private val values = mutableMapOf<String, String>()

    override fun readString(key: String): Result<String?> = Result.success(values[key])

    override fun writeString(key: String, value: String): Result<Unit> =
        writeStrings(mapOf(key to value))

    override fun writeStrings(values: Map<String, String>, removeKeys: Set<String>): Result<Unit> {
        removeKeys.forEach(this.values::remove)
        this.values.putAll(values)
        return Result.success(Unit)
    }

    override fun removeString(key: String): Result<Unit> {
        values.remove(key)
        return Result.success(Unit)
    }

    override fun clearByPrefix(keyPrefix: String): Result<Unit> {
        val keysToRemove = values.keys.filter { it.startsWith(keyPrefix) }.toList()
        keysToRemove.forEach(values::remove)
        return Result.success(Unit)
    }
}
