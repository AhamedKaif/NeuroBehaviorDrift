package com.neurobehavior.drift.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.neurobehavior.drift.R
import com.neurobehavior.drift.workers.BehaviorAnalysisWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var offlineLayout: LinearLayout
    private lateinit var btnRetry: Button

    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_REQUEST_CODE = 1001

    private val WEB_URL = "http://10.0.2.2:5000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BehaviorAnalysisWorker.schedule(this)

        webView = findViewById(R.id.webView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        progressBar = findViewById(R.id.progressBar)
        offlineLayout = findViewById(R.id.offlineLayout)
        btnRetry = findViewById(R.id.btnRetry)

        setupWebView()
        setupListeners()

        loadUrl(WEB_URL)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // Cookie persistence
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    view?.loadUrl(url)
                } else {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } catch (e: Exception) {
                        // Ignore external app schemes
                    }
                }
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
                CookieManager.getInstance().flush()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    showOfflineScreen()
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                }
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = filePathCallback

                val intent = fileChooserParams?.createIntent() ?: return false
                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)
                } catch (e: Exception) {
                    fileChooserCallback = null
                    return false
                }
                return true
            }
        }

        webView.setDownloadListener { url, _, _, _, _ ->
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            } catch (e: Exception) {
                // Ignore download fail
            }
        }
    }

    private fun setupListeners() {
        swipeRefreshLayout.setOnRefreshListener {
            if (isNetworkAvailable()) {
                hideOfflineScreen()
                webView.reload()
            } else {
                swipeRefreshLayout.isRefreshing = false
                showOfflineScreen()
            }
        }

        btnRetry.setOnClickListener {
            if (isNetworkAvailable()) {
                hideOfflineScreen()
                loadUrl(WEB_URL)
            }
        }
    }

    private fun loadUrl(url: String) {
        if (isNetworkAvailable()) {
            hideOfflineScreen()
            webView.loadUrl(url)
        } else {
            showOfflineScreen()
        }
    }

    private fun showOfflineScreen() {
        webView.visibility = View.GONE
        progressBar.visibility = View.GONE
        offlineLayout.visibility = View.VISIBLE
    }

    private fun hideOfflineScreen() {
        webView.visibility = View.VISIBLE
        offlineLayout.visibility = View.GONE
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onBackPressed() {
        if (webView.visibility == View.VISIBLE && webView.canGoBack()) {
            webView.goBack()
        } else {
            showExitConfirmationDialog()
        }
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit the application?")
            .setPositiveButton("Yes") { _, _ ->
                super.onBackPressed()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (fileChooserCallback == null) return
            val results = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            fileChooserCallback?.onReceiveValue(results)
            fileChooserCallback = null
        }
    }
}
