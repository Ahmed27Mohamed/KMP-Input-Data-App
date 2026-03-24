package com.a2004256_ahmedmohamed.inputdatadoctor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import inputdatadoctor.composeapp.generated.resources.Res
import inputdatadoctor.composeapp.generated.resources.arrowback
import inputdatadoctor.composeapp.generated.resources.notosansarabic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.jetbrains.compose.resources.painterResource

class EditCustomerScreen(private val customerId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val scope = rememberCoroutineScope()

        val arabicFont = FontFamily(
            org.jetbrains.compose.resources.Font(Res.font.notosansarabic)
        )

        var isLoading by remember { mutableStateOf(true) }

        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var dateKashf by remember { mutableStateOf("") }
        var numberID by remember { mutableStateOf("") }
        var tashes by remember { mutableStateOf("") }
        var alag by remember { mutableStateOf("") }
        var nextReview by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var tests by remember { mutableStateOf("") }
        var isUploading by remember { mutableStateOf(false) }
        var imageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
        val updatedImages = remember { mutableStateMapOf<Int, PickedImage>() }
        var newImages by remember { mutableStateOf<List<PickedImage>>(emptyList()) }

        var message by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            try {
                val data = FirebaseRestClient
                    .get("users/$customerId")
                    .jsonObject

                name = data["name"]?.jsonPrimitive?.content ?: ""
                phone = data["phone"]?.jsonPrimitive?.content ?: ""
                dateKashf = data["dateKashf"]?.jsonPrimitive?.content ?: ""
                numberID = data["numberID"]?.jsonPrimitive?.content ?: ""
                tashes = data["tashes"]?.jsonPrimitive?.content ?: ""
                alag = data["alag"]?.jsonPrimitive?.content ?: ""
                nextReview = data["nextReview"]?.jsonPrimitive?.content ?: ""
                notes = data["notes"]?.jsonPrimitive?.content ?: ""
                tests = data["tests"]?.jsonPrimitive?.content ?: ""

                imageUrls = data["images"]
                    ?.jsonArray
                    ?.map { it.jsonPrimitive.content }
                    ?: emptyList()

            } catch (e: Exception) {
                message = "حدث خطأ أثناء تحميل البيانات"
            } finally {
                isLoading = false
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.arrowback),
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { navigator?.pop() }
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "تعديل بيانات المريض",
                    fontFamily = arabicFont,
                    fontSize = 22.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            input("اسم المريض", name, arabicFont) { name = it }
            input("رقم الجوال", phone, arabicFont) { phone = it }
            input("تاريخ الكشف", dateKashf, arabicFont) { dateKashf = it }
            input("رقم الهوية", numberID, arabicFont) { numberID = it }
            input("التشخيص", tashes, arabicFont) { tashes = it }
            input("العلاج", alag,arabicFont) { alag = it }
            input("المراجعة القادمة", nextReview, arabicFont) { nextReview = it }
            input("ملاحظات", notes, arabicFont) { notes = it }
            input("الفحوصات والتحاليل", tests, arabicFont) { tests = it }

            if (imageUrls.isNotEmpty()) {
                Text("الصور الحالية", fontFamily = arabicFont)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    imageUrls.forEachIndexed { index, url ->
                        ImageGalleryItem(
                            url = url,
                            arabicFont = arabicFont,
                            index = index,
                            onDeleteOnline = { idx -> deleteImageOnline(idx, imageUrls, customerId) },
                            onReplaceOnline = { idx -> replaceImageOnline(idx, imageUrls, customerId) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    val picked = pickImages()
                    if (picked.isNotEmpty()) {
                        newImages = newImages + picked
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إضافة صور جديدة", fontFamily = arabicFont)
            }

            if (newImages.isNotEmpty()) {
                Text(
                    "تم اختيار ${newImages.size} صورة جديدة",
                    fontFamily = arabicFont,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            isUploading = true

                            val finalImages = mutableListOf<String>()

                            updatedImages.forEach { (idx, image) ->
                                val imageUrl = SupabaseStorageRestClient.uploadImage(
                                    uid = customerId,
                                    index  = imageUrls.size + idx,
                                    bytes = image.bytes
                                )
                                finalImages.add(imageUrl)
                            }

                            imageUrls.forEachIndexed { idx, url ->
                                if (!updatedImages.containsKey(idx)) finalImages.add(url)
                            }

                            newImages.forEachIndexed { i, img ->
                                val uniqueIndex = imageUrls.size + i + updatedImages.size
                                val imageUrl = SupabaseStorageRestClient.uploadImage(
                                    uid = customerId,
                                    index  = uniqueIndex,
                                    bytes = img.bytes
                                )
                                finalImages.add(imageUrl)
                            }

                            val body = buildJsonObject {
                                put("name", name)
                                put("phone", phone)
                                put("dateKashf", dateKashf)
                                put("numberID", numberID)
                                put("tashes", tashes)
                                put("alag", alag)
                                put("nextReview", nextReview)
                                put("notes", notes)
                                put("tests", tests)
                                put(
                                    "images",
                                    kotlinx.serialization.json.JsonArray(finalImages.map {
                                        kotlinx.serialization.json.JsonPrimitive(it)
                                    })
                                )
                            }

                            FirebaseRestClient.put("users/$customerId", body)
                            navigator?.pop()

                        } catch (e: Exception) {
                            message = "حدث خطأ أثناء حفظ التعديلات: ${e.message}"
                        } finally {
                            isUploading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading
            ) {
                Text(
                    if (isUploading) "جاري الحفظ..." else "حفظ التعديلات",
                    fontFamily = arabicFont
                )
            }

            message?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = Color.Red, fontFamily = arabicFont)
            }
        }
    }
}

suspend fun deleteImageOnline(
    idx: Int,
    imageUrls: List<String>,
    customerId: String
): List<String> {
    // حذف من Supabase
    SupabaseStorageRestClient.deleteImage(uid = customerId, index = idx)

    // إنشاء نسخة جديدة بعد حذف العنصر
    val updatedList = imageUrls.toMutableList().also { it.removeAt(idx) }

    // تحديث Firebase
    val body = buildJsonObject {
        put("images", kotlinx.serialization.json.JsonArray(updatedList.map { kotlinx.serialization.json.JsonPrimitive(it) }))
    }
    FirebaseRestClient.put("users/$customerId", body)

    return updatedList
}

suspend fun replaceImageOnline(
    idx: Int,
    imageUrls: List<String>,
    customerId: String
): List<String> {
    val picked = pickImages()
    if (picked.isNotEmpty()) {
        val img = picked.first()

        // رفع الصورة الجديدة على Supabase
        val newUrl = SupabaseStorageRestClient.uploadImage(customerId, idx, img.bytes)

        // إنشاء نسخة جديدة بعد التعديل
        val updatedList = imageUrls.toMutableList().also { it[idx] = newUrl }

        // تحديث Firebase
        val body = buildJsonObject {
            put("images", kotlinx.serialization.json.JsonArray(updatedList.map { kotlinx.serialization.json.JsonPrimitive(it) }))
        }
        FirebaseRestClient.put("users/$customerId", body)

        return updatedList
    }

    return imageUrls
}

@Composable
fun ImageGalleryItem(
    url: String,
    index: Int,
    onDeleteOnline: suspend (Int) -> Unit,
    onReplaceOnline: suspend (Int) -> Unit,
    arabicFont: FontFamily
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var image by remember { mutableStateOf<ImageBitmap?>(null) }

        LaunchedEffect(url) {
            image = loadImage(url)
        }

        image?.let {
            Image(bitmap = it, contentDescription = null)
        }

        Spacer(Modifier.height(4.dp))

        Row {
            Text(
                text = "حذف",
                color = Color.Red,
                fontFamily = arabicFont,
                modifier = Modifier
                    .clickable {  scope.launch { onDeleteOnline(index) } }
                    .padding(4.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "تعديل",
                color = Color.Blue,
                fontFamily = arabicFont,
                modifier = Modifier
                    .clickable {  scope.launch { onReplaceOnline(index) } }
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun input(label: String, value: String, arabicFont: FontFamily, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontFamily = arabicFont) },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
}