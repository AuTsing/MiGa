package com.autsing.miga.presentation.model

sealed class Component {

    data class Switch(
        val property: DeviceInfo.Property,
        val headline: String,
        val value: Boolean,
        val readOnly: Boolean,
    ) : Component()

    data class Slider(
        val property: DeviceInfo.Property,
        val headline: String,
        val value: Float,
        val range: DeviceInfo.Property.Range,
        val readOnly: Boolean,
        val valueDisplay: String,
    ) : Component()

    data class Selector(
        val property: DeviceInfo.Property,
        val headline: String,
        val value: Int,
        val values: List<GetDeviceInfoResponse.Props.Spec.Service.Property.Value>,
        val readOnly: Boolean,
        val valueDisplay: String,
    ) : Component()

    data class Trigger(
        val action: DeviceInfo.Action,
        val headline: String,
    ) : Component()
}
