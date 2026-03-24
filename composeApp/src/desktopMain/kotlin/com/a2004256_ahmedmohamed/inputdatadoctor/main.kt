package com.a2004256_ahmedmohamed.inputdatadoctor

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "تسجيل بيانات للمراجعين",
    ) {
        App()
    }
}