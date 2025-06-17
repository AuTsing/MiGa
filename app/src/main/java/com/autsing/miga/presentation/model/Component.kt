package com.autsing.miga.presentation.model

sealed class Component {

    data class Switch(
        val property: DeviceInfo.Property,
        val headline: String,
        val value: Boolean,
        val readOnly: Boolean,
    ) : Component() {

        companion object {
            fun from(property: DeviceInfo.Property, value: DevicePropertyValue): Switch {
                val v = if (value is DevicePropertyValue.Boolean) {
                    value.value
                } else {
                    false
                }
                return Switch(
                    property = property,
                    headline = property.descZhCn.takeIf { it.isNotBlank() }
                        ?.split(" ", "，")[0]
                        ?: property.description,
                    value = v,
                    readOnly = !property.access.write,
                )
            }
        }
    }

    data class Slider(
        val property: DeviceInfo.Property,
        val headline: String,
        val value: Float,
        val range: DevicePropertyRange,
        val readOnly: Boolean,
        val valueDisplay: String,
    ) : Component() {

        companion object {
            fun from(property: DeviceInfo.Property, value: DevicePropertyValue): Slider {
                val (sliderValue, sliderDisplay) = when (property.range) {

                    is DevicePropertyRange.Int -> {
                        if (value is DevicePropertyValue.Int) {
                            val v = value.value
                            val min = property.range.from
                            val max = property.range.to
                            val percentage = (v - min).toFloat() / (max - min)
                            val sliderDisplay = "$v ${property.unit}"
                            Pair(percentage, sliderDisplay)
                        } else {
                            Pair(0F, "")
                        }
                    }

                    is DevicePropertyRange.Float -> {
                        if (value is DevicePropertyValue.Float) {
                            val v = value.value
                            val min = property.range.from
                            val max = property.range.to
                            val percentage = (v - min).toFloat() / (max - min).toInt()
                            val sliderDisplay = "$v ${property.unit}"
                            Pair(percentage, sliderDisplay)
                        } else {
                            Pair(0F, "")
                        }
                    }

                    else -> Pair(0F, "")
                }
                return Slider(
                    property = property,
                    headline = property.descZhCn.takeIf { it.isNotBlank() }
                        ?.split(" ", "，")[0]
                        ?: property.description,
                    value = sliderValue,
                    range = property.range,
                    readOnly = !property.access.write,
                    valueDisplay = sliderDisplay,
                )
            }
        }
    }

    data class Selector(
        val property: DeviceInfo.Property,
        val headline: String,
        val value: Int,
        val values: List<DeviceInfo.Property.Value>,
        val readOnly: Boolean,
        val valueDisplay: String,
    ) : Component() {

        companion object {
            fun from(property: DeviceInfo.Property, value: DevicePropertyValue): Selector {
                val selectorValue = property.values.find { it.value == value } ?: property.values[0]
                val selectorDisplay = selectorValue.desc_zh_cn ?: selectorValue.description
                return Selector(
                    property = property,
                    headline = property.descZhCn.takeIf { it.isNotBlank() } ?: property.name,
                    value = property.values.indexOf(selectorValue),
                    values = property.values,
                    readOnly = !property.access.write,
                    valueDisplay = selectorDisplay,
                )
            }
        }
    }

    data class Trigger(
        val action: DeviceInfo.Action,
        val headline: String,
    ) : Component() {

        companion object {
            fun from(action: DeviceInfo.Action): Trigger {
                return Trigger(
                    action = action,
                    headline = action.descZhCn.takeIf { it.isNotBlank() }
                        ?.split(" ", "，")[0]
                        ?: action.name,
                )
            }
        }
    }
}
