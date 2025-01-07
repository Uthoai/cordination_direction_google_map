package com.cordination

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.cordination.databinding.ActivityShowDirectionBinding

class ShowDirectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShowDirectionBinding
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityShowDirectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val webView = binding.webView
        val url = intent.getStringExtra("MAP_URL")

        webView.webViewClient = object : WebViewClient() {
            @SuppressLint("QueryPermissionsNeeded")
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let {
                    if (it.startsWith("https://www.google.com/maps")) {
                        // Debug: Log the URL
                        android.util.Log.d("WebViewActivity", "Intercepted URL: $it")

                        // Launch Google Maps app
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it)).apply {
                            setPackage("com.google.android.apps.maps") // Force Google Maps app
                        }

                        try {
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                            } else {
                                // Fallback: Open in browser if Google Maps is not installed
                                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                                startActivity(fallbackIntent)
                            }
                        } catch (e: Exception) {
                            // Log and show a toast for unexpected errors
                            android.util.Log.e("ShowDirectionActivity", "Error launching Maps: ${e.message}")
                            Toast.makeText(this@ShowDirectionActivity, "Unable to open Maps.", Toast.LENGTH_SHORT).show()
                        }
                        return true // Prevent WebView from handling the URL
                    }
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

        }

        webView.settings.javaScriptEnabled = true

        if (url != null) {
            webView.loadUrl(url)
        }

    }


}