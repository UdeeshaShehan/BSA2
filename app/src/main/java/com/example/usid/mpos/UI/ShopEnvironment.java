package com.example.usid.mpos.UI;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.usid.mpos.R;

/**
 * Created by Udeesha on 1/30/2017.
 */

public class ShopEnvironment extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.environment);

        WebView htmlWebView = (WebView)findViewById(R.id.webView);
        htmlWebView.setWebViewClient(new CustomWebViewClient());
        WebSettings webSetting = htmlWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setDisplayZoomControls(true);
        htmlWebView.loadUrl("https://www.ddilushan.tk/fyp/stats.php");
    }
    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
