package com.dauleets.flutter_tiktok_sdk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.bytedance.sdk.open.tiktok.TikTokOpenApiFactory
import com.bytedance.sdk.open.tiktok.api.TikTokOpenApi
import com.bytedance.sdk.open.tiktok.authorize.model.Authorization
import com.bytedance.sdk.open.tiktok.common.handler.IApiEventHandler
import com.bytedance.sdk.open.tiktok.common.model.BaseReq
import com.bytedance.sdk.open.tiktok.common.model.BaseResp
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.embedding.engine.dart.DartExecutor

class TikTokEntryActivity : Activity(), IApiEventHandler {

    private lateinit var tikTokOpenApi: TikTokOpenApi
    private lateinit var flutterEngine: FlutterEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tikTokOpenApi = TikTokOpenApiFactory.create(this)
        tikTokOpenApi.handleIntent(intent, this)

        // Инициализация FlutterEngine
        flutterEngine = FlutterEngine(this)
        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )
    }

    override fun onReq(req: BaseReq) {
        // Обработка запросов от TikTok SDK, если требуется
    }

    override fun onResp(resp: BaseResp) {
        if (resp is Authorization.Response) {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent == null) {
                finish()
                return
            }
            launchIntent.putExtra(TIKTOK_LOGIN_RESULT_SUCCESS, resp.isSuccess)
            launchIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (resp.isSuccess) {
                launchIntent.putExtra(TIKTOK_LOGIN_RESULT_AUTH_CODE, resp.authCode)
                launchIntent.putExtra(TIKTOK_LOGIN_RESULT_STATE, resp.state)
                launchIntent.putExtra(TIKTOK_LOGIN_RESULT_GRANTED_PERMISSIONS, resp.grantedPermissions)
            } else {
                launchIntent.putExtra(TIKTOK_LOGIN_RESULT_CANCEL, resp.isCancel)
                launchIntent.putExtra(TIKTOK_LOGIN_RESULT_ERROR_CODE, resp.errorCode)
                launchIntent.putExtra(TIKTOK_LOGIN_RESULT_ERROR_MSG, resp.errorMsg)
            }
            startActivity(launchIntent)
            finish()
        } else {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_TIKTOK_AUTH) {
            val authCode = data?.getStringExtra("authCode")
            val error = data?.getStringExtra("error")

            if (!authCode.isNullOrEmpty()) {
                MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.dauleets/flutter_tiktok_sdk")
                    .invokeMethod("onCodeReceived", authCode)
            } else if (!error.isNullOrEmpty()) {
                MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.dauleets/flutter_tiktok_sdk")
                    .invokeMethod("onError", error)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onErrorIntent(intent: Intent) {
        finish()
    }

    companion object {
        const val TIKTOK_LOGIN_RESULT_SUCCESS = "TIKTOK_LOGIN_RESULT_SUCCESS"
        const val TIKTOK_LOGIN_RESULT_CANCEL = "TIKTOK_LOGIN_RESULT_CANCEL"
        const val TIKTOK_LOGIN_RESULT_AUTH_CODE = "TIKTOK_LOGIN_RESULT_AUTH_CODE"
        const val TIKTOK_LOGIN_RESULT_STATE = "TIKTOK_LOGIN_RESULT_STATE"
        const val TIKTOK_LOGIN_RESULT_GRANTED_PERMISSIONS = "TIKTOK_LOGIN_RESULT_GRANTED_PERMISSIONS"
        const val TIKTOK_LOGIN_RESULT_ERROR_CODE = "TIKTOK_LOGIN_RESULT_ERROR_CODE"
        const val TIKTOK_LOGIN_RESULT_ERROR_MSG = "TIKTOK_LOGIN_RESULT_ERROR_MSG"
        const val REQUEST_CODE_TIKTOK_AUTH = 1001
    }
}
