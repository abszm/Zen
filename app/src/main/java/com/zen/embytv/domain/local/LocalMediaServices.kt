package com.zen.embytv.domain.local

interface SmbMountService {
    suspend fun connect(host: String, share: String, username: String, password: String): Result<Unit>
    suspend fun list(path: String): Result<List<SmbEntry>>
}

interface NfsMountService {
    suspend fun connect(host: String, remotePath: String): Result<Unit>
}

interface LocalScannerService {
    suspend fun scanAll(): Result<Int>
}
