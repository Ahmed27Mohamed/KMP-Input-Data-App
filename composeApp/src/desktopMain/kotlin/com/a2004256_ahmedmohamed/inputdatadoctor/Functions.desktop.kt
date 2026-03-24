package com.a2004256_ahmedmohamed.inputdatadoctor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import javax.imageio.ImageIO
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.withContext
import java.net.URL

actual fun pickImages(): List<PickedImage> {
    val dialog = FileDialog(Frame(), "اختر الصور", FileDialog.LOAD)
    dialog.isMultipleMode = true
    dialog.isVisible = true

    return dialog.files.map { file ->
        PickedImage(
            name = file.name,
            bytes = file.readBytes()
        )
    }
}

actual suspend fun loadImage(url: String): ImageBitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val bufferedImage = ImageIO.read(URL(url))
            bufferedImage.toComposeImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}