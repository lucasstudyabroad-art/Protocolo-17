# ECR17 / Protocol 17 Coverage Matrix

Source of truth: `docs/PAX_ECR17_Protocol_Specification.pdf` (Fabrick S.p.A., "ECR17 Protocol Specification", provided version — hereafter "the PDF"). All positions below are **1-based**, as printed in the PDF. Lengths are in bytes/ASCII characters. All application messages are ASCII (0x00-0x7F).

Every request/response frame is wrapped as `[STX][application message][ETX][LRC]` (§4.1) and requires a physical `ACK`/`NAK` (§4.3). Progress packets (§4.2) are `[SOH][20-byte message][EOT]` and need no physical ACK.

Legend for **Status**: `DONE` = builder + parser + unit test exist; `PARTIAL` = builder or parser exists but not both / tests incomplete; `TODO` = not started.

---

## 1. Payment (Basic) — §5.1

| | |
|---|---|
| Operation name | Basic Payment |
| Request code | `P` (0x50) |
| Response code | `E` (0x45) — no currency exchange (§5.1.2), or `V` (0x56) — with currency exchange (§5.1.3) |
| Request length | 168 bytes (pos 1-160 field is 128 bytes ending at 159, +8 reserved = 160..167, i.e. total length 167; see field table) |
| Response variants | Positive (result "00"/"05"/"09"): PAN, txn type, auth code, date/time, common trailer. Negative ("01"): result description, reserved, common trailer |
| ACK required | Yes, both directions |
| Special flow | POS status must be checked first (§3). May be preceded by `E1` (enable ECR receipt) and followed by `U` (additional data) request/response and `S` (receipt) messages — §3.2 |
| Implementation class | `PaymentRequest`, `PaymentResponseParser` (`protocol/messages/payment`) |
| Unit test class | `PaymentRequestTest`, `PaymentResponseParserTest` |
| Status | DONE |

### Request fields (§5.1.1)
| Pos | Len | Type | Field |
|---|---|---|---|
| 1 | 8 | N | Terminal ID |
| 9 | 1 | A | Reserved = '0' |
| 10 | 1 | A | Message code 'P' |
| 11 | 8 | N | Cash Register ID |
| 19 | 1 | N | Additional-data-present flag ('0'/'1') |
| 20 | 2 | N | Reserved = '00' |
| 22 | 1 | N | Card-already-present ('0'/'1') |
| 23 | 1 | N | Payment type ('0'-'3') |
| 24 | 8 | N | Amount (cents), right-aligned, '0'-filled |
| 32 | 128 | AN | Receipt/contract text, right-aligned, ' '-filled |
| 160 | 8 | N | Reserved = '00000000' |

Total length = 167 bytes.

### Response fields, positive "00" (§5.1.2)
| Pos | Len | Type | Field |
|---|---|---|---|
| 1 | 8 | N | Terminal ID |
| 9 | 1 | A | Reserved |
| 10 | 1 | A | 'E' |
| 11 | 2 | N | Result "00"/"01"/"05"/"09" |
| 13 | 19 | N | PAN (masked) |
| 32 | 3 | A | Txn type ICC/MAG/MAN/CLM/CLI |
| 35 | 6 | A | Auth code |
| 41 | 7 | A | Date/time DDDHHMM |
| 48 | 1 | N | Card type 1/2/3 |
| 49 | 11 | N | Acquirer ID |
| 60 | 6 | N | STAN |
| 66 | 6 | N | ID online |

### Response fields, negative "01"
| Pos | Len | Type | Field |
|---|---|---|---|
| 13 | 24 | A | Result description |
| 37 | 11 | N | Reserved |
| (common trailer same positions 48-71 as above) |

---

## 2. Payment response with currency exchange — §5.1.3

