package com.autsing.miga.presentation.data

sealed interface Component {

    data class Switch(
        val property: DeviceInfoV2.Property,
        val headline: String,
        val value: Boolean,
        val readOnly: Boolean,
    ) : Component {

        companion object {

            fun from(
                property: DeviceInfoV2.Property,
                value: DevicePropertyValueV2.Boolean,
            ): Switch {
                return Switch(
                    property = property,
                    headline = property.nameZh ?: property.name,
                    value = value.value,
                    readOnly = !property.access.write,
                )
            }
        }
    }

    data class Selector(
        val property: DeviceInfoV2.Property,
        val headline: String,
        val value: Int,
        val size: Int,
        val readOnly: Boolean,
        val display: String,
    ) : Component {

        companion object {

            fun from(property: DeviceInfoV2.Property, value: DevicePropertyValueV2): Selector {
                TODO()
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
