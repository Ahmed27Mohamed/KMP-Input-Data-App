package com.a2004256_ahmedmohamed.inputdatadoctor

import android.net.Uri

interface ActivityResultCallback {
    fun onImagePicked(uri: Uri?)
}

object ActivityResultHolder {
    var callback: ActivityResultCallback? = null
}