| | |
|---|---|
| Operation name | Basic Payment (DCC response variant) |
| Response code | `V` (0x56) |
| Fields | Terminal ID, reserved, 'V', result (11,2). Positive: PAN(13,19), txn type(32,3), auth code(35,6), date(41,7). Negative: description(13,24), reserved(37,11). Common: card type(48,1), acquirer(49,11), STAN(60,6), id online(66,6), action code(72,3), original amount(75,8), DCC flag(83,1), exchange rate(84,8), currency code(92,3), DCC amount(95,12), precision(107,1), reserved(108,10) |
| Total length | 117 bytes |
| Implementation class | `PaymentResponseParser` (handles both `E` and `V` variants) |
| Unit test class | `PaymentResponseParserTest` |
| Status | DONE |

---

## 3. Payment with extended result — §5.2

| | |
|---|---|
| Operation name | Extended Payment |
| Request code | `X` (0x58) |
| Response code | `E` (0x45) |
| Request length | 167 bytes — identical layout to Basic Payment except message code |
| Response | Same as Basic Payment positive/negative + common trailer **plus** field 75(8,N) amount received by host, 83(10,N) reserved. No DCC variant documented for extended payment. |
| ACK required | Yes |
| Implementation class | `ExtendedPaymentRequest`, `ExtendedPaymentResponseParser` |
| Unit test class | `ExtendedPaymentRequestTest`, `ExtendedPaymentResponseParserTest` |
| Status | DONE |

---

## 4. Refund — §5.3

| | |
|---|---|
| Request code | `A` (0x41) |
| Response code | `A` (0x41) |
| Request fields | TermID(1,8) reserved(9,1) 'A'(10,1) CashRegID(11,8) addlData(19,1) reserved(20,4) amount(24,8) reserved(32,8) → length 39 |
| Response fields | TermID reserved 'A' result(11,2 "00"/"01"/"09"). PAN(13,19) txnType(32,3) authCode(35,6) acquirerId(41,11) date(52,7) → length 58 |
| ACK | Yes |
| Implementation class | `RefundRequest`, `RefundResponseParser` |
| Unit test class | `RefundRequestTest`, `RefundResponseParserTest` |
| Status | DONE |

---

## 5. Close session — §5.4

| | |
|---|---|
| Request code | `C` (0x43) |
| Response code | `C` (0x43) |
| Request fields | TermID reserved 'C' CashRegID addlData(19,1) reserved(20,7) → length 26 |
| Response positive | result="00", POS total(13,16) Host total(29,16) → length 44 |
| Response negative | result="01", description(13,19) actionCode(32,3) reserved(35,10) → length 44 |
| ACK | Yes |
| Implementation class | `CloseSessionRequest`, `CloseSessionResponseParser` |
| Unit test class | `CloseSessionRequestTest`, `CloseSessionResponseParserTest` |
| Status | DONE |

---

## 6. Terminal totals — §5.5

| | |
|---|---|
| Request code | `T` (0x54) |
| Response code | `T` (0x54) |
| Request fields | TermID reserved 'T' CashRegID addlData(19,1) reserved(20,7) → length 26 |
| Response fields | TermID reserved 'T' result(11,2) POStotal(13,16) reserved(29,6) → length 34 |
| ACK | Yes |
| Implementation class | `TerminalTotalsRequest`, `TerminalTotalsResponseParser` |
| Unit test class | `TerminalTotalsRequestTest`, `TerminalTotalsResponseParserTest` |
| Status | DONE |

---

## 7. Reversal — §5.6

| | |
|---|---|
| Request code | `S` (0x53) — **note**: same letter as the unrelated "Send ticket" message that flows Terminal→ECR (§5.13); no collision because direction differs |
| Response code | `E` (0x45) |
| Request fields | TermID reserved 'S' CashRegID(11,8) STAN-to-reverse(19,6) addlData(25,1) reserved(26,1, PAX ignores documented values '1'/'2') → length 26 |
| Response positive | PAN(13,19) txnType(32,3) reserved(35,6) date(41,7) | 
| Response negative | description(13,24) reserved(37,11) |
| Common trailer | cardType(48,1) acquirer(49,11) STAN(60,6) idOnline(66,6) actionCode(72,3) reserved(75,10) → length 84 |
| ACK | Yes |
| Implementation class | `ReversalRequest`, `ReversalResponseParser` |
| Unit test class | `ReversalRequestTest`, `ReversalResponseParserTest` |
| Status | DONE |

