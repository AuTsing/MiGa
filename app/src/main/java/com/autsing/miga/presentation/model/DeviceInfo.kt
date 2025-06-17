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
        val access: Access,
        val unit: String,
        val range: DevicePropertyRange,
        val values: List<Value>,
        val method: Method,
    ) {

        @Serializable
        data class Access(
            val read: Boolean,
            val write: Boolean,
            val notify: Boolean,
        )

        @Serializable
        data class Value(
            val value: DevicePropertyValue,
            val description: String,
            val desc_zh_cn: String? = null,
        )

        @Serializable
        data class Method(
            val siid: Int,
            val piid: Int,
        )
    }

    @Serializable
    data class Action(
        val name: String,
        val description: String,
        val descZhCn: String,
        val method: Method,
        val inPiids: List<Int>,
        val outPiids: List<Int>,
    ) {

        @Serializable
        data class Method(
            val siid: Int,
            val aiid: Int,
        )
    }
}
