package com.autsing.miga.presentation.model

import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.intOrNull

object IntToStringTransformingSerializer : JsonTransformingSerializer<String>(String.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element is JsonPrimitive) {
            return element.intOrNull?.let { JsonPrimitive(element.content) } ?: element
        }
        throw SerializationException("Expected JsonPrimitive, got $element")
    }
}
