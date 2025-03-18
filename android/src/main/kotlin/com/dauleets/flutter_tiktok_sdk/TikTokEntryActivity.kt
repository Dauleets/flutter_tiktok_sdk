package com.dauleets.flutter_tiktok_sdk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.tiktok.open.sdk.auth.AuthApi
import com.tiktok.open.sdk.auth.AuthResponse

class TikTokEntryActivity : Activity() {

    private lateinit var authApi: AuthApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authApi = AuthApi(this)

        handleAuthResponse(intent)
    }

    private fun handleAuthResponse(intent: Intent?) {
        val redirectUrl = "https://behype.io/tiktoksuccesslogin"
        val authResponse = authApi.getAuthResponseFromIntent(intent, redirectUrl)

        if (authResponse != null) {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                launchIntent.putExtra("TIKTOK_LOGIN_RESULT_SUCCESS", authResponse.authCode.isNotEmpty())
                launchIntent.putExtra("TIKTOK_LOGIN_RESULT_AUTH_CODE", authResponse.authCode)
                launchIntent.putExtra("TIKTOK_LOGIN_RESULT_STATE", authResponse.state)
                launchIntent.putExtra("TIKTOK_LOGIN_RESULT_GRANTED_PERMISSIONS", authResponse.grantedPermissions)
                startActivity(launchIntent)
            }
        }
        finish()
    }
}