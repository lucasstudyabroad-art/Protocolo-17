# LRC Implementation Note

## What the PDF says

§2.1 (Logical communication protocol / Protocol rules and flow):

> All messages include a control byte: LRC. LRC character is computed executing an
> exclusive OR on any message byte, using as base value 0x7F.

§4.1-§4.3 define three frame shapes, each ending in an LRC byte:

- Application packet: `STX(1) + message(N) + ETX(1) + LRC(1)`
- ACK: `ACK(1) + ETX(1) + LRC(1)`
- NAK: `NAK(1) + ETX(1) + LRC(1)`

The PDF does **not** include a worked example (no sample hex dump with an expected LRC
value), and does not explicitly say whether the leading control byte (STX/ACK/NAK)
participates in the XOR chain.

## Chosen interpretation (this codebase)

- **Initial value**: `0x7F`, applied as the seed before XOR-folding any bytes (i.e. the
  first XOR is `0x7F ^ byte[0]`).
- **Included bytes**: every byte of the frame **after** the leading control byte, up to and
  including `ETX`.
  - Application packet: `message bytes` + `ETX`.
  - ACK/NAK: just `ETX` (the control byte itself is excluded, so ACK and NAK happen to
    produce the same LRC: `0x7F ^ 0x03 = 0x7C`).
- **Excluded bytes**: `STX`/`ACK`/`NAK` (the very first byte of the frame) and the `LRC`
  byte itself (it is the output, not an input).

This is the conventional scope used by comparable Italian ECR/POS serial protocols of
this family (message + ETX, leading control byte excluded), and it is the interpretation
implemented in `Ecr17FrameCodec` / `Ecr17StreamParser`.

## Assumptions

1. STX is excluded from the XOR chain (only "message" bytes + ETX participate, matching
   the PDF's phrasing "exclusive OR on any message byte" — STX is a framing byte, not a
   message byte).
2. ETX is included (it's the natural terminator of the XOR chain and is present in the
   byte sequence being protected).
3. The XOR is a simple left-to-right fold with no bit reversal, no two's complement, and
   no post-processing (e.g. no final XOR mask beyond the running value).
4. ACK and NAK share the same LRC value (`0x7C`) because both are two-byte-plus-control
   frames with identical trailing bytes (`ETX`) and the control byte itself is excluded
   from the sum. This is unusual (most protocols would differentiate ACK/NAK LRC) but
   follows directly from assumption 1; it is flagged here explicitly as a risk area.

## Test vectors (self-consistent, not vendor-certified)

Implemented in `Ecr17LrcCalculatorTest` / `Ecr17FrameCodecTest`:

| Frame | Bytes XORed (hex) | Seed | LRC |
|---|---|---|---|
| ACK | `03` | `7F` | `7C` |
| NAK | `03` | `7F` | `7C` |
| Application, message = "s" (1 byte, e.g. start of a POS-status-like probe) | `73 03` | `7F` | `7F ^ 73 ^ 03 = 0x0B` |
| Application, empty-content sanity (not a real frame, just an LRC-only check) | `03` | `7F` | `7C` |

These vectors are internally consistent (derived from the algorithm itself) and are
useful as regression tests, but they are **not** independently verified against real
PAX/PAXTools terminal traffic or an official Fabrick test vector.

## Production-readiness TODO

**Before any production or certification use**, the LRC byte-scope assumption above MUST
be validated against either:

- an official Fabrick/PAX worked example (request the ECR17 certification test vectors), or
- a live capture of traffic between a real PAXTools-configured terminal and a known-good
  ECR implementation (e.g. Wireshark/serial sniffer on a certified integration).

If the real terminal's LRC does not match this implementation's output, only
`Ecr17LrcCalculator` (and the byte-scope selection in `Ecr17FrameCodec`) need to change —
this is why LRC computation is isolated behind the `LrcCalculator` interface and never
inlined elsewhere in the codebase.

## Development-only diagnostic strategy

For bring-up against a real terminal before certification, `Ecr17FrameCodec` accepts an
injected `LrcCalculator`. A development build may wire in a diagnostic decorator that logs
every candidate LRC scope (e.g. "message only", "message+ETX", "STX+message+ETX") next to
the terminal's actual observed LRC byte, to empirically confirm the correct scope — this
must only be used to *observe* traffic, never to silently accept multiple LRC scopes as
valid during a live financial operation. No such logic silently changes behavior in
production code paths; it is opt-in via dependency injection only.
