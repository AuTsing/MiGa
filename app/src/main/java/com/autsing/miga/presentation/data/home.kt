package com.autsing.miga.presentation.data

import kotlinx.serialization.Serializable

@Serializable
data class Home(
    val id: String,
    val name: String,
    val uid: Long,
)

@Serializable
data class GetHomesReqData(
    val fg: Boolean,
    val fetch_share: Boolean,
    val fetch_share_dev: Boolean,
    val fetch_cariot: Boolean,
    val limit: Int,
    val app_ver: Int,
    val plat_form: Int,
)

@Serializable
data class GetHomesRespData(
    val code: Int,
    val message: String,
    val result: Result,
) {

    @Serializable
    data class Result(
        val homelist: List<Home>,
    )
}
