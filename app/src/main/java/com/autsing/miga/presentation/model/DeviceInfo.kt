package com.autsing.miga.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(
    val name: String,
    val model: String,
    val properties: List<Property>,
    val actions: List<Action>,
) {

    companion object {

        fun from(model: String, resp: GetDeviceInfoResponse): DeviceInfo {
            val (deviceInfoName, deviceInfoModel) = if (resp.props.product != null) {
                Pair(
                    resp.props.product.name,
                    resp.props.product.model,
                )
            } else {
                Pair(resp.props.spec.name, model)
            }
            val deviceInfoProperties = mutableListOf<DeviceInfo.Property>()
            val deviceInfoActions = mutableListOf<DeviceInfo.Action>()

            val services = resp.props.spec.services.values
            for (service in services) {

                val properties = service.properties?.values ?: emptyList()
                for (property in properties) {

                    val type = property.format
                    val access = Property.Access(
                        read = property.access.contains("read"),
                        write = property.access.contains("write"),
                        notify = property.access.contains("notify"),
                    )
                    val range = DevicePropertyRange.from(property.valueRange)
                    val values = when (property.valueList) {
                        is GetDeviceInfoResponse.Props.Spec.Service.Property.Values.Uint8 -> property.valueList.values.map {
                            Property.Value(
                                value = DevicePropertyValue.Long(it.value.toLong()),
                                description = it.description,
                                desc_zh_cn = it.desc_zh_cn,
                            )
                        }

                        is GetDeviceInfoResponse.Props.Spec.Service.Property.Values.String -> property.valueList.values.map {
                            Property.Value(
                                value = DevicePropertyValue.String(it.value),
                                description = it.description,
                                desc_zh_cn = it.desc_zh_cn,
                            )
                        }

                        else -> emptyList()
                    }

                    deviceInfoProperties.add(
                        Property(
                            name = property.name,
                            description = property.description,
                            descZhCn = property.desc_zh_cn ?: "",
                            type = type,
                            access = access,
                            unit = property.unit ?: "",
                            range = range,
                            values = values,
                            method = Property.Method(
                                siid = service.iid,
                                piid = property.iid,
                            )
                        )
                    )
                }

                val actions = service.actions?.values ?: emptyList()
                for (action in actions) {
                    deviceInfoActions.add(
                        Action(
                            name = action.name,
                            description = action.description,
                            descZhCn = action.desc_zh_cn ?: "",
                            method = Action.Method(
                                siid = service.iid,
                                aiid = action.iid,
                            ),
                            inPiids = action._in,
                            outPiids = action._out,
                        )
                    )
                }
            }

            return DeviceInfo(
                name = deviceInfoName,
                model = deviceInfoModel,
                properties = deviceInfoProperties,
                actions = deviceInfoActions,
            )
        }
    }

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
