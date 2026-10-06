package org.connecttag.lib.securestorage

import android.content.Context

class AndroidStorageEncryptionBinding(
    context: Context,
) : StorageEncryptionBinding {
    private val applicationContext = context.applicationContext

    override fun createStringVault(spec: EncryptedStorageSpec): PlatformSecureStringVault =
        AndroidKeystoreSecureStringVault(
            context = applicationContext,
            preferencesName = spec.name,
            keyAlias = spec.keyAlias,
        )
}
