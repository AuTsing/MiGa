package com.autsing.miga.presentation.data

import kotlinx.serialization.json.Json

inline fun <reified T> String.decode(): Result<T> = runCatching { Json.decodeFromString<T>(this) }

inline fun <reified T> T.encode(): Result<String> = runCatching { Json.encodeToString(this) }

fun <T> Result<List<T>>.getOrDefault() = getOrDefault(emptyList())

fun <K, V> Result<Map<K, V>>.getOrDefault() = getOrDefault(emptyMap())