---

## 8. Card verification — §5.7

| | |
|---|---|
| Request code | `H` (0x48) |
| Response code | `E` (0x45) |
| Request fields | TermID reserved 'H' CashRegID addlData(19,1) reserved(20,2) telematico(22,1) paymentType(23,1) reserved(24,16) → length 39 |
| Response positive | PAN(13,19) txnType(32,3) authCode(35,6) date(41,7) |
| Response negative | description(13,24) actionCode(37,3) reserved(40,8) |
| Common trailer | cardType(48,1) acquirer(49,11) STAN(60,6) idOnline(66,6) → length 71 |
| ACK | Yes |
| Implementation class | `CardVerificationRequest`, `CardVerificationResponseParser` |
| Unit test class | `CardVerificationRequestTest`, `CardVerificationResponseParserTest` |
| Status | DONE |

---

## 9. Run DLL — §5.8

| | |
|---|---|
| Request code | `D` (0x44) |
| Response code | `E` (0x45) |
| Request fields | TermID reserved 'D' reserved(11,6) txnTypeFlag(17,1: 0=manual DLL,1=First DLL,2=RFU) reserved(18,1) reserved(19,2) reserved(21,100) → length 120 |
| Response result | "00"/"01" (no "09") at (11,2); STAN(13,6) idOnline(19,6) |
| Response positive | date(25,7) reserved(32,220) → length 251 |
| Response negative | description(25,24) reserved(49,11) → length 59 |
| Note | Additional-TAG management cannot be used with Run DLL |
| ACK | Yes |
| Implementation class | `RunDllRequest`, `RunDllResponseParser` |
| Unit test class | `RunDllRequestTest`, `RunDllResponseParserTest` |
| Status | DONE |

---

## 10. POS status — §5.9

| | |
|---|---|
| Request code | `s` (0x73) lower case |
| Response code | `s` (0x73) |
| Request fields | TermID(1,8) reserved(9,1) 's'(10,1) → length 10 |
| Response fields | TermID reserved 's' reserved(11,10) dateTime(21,10 "DDMMYYhhmm") status(31,1: '0'..'6') SW release string(32, N*8, repeating 3-letter-module + 5-char version blocks, e.g. `SYS03.4ESSA05.5CMST08.55EMV08.81ECR01.55`) |
| Status codes | 0=not configured,1=configured/no DLL,2=operative,3=not aligned (First DLL requested),4=key corrupted (first DLL requested),5=DLL solicited by GT pending,6=remote SW update pending |
| Special rule | If ECR connection params not configured on terminal, there is **no response at all** (must be handled as a response timeout, not an error frame) |
| ACK | Yes (when a response is sent) |
| Implementation class | `PosStatusRequest`, `PosStatusResponseParser` |
| Unit test class | `PosStatusRequestTest`, `PosStatusResponseParserTest` — one test per status code 0-6 |
| Status | DONE |

---

## 11. Additional data to/from host — TAG — §5.10

| | |
|---|---|
| Operation name | Additional data for GT delivery ("U", ECR→Terminal) / Additional data from GT result ("U", Terminal→ECR) |
| Request code | `U` (0x55) |
| Response code | `U` (0x55) (only sent by terminal if ISO field ≠ "00") |
| Request fields | TermID reserved 'U' paymentType(11,6) isoField(17,2, currently fixed "62") tagNumber(19,8, currently fixed "DF8D01") reserved(27,1) exclusiveTagIndex(28,4) reserved(32,5) privativeTagContent(37, 1-100 variable, terminated by 0x1B) — repeatable up to 4 times |
| Response fields | TermID reserved 'U' reserved(11,6) dataLength(17,3) additionalData(20, 1-255 variable) reserved(20+n,10) |
| ACK | Yes |
| Special flow | Sent only when the "additional data present" flag was set to '1' in the triggering financial command (Payment/Refund/etc.) |
| Implementation class | `TagDeliveryRequest`, `TagResultResponseParser` (`protocol/messages/tag`) |
| Unit test class | `TagDeliveryRequestTest`, `TagResultResponseParserTest` |
| Status | DONE |

