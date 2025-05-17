package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class GetHomesData(
    val fg: Boolean,
    val fetch_share: Boolean,
    val fetch_share_dev: Boolean,
    val limit: Int,
    val app_ver: Int,
)
