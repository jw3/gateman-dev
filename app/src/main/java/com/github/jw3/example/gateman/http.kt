package com.github.jw3.example.gateman

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request

object http {
    fun client(ctx: Context): OkHttpClient {
        return OkHttpClient.Builder().build()
    }

    fun wsreq(url: String): Request {
        return Request.Builder().get().url(url).build()
    }
}
