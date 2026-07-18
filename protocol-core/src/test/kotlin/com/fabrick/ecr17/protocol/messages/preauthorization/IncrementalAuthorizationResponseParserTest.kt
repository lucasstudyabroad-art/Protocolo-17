package com.fabrick.ecr17.protocol.messages.preauthorization

import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode
import org.junit.Assert.assertEquals
import org.junit.Test

class IncrementalAuthorizationResponseParserTest {
    @Test
    fun `parses incremental authorization response`() {
        val message = buildString {
            append("12345678"); append("0"); append("i"); append("00")
            append("0".repeat(15) + "9012") // pan 19
            append("ICC") // 3
            append("ACQUIRER-01") // 11
            append("AUTH12") // 6
            append("000123") // stan 6
            append("000456") // idOnline 6
            append("2111520") // date 7
            append("001") // action code 3
            append("00") // reserved 2
        }
        val response = IncrementalAuthorizationResponseParser.parse(message)
        assertEquals(TransactionResultCode.OK, response.resultCode)
        assertEquals("9012", response.maskedPan.last4)
        assertEquals("ACQUIRER-01", response.acquirerId)
        assertEquals("001", response.actionCode)
    }
}
