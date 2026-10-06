package org.connecttag.lib.storage

enum class StorageKind {
    Database,
    PreferencesDataStore,
    SecureVault,
}

enum class StorageClassification {
    FlagsMetadata,
    SnapshotCacheProjection,
    SecretSession,
    UserLocalState,
}

/** Stable, platform-neutral identity and policy for one persistent store. */
interface StorageSpec {
    val name: String
    val kind: StorageKind
    val classification: StorageClassification
    val schemaVersion: Int?
        get() = null
    val rebuildable: Boolean
        get() = false
    val encrypted: Boolean
        get() = false
}
