package com.fabrick.ecr17.protocol.messages.status

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class PosStatusResponseParserTest {

    private fun buildMessage(statusCode: Char, swRelease: String = "SYS03.4ESSA05.5CMST08.55EMV08.81ECR01.55"): String = buildString {
        append("12345678")
        append("0")
        append("s")
        append("0".repeat(10))
        append("1707261530") // DDMMYYhhmm
        append(statusCode)
        append(swRelease)
    }

    @Test
    fun `status 0 not configured is not operative`() {
        val response = PosStatusResponseParser.parse(buildMessage('0'))
        assertEquals(PosTerminalStatus.NOT_CONFIGURED, response.status)
        assertFalse(response.status.operative)
    }

    @Test
    fun `status 1 configured no dll is not operative`() {
        assertEquals(PosTerminalStatus.CONFIGURED_NO_DLL, PosStatusResponseParser.parse(buildMessage('1')).status)
    }

    @Test
    fun `status 2 operative is operative`() {
        val response = PosStatusResponseParser.parse(buildMessage('2'))
        assertEquals(PosTerminalStatus.OPERATIVE, response.status)
        assertTrue(response.status.operative)
    }

    @Test
    fun `status 3 not aligned is not operative`() {
        assertEquals(PosTerminalStatus.NOT_ALIGNED, PosStatusResponseParser.parse(buildMessage('3')).status)
    }

    @Test
    fun `status 4 key corrupted is not operative`() {
        assertEquals(PosTerminalStatus.KEY_CORRUPTED, PosStatusResponseParser.parse(buildMessage('4')).status)
    }

    @Test
    fun `status 5 dll pending is not operative`() {
        assertEquals(PosTerminalStatus.DLL_SOLICITED_PENDING, PosStatusResponseParser.parse(buildMessage('5')).status)
    }

    @Test
    fun `status 6 remote update pending is not operative`() {
        assertEquals(PosTerminalStatus.REMOTE_UPDATE_PENDING, PosStatusResponseParser.parse(buildMessage('6')).status)
    }

    @Test(expected = IllegalStateException::class)
    fun `unknown status code is never silently treated as operative`() {
        PosStatusResponseParser.parse(buildMessage('9'))
    }

    @Test
    fun `parses software module versions from repeating 8 char blocks`() {
        val response = PosStatusResponseParser.parse(buildMessage('2'))
        assertEquals(5, response.softwareModules.size)
        assertEquals(SoftwareModule("SYS", "03.4E"), response.softwareModules[0])
        assertEquals(SoftwareModule("ECR", "01.55"), response.softwareModules[4])
    }

    @Test
    fun `handles response with no software modules present`() {
        val response = PosStatusResponseParser.parse(buildMessage('0', swRelease = ""))
        assertTrue(response.softwareModules.isEmpty())
    }
}
