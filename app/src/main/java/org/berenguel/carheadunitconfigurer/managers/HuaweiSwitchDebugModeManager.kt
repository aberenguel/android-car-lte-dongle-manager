package org.berenguel.carheadunitconfigurer.managers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.webkit.*

object HuaweiSwitchDebugModeManager {

    private const val TAG = "HuaweiDeviceModeSet"

    private var error = false

    @SuppressLint("SetJavaScriptEnabled")
    fun changeDeviceModeSet(context: Context) {

        Log.i(TAG, "Changing to DebugMode")

        val webView = WebView(context)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.v(TAG, "Loading page $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (url?.endsWith("home.html") == true) {
                    Log.v(TAG, "Loading switchDebugMode page")
                    view?.loadUrl("http://192.168.8.1/html/switchDebugMode.html")
                } else {
                    Log.i(TAG, "Finished")
                    view?.destroy()
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Log.e(TAG, "onReceivedError ${error?.errorCode} ${error?.description}")

            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                Log.e(TAG, "onReceivedHttpError $errorResponse")
            }
        }

        webView.loadUrl("http://192.168.8.1/html/home.html")
    }
}