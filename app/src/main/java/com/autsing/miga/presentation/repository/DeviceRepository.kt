package com.autsing.miga.presentation.repository

import android.annotation.SuppressLint
import android.content.Context
import com.autsing.miga.presentation.api.GetDeviceInfoRespData
import com.autsing.miga.presentation.api.reqGetDeviceIconUrl
import com.autsing.miga.presentation.api.reqGetDeviceInfo
import com.autsing.miga.presentation.api.reqGetDeviceProperties
import com.autsing.miga.presentation.api.reqGetDevices
import com.autsing.miga.presentation.data.DevicePropertyRange
import com.autsing.miga.presentation.data.DevicePropertyValue
import com.autsing.miga.presentation.data.getDeviceIconUrls
import com.autsing.miga.presentation.data.getDeviceInfo
import com.autsing.miga.presentation.data.getDevices
import com.autsing.miga.presentation.data.getFavoriteDeviceIds
import com.autsing.miga.presentation.data.setDeviceIconUrls
import com.autsing.miga.presentation.data.setDeviceInfo
import com.autsing.miga.presentation.data.setDevices
import com.autsing.miga.presentation.data.setFavoriteDeviceIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val did: String,
    val name: String,
    val isOnline: Boolean,
    val model: String,
)

fun List<Device>.sort(favoriteDeviceIds: List<String>): List<Device> {
    val favoriteDeviceIdsMap = favoriteDeviceIds.withIndex()
        .associate { it.value to it.index }
    return this.sortedBy { favoriteDeviceIdsMap[it.did] ?: Int.MAX_VALUE }
}

@Serializable
data class DeviceInfo(
    val name: String,
    val model: String,
    val properties: List<Property>,
    val actions: List<Action>,
) {

    companion object {

        fun from(model: String, resp: GetDeviceInfoRespData): DeviceInfo {
            val (deviceInfoName, deviceInfoModel) = if (resp.props.product != null) {
                Pair(
                    resp.props.product.name,
                    resp.props.product.model,
                )
            } else {
                Pair(resp.props.spec.name, model)
            }
            val deviceInfoProperties = mutableListOf<Property>()
            val deviceInfoActions = mutableListOf<Action>()

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
                        is GetDeviceInfoRespData.Props.Spec.Service.Property.Values.Uint8 -> property.valueList.values.map {
                            Property.Value(
                                value = DevicePropertyValue.Long(it.value.toLong()),
                                description = it.description,
                                desc_zh_cn = it.desc_zh_cn,
                            )
                        }

                        is GetDeviceInfoRespData.Props.Spec.Service.Property.Values.String -> property.valueList.values.map {
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

class DeviceRepository(
    private val context: Context,
) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: DeviceRepository
    }

    suspend fun getLocalDevices(): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching { context.getDevices().getOrThrow().sortedByDescending { it.isOnline } }
    }

    suspend fun getFavoriteDeviceIds(): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching { context.getFavoriteDeviceIds().getOrThrow() }
    }

    suspend fun setFavoriteDeviceIds(ids: List<String>) = withContext(Dispatchers.IO) {
        runCatching { context.setFavoriteDeviceIds(ids).getOrThrow() }
    }

    suspend fun getLocalDeviceIconUrls(): Result<Map<String, String>> =
        withContext(Dispatchers.IO) {
            runCatching { context.getDeviceIconUrls().getOrThrow() }
        }

    suspend fun getRemoteDevices(auth: Auth): Result<List<Device>> = withContext(Dispatchers.IO) {
        runCatching {
            val devices = reqGetDevices(auth).getOrThrow().sortedByDescending { it.isOnline }

            context.setDevices(devices).getOrThrow()

            devices
        }
    }

    suspend fun getRemoteDeviceIconUrls(
        devices: List<Device>,
    ): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val urls = devices
                .map { async { reqGetDeviceIconUrl(it.model).getOrDefault("") } }
                .awaitAll()
                .withIndex()
                .associate { (i, url) -> devices[i].model to url }

            context.setDeviceIconUrls(urls).getOrThrow()

            urls
        }
    }

    suspend fun getLocalDeviceInfo(
        model: String,
    ): Result<DeviceInfo> = withContext(Dispatchers.IO) {
        runCatching {
            context.getDeviceInfo(model).getOrThrow() ?: throw Exception("$model 不存在")
        }
    }

    suspend fun getRemoteDeviceInfo(
        model: String,
    ): Result<DeviceInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val info = reqGetDeviceInfo(model).getOrThrow()

            context.setDeviceInfo(model, info).getOrThrow()

            info
        }
    }

    suspend fun getDeviceProperties(
        auth: Auth,
        device: Device,
        deviceInfo: DeviceInfo,
    ): Result<List<Pair<DeviceInfo.Property, DevicePropertyValue>>> = withContext(Dispatchers.IO) {
        runCatching { reqGetDeviceProperties(auth, device, deviceInfo).getOrThrow() }
    }

    suspend fun setDeviceProperty(
        auth: Auth,
        device: Device,
        deviceProperty: DeviceInfo.Property,
        value: DevicePropertyValue,
    ): Result<Pair<DeviceInfo.Property, DevicePropertyValue>> = withContext(Dispatchers.IO) {
        TODO()
//        runCatching { requestSetDeviceProperty(auth, device, deviceProperty, value).getOrThrow() }
    }
}
