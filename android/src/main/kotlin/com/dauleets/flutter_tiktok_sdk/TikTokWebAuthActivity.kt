package io.behype.android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent

class TikTokWebAuthActivity : Activity() {

    companion object {
        const val CLIENT_KEY = "awap9f1y7lyavyr7"
        const val REDIRECT_URI = "https://behype.io/tiktoksuccesslogin"
        const val AUTH_URL = "https://www.tiktok.com/v2/auth/authorize?client_key=7288134934785425410&scope=user.info.basic%2Cbiz.creator.info%2Cbiz.creator.insights%2Cvideo.list%2Ctcm.order.update%2Cuser.info.username%2Cuser.info.stats%2Cuser.account.type%2Cuser.insights%2Cvideo.insights%2Ccomment.list%2Ccomment.list.manage%2Cvideo.publish&response_type=code&redirect_uri=https%3A%2F%2Fbehype.io%2Ftiktoksuccesslogin%2F&state=%7B%22%22%3D%22undefined%22%7D?"
        const val REQUEST_CODE = 1001
        const val EXTRA_AUTH_RESULT = "AUTH_RESULT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startWebAuth()
    }

    private fun startWebAuth() {
        val scope = "user.info.basic"
        val state = "random_state_value"

        // Формирование URL авторизации
        val authUri = Uri.parse(AUTH_URL).buildUpon().build()

        // Открытие браузера
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(this, authUri)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleAuthResult(intent)
    }

    private fun handleAuthResult(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.toString().startsWith(REDIRECT_URI)) {
            val authCode = data.getQueryParameter("code")
            val state = data.getQueryParameter("state")
            val error = data.getQueryParameter("error")

            val resultIntent = Intent()
            if (authCode != null) {
                resultIntent.putExtra(EXTRA_AUTH_RESULT, "authCode: $authCode, state: $state")
                setResult(RESULT_OK, resultIntent)
            } else {
                resultIntent.putExtra(EXTRA_AUTH_RESULT, "error: $error")
                setResult(RESULT_CANCELED, resultIntent)
            }
            finish()
        }
    }
}