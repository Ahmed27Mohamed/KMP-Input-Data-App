package com.a2004256_ahmedmohamed.inputdatadoctor

import androidx.compose.ui.graphics.ImageBitmap

expect fun pickImages(): List<PickedImage>
data class PickedImage(
    val name: String,
    val bytes: ByteArray
)
typealias ImageType = ImageBitmap
expect suspend fun loadImage(url: String): ImageType?