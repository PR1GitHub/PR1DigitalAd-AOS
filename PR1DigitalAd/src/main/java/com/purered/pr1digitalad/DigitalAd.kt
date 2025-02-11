package com.purered.pr1digitalad

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.Toast
import com.google.gson.Gson

data class DigitalAdInput(
    val domain: String,
    val apiKey:String,
    val storeKey:String,
    val viewMode:String,
    val cartItems: List<Map<String, Any>>?,
    val clippedCoupons: List<Map<String, Any>>?,
    var environment: String? ="PROD",// "QA",
    var payloadJsonString:String?,
    var callbackHandler: (ActionPayload) -> Unit,
)
data class ActionPayload(
    var status: String? = "",
    var message: String? = "",
    var value:Any?,
    var actionContext: Any? = null,
    var actionName: String? = "",
    var customData: String? = ""
)

class DigitalAd  @JvmOverloads constructor(
    context: Context,
    options:DigitalAdInput,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) {

    private val webView: WebView
    private val config :DigitalAdInput = options

    var parentContext = context

    var viewRef: WebView? = null
        get() = webView

    init {
        webView = WebView(context, attrs, defStyleAttr)

        WebView.setWebContentsDebuggingEnabled(true)
        init(context)
    }
    fun convertMapToJsonString(map: DigitalAdInput): String {
        val gson = Gson()
        return gson.toJson(map)
    }


    private fun init(context: Context) {
        // Initialize WebView settings, such as enabling JavaScript
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.setSupportZoom(true)
        webView.requestFocus(View.FOCUS_DOWN);
        webView.focusable = View.FOCUSABLE_AUTO
        webView.setFocusableInTouchMode(true)
        webView.requestFocusFromTouch()

        webView.addJavascriptInterface(this,"pr1NativeWrapper")

        // Validate Client with API call then load WebView..
        var domainString = config.domain
        var urlWithDomain = "https://${domainString}/pr1da/native/index.html?env=aos"

        loadUrl(urlWithDomain)

        webView.webViewClient = object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {

                var optionsJSONString:String =  convertMapToJsonString(config)

                val javascriptCode = """ 
                                    initDigitalAd($optionsJSONString)
                                """.trimIndent()
                super.onPageFinished(view, url)
                webView.evaluateJavascript(javascriptCode, null)
                Log.e("SDK-WebView","Successfully set data to window")
                //  Toast.makeText( context," DigitalAd Initiated Successfully ", Toast.LENGTH_LONG).show()

            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                // super.onReceivedError(view, request, error)
                print(error)
                Log.e("SDK-WebView",error.toString())

                //Toast.makeText(context,"Error : while loading webview.!!", Toast.LENGTH_LONG).show()
            }
        }
        // Other WebView configurations can be applied here
    }

    fun getViewReference(): WebView {
        return webView
    }

    fun updateLayoutParams(layoutParams: LinearLayout.LayoutParams){
        webView.layoutParams = layoutParams
    }

    fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    @JavascriptInterface
    fun callBackHandler(payloadJsonString: String):String{

        try {

            val actionPayload:ActionPayload = Gson().fromJson(payloadJsonString, ActionPayload::class.java)

            config.callbackHandler(actionPayload);

            return "success";
        } catch (e: Exception) {
            println("callBackHandler .... failed")
            println(e)
            return "failed";
        }

    }

    fun  dispatch(payload: ActionPayload){
        val payLoadString:String =  Gson().toJson(payload)

        val jSScriptString =  """ 
                                    digitalAdHandler($payLoadString)
                                """.trimIndent()
        webView.post {
            webView.evaluateJavascript(jSScriptString) { returnValue: String ->
                {
                    //Toast.makeText(this.parentContext, returnValue, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}