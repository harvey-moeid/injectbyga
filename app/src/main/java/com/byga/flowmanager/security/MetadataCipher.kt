package com.byga.flowmanager.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Encrypts local BYGA metadata, never Google credentials or sessions. */
class MetadataCipher(private val keyProvider: () -> SecretKey = AndroidKeyProvider()::get) {
    fun encrypt(value: String, context: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keyProvider())
        cipher.updateAAD(context.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8)))
    }

    fun decrypt(value: String, context: String): String {
        val packed = Base64.getDecoder().decode(value)
        require(packed.size >= 28) { "Metadata rusak." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keyProvider(), GCMParameterSpec(128, packed.copyOfRange(0, 12)))
        cipher.updateAAD(context.toByteArray(Charsets.UTF_8))
        return cipher.doFinal(packed.copyOfRange(12, packed.size)).toString(Charsets.UTF_8)
    }
}

private class AndroidKeyProvider {
    fun get(): SecretKey = synchronized(keyLock) {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return@synchronized it }
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build())
        }.generateKey()
    }
    companion object {
        const val ALIAS = "byga.poc.metadata.v1"
        private val keyLock = Any()
    }
}
