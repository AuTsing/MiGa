package com.autsing.miga.presentation.data

import kotlin.math.roundToInt

sealed interface Component {

    data class Switch(
        val property: DeviceInfoV2.Property,
        val headline: String = "",
        val value: Boolean = false,
        val readOnly: Boolean = true,
    ) : Component {

        companion object {

            fun from(property: DeviceInfoV2.Property, value: DevicePropertyValueV2): Switch {
                return if (value !is DevicePropertyValueV2.Boolean) {
                    Switch(property)
                } else {
                    Switch(
                        property = property,
                        headline = property.nameZh ?: property.name,
                        value = value.value,
                        readOnly = !property.access.write,
                    )
                }
            }
        }
    }

    data class Selector(
        val property: DeviceInfoV2.Property,
        val headline: String = "",
        val value: Int = 0,
        val size: Int = 0,
        val readOnly: Boolean = true,
        val display: String = "",
    ) : Component {

        companion object {

            fun from(property: DeviceInfoV2.Property, value: DevicePropertyValueV2): Selector {
                return if (property is DeviceInfoV2.Property.DoubleRange && value is DevicePropertyValueV2.Double) {
                    val v = (((value.value - property.from) / (property.to - property.from)) * 9)
                        .roundToInt()
                        .coerceIn(0, 9)
                    Selector(
                        property = property,
                        headline = property.nameZh ?: property.name,
                        value = v,
                        size = 10,
                        readOnly = !property.access.write,
                        display = value.value.toString(),
                    )
                } else if (property is DeviceInfoV2.Property.LongRange && value is DevicePropertyValueV2.Long) {
                    val from = property.from.toDouble()
                    val to = property.to.toDouble()
                    val v = (((value.value.toDouble() - from) / (to - from)) * 10)
                        .roundToInt()
                        .coerceIn(0, 9)
                    Selector(
                        property = property,
                        headline = property.nameZh ?: property.name,
                        value = v,
                        size = 9,
                        readOnly = !property.access.write,
                        display = value.value.toString(),
                    )
                } else if (property is DeviceInfoV2.Property.IntEnum && value is DevicePropertyValueV2.Long) {
                    val i = value.value.toInt()
                    val v = property.values[i]
                    Selector(
                        property = property,
                        headline = property.nameZh ?: property.name,
                        value = i,
                        size = property.values.size,
                        readOnly = !property.access.write,
                        display = v.nameZh ?: v.name,
                    )
                } else if (property is DeviceInfoV2.Property.StringEnum && value is DevicePropertyValueV2.String) {
                    val i = property.values.indexOfFirst { it.value == value.value }
                    val v = property.values[i]
                    Selector(
                        property = property,
                        headline = property.nameZh ?: property.name,
                        value = i,
                        size = property.values.size,
                        readOnly = !property.access.write,
                        display = v.nameZh ?: v.name,
                    )
                } else {
                    Selector(property)
                }
            }
        }
    }

    data class Trigger(
        val action: DeviceInfoV2.Action,
        val headline: String,
    ) : Component {

        companion object {

            fun from(action: DeviceInfoV2.Action): Trigger {
                return Trigger(
                    action = action,
                    headline = action.nameZh ?: action.name,
                )
            }
        }
    }
}