---

## 12. Send last result — §5.11

| | |
|---|---|
| Operation name | Receipt reprint / "send last result" |
| Request code | `G` (0x47) |
| Response code | *variable* — identical to whatever the last saved RESULT message was (Payment/Refund/Reversal/CardVerification/PreAuth family) |
| Request fields | TermID reserved 'G' CashRegID addlData(19,1) reserved(20,3) → length 22 |
| ACK | Yes |
| Special flow | Terminal replays its last saved financial result verbatim (§5.11.2); if that included TAG data, the `U` message follows too. Implementation must parse the *replayed* frame with the same parser used for the original operation, keyed by message code. |
| Implementation class | `LastResultRequest` + dispatch through `Ecr17ResponseDispatcher` |
| Unit test class | `LastResultRequestTest`, `Ecr17ResponseDispatcherTest` |
| Status | DONE |

---

## 13. Enable/disable printing receipt on ECR — §5.12

| | |
|---|---|
| Request code | `E` (0x45) — **note**: shares the letter with the Basic-Payment *response* code; unambiguous because direction/context differ (this one is ECR→Terminal and is followed only by ACK, no application response) |
| Response | ACK/NAK only — no application-level response message |
| Request fields | TermID reserved 'E' mode(11,1: '0' disable / '1' enable-on-ECR / '2' enable-both / '3' merchant-on-ECR+customer-on-terminal) → length 11 |
| ACK | Yes (physical only) |
| Implementation class | `ReceiptModeRequest` |
| Unit test class | `ReceiptModeRequestTest` |
| Status | DONE |

---

## 14. Send ticket — §5.13

| | |
|---|---|
| Direction | Terminal → ECR (unsolicited, part of a receipt-on-ECR flow) |
| Message code | `S` (0x53) |
| Fields | TermID(1,8) reserved(9,1) 'S'(10,1) lines(11, 1-200 AN) |
| Control chars | 0x7D = newline+format reset, 0x7F = start double-height-bold, (0x7E/0x7B/0x7C/0x5E documented but "NOT USED" by PAX) |
| Termination | Final `S` message ends with six 0x7D + one 0x1B |
| ACK | Yes, each `S` message individually |
| Special flow | Multiple `S` messages may arrive; ECR must concatenate them in arrival order until the 0x1B terminator |
| Implementation class | `TicketLineParser`, `TicketAccumulator` (`protocol/messages/ticket`) |
| Unit test class | `TicketLineParserTest`, `TicketAccumulatorTest` |
| Status | DONE |

---

## 15. Reprint ticket — §5.14

| | |
|---|---|
| Request code | `R` (0x52) (ECR→Terminal) |
| Response | none directly — terminal either prints locally or replies with `S` ticket message(s) per the mode flag |
| Request fields | TermID reserved(9,1,N) 'R'(10,1) ecrPrintFlag(11,1: 0=terminal prints,1=send via S) ticketType(12,1: 0=last financial,1=last service) reserved(13,10) → length 22 |
| ACK | Yes |
| Implementation class | `ReprintTicketRequest` |
| Unit test class | `ReprintTicketRequestTest` |
| Status | DONE |

---

## 16. Pre-authorization request — §5.15

