package my.stormov.kauthserver.yggdrasil.api.dto.minecraftservices

data class Privileges(
    val onlineChat: PrivilegeFlag,
    val multiplayerServer: PrivilegeFlag,
    val multiplayerRealms: PrivilegeFlag,
    val telemetry: PrivilegeFlag
)