package com.a2004256_ahmedmohamed.inputdatadoctor

import com.benasher44.uuid.UUID
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
//import io.ktor.client.plugins.logging.LogLevel
//import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.random.Random

object SupabaseStorageRestClient {

    private const val SUPABASE_URL = "https://uhbkbigqycrlidisehcp.supabase.co"

    // استخدم Service Role Key للعمليات الإدارية (تخزينه بأمان)
    private const val SUPABASE_SERVICE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVoYmtiaWdxeWNybGlkaXNlaGNwIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc2NjMyNTU4NywiZXhwIjoyMDgxOTAxNTg3fQ.hFdLVxbjGtiOcykHtmXbShqVcngWrGWEi1X4y9_U2xE"

    // أو استخدم Anon Key للعمليات العامة
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVoYmtiaWdxeWNybGlkaXNlaGNwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjYzMjU1ODcsImV4cCI6MjA4MTkwMTU4N30.xgle1DhS85T2PDSBV1Gkg2-E2W5hm2kjcy2zx4RIQjA"

    private const val BUCKET_NAME = "image" // تأكد أن الاسم صحيح

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 120_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 120_000
        }

//        install(Logging) {
//            level = LogLevel.ALL
//        }
    }

    suspend fun uploadImage(
        uid: String,
        index: Int,
        bytes: ByteArray,
        contentType: ContentType = ContentType.Image.PNG
    ): String {

        val fileName = "user_${uid}_image_$index.png"
        val url = "$SUPABASE_URL/storage/v1/object/$BUCKET_NAME/$fileName"

        val response = client.put(url) {
            header("apikey", SUPABASE_SERVICE_KEY)
            header(HttpHeaders.Authorization, "Bearer $SUPABASE_SERVICE_KEY")
            header("x-upsert", "true")
            contentType(contentType)
            setBody(bytes)
        }

        if (!response.status.isSuccess()) {
            throw Exception("Upload failed: ${response.status}")
        }

        return "$SUPABASE_URL/storage/v1/object/public/$BUCKET_NAME/$fileName"
    }


    fun getPublicImageUrl(path: String): String {
        return "$SUPABASE_URL/storage/v1/object/public/$BUCKET_NAME/$path"
    }

    suspend fun deleteImage(uid: String, index: Int) {
        val fileName = "user_${uid}_image_$index.png"
        val url = "$SUPABASE_URL/storage/v1/object/image/$fileName"

        client.delete(url) {
            header("apikey", SUPABASE_SERVICE_KEY)
            header(HttpHeaders.Authorization, "Bearer $SUPABASE_SERVICE_KEY")
        }
    }

    // دالة مساعدة لمعرفة نوع الملف
    fun getContentType(fileExtension: String): String {
        return when (fileExtension.lowercase()) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            else -> "application/octet-stream"
        }
    }

    // دالة لإنشاء مجلد جديد
    suspend fun createFolder(folderPath: String): Boolean {
        return try {
            val url = "$SUPABASE_URL/storage/v1/object/$BUCKET_NAME"

            val response = client.post(url) {
                header("apikey", SUPABASE_SERVICE_KEY)
                header(HttpHeaders.Authorization, "Bearer $SUPABASE_SERVICE_KEY")
                header("Content-Type", "application/json")
                setBody("""{"name": "$folderPath/"}""")
            }

            response.status.isSuccess()
        } catch (e: Exception) {
            println("Create folder error: ${e.message}")
            false
        }
    }
}