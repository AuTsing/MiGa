package com.autsing.miga.presentation.data

import kotlinx.serialization.Serializable

@Serializable
data class Auth(
    val deviceId: String,
    val cookies: Map<String, String>,
)

@Serializable
data class ServiceLoginRespData(
    val qs: String,
    val _sign: String,
    val callback: String,
    val location: String,
)

@Serializable
data class LoginUrlRespData(
    val loginUrl: String,
    val lp: String,
)


@Serializable
data class LoginLpRespData(
    val location: String,
    val userId: Long,
    val cUserId: String,
    val nonce: Long,
    val ssecurity: String,
    val psecurity: String,
    val passToken: String,
)
