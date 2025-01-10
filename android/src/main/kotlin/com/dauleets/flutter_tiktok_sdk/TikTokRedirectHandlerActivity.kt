package com.dauleets.flutter_tiktok_sdk

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.plugin.common.MethodChannel

class TikTokRedirectHandlerActivity : Activity() {

    private lateinit var flutterEngine: FlutterEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация FlutterEngine
        flutterEngine = FlutterEngine(this)
        flutterEngine.dartExecutor.executeDartEntrypoint(
            io.flutter.embedding.engine.dart.DartExecutor.DartEntrypoint.createDefault()
        )
        FlutterEngineCache.getInstance().put("tiktok_engine", flutterEngine)

        // Получение данных из Uri
        val uri = intent.data
        if (uri != null && uri.toString().startsWith("https://behype.io/tiktoksuccesslogin")) {
            val code = uri.getQueryParameter("code")
            val error = uri.getQueryParameter("error")

            // Обработка данных
            if (!code.isNullOrEmpty()) {
                MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.dauleets/flutter_tiktok_sdk")
                    .invokeMethod("onCodeReceived", code)
            } else if (!error.isNullOrEmpty()) {
                MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.dauleets/flutter_tiktok_sdk")
                    .invokeMethod("onError", error)
            }
        }

        finish() // Закрытие активности после обработки
    }
}
