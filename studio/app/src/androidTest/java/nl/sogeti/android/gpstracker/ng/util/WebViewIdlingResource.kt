package nl.sogeti.android.gpstracker.ng.util

import android.support.test.espresso.IdlingResource
import android.webkit.WebChromeClient
import android.webkit.WebView
import nl.sogeti.android.gpstracker.ng.utils.executeOnUiThread

class WebViewIdlingResource(val webView: WebView) : WebChromeClient(), IdlingResource {

    var callback: IdlingResource.ResourceCallback? = null

    init {
        executeOnUiThread { webView.setWebChromeClient(this@WebViewIdlingResource) }
    }

    override fun isIdleNow(): Boolean {
        val isIdle = webView.progress == 100
        if (isIdle) {
            callback?.onTransitionToIdle()
        }
        return isIdle
    }

    override fun getName() = "WebViewResource_${webView.id}"

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (webView.progress == 100) {
            callback?.onTransitionToIdle()
        }
    }
}