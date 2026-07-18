# Protocol Assumptions & Documented Deviations

This file consolidates every place where the PDF (`docs/PAX_ECR17_Protocol_Specification.pdf`)
was internally inconsistent, ambiguous, or silent, and records the interpretation this
codebase implements. Per the task's instructions, the PDF is authoritative; where this
document's assumption might be wrong, the fix is isolated to one parser/builder class
(named below) plus its test.

## 1. LRC byte scope

See `docs/lrc-implementation-note.md` for the full writeup. Summary: XOR chain seeded at
`0x7F`, covering every byte after the leading control byte (STX/ACK/NAK) through and
including ETX. Not backed by an official worked example — flagged as a production
certification TODO.

## 2. TAG delivery "Payment Type" field length (§5.10.1)

The PDF's prose says the value is a single digit ('0' standard / '1' bill payment), but the
field table gives it length 6 at position 11. Implemented per the table: a 6-char
zero-padded numeric field (`"000000"` / `"000001"`).

Class: `protocol.messages.tag.TagDeliveryRequest`.

## 3. Reprint Ticket section header mismatch (§5.14.1)

The PDF's §5.14.1 heading reads "Send ticket message (from Terminal)", but the field table
underneath it is unambiguously an ECR-originated request (Terminal ID, reserved, message
code 'R', print-on-ECR flag, ticket-type flag) — matching §5.14's own prose: "the ECR ...
sends the terminal this command." Implemented as an ECR→Terminal request; the true
Terminal→ECR "Send ticket" message (code 'S') is modeled separately per §5.13.

Class: `protocol.messages.ticket.ReprintTicketRequest`.

## 4. Retroactive Reversal date/time field length (§5.18.1 pos 61)

The PDF labels the format "DMMYYhhmm" (9 characters if taken literally) but gives the
field length as 10. Implemented as a 10-char field assumed to be "DDMMYYhhmm" (two-digit
day), for consistency with the equivalent field in §5.9.2 (POS status terminal date/time,
which is unambiguously "DDMMYYhhmm").

Class: `protocol.messages.retroactivereversal.RetroactiveReversalRequest`.

## 5. PAN handling / redaction

Per §1 ("PAN – Card data"), the terminal already truncates the PAN on the wire (only the
last 4 digits are real, the rest sent as '0'). This codebase goes one step further: the
`MaskedPan` value type discards everything except those last 4 digits at parse time, so no
downstream code path (persistence, logs, UI) can ever hold a longer fragment even
transiently.

Class: `protocol.messages.common.MaskedPan`.

## 6. Run DLL "return Acquirer Data" (§5.8.1 pos 17, value '2')

Explicitly marked RFU ("Reserved for Future Use") by the PDF. The enum value is modeled
for completeness (`DllTransactionType.RETURN_ACQUIRER_DATA_RFU`) but the UI does not offer
it as a default action, and its response shape is undocumented — sending it is
unsupported until Fabrick publishes the RFU behavior.

## 7. Reversal request "card requirement" flag (§5.6.1 pos 26)

The PDF documents three possible values: '0' (terminal requires the same card — the only
value PAX terminals implement), and '1'/'2', which the PDF itself calls out as "NOT
MANAGED by Pax Terminal". All three are exposed on `ReversalRequest.cardRequirementFlag`
for spec completeness, but the UI defaults to and recommends only '0'.

## 8. ACK and NAK produce the same LRC value

A direct consequence of assumption #1 (STX/ACK/NAK excluded from the XOR chain): both
ACK (`06 03 LRC`) and NAK (`15 03 LRC`) end up with `LRC = 0x7C`, since only the trailing
`ETX` byte is summed in either case. This is unusual for a control-frame LRC and is a key
thing to verify once real terminal traffic is available (see the LRC note's
production-readiness TODO) — packet *type* (ACK vs NAK) is still always determined by the
leading control byte, never by the LRC, so this does not create an ambiguity in parsing,
only a possible mismatch against a real terminal's LRC output.

## 9. No universal TCP port

The PDF describes RS232/Ethernet TCP-IP/USB as transport options (§1) but never states a
port number. The app requires the port as a mandatory Configuration field with no
hard-coded default; see `docs/production-checklist.md`.

## 10. The message code 'E' is heavily overloaded

'E' appears as: (a) the ECR→Terminal "Enable/disable printing receipt on ECR" *request*
(§5.12, ACK-only reply), and (b) the Terminal→ECR "Transaction Result" *response* for
Payment (§5.1.2), Extended Payment (§5.2.2), Reversal (§5.6.2), and Retroactive Reversal
(§5.18.2) — each with a **different field layout**. This codebase never dispatches purely
on message code for 'E': each request/response pair is a distinct, independently-tested
class, and the service layer always knows which operation it just sent, so it always knows
which parser to hand an incoming 'E' frame to (except for the Last Result replay — see #12).

## 11. POS status: silent non-response when ECR params aren't configured

§5.9.2: "If the terminal ECR connection parameters are not configured, the command will
not have response." This cannot be expressed as a parse result — it surfaces as a response
timeout (`Ecr17Error.ResponseTimeout`) at the transport/session layer, distinct from a
NAK or an LRC/framing error, and is mapped to a specific user-facing message
("Verify the ECR configuration on the POS / PAXTools application") rather than the generic
timeout message.

## 12. Last Result replay requires caller context

§5.11.2: the terminal's reply to "send last result" is "exactly the same as the last
RESULT message" — i.e. its wire shape depends entirely on *which* operation was last run
(Payment vs Refund vs Reversal vs Card Verification vs Pre-authorization family), not on
anything in the Last Result response frame itself. `Ecr17ResponseDispatcher` therefore
requires the caller to supply the expected `OperationType`, sourced from local operation
history, and returns `null` (never a guessed/wrong parse) when that context is unavailable
or refers to an operation not covered by §5.11's save list (Payment/Offset/Credit/
Pre-authorization family/Card check — Retroactive Reversal is not in that list and is
excluded here).
