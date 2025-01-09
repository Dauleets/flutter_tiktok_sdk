package com.dauleets.flutter_tiktok_sdk

import android.app.Application
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Создаем экземпляр FlutterEngine
        val flutterEngine = FlutterEngine(this)
        // Кэшируем FlutterEngine с ключом
        FlutterEngineCache.getInstance().put("tiktok_engine", flutterEngine)
    }
}
