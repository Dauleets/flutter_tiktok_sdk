package com.dauleets.flutter_tiktok_sdk

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache

class TikTokRedirectHandlerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent.data
        if (uri != null && uri.toString().startsWith("https://behype.io/tiktoksuccesslogin")) {
            val code = uri.getQueryParameter("code")
            val error = uri.getQueryParameter("error")

            if (!code.isNullOrEmpty()) {
                MethodChannel(FlutterEngineCache.getInstance().get("tiktok_engine")!!.dartExecutor, "com.dauleets/flutter_tiktok_sdk")
                    .invokeMethod("onCodeReceived", code)
            } else if (!error.isNullOrEmpty()) {
                MethodChannel(FlutterEngineCache.getInstance().get("tiktok_engine")!!.dartExecutor, "com.dauleets/flutter_tiktok_sdk")
                    .invokeMethod("onError", error)
            }
        }

        finish()
    }
}
