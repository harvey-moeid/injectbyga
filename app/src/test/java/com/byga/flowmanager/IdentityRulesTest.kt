package com.byga.flowmanager

import com.byga.flowmanager.domain.IdentityRules
import com.byga.flowmanager.domain.Observation
import com.byga.flowmanager.domain.Outcome
import org.junit.Assert.*
import org.junit.Test

class IdentityRulesTest {
    @Test fun selectedBButObservedAIsMismatch() {
        assertEquals(Outcome.MISMATCH, IdentityRules.review(Outcome.UNVERIFIED,
            "account-b@gmail.com", "account-a@gmail.com", Observation.IDENTITY))
    }
    @Test fun onlyTesterObservationCanProduceManualMatch() {
        assertEquals(Outcome.VERIFIED_MANUAL, IdentityRules.review(Outcome.UNVERIFIED,
            "Account.B@gmail.com", " account.b@GMAIL.com ", Observation.IDENTITY))
    }
    @Test fun noIdentityEvidenceRemainsUnknown() {
        assertEquals(Outcome.UNKNOWN, IdentityRules.review(Outcome.UNVERIFIED,
            "account-b@gmail.com", "", Observation.UNKNOWN))
    }
    @Test fun loginPromptDoesNotImplyExpiration() {
        assertEquals(Outcome.NEEDS_AUTH, IdentityRules.review(Outcome.UNVERIFIED,
            "account-b@gmail.com", "", Observation.NEEDS_AUTH))
        assertEquals(Outcome.EXPIRED, IdentityRules.review(Outcome.UNVERIFIED,
            "account-b@gmail.com", "", Observation.EXPIRED))
    }
    @Test fun cannotConfirmFailedBlockedOrPreviouslyConfirmedTrial() {
        (Outcome.entries - Outcome.UNVERIFIED).forEach { state ->
            assertThrows(IllegalArgumentException::class.java) {
                IdentityRules.review(state, "b@gmail.com", "b@gmail.com", Observation.IDENTITY)
            }
        }
    }
    @Test fun emptyOrInvalidObservationIsNeverAMatch() {
        listOf("", "B", "b@", "@gmail.com", "b@gmail.com\npassword", "b..a@gmail.com").forEach { email ->
            assertThrows(IllegalArgumentException::class.java) {
                IdentityRules.review(Outcome.UNVERIFIED, "b@gmail.com", email, Observation.IDENTITY)
            }
        }
    }
    @Test fun emailValidationAllowsWorkspaceWithoutRewritingDotsOrPlus() {
        assertTrue(IdentityRules.isValid("user+flow@studio.example"))
        assertTrue(IdentityRules.isValid("a.b@gmail.com"))
        assertFalse(IdentityRules.isValid(".user@gmail.com"))
        assertFalse(IdentityRules.isValid("user@-example.com"))
        assertFalse(IdentityRules.isValid("user @gmail.com"))
        assertEquals("a.b+flow@gmail.com", IdentityRules.normalize(" A.B+Flow@Gmail.com "))
    }
}
