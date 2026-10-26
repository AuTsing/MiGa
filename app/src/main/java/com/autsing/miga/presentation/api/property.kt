package com.autsing.miga.presentation.api

import kotlin.random.Random.Default.nextInt

private fun randomHex(length: Int): String {
    val chars = "0123456789ABCDEF"
    val value = buildString(length) {
        repeat(length) {
            append(chars[nextInt(chars.length)])
        }
    }

    return value
}

fun genPassO(): String {
    val chars = "0123456789abcdef"
    val value = buildString(16) {
        repeat(16) {
            append(chars[nextInt(chars.length)])
        }
    }

    return value
}

fun genUserAgent(po: String): String {
    val uaId1 = randomHex(40)
    val uaId2 = randomHex(32)
    val uaId3 = randomHex(32)
    val uaId4 = randomHex(40)

    val ua = "Android-15-11.0.701-Xiaomi-23046RP50C-OS2.0.212.0.VMYCNXM-" +
            "$uaId1-CN-$uaId3-$uaId2-SmartHome-MI_APP_STORE-$uaId1|$uaId4|$po-64"

    return ua
}

fun genDeviceId(): String {
    val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-"
    val value = buildString(16) {
        repeat(16) {
            append(chars[nextInt(chars.length)])
        }
    }

    return value
}
