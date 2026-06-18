package com.tored.bridgelauncher.api2.webview

import android.content.Intent
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.tored.bridgelauncher.api2.server.BridgeServer
import com.tored.bridgelauncher.webview.AccompanistWebViewClient
import kotlinx.coroutines.runBlocking

private const val TAG = "BridgeWebViewClient"

class BridgeWebViewClient(
    private val _bridgeServer: BridgeServer,
) : AccompanistWebViewClient()
{
    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse?
    {
        return request?.let {
            runBlocking { _bridgeServer.handle(request) }
        }
    }

    override fun onPageFinished(view: WebView, url: String?)
    {
        Log.d(TAG, "onPageFinished: $url")
        super.onPageFinished(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean
    {
        val url = request.url.toString()
        return handleIntentUrl(view, url)
    }

    @Suppress("DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean
    {
        return handleIntentUrl(view, url)
    }

    private fun handleIntentUrl(view: WebView, url: String): Boolean
    {
        if (!url.startsWith("intent://")) return false

        try
        {
            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            val packageName = intent.`package`

            if (packageName != null)
            {
                val pm = view.context.packageManager
                val launchIntent = pm.getLaunchIntentForPackage(packageName)

                if (launchIntent != null)
                {
                    Log.d(TAG, "Launching $packageName via getLaunchIntentForPackage")
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    view.context.startActivity(launchIntent)
                    return true
                }
            }

            val pm = view.context.packageManager
            if (intent.resolveActivity(pm) != null)
            {
                Log.d(TAG, "Launching via raw intent fallback for $url")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                view.context.startActivity(intent)
                return true
            }
        }
        catch (e: Exception)
        {
            Log.w(TAG, "Failed to handle intent URL: $url", e)
        }

        return false
    }
}
