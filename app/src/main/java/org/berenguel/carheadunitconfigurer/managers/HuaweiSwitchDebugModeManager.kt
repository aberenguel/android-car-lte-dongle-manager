package org.berenguel.carheadunitconfigurer.managers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.webkit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import java.lang.RuntimeException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object HuaweiSwitchDebugModeManager {

    private const val TAG = "HuaweiDeviceModeSet"

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun changeDeviceModeSet(context: Context) {

        Log.i(TAG, "Changing to DebugMode")

        val countDownLatch = CountDownLatch(1)
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
                    countDownLatch.countDown()
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Log.e(TAG, "onReceivedError ${error?.errorCode} ${error?.description}")
                countDownLatch.countDown()
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                Log.e(TAG, "onReceivedHttpError $errorResponse")
                countDownLatch.countDown()
            }
        }

        webView.loadUrl("http://192.168.8.1/html/home.html")

        // await pages loading
        val finished = withContext(Dispatchers.IO) {
            countDownLatch.await(5000, TimeUnit.MILLISECONDS)
        }
        webView.destroy()
        if (!finished) {
            throw TimeoutException()
        }
    }
}