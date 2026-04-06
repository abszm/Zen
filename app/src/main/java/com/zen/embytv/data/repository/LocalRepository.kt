package com.zen.embytv.data.repository

interface LocalRepository {
    suspend fun browse(path: String): Result<List<String>>
    suspend fun scanLibraryRoots(): Result<Int>
}
