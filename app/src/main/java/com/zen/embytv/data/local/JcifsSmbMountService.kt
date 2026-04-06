package com.zen.embytv.data.local

import com.zen.embytv.domain.local.SmbEntry
import com.zen.embytv.domain.local.SmbMountService
import jcifs.CIFSContext
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JcifsSmbMountService : SmbMountService {
    private var baseUrl: String? = null
    private var context: CIFSContext? = null

    override suspend fun connect(
        host: String,
        share: String,
        username: String,
        password: String,
    ): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            require(host.isNotBlank()) { "Host is required" }
            require(share.isNotBlank()) { "Share is required" }

            val props = java.util.Properties().apply {
                put("jcifs.smb.client.enableSMB2", "true")
                put("jcifs.smb.client.disableSMB1", "true")
                put("jcifs.resolveOrder", "DNS")
            }
            val baseContext = BaseContext(PropertyConfiguration(props))
            val authContext = if (username.isBlank()) {
                baseContext
            } else {
                val auth = NtlmPasswordAuthenticator(null, username, password)
                baseContext.withCredentials(auth)
            }

            val normalizedBase = "smb://${host.trim().trimEnd('/')}/${share.trim().trim('/')}/"
            SmbFile(normalizedBase, authContext).listFiles()

            baseUrl = normalizedBase
            context = authContext
        }
    }

    override suspend fun list(path: String): Result<List<SmbEntry>> = runCatching {
        withContext(Dispatchers.IO) {
            val currentBase = requireNotNull(baseUrl) { "Please connect SMB first." }
            val currentContext = requireNotNull(context) { "Please connect SMB first." }

            val normalizedPath = when {
                path.isBlank() || path == "/" -> ""
                path.startsWith("/") -> path.trim('/').plus("/")
                else -> path.trim('/').plus("/")
            }
            val targetUrl = if (normalizedPath.isBlank()) currentBase else "$currentBase$normalizedPath"
            val dir = SmbFile(targetUrl, currentContext)
            val files = dir.listFiles()?.toList().orEmpty()

            files.map { file ->
                val fileName = file.name.trimEnd('/').ifBlank { file.canonicalPath }
                val nextPath = buildRelativePath(path = path, name = fileName)
                SmbEntry(
                    name = fileName,
                    path = nextPath,
                    isDirectory = file.isDirectory,
                )
            }.sortedWith(
                compareByDescending<SmbEntry> { it.isDirectory }
                    .thenBy { it.name.lowercase() },
            )
        }
    }

    private fun buildRelativePath(path: String, name: String): String {
        val cleanName = name.trim('/').trim()
        if (cleanName.isBlank()) return "/"
        return when {
            path.isBlank() || path == "/" -> "/$cleanName"
            else -> "${path.trimEnd('/')}/$cleanName"
        }
    }
}
