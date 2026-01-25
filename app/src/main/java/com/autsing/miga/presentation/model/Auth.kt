package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class Auth(
    val deviceId: String,
    val serviceToken: String,
    val userId: Long,
    val cUserId: String,
    val nonce: Long,
    val ssecurity: String,
    val psecurity: String,
    val passToken: String,
) {

    fun toCookie(): String {
        return listOf(
            "userId=$userId",
            "cUserId=$cUserId",
            "nonce=$nonce",
            "ssecurity=$ssecurity",
            "psecurity=$psecurity",
            "passToken=$passToken",
        ).joinToString("; ")
    }
}
