package com.autsing.miga.presentation.data

import kotlinx.serialization.json.Json

val defaultJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

inline fun <reified T> String.decode(): Result<T> = runCatching {
    defaultJson.decodeFromString<T>(this)
}

inline fun <reified T> T.encode(): Result<String> = runCatching { defaultJson.encodeToString(this) }

fun <T> Result<List<T>>.getOrDefault() = getOrDefault(emptyList())

fun <K, V> Result<Map<K, V>>.getOrDefault() = getOrDefault(emptyMap())
