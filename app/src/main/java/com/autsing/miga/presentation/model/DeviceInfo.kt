package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(
    val name: String,
    val model: String,
    val properties: List<Property>,
    val actions: List<Action>,
) {

    @Serializable
    data class Property(
        val name: String,
        val description: String,
        val descZhCn: String,
        val type: String,
        val accesses: Set<GetDeviceInfoResponse.Props.Spec.Service.Property.Access>,
        val unit: String,
        val range: GetDeviceInfoResponse.Props.Spec.Service.Property.Ranges,
        val values: List<GetDeviceInfoResponse.Props.Spec.Service.Property.Value>,
        val method: Method,
    ) {

        @Serializable
        data class Method(
            val ssid: Int,
            val piid: Int,
        )
    }

    @Serializable
    data class Action(
        val name: String,
        val description: String,
        val descZhCn: String,
        val method: Method,
    ) {

        @Serializable
        data class Method(
            val ssid: Int,
            val aiid: Int,
        )
    }
}
