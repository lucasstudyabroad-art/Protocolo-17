package com.fabrick.ecr17.protocol.messages.status

/** Terminal status codes — PDF §5.9.2 pos 31. */
enum class PosTerminalStatus(val code: Char, val description: String, val operative: Boolean) {
    NOT_CONFIGURED('0', "Terminal not configured", false),
    CONFIGURED_NO_DLL('1', "Terminal configured, no DLL", false),
    OPERATIVE('2', "Terminal operative (after a DLL)", true),
    NOT_ALIGNED('3', "Terminal not aligned (First DLL requested)", false),
    KEY_CORRUPTED('4', "KMPB/KPOS key corrupted (first DLL requested)", false),
    DLL_SOLICITED_PENDING('5', "DLL solicited by GT pending", false),
    REMOTE_UPDATE_PENDING('6', "Remote SW update request pending", false),
    ;

    companion object {
        /** Unknown codes are never silently treated as operative — callers must check for null. */
        fun fromCode(code: Char): PosTerminalStatus? = entries.find { it.code == code }
    }
}

/** A single terminal software module version — PDF §5.9.2 pos 32, e.g. "SYS03.4E". */
data class SoftwareModule(val moduleId: String, val versionLabel: String)

/** POS status response — PDF §5.9.2, message code 's'. */
data class PosStatusResponse(
    val terminalId: String,
    /** Raw "DDMMYYhhmm" as received; parsing into a real date/time is a UI-layer concern. */
    val terminalDateTimeRaw: String,
    val status: PosTerminalStatus,
    val softwareModules: List<SoftwareModule>,
)
