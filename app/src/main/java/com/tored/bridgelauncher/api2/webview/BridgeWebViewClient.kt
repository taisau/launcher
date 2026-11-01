package com.tored.bridgelauncher.api2.webview

import android.content.Intent
import com.tored.bridgelauncher.services.apps.WorkProfileApps
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.tored.bridgelauncher.api2.server.BridgeServer
import com.tored.bridgelauncher.webview.AccompanistWebViewClient
import kotlinx.coroutines.runBlocking

private const val TAG = "BridgeWebViewClient"
private const val MAX_RECOVERY_ATTEMPTS = 5
private const val RECOVERY_DELAY_MS = 300L

class BridgeWebViewClient(
    private val _bridgeServer: BridgeServer,
) : AccompanistWebViewClient()
{
    private var recoveryAttempts = 0
    private var recovering = false

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

        // A successful load of the project after a recovery: drop the dead error
        // entries so the back stack is not littered with them.
        if (recovering && url?.startsWith(BridgeServer.PROJECT_URL) == true)
        {
            recovering = false
            recoveryAttempts = 0
            view.clearHistory()
        }
    }

    // Self-healing: if the project URL ever reaches real DNS (the request was
    // issued before this client was attached), the WebView shows
    // net::ERR_NAME_NOT_RESOLVED. Reload the project; now that the client is
    // attached the request is intercepted properly.
    override fun onReceivedError(view: WebView, request: WebResourceRequest?, error: WebResourceError?)
    {
        super.onReceivedError(view, request, error)

        if (request?.isForMainFrame != true) return
        if (request.url.host?.lowercase() != BridgeServer.HOST) return
        if (recoveryAttempts >= MAX_RECOVERY_ATTEMPTS)
        {
            Log.w(TAG, "giving up recovering ${request.url} after $recoveryAttempts attempts")
            return
        }

        recoveryAttempts++
        recovering = true
        Log.w(TAG, "main-frame error on ${request.url} (attempt $recoveryAttempts), reloading project")
        view.postDelayed({ view.loadUrl(BridgeServer.PROJECT_URL) }, RECOVERY_DELAY_MS)
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
            val pm = view.context.packageManager

            // explicit component (Firefox web apps, Bridge's own activities)
            if (intent.component != null)
            {
                Log.d(TAG, "Launching explicit component ${intent.component}")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                view.context.startActivity(intent)
                return true
            }

            val packageName = intent.`package`
            if (packageName != null)
            {
                if (WorkProfileApps.tryLaunch(view.context, packageName)) return true

                val query = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setPackage(packageName)
                }
                val info = pm.queryIntentActivities(query, 0).firstOrNull()
                if (info != null)
                {
                    val launch = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        setClassName(info.activityInfo.packageName, info.activityInfo.name)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    view.context.startActivity(launch)
                    return true
                }

                val fallback = pm.getLaunchIntentForPackage(packageName)
                if (fallback != null)
                {
                    fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    view.context.startActivity(fallback)
                    return true
                }
            }

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
