package com.kidnews.tv;

import android.app.Activity;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    static final String TAG = "KidNews";
    static final String HOST = "kiri0082.github.io";
    static final String PAGE = "https://" + HOST + "/kids-news-app/?tv=1";

    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        WebView.setWebContentsDebuggingEnabled(true);
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#fff8ec"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);

        web.setWebViewClient(new WebViewClient() {
            // The page never links out, but refuse any navigation off our host regardless.
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                boolean ours = HOST.equals(req.getUrl().getHost());
                if (!ours) Log.d(TAG, "blocked navigation to " + req.getUrl());
                return !ours;
            }
            @Override public void onReceivedSslError(WebView v, SslErrorHandler h, SslError err) {
                Log.d(TAG, "SSL error: " + err);
                h.cancel();
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onConsoleMessage(ConsoleMessage m) {
                Log.d(TAG, m.message() + " @" + m.lineNumber());
                return true;
            }
        });

        web.loadUrl(PAGE);
        setContentView(web);
        web.requestFocus(View.FOCUS_DOWN);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        boolean handled = super.dispatchKeyEvent(e);
        Log.d(TAG, "key " + KeyEvent.keyCodeToString(e.getKeyCode()) + " action=" + e.getAction()
            + " handled=" + handled + " webFocus=" + web.hasFocus());
        return handled;
    }

    // Remote's Back closes an open story; on the story grid it exits the app.
    @Override
    public void onBackPressed() {
        web.evaluateJavascript("window.kidnews ? window.kidnews.back() : false",
            value -> { Log.d(TAG, "back -> " + value); if (!"true".equals(value)) finish(); });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideBars();
    }

    private void hideBars() {
        web.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    protected void onDestroy() {
        web.destroy();
        super.onDestroy();
    }
}
