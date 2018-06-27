package eyow.xyz.webapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class WebActivity extends AppCompatActivity {
    private WebView webview;
    private LinearLayout.LayoutParams params;
    private LinearLayout linearLayout;
    private ValueCallback<Uri> mUploadCallbackBelow;
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final static int REQUEST_CODE = 10000;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        linearLayout = findViewById(R.id.test_webview);
        //创建WebView实例
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        webview = new WebView(getApplicationContext());
        webview.setLayoutParams(params);
        setDefaultWebSettings(webview);

//        webview.loadUrl("http://work.flybycloud.com:1013/login?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0bWNJZCI6MTAwNDEsImNvcnBJZCI6MTAxMDcsInVzZXJJZCI6MjExMTIwMDQ0LCJ0cyI6MTUyODE2OTA3NTkzMX0.8yIUX_XjcCdi23D5rDWhhtgmw0jm3b2OYOGTPkga2CQ");
        webview.loadUrl("http://192.168.1.189:8080");
        linearLayout.addView(webview);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setDefaultWebSettings(WebView webView) {
        WebSettings settings = webView.getSettings();
        //5.0以上开启混合模式加载
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setJavaScriptEnabled(true);//允许js代码
        settings.setDisplayZoomControls(false);//禁用放缩
        settings.setBuiltInZoomControls(false);
        settings.setSavePassword(false);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);//不使用网络缓存
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setLoadsImagesAutomatically(true);//自动加载图片
        settings.setBlockNetworkImage(true);// 不阻塞图片
        settings.setBlockNetworkLoads(false);
        webView.setVerticalScrollbarOverlay(true);
        webView.setWebChromeClient(new WebChromeClient() {

            /**
             * 8(Android 2.2) <= API <= 10(Android 2.3)回调此方法
             */
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                Log.e("WangJ", "运行方法 openFileChooser-1");
                // (2)该方法回调时说明版本API < 21，此时将结果赋值给 mUploadCallbackBelow，使之 != null
                mUploadCallbackBelow = uploadMsg;
                takePhoto();
            }

            /**
             * 11(Android 3.0) <= API <= 15(Android 4.0.3)回调此方法
             */
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                Log.e("WangJ", "运行方法 openFileChooser-2 (acceptType: " + acceptType + ")");
                openFileChooser(uploadMsg);
            }

            /**
             * 16(Android 4.1.2) <= API <= 20(Android 4.4W.2)回调此方法
             */
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                Log.e("WangJ", "运行方法 openFileChooser-3 (acceptType: " + acceptType + "; capture: " + capture + ")");
                openFileChooser(uploadMsg);
            }

            /**
             * API >= 21(Android 5.0.1)回调此方法
             */
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                Log.e("WangJ", "运行方法 onShowFileChooser");
                // (1)该方法回调时说明版本API >= 21，此时将结果赋值给 mUploadCallbackAboveL，使之 != null
                mUploadCallbackAboveL = filePathCallback;
                takePhoto();
                return true;
            }
        });

    }

    /**
     * 调用相机
     */
    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File cameraPhoto = FileManager.getImgFile(getApplicationContext());
            imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", cameraPhoto);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            imageUri = Uri.fromFile(FileManager.getImgFile(getApplicationContext()));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        }
        startActivityForResult(intent, REQUEST_CODE);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            // 经过上边(1)、(2)两个赋值操作，此处即可根据其值是否为空来决定采用哪种处理方法
            if (mUploadCallbackBelow != null) {
                chooseBelow(resultCode, data);
            } else if (mUploadCallbackAboveL != null) {
                chooseAbove(resultCode, data);
            } else {
                Toast.makeText(this, "发生错误", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Android API < 21(Android 5.0)版本的回调处理
     *
     * @param resultCode 选取文件或拍照的返回码
     * @param data       选取文件或拍照的返回结果
     */
    private void chooseBelow(int resultCode, Intent data) {
        Log.e("WangJ", "返回调用方法--chooseBelow");

        if (RESULT_OK == resultCode) {
            updatePhotos();

            if (data != null) {
                // 这里是针对文件路径处理
                Uri uri = data.getData();
                if (uri != null) {
                    Log.e("WangJ", "系统返回URI：" + uri.toString());
                    mUploadCallbackBelow.onReceiveValue(uri);
                } else {
                    mUploadCallbackBelow.onReceiveValue(null);
                }
            } else {
                // 以指定图像存储路径的方式调起相机，成功后返回data为空
                Log.e("WangJ", "自定义结果：" + imageUri.toString());
                mUploadCallbackBelow.onReceiveValue(imageUri);
            }
        } else {
            mUploadCallbackBelow.onReceiveValue(null);
        }
        mUploadCallbackBelow = null;
    }

    /**
     * Android API >= 21(Android 5.0) 版本的回调处理
     *
     * @param resultCode 选取文件或拍照的返回码
     * @param data       选取文件或拍照的返回结果
     */
    private void chooseAbove(int resultCode, Intent data) {
        Log.e("WangJ", "返回调用方法--chooseAbove");

        if (RESULT_OK == resultCode) {
            updatePhotos();

            if (data != null) {
                // 这里是针对从文件中选图片的处理
                Uri[] results;
                Uri uriData = data.getData();
                if (uriData != null) {
                    results = new Uri[]{uriData};
                    for (Uri uri : results) {
                        Log.e("WangJ", "系统返回URI：" + uri.toString());
                    }
                    mUploadCallbackAboveL.onReceiveValue(results);
                } else {
                    mUploadCallbackAboveL.onReceiveValue(null);
                }
            } else {
                Log.e("WangJ", "自定义结果：" + imageUri.toString());
                mUploadCallbackAboveL.onReceiveValue(new Uri[]{imageUri});
            }
        } else {
            mUploadCallbackAboveL.onReceiveValue(null);
        }
        mUploadCallbackAboveL = null;
    }

    private void updatePhotos() {
        // 该广播即使多发（即选取照片成功时也发送）也没有关系，只是唤醒系统刷新媒体文件
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(imageUri);
        sendBroadcast(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && webview != null) {
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && webview != null) {
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    @Override
    public void onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack();
        } else {
            finish();
        }
    }
}
