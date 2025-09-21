package my.stormov.kauthserver.yggdrasil.service

import my.stormov.kauthserver.yggdrasil.config.YggProperties
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest

@Service
class TextureStorageService(private val ygg: YggProperties) {

    fun storeSkin(bytes: ByteArray): String {
        require(ygg.textures.serveLocal) { "Local texture storage is disabled (ygg.textures.serve-local=false)" }
        val skinsDir = Paths.get(ygg.textures.storageDir, "skins")
        Files.createDirectories(skinsDir)

        val hash = sha256(bytes)
        val filename = "$hash.png"
        val target: Path = skinsDir.resolve(filename)

        if (!Files.exists(target)) {
            Files.write(target, bytes)
        }
        return texturesBaseUrl().trimEnd('/') + "/skins/$filename"
    }

    private fun sha256(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val d = md.digest(bytes)
        return d.joinToString("") { "%02x".format(it) }
    }

    private fun texturesBaseUrl(): String {
        return ygg.textures.baseUrl
            ?: (ygg.baseUrl?.trimEnd('/')?.plus("/textures")
                ?: error("Configure ygg.base-url or ygg.textures.base-url"))
    }
}