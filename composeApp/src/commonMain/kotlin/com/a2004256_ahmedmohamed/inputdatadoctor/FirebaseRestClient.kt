package com.a2004256_ahmedmohamed.inputdatadoctor

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

object FirebaseRestClient {

    private const val BASE_URL =
        "https://input-data-doctor-default-rtdb.firebaseio.com/"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun put(path: String, data: JsonObject) {
        client.put("$BASE_URL$path.json") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(JsonObject.serializer(), data))
        }
    }

    suspend fun get(path: String): JsonElement {
        return client.get("$BASE_URL$path.json").body()
    }

    suspend fun delete(path: String) {
        client.delete("$BASE_URL$path.json")
    }
}