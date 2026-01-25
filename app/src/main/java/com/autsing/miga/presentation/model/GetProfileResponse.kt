package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class GetProfileResponse(
    val code: Int,
    val message: String,
    val result: Result? = null,
) {

    @Serializable
    data class Result(
        val icon: String,
        val nickname: String,
        val userid: Long,
    )
}