| | |
|---|---|
| Request code | `p` (0x70) lower case |
| Response code | `e` (0x65) lower case |
| Request fields | Same layout as Basic Payment (TermID, reserved, 'p', CashRegID, addlData, reserved(20,2), cardPresent(22,1), paymentType(23,1), amount(24,8), receiptText(32,128), reserved(160,8)) → length 167 |
| Response positive | PAN(13,19) txnType(32,3) authCode(35,6) preAuthAmount(41,8) preAuthCode(49,9) actionCode(58,3) date(61,7) reserved(68,3) |
| Response negative | description(13,24) actionCode(37,3) reserved(40,31) |
| Common trailer | cardType(71,1) acquirer(72,11) STAN(83,6) idOnline(89,6) reserved(95,12) → length 106 |
| ACK | Yes |
| Implementation class | `PreAuthorizationRequest`, `PreAuthorizationResponseParser` |
| Unit test class | `PreAuthorizationRequestTest`, `PreAuthorizationResponseParserTest` |
| Status | DONE |

---

## 17. Incremental authorization — §5.16

| | |
|---|---|
| Request code | `i` (0x69) lower case |
| Response code | `i` (0x69) |
| Request fields | TermID reserved 'i' CashRegID addlData(19,1) reserved(20,4) amount(24,8) receiptText(32,128) origPreAuthCode(160,9) reserved(169,8) → length 176 |
| Response fields | TermID reserved 'i' result(11,2 "00"/"01"/"09") PAN(13,19) txnType(32,3) acquirer(35,11) authCode(46,6) STAN(52,6) idOnline(58,6) date(64,7) actionCode(71,3) reserved(74,2) → length 75 |
| ACK | Yes |
| Implementation class | `IncrementalAuthorizationRequest`, `IncrementalAuthorizationResponseParser` |
| Unit test class | `IncrementalAuthorizationRequestTest`, `IncrementalAuthorizationResponseParserTest` |
| Status | DONE |

---

## 18. Pre-authorization closure — §5.17

| | |
|---|---|
| Request code | `c` (0x63) lower case |
| Response code (no DCC) | `c` (0x63) |
| Response code (with DCC) | `v` (0x76) lower case |
| Request fields | TermID reserved 'c' CashRegID addlData(19,1) reserved(20,4) amount(24,8) receiptText(32,128) origPreAuthCode(160,9) reserved(169,12) → length 180 |
| Response `c` positive | PAN(13,19) txnType(32,3) authCode(35,6) date(41,7); negative: description(13,24) reserved(37,11); trailer cardType(48,1) acquirer(49,11) STAN(60,6) idOnline(66,6) actionCode(72,3) → length 74 |
| Response `v` (DCC) | same as `c` trailer plus dccFlag(75,1) exchRate(76,8) currencyCode(84,3) dccAmount(87,12) precision(99,1) reserved(100,10) → length 109 |
| ACK | Yes |
| Implementation class | `PreAuthorizationClosureRequest`, `PreAuthorizationClosureResponseParser` |
| Unit test class | `PreAuthorizationClosureRequestTest`, `PreAuthorizationClosureResponseParserTest` |
| Status | DONE |

---

## 19. Retroactive Reversal — §5.18

| | |
|---|---|
| Request code | `Q` (0x51) |
| Response code | `E` (0x45) |
| Request fields | TermID reserved 'Q' CashRegID(11,8) reserved(19,5) txnTypeToReverse(24,1: 0=purchase,1=pre-auth,2=pre-auth closure) STAN(25,6) cardData(31,5: "C"+last4 for intl, ABI code for Bancomat) acquirerId(36,11) amount(47,8) authCode(55,6) dateTime(61,10 "DMMYYhhmm") reserved(71,10) → length 80 |
| Response positive | PAN(13,19) txnType(32,3) reserved(35,6) date(41,7) |
| Response negative | description(13,24) reserved(37,11) |
| Common trailer | cardType(48,1) acquirer(49,11) STAN(60,6) idOnline(66,6) actionCode(72,3) reserved(75,10) → length 84 |
| Special rule | If terminal cannot perform retroactive reversal, result = "91" (undocumented as OK/KO but explicitly named in prose, §5.18) — must be handled as an explicit denial code, not just 00/01/09 |
| ACK | Yes |
| Implementation class | `RetroactiveReversalRequest`, `RetroactiveReversalResponseParser` |
| Unit test class | `RetroactiveReversalRequestTest`, `RetroactiveReversalResponseParserTest` |
| Status | DONE |

