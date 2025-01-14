package com.dauleets.flutter_tiktok_sdk

import android.app.Activity
import android.content.Intent
import androidx.annotation.NonNull
import com.tiktok.open.sdk.auth.AuthApi
import com.tiktok.open.sdk.auth.AuthRequest
import com.tiktok.open.sdk.auth.AuthResponse
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry

import java.security.MessageDigest
import java.security.SecureRandom

class FlutterTiktokSdkPlugin : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware, PluginRegistry.NewIntentListener {

    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private var tikTokAuthApi: AuthApi? = null
    private var loginResult: MethodChannel.Result? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "com.dauleets/flutter_tiktok_sdk")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        when (call.method) {
            "setup" -> {
                val clientKey = call.argument<String>("clientKey")
                if (clientKey.isNullOrEmpty()) {
                    result.error("invalid_client_key", "Client key is missing or invalid", null)
                    return
                }
                tikTokAuthApi = AuthApi(activity!!)
                result.success(null)
            }
            "login" -> {
                val scope = call.argument<String>("scope") ?: ""
                val state = call.argument<String>("state") ?: ""
                val codeVerifier = PKCEUtils.generateCodeVerifier()

                val authRequest = AuthRequest(
                    clientKey = call.argument<String>("clientKey") ?: "",
                    scope = scope,
                    redirectUri = "https://behype.io/tiktoksuccesslogin",
                    codeVerifier = codeVerifier,
                    state = state,
                )

                val success = tikTokAuthApi?.authorize(authRequest, AuthApi.AuthMethod.ChromeTab) ?: false
                if (!success) {
                    result.error("authorization_failed", "Authorization process could not be started", null)
                } else {
                    loginResult = result
                }
            }
            else -> result.notImplemented()
        }
    }

    override fun onNewIntent(intent: Intent): Boolean {
        val redirectUrl = "https://your-redirect-uri.com"
        val authResponse = tikTokAuthApi?.getAuthResponseFromIntent(intent, redirectUrl)

        if (authResponse != null) {
            if (authResponse.authCode.isNotEmpty()) {
                val resultData = mapOf(
                    "authCode" to authResponse.authCode,
                    "state" to authResponse.state,
                    "grantedPermissions" to authResponse.grantedPermissions,
                )
                loginResult?.success(resultData)
            } else {
                loginResult?.error(
                    authResponse.errorCode.toString(),
                    authResponse.errorMsg ?: "Unknown error",
                    null
                )
            }
            loginResult = null
            return true
        }
        return false
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addOnNewIntentListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}


object PKCEUtils {
    private const val BYTE_ARRAY_SIZE = 32

    fun generateCodeVerifier(): String {
        val alphanumeric = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val secureRandom = SecureRandom()
        return (1..BYTE_ARRAY_SIZE)
            .map { alphanumeric[secureRandom.nextInt(alphanumeric.size)] }
            .joinToString("")
    }

    fun generateCodeChallenge(codeVerifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(codeVerifier.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
