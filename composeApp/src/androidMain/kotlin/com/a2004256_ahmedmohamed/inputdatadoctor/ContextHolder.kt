package com.a2004256_ahmedmohamed.inputdatadoctor

import android.content.Context

private lateinit var applicationContext: Context

fun initContext(context: Context) {
    applicationContext = context
}

fun getContext(): Context = applicationContext