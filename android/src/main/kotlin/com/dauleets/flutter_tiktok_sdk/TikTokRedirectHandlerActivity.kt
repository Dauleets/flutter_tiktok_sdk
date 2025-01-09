package com.dauleets.flutter_tiktok_sdk

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log

class TikTokRedirectHandlerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent.data
        if (uri != null && uri.toString().startsWith("https://behype.io/tiktoksuccesslogin")) {
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            val error = uri.getQueryParameter("error")

            if (!code.isNullOrEmpty()) {
                // Успешная авторизация
                Log.d("TikTokAuth", "Authorization code: $code")
                val resultIntent = Intent().apply {
                    putExtra("authCode", code)
                    putExtra("state", state)
                }
                setResult(Activity.RESULT_OK, resultIntent)
            } else if (!error.isNullOrEmpty()) {
                // Ошибка авторизации
                Log.e("TikTokAuth", "Error: $error")
                val errorIntent = Intent().apply {
                    putExtra("error", error)
                }
                setResult(Activity.RESULT_CANCELED, errorIntent)
            }
        }
        finish()
    }
}
