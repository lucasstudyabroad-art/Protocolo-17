package com.fabrick.ecr17.protocol.messages.payment

import com.fabrick.ecr17.protocol.messages.common.CardType
import com.fabrick.ecr17.protocol.messages.common.TransactionResultCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentResponseParserTest {

    private fun buildApprovedNoDcc(): String = buildString {
        append("12345678") // terminal id
        append("0") // reserved
        append("E") // message code
        append("00") // result OK
        append("0".repeat(15) + "9012") // PAN, last4 = 9012
        append("ICC") // txn type
        append("AUTH12") // auth code
        append("2111520") // date
        append("2") // card type = credit
        append("ACQUIRER-01") // acquirer, 11 chars
        append("000123") // stan
        append("000456") // id online
    }

    private fun buildDeniedNoDcc(): String = buildString {
        append("12345678")
        append("0")
        append("E")
        append("01")
        append("CARD DECLINED".padEnd(24).take(24)) // description, 24 chars
        append("0".repeat(11)) // reserved
        append("1") // card type
        append("ACQUIRER-01")
        append("000124")
        append("000457")
    }

    @Test
    fun `parses approved payment without currency exchange`() {
        val response = PaymentResponseParser.parse(buildApprovedNoDcc()) as PaymentResponse.Approved
        assertEquals("12345678", response.terminalId)
        assertEquals(TransactionResultCode.OK, response.resultCode)
        assertEquals("9012", response.maskedPan.last4)
        assertEquals("AUTH12", response.authorizationCode)
        assertEquals("2111520", response.transactionDateTime)
        assertEquals(CardType.CREDIT, response.trailer.cardType)
        assertEquals("ACQUIRER-01", response.trailer.acquirerId)
        assertEquals("000123", response.trailer.stan)
        assertEquals("000456", response.trailer.idOnline)
        assertNull(response.currencyExchange)
    }

    @Test
    fun `parses denied payment without currency exchange`() {
        val response = PaymentResponseParser.parse(buildDeniedNoDcc()) as PaymentResponse.Denied
        assertEquals(TransactionResultCode.KO, response.resultCode)
        assertTrue(response.resultDescription.startsWith("CARD DECLINED"))
        assertEquals("000124", response.trailer.stan)
    }

    @Test
    fun `parses card not present result`() {
        val message = buildDeniedNoDcc().let {
            it.substring(0, 10) + "05" + it.substring(12)
        }
        val response = PaymentResponseParser.parse(message) as PaymentResponse.Denied
        assertEquals(TransactionResultCode.CARD_NOT_PRESENT, response.resultCode)
    }

    @Test
    fun `parses approved payment with currency exchange data`() {
        val message = buildString {
            append("12345678")
            append("0")
            append("V")
            append("00")
            append("0".repeat(15) + "9012")
            append("ICC")
            append("AUTH12")
            append("2111520")
            append("2")
            append("ACQUIRER-01")
            append("000123")
            append("000456")
            append("001") // action code
            append("00000650") // original amount
            append("1") // dcc flag
            append("00011234") // exchange rate, 4 decimals implied -> 1.1234
            append("USD") // currency code
            append("000000000715") // amount in transaction currency
            append("2") // precision
            append("0".repeat(10)) // reserved
        }

        val response = PaymentResponseParser.parse(message) as PaymentResponse.Approved
        assertEquals("001", response.actionCode)
        val dcc = response.currencyExchange!!
        assertEquals(650L, dcc.originalAmountMinorUnits)
        assertEquals("00011234", dcc.exchangeRate)
        assertEquals("USD", dcc.currencyCode)
        assertEquals(715L, dcc.transactionAmountInCurrency)
        assertEquals(2, dcc.precisionDecimals)
    }

    @Test
    fun `dcc flag zero yields no currency exchange data`() {
        val message = buildString {
            append("12345678")
            append("0")
            append("V")
            append("00")
            append("0".repeat(15) + "9012")
            append("ICC")
            append("AUTH12")
            append("2111520")
            append("2")
            append("ACQUIRER-01")
            append("000123")
            append("000456")
            append("001")
            append("00000650")
            append("0") // dcc flag off
            append("0".repeat(8))
            append("EUR")
            append("0".repeat(12))
            append("0")
            append("0".repeat(10))
        }
        val response = PaymentResponseParser.parse(message) as PaymentResponse.Approved
        assertNull(response.currencyExchange)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects unexpected message code`() {
        val bad = buildApprovedNoDcc().let { it.substring(0, 9) + "Z" + it.substring(10) }
        PaymentResponseParser.parse(bad)
    }
}
