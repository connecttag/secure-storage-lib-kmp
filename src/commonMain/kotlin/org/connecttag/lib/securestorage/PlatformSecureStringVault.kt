package org.connecttag.lib.securestorage

public interface PlatformSecureStringVault {
    public fun readString(key: String): Result<String?>
    public fun writeString(key: String, value: String): Result<Unit>
    public fun writeStrings(values: Map<String, String>, removeKeys: Set<String> = emptySet()): Result<Unit>
    public fun removeString(key: String): Result<Unit>
    public fun clearByPrefix(keyPrefix: String): Result<Unit>
}