---

## Framing / transport layer (§4)

| Item | Spec | Implementation class | Test class | Status |
|---|---|---|---|---|
| Application packet | STX + message + ETX + LRC | `Ecr17FrameCodec` | `Ecr17FrameCodecTest` | DONE |
| Progress packet | SOH + 20-byte message + EOT, no physical ACK | `Ecr17FrameCodec` / `Ecr17Packet.Progress` | `Ecr17StreamParserTest` | DONE |
| ACK | 0x06 0x03 LRC | `Ecr17FrameCodec` | `Ecr17FrameCodecTest` | DONE |
| NAK | 0x15 0x03 LRC | `Ecr17FrameCodec` | `Ecr17FrameCodecTest` | DONE |
| LRC algorithm | XOR of message bytes, base 0x7F | `Ecr17LrcCalculator` | `Ecr17LrcCalculatorTest` | DONE (see `docs/lrc-implementation-note.md` — byte-scope not certified against an official vector) |
| Stream (de)framing | partial frames, multiple frames per read, mixed ACK+response, malformed data | `Ecr17StreamParser` | `Ecr17StreamParserTest` | DONE |
| Retry policy | max 3 transmissions, NAK ⇒ retransmit, timeout ⇒ retransmit | `Ecr17TransmissionSession` | `Ecr17TransmissionSessionTest` | DONE |
| Terminal-ID validation | request Terminal ID must equal configured Terminal ID or `00000000` | `Ecr17ResponseValidator` | `Ecr17ResponseValidatorTest` | DONE |
| "J" command | must never be used/accepted; unexpected message ⇒ NAK | `Ecr17FrameCodec` (encode-time guard) | `Ecr17FrameCodecTest` | DONE |

---

## Not directly modeled as a separate message (rationale)

- **§5.7.1 Card Verification request table** in the PDF is printed merged with §5.8's page but its content is fully captured under §5.7 above (message code `H`) — no separate entry needed.
- **RFU fields** (`Run DLL` txnTypeFlag=2 "return Acquirer Data") are accepted as an enum value but the terminal behavior for it is undocumented (RFU); exposed in the UI as a disabled/labelled-RFU option, not sent by default.
- The **`E` command** is overloaded by the PDF for three different meanings depending on direction/content: (1) ECR→Terminal "Enable/disable printing receipt on ECR" (§5.12, request only, ACK-only reply), (2) Terminal→ECR payment/reversal/pre-auth "Transaction Result" response (§5.1.2/§5.2.2/§5.6.2/§5.18.2). These are modeled as distinct classes dispatched by direction, never conflated.

## Deviations from this prompt (prompt vs PDF)

| Prompt said | PDF says | Resolution |
|---|---|---|
| Universal TCP port | No TCP port is defined anywhere in the PDF | Port is a required, user-supplied Configuration field; no default is hard-coded. |
| "TAG Data: structured field editor" | TAG structure is only fully defined for §5.10 delivery/result messages | Structured editor implemented for §5.10 fields; a raw-ASCII advanced mode covers any other privative content. |
| List of operations to implement | PDF has exactly the 19 operations enumerated above (§5.1-§5.18, counting Basic+Extended+Refund+Close+Totals+Reversal+CardVerif+RunDLL+PosStatus+TAG+LastResult+ReceiptMode+SendTicket+ReprintTicket+PreAuth+Incremental+PreAuthClosure+RetroactiveReversal) | All 19 are implemented; matches the prompt's list exactly (Send Ticket / Reprint Ticket / Last Result Feedback map onto §5.11/§5.13/§5.14). |

## Summary

19 of 19 request/response operation families from the PDF are implemented with builder + parser + unit tests, plus the full framing/transport layer. See `docs/implementation-plan.md` for phase-by-phase build history and `docs/lrc-implementation-note.md` for the LRC certification caveat.
