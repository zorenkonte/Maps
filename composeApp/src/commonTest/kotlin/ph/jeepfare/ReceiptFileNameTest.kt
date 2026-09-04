package ph.jeepfare

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.jeepfare.ui.receiptFileName

class ReceiptFileNameTest {

    @Test
    fun buildsAFileSafeNameFromTheReceiptDate() {
        assertEquals(
            "pamasahe-Sep-4-2026-9-41-AM",
            receiptFileName("Sep 4, 2026 · 9:41 AM"),
        )
    }

    @Test
    fun collapsesRunsOfSeparatorsInsteadOfLeavingBlanks() {
        val name = receiptFileName("Jul 21, 2026 · 12:05 PM")
        assertEquals("pamasahe-Jul-21-2026-12-05-PM", name)
        assertTrue(name.none { it == '.' || it == '/' || it == ' ' }, "no path-unsafe characters")
        assertTrue("--" !in name, "no empty segments")
    }

    @Test
    fun fallsBackWhenTheDateLabelCarriesNothingUsable() {
        assertEquals("pamasahe-receipt", receiptFileName(" · , "))
    }
}
