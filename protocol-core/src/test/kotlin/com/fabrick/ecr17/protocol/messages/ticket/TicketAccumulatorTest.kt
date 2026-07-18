package com.fabrick.ecr17.protocol.messages.ticket

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketAccumulatorTest {

    @Test
    fun `concatenates multiple parts in arrival order`() {
        val accumulator = TicketAccumulator()
        accumulator.accept("PART ONE")
        accumulator.accept("PART TWO")
        assertEquals("PART ONEPART TWO", accumulator.rawConcatenated())
        assertEquals(2, accumulator.partCount())
    }

    @Test
    fun `not complete until terminator sequence is seen`() {
        val accumulator = TicketAccumulator()
        accumulator.accept("RECEIPT LINE 1")
        assertFalse(accumulator.isComplete())
    }

    @Test
    fun `detects completion on six newline bytes plus terminator`() {
        val accumulator = TicketAccumulator()
        accumulator.accept("TOTAL: 6.50")
        accumulator.accept(TicketAccumulator.TERMINATOR)
        assertTrue(accumulator.isComplete())
    }

    @Test
    fun `renders plain text converting newline bytes and stripping bold marker`() {
        val accumulator = TicketAccumulator()
        accumulator.accept("LINE ONE" + TicketAccumulator.NEW_LINE_CHAR + TicketAccumulator.BOLD_START_CHAR + "BOLD LINE")
        accumulator.accept(TicketAccumulator.TERMINATOR)
        val rendered = accumulator.renderPlainText()
        assertEquals("LINE ONE\nBOLD LINE", rendered)
    }

    @Test(expected = IllegalStateException::class)
    fun `rejects further content after completion`() {
        val accumulator = TicketAccumulator()
        accumulator.accept(TicketAccumulator.TERMINATOR)
        accumulator.accept("too late")
    }

    @Test
    fun `reset clears accumulated state`() {
        val accumulator = TicketAccumulator()
        accumulator.accept(TicketAccumulator.TERMINATOR)
        assertTrue(accumulator.isComplete())
        accumulator.reset()
        assertFalse(accumulator.isComplete())
        assertEquals(0, accumulator.partCount())
    }
}
