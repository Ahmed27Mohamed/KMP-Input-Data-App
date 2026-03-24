package com.a2004256_ahmedmohamed.inputdatadoctor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import inputdatadoctor.composeapp.generated.resources.Res
import inputdatadoctor.composeapp.generated.resources.arrowback
import inputdatadoctor.composeapp.generated.resources.notosansarabic
import io.ktor.http.ContentType
import io.ktor.websocket.Frame
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random

class AddCustomerScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        AddCustomerContent(navigator)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerContent(navController: Navigator) {

    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dateKashf by remember { mutableStateOf("") }
    var numberID by remember { mutableStateOf("") }
    var tashes by remember { mutableStateOf("") }
    var alag by remember { mutableStateOf("") }
    var nextReview by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var tests by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<PickedImage>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }

    var notificationMessage by remember { mutableStateOf<String?>(null) }

    val arabicFont = FontFamily(
        org.jetbrains.compose.resources.Font(Res.font.notosansarabic)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Row {
            Image(
                painter = painterResource(Res.drawable.arrowback),
                contentDescription = null,
                modifier = Modifier
                    .background(color = Color.Black)
                    .clickable {
                    navController.pop()
                }
            )
            Spacer(Modifier.width(10.dp))
            Text(text = "تسجيل عميل جديد", style = MaterialTheme.typography.headlineMedium, fontFamily = arabicFont)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TextFields
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("اسم المريض", fontFamily = arabicFont) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("رقم الجوال", fontFamily = arabicFont) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = dateKashf,
            onValueChange = { dateKashf = it },
            label = { Text("تاريخ الكشف", fontFamily = arabicFont) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = numberID,
            onValueChange = { numberID = it },
            label = { Text("رقم الهوية", fontFamily = arabicFont) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = tashes,
            onValueChange = { tashes = it },
            label = { Text("التشخيص", fontFamily = arabicFont) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = alag,
            onValueChange = { alag = it },
            label = { Text("العلاج", fontFamily = arabicFont) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = nextReview,
            onValueChange = { nextReview = it },
            label = { Text("المراجعة القادمة", fontFamily = arabicFont) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("ملاحظات", fontFamily = arabicFont) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = tests,
            onValueChange = { tests = it },
            label = { Text("الفحوصات والتحاليل", fontFamily = arabicFont) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                selectedImages = pickImages()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("اختيار صور (تحاليل / أشعة)", fontFamily = arabicFont)
        }

        if (selectedImages.isNotEmpty()) {
            Text(
                text = "تم اختيار ${selectedImages.size} صورة",
                color = Color.Gray,
                fontFamily = arabicFont
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                val uid = "user_${Random.nextInt(100000, 999999)}"

                scope.launch {
                    try {
                        isUploading = true

                        val imageUrls = mutableListOf<String>()

                        selectedImages.forEachIndexed { index, file ->

                            notificationMessage = "⏳ جاري رفع الصورة ${index + 1} من ${selectedImages.size}..."

                            val imageUrl = SupabaseStorageRestClient.uploadImage(
                                uid = uid,
                                index  = index,
                                bytes = file.bytes
                            )

                            imageUrls.add(imageUrl)
                        }

                        val body = buildJsonObject {
                            put("uid", uid)
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
                                kotlinx.serialization.json.JsonArray(
                                    imageUrls.map { kotlinx.serialization.json.JsonPrimitive(it) }
                                )
                            )
                        }

                        FirebaseRestClient.put("users/$uid", body)

                        notificationMessage = "تم التسجيل ورفع الصور بنجاح ✅"
                        navController.pop()

                    } catch (e: Exception) {
                        notificationMessage = "خطأ: ${e.message}"
                    } finally {
                        isUploading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        ) {
            Text(
                if (isUploading) "جاري الحفظ..." else "تسجيل",
                fontFamily = arabicFont
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        notificationMessage?.let { msg ->
            Text(
                text = msg,
                color = if (msg.contains("نجاح")) Color.Green else Color.Red,
                fontFamily = arabicFont
            )
        }
    }
}