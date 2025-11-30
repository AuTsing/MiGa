package com.autsing.miga.presentation.helper

import kotlinx.serialization.json.Json

class SerdeHelper {

    companion object {
        lateinit var instance: SerdeHelper
    }

    val serializer: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    inline fun <reified T> encode(value: T): Result<String> = runCatching {
        return@runCatching serializer.encodeToString(value)
    }

    inline fun <reified T> decode(valueJson: String): Result<T> = runCatching {
        return@runCatching serializer.decodeFromString(valueJson)
    }
}
