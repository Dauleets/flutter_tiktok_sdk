package io.behype.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import com.tiktok.open.sdk.auth.AuthApi
import com.tiktok.open.sdk.auth.AuthRequest
import com.tiktok.open.sdk.auth.utils.PKCEUtils

class MainActivity : FlutterActivity() {
    private lateinit var authApi: AuthApi
    private val CHANNEL = "com.dauleets/flutter_tiktok_sdk"
    private var codeVerifier: String? = null
    private val redirectUri = "https://behype.io/tiktoksuccesslogin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TikTokAuth", "MainActivity onCreate - INIT")
        authApi = AuthApi(activity = this)
        handleAuthResponse(intent)
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "authorize") {
                Log.d("TikTokAuth", "Flutter called 'authorize'")
                authorize()
                result.success(null)
            } else {
                result.notImplemented()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("TikTokAuth", "onNewIntent: ${intent.data}")
        handleAuthResponse(intent)
    }

    private fun authorize() {
        codeVerifier = PKCEUtils.generateCodeVerifier()
        Log.d("TikTokAuth", "Generated codeVerifier: $codeVerifier")

        val request = AuthRequest(
            clientKey = "awap9f1y7lyavyr7",
            scope = "user.info.basic",
            redirectUri = redirectUri,
            codeVerifier = codeVerifier!!
        )

        Log.d("TikTokAuth", "Sending TikTok authorize request (TikTokApp)")
        authApi.authorize(request, AuthApi.AuthMethod.TikTokApp)
    }

    private fun handleAuthResponse(intent: Intent) {
    val data = intent.data
    Log.d("TikTokAuth", "🔄 handleAuthResponse: ${data.toString()}")

    if (data != null) {
        val authCode = data.getQueryParameter("code")
        if (!authCode.isNullOrEmpty()) {
            Log.d("TikTokAuth", "✅ Получен authCode из URI: $authCode")
            
            val sharedPref = getSharedPreferences("tikTokPrefs", MODE_PRIVATE)
            sharedPref.edit().putString("authCode", authCode).apply()

            sendAuthCodeToFlutter(authCode)
            return
        }
    }

        Log.e("TikTokAuth", "❌ Ошибка: код отсутствует в intent.data")
    }


    private fun sendAuthCodeToFlutter(authCode: String?) {
        Log.d("TikTokAuth", "Sending authCode to Flutter: $authCode")
        MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger, CHANNEL)
            .invokeMethod("onAuthCodeReceived", authCode)
    }

    private fun logErrorToFlutter(errorMessage: String) {
        Log.e("TikTokAuth", "Sending error to Flutter: $errorMessage")
        MethodChannel(flutterEngine!!.dartExecutor.binaryMessenger, CHANNEL)
            .invokeMethod("onAuthError", errorMessage)
    }
}
