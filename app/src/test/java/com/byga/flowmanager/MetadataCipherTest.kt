package com.byga.flowmanager

import com.byga.flowmanager.security.MetadataCipher
import org.junit.Assert.*
import org.junit.Test
import java.util.Base64
import javax.crypto.AEADBadTagException
import javax.crypto.KeyGenerator

class MetadataCipherTest {
    private val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
    private val cipher = MetadataCipher { key }

    @Test fun metadataRoundTripsAndUsesRandomIV() {
        val first = cipher.encrypt("sensitive@example.com", "slot:A")
        val second = cipher.encrypt("sensitive@example.com", "slot:A")
        assertNotEquals(first, second)
        assertFalse(first.contains("sensitive"))
        assertEquals("sensitive@example.com", cipher.decrypt(first, "slot:A"))
    }
    @Test fun tamperedMetadataIsRejected() {
        val bytes = Base64.getDecoder().decode(cipher.encrypt("b@example.com", "expected:trial-1"))
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        assertThrows(AEADBadTagException::class.java) {
            cipher.decrypt(Base64.getEncoder().encodeToString(bytes), "expected:trial-1")
        }
    }
    @Test fun cannotSwapEncryptedEmailsBetweenSlotsOrTrials() {
        val encrypted = cipher.encrypt("a@example.com", "slot:A")
        assertThrows(AEADBadTagException::class.java) { cipher.decrypt(encrypted, "slot:B") }
    }
    @Test fun incorrectKeyCannotReadMetadata() {
        val otherKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        assertThrows(AEADBadTagException::class.java) {
            MetadataCipher { otherKey }.decrypt(cipher.encrypt("a@example.com", "slot:A"), "slot:A")
        }
    }
}
