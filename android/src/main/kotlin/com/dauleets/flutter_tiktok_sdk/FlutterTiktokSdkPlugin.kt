package com.dauleets.flutter_tiktok_sdk

import android.app.Activity
import android.content.Intent
import androidx.annotation.NonNull
import com.bytedance.sdk.open.tiktok.TikTokOpenApiFactory
import com.bytedance.sdk.open.tiktok.TikTokOpenConfig
import com.bytedance.sdk.open.tiktok.api.TikTokOpenApi
import com.bytedance.sdk.open.tiktok.authorize.model.Authorization
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry

/** FlutterTiktokSdkPlugin */
class FlutterTiktokSdkPlugin: FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware, PluginRegistry.NewIntentListener {

    private lateinit var channel: MethodChannel
    private lateinit var tikTokOpenApi: TikTokOpenApi

    private var activity: Activity? = null
    private var activityPluginBinding: ActivityPluginBinding? = null
    private var loginResult: MethodChannel.Result? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "com.dauleets/flutter_tiktok_sdk")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        when (call.method) {
            "setup" -> {
                val activity = activity
                if (activity == null) {
                    result.error(
                        "no_activity_found",
                        "There is no valid Activity found to present TikTok SDK Login screen.",
                        null
                    )
                    return
                }

                val clientKey = call.argument<String?>("clientKey")
                if (clientKey.isNullOrEmpty()) {
                    result.error("invalid_client_key", "Client key is missing or invalid", null)
                    return
                }

                TikTokOpenApiFactory.init(TikTokOpenConfig(clientKey))
                tikTokOpenApi = TikTokOpenApiFactory.create(activity)
                result.success(null)
            }
            "login" -> {
                val request = Authorization.Request()
                val scope = call.argument<String>("scope")
                request.scope = scope ?: ""
                val state = call.argument<String>("state")
                if (!state.isNullOrEmpty()) {
                    request.state = state
                }

                request.callerLocalEntry = "com.dauleets.flutter_tiktok_sdk.TikTokEntryActivity"

                tikTokOpenApi.authorize(request)
                loginResult = result
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        bindActivityBinding(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        unbindActivityBinding()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        bindActivityBinding(binding)
    }

    override fun onDetachedFromActivity() {
        unbindActivityBinding()
    }

    private fun bindActivityBinding(binding: ActivityPluginBinding) {
        activity = binding.activity
        activityPluginBinding = binding
        binding.addOnNewIntentListener(this)
    }

    private fun unbindActivityBinding() {
        activityPluginBinding?.removeOnNewIntentListener(this)
        activity = null
        activityPluginBinding = null
    }

    override fun onNewIntent(intent: Intent): Boolean {
        val uri = intent.data
        if (uri != null) {
            // Отправка ссылки в Flutter
            val linkData = uri.toString()
            channel.invokeMethod("onLinkOpened", linkData)
            return true
        }
        return super.onNewIntent(intent)
        val isSuccess = intent.getBooleanExtra(TikTokEntryActivity.TIKTOK_LOGIN_RESULT_SUCCESS, false)
        if (isSuccess) {
            val resultMap = mapOf(
                "authCode" to intent.getStringExtra(TikTokEntryActivity.TIKTOK_LOGIN_RESULT_AUTH_CODE),
                "state" to intent.getStringExtra(TikTokEntryActivity.TIKTOK_LOGIN_RESULT_STATE),
                "grantedPermissions" to intent.getStringExtra(TikTokEntryActivity.TIKTOK_LOGIN_RESULT_GRANTED_PERMISSIONS),
            )
            loginResult?.success(resultMap)
        } else {
            val errorCode = intent.getIntExtra(TikTokEntryActivity.TIKTOK_LOGIN_RESULT_ERROR_CODE, -999)
            val errorMessage = intent.getStringExtra(TikTokEntryActivity.TIKTOK_LOGIN_RESULT_ERROR_MSG)
            loginResult?.error(
                errorCode.toString(),
                errorMessage,
                null
            )
        }
        loginResult = null
        return true
    }
}