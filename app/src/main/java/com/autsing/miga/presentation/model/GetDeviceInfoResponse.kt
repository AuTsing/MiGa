package com.autsing.miga.presentation.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class GetDeviceInfoResponse(
    val props: Props,
) {

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonIgnoreUnknownKeys
    data class Props(
        val product: Product? = null,
        val spec: Spec,
    ) {

        @OptIn(ExperimentalSerializationApi::class)
        @Serializable
        @JsonIgnoreUnknownKeys
        data class Product(
            val name: String,
            val model: String,
        )

        @OptIn(ExperimentalSerializationApi::class)
        @Serializable
        @JsonIgnoreUnknownKeys
        data class Spec(
            val name: String,
            val services: Map<String, Service>,
        ) {

            @OptIn(ExperimentalSerializationApi::class)
            @Serializable
            @JsonIgnoreUnknownKeys
            data class Service(
                val iid: Int,
                val urn: String,
                val properties: Map<String, Property>?,
                val actions: Map<String, Action>?,
            ) {

                @OptIn(ExperimentalSerializationApi::class)
                @Serializable
                @JsonIgnoreUnknownKeys
                data class Property(
                    val iid: Int,
                    val urn: String,
                    val format: String,
                    val name: String,
                    val description: String,
                    val access: Set<Access>,
                    val unit: String? = null,
                    @SerialName("value-range")
                    val valueRange: List<Int>? = null,
                    @SerialName("value-list")
                    val valueList: List<Value>? = null,
                    val desc_zh_cn: String? = null,
                ) {

                    enum class Access {
                        read,
                        write,
                        notify,
                    }

                    @OptIn(ExperimentalSerializationApi::class)
                    @Serializable
                    @JsonIgnoreUnknownKeys
                    data class Value(
                        val value: Int,
                        val description: String,
                        val desc_zh_cn: String? = null,
                    )
                }

                @OptIn(ExperimentalSerializationApi::class)
                @Serializable
                @JsonIgnoreUnknownKeys
                data class Action(
                    val iid: Int,
                    val urn: String,
                    val name: String,
                    val description: String,
                    val desc_zh_cn: String? = null,
                )
            }
        }
    }
}
