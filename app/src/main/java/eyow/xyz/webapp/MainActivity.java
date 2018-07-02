package eyow.xyz.webapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //创建WebView实例
        webview = findViewById(R.id.local_webview);

        WebSettings webSettings = webview.getSettings();
        webSettings.setAllowContentAccess(true);
        webSettings.setJavaScriptEnabled(true);//设置支持javascript
        webSettings.setDomStorageEnabled(true);


        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("zatlog", "shouldOverrideUrlLoading: " + url);
                if (url == null) return false;

                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }

            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                Log.d("zatlog", "doUpdateVisitedHistory: " + url);
                super.doUpdateVisitedHistory(view, url, isReload);
                if (view.canGoBack()) {
                    Log.d("zatlog", "可返回");
                }
            }
        };

        webview.setWebViewClient(webViewClient);
        webview.loadUrl("http://47.98.56.196/#/home?token=nNwrBxS9u3JIEmGdYY0HkqUQk1E");


        findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, WebActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_test2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, H5Activity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (webview.canGoBack()) {
            WebBackForwardList webBackForwardList = webview.copyBackForwardList();

            Log.d(TAG, "onKeyDown: " + webBackForwardList.getSize() + ":" + webBackForwardList.getCurrentIndex() + ":" + webBackForwardList.getCurrentItem().getUrl());
            if (webBackForwardList.getCurrentIndex() == 1) {//当前处于D界面
                finish();
                return true;
            }
            webview.goBack();
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (webview.canGoBack()) {
            WebBackForwardList webBackForwardList = webview.copyBackForwardList();
            Log.d(TAG, "onKeyDown: " + webBackForwardList.getSize() + ":" + webBackForwardList.getCurrentIndex() + ":" + webBackForwardList.getCurrentItem().getUrl());
            if (webBackForwardList.getCurrentIndex() == 1) {//当前处于D界面
                finish();
            }
            webview.goBack();
        } else {
            finish();
        }
    }
}
