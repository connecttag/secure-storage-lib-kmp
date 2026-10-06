package org.connecttag.lib.securestorage

import org.connecttag.lib.storage.StorageClassification
import org.connecttag.lib.storage.StorageKind
import org.connecttag.lib.storage.StorageSpec

data class EncryptedStorageSpec(
    override val name: String,
    val keyAlias: String,
) : StorageSpec {
    override val kind: StorageKind = StorageKind.SecureVault
    override val classification: StorageClassification = StorageClassification.SecretSession
    override val encrypted: Boolean = true
}

/** Platform binding that creates an encrypted vault without leaking platform runtime types to storage owners. */
interface StorageEncryptionBinding {
    fun createStringVault(spec: EncryptedStorageSpec): PlatformSecureStringVault
}
