package com.dauleets.flutter_tiktok_sdk_example

import io.flutter.embedding.android.FlutterActivity

class MainActivity: FlutterActivity() {
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_TIKTOK_AUTH) {
            val authCode = data?.getStringExtra("authCode")
            val error = data?.getStringExtra("error")

            if (!authCode.isNullOrEmpty()) {
                MethodChannel(flutterEngine?.dartExecutor?.binaryMessenger, "com.dauleets/flutter_tiktok_sdk")
                .invokeMethod("onCodeReceived", authCode)
            } else if (!error.isNullOrEmpty()) {
                MethodChannel(flutterEngine?.dartExecutor?.binaryMessenger, "com.dauleets/flutter_tiktok_sdk")
                .invokeMethod("onError", error)
            }
        }
    }
}
