package com.byga.flowmanager.organizer

import com.byga.flowmanager.organizer.domain.*
import org.junit.Assert.*
import org.junit.Test

class AccountImportTest {
    @Test fun csvSupportsBomQuotesCommasAndEscapedQuotes() {
        val preview = AccountImport.preview("\uFEFFemail,alias,group\r\na@example.com,\"Konten, Utama\",\"Tim \"\"A\"\"\"\r\n", true, emptySet())
        assertEquals(1, preview.valid.size)
        assertEquals("Konten, Utama", preview.valid.single().alias)
        assertEquals("Tim \"A\"", preview.valid.single().group)
    }
    @Test fun txtDeduplicatesExistingAndSameFileWithoutCollapsingDotsOrPlus() {
        val preview = AccountImport.preview(" EXISTING@EXAMPLE.COM \nnew@example.com\nNEW@example.com\na.b+flow@gmail.com\nab@gmail.com\n", false, setOf("existing@example.com"))
        assertEquals(3, preview.valid.size)
        assertEquals(2, preview.duplicates.size)
        assertTrue(preview.valid.any { it.email == "a.b+flow@gmail.com" })
    }
    @Test fun invalidRowsAreReportedWithoutEchoingPossibleSecrets() {
        val preview = AccountImport.preview("email,alias,group\ninvalid-secret,Alias,G\nb@example.com,Good,G\nc@example.com,Too,many,columns", true, emptySet())
        assertEquals(1, preview.valid.size)
        assertEquals(listOf(2, 4), preview.rejected.map { it.line })
        assertFalse(preview.rejected.toString().contains("invalid-secret"))
    }
    @Test fun credentialColumnsAndUnknownHeadersRejectWholeFile() {
        listOf("password", "otp", "cookie", "token", "notes").forEach { header ->
            assertThrows(IllegalArgumentException::class.java) {
                AccountImport.preview("email,$header\na@example.com,secret", true, emptySet())
            }
        }
    }
    @Test fun malformedQuotesAndDuplicateHeaderAreRejected() {
        listOf("email,alias\na@example.com,\"unterminated", "email,email\na@example.com,b@example.com",
            "email,alias\na@example.com,\"good\"bad").forEach { text ->
            assertThrows(IllegalArgumentException::class.java) { AccountImport.preview(text, true, emptySet()) }
        }
    }
    @Test fun inputLimitsAreEnforcedBeforeImport() {
        assertThrows(IllegalArgumentException::class.java) { AccountImport.preview("x".repeat(AccountImport.MAX_BYTES + 1), false, emptySet()) }
        assertThrows(IllegalArgumentException::class.java) {
            AccountImport.preview((1..AccountImport.MAX_ROWS + 1).joinToString("\n") { "user$it@example.com" }, false, emptySet())
        }
    }
    @Test fun csvExportEscapesFormulaAndQuotesAndOnlyExportsDefinedMetadata() {
        val account = ManagedAccount("a", "a@example.com", "=HYPERLINK(\"bad\")", note = "not exported")
        val csv = AccountImport.csv(listOf(account), emptyList())
        assertTrue(csv.contains("\"'=HYPERLINK(\"\"bad\"\")\""))
        assertFalse(csv.contains("not exported"))
        assertTrue(csv.startsWith("email,alias,group\r\n"))
    }
    @Test fun headerOnlyAndEmptyTxtProduceHonestEmptyPreview() {
        assertEquals(0, AccountImport.preview("email,alias,group\n", true, emptySet()).total)
        assertEquals(0, AccountImport.preview("\n \n", false, emptySet()).total)
    }
}
