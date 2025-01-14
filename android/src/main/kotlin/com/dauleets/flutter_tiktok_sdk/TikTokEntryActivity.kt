package com.dauleets.flutter_tiktok_sdk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.tiktok.open.sdk.auth.AuthApi
import com.tiktok.open.sdk.auth.AuthResponse
import com.tiktok.open.sdk.core.model.BaseReq
import com.tiktok.open.sdk.core.model.BaseResp
import com.tiktok.open.sdk.core.constants.Keys
import com.tiktok.open.sdk.auth.webauth.WebAuthHelper

class TikTokEntryActivity : Activity() {

    private lateinit var authApi: AuthApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authApi = AuthApi(this)

        // Handle the intent passed from TikTok SDK
        handleAuthResponse(intent)
    }

    private fun handleAuthResponse(intent: Intent?) {
        val redirectUrl = "https://your-redirect-uri.com" // Укажите ваш redirect URL
        val authResponse: AuthResponse? = authApi.getAuthResponseFromIntent(intent, redirectUrl)

        if (authResponse != null) {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent == null) {
                finish()
                return
            }

            launchIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (authResponse.authCode.isNotEmpty()) {
                launchIntent.putExtra(TIKTOK_LOGIN_RESULT_SUCCESS, true)
                launchIntent.putExtra(TIKTOK_LOGIN_RESULT_AUTH_CODE, authResponse.authCode)
                launchIntent.putExtra(TIKTOK_LOGIN_RESULT_STATE, authResponse.state)
                launchIntent.putExtra(TIKTOK_LOGIN_RESULT_GRANTED_PERMISSIONS, authResponse.grantedPermissions)
            } else {
                launchIntent.putExtra(TIKTOK_LOGIN_RESULT_SUCCESS, false)
                launchIntent.putExtra(TIKTOK_LOGIN_RESULT_ERROR_CODE, authResponse.errorCode)
                launchIntent.putExtra(TIKTOK_LOGIN_RESULT_ERROR_MSG, authResponse.errorMsg)
            }
            startActivity(launchIntent)
        }
        finish()
    }

    companion object {
        const val TIKTOK_LOGIN_RESULT_SUCCESS = "TIKTOK_LOGIN_RESULT_SUCCESS"
        const val TIKTOK_LOGIN_RESULT_AUTH_CODE = "TIKTOK_LOGIN_RESULT_AUTH_CODE"
        const val TIKTOK_LOGIN_RESULT_STATE = "TIKTOK_LOGIN_RESULT_STATE"
        const val TIKTOK_LOGIN_RESULT_GRANTED_PERMISSIONS = "TIKTOK_LOGIN_RESULT_GRANTED_PERMISSIONS"
        const val TIKTOK_LOGIN_RESULT_ERROR_CODE = "TIKTOK_LOGIN_RESULT_ERROR_CODE"
        const val TIKTOK_LOGIN_RESULT_ERROR_MSG = "TIKTOK_LOGIN_RESULT_ERROR_MSG"
    }
}
