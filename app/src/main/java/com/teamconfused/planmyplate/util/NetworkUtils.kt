package com.teamconfused.planmyplate.util

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object NetworkUtils {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseError(e: Exception): String {
        if (e is retrofit2.HttpException) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                if (!errorBody.isNullOrBlank()) {
                    val element = json.parseToJsonElement(errorBody)
                    if (element is JsonObject) {
                        // 1. Check for "message" (common in custom validation errors)
                        val message = element["message"]?.let {
                            if (it is JsonPrimitive) it.content else null
                        }
                        if (message != null) return message

                        // 2. Check for "detail" (FastAPI standard)
                        val detail = element["detail"]
                        if (detail != null) {
                            if (detail is JsonPrimitive) {
                                return detail.content
                            } else if (detail is JsonArray) {
                                val firstDetail = detail.firstOrNull()
                                if (firstDetail is JsonObject) {
                                    val msg = firstDetail["msg"]?.let {
                                        if (it is JsonPrimitive) it.content else null
                                    }
                                    if (msg != null) return msg
                                }
                            }
                        }

                        // 3. Check for "error"
                        val error = element["error"]?.let {
                            if (it is JsonPrimitive) it.content else null
                        }
                        if (error != null) return error
                    }
                }
            } catch (ex: Exception) {
                Log.e("NetworkUtils", "Failed to parse error body", ex)
            }
            return "Server Error: ${e.code()} ${e.message()}"
        }
        return e.localizedMessage ?: "An unknown error occurred"
    }
}
