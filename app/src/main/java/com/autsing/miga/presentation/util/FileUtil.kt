package com.autsing.miga.presentation.util

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("StaticFieldLeak")
class FileUtil(
    private val context: Context,
) {

    companion object {
        lateinit var instance: FileUtil
    }

    suspend fun readJson(filename: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val file = context.filesDir.resolve(filename)
            val json = file.readText()
            return@runCatching json
        }
    }

    suspend fun writeJson(
        filename: String,
        json: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val file = context.filesDir.resolve(filename)
            file.writeText(json)
        }
    }
}
