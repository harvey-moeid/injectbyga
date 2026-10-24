package com.byga.flowmanager.organizer

import com.byga.flowmanager.organizer.domain.*
import org.junit.Assert.*
import org.junit.Test

class OrganizerRulesTest {
    private val a = ManagedAccount("a", "a@example.com", "Konten", "g", favorite = true, lastOpenedAt = 10)
    private val b = ManagedAccount("b", "b@example.com", "Eksperimen", lastOpenedAt = 20)
    private val c = ManagedAccount("c", "c@example.com", "Cadangan", "g")
    @Test fun searchFavoritesAndGroupAreCombined() {
        assertEquals(listOf(a), OrganizerRules.filter(listOf(a, b, c), "KONT", "g", true, SortMode.NAME))
        assertTrue(OrganizerRules.filter(listOf(a, b, c), "b@example", "g", false, SortMode.NAME).isEmpty())
    }
    @Test fun sortingIsDeterministicAndUnopenedAccountsComeLast() {
        assertEquals(listOf(b, a, c), OrganizerRules.filter(listOf(c, a, b), "", null, false, SortMode.RECENT))
        assertEquals(listOf(a, c, b), OrganizerRules.filter(listOf(c, b, a), "", null, false, SortMode.FAVORITE))
        assertEquals(listOf(a, b, c), OrganizerRules.filter(listOf(c, b, a), "", null, false, SortMode.EMAIL))
    }
    @Test fun metadataValidationRejectsEmptyAliasControlsAndOversizedNotes() {
        assertThrows(IllegalArgumentException::class.java) { OrganizerRules.validate("a@example.com", " ") }
        assertThrows(IllegalArgumentException::class.java) { OrganizerRules.validate("a@example.com", "x\ny") }
        assertThrows(IllegalArgumentException::class.java) { OrganizerRules.validate("a@example.com", "A", "x".repeat(1001)) }
        assertFalse(OrganizerRules.validGroup(" "))
        assertFalse(OrganizerRules.validGroup("x\ny"))
        OrganizerRules.validate("a.b+flow@gmail.com", "Flow Utama")
    }
}
