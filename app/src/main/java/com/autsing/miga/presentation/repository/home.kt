package com.autsing.miga.presentation.repository

import kotlinx.serialization.Serializable

@Serializable
data class Home(
    val id: String,
    val name: String,
)
