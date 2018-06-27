package eyow.xyz.webapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class QuickMarkActivity extends AppCompatActivity implements OnClickListener {
    private static final String TAG = "QuickMarkActivity";
    private static final int CODE_GALLERY_REQUEST = 1;
    private TextView txtShow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_mark);

        findViewById(R.id.btn_select).setOnClickListener(this);
        txtShow = findViewById(R.id.txt_show);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_select:
                openGallery();
                break;
            default:
                break;
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, CODE_GALLERY_REQUEST);

//        Intent intentFromGallery = new Intent();
//        intentFromGallery.setType("image/*");
//        intentFromGallery.setAction(Intent.ACTION_PICK);
//        startActivityForResult(intentFromGallery, CODE_GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_GALLERY_REQUEST) {
            String photoPath = PathUtil.getRealPathFromUri(this, data.getData());
            Log.d(TAG, "onActivityResult: "+photoPath);
            //显示获取到的信息
            new ParseTask(QuickMarkActivity.this).execute(photoPath);
        }
    }


    private class ParseTask extends AsyncTask<String, Integer, String> {
        // 弱引用是允许被gc回收的;
        private final WeakReference<QuickMarkActivity> weakActivity;

        ParseTask(QuickMarkActivity activity) {
            this.weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... params) {
            // 解析二维码/条码
            return QRCodeDecoder.syncDecodeQRCode(params[0]);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        protected void onPostExecute(String s) {
            QuickMarkActivity activity = weakActivity.get();
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return;
            }

            // 继续更新ui
            if (null == s) {
                Toast.makeText(activity, "图片获取失败,请重试", Toast.LENGTH_SHORT).show();
            } else {
                // 识别出图片二维码/条码，内容为s
                Log.d(TAG, "onPostExecute: " + s);
                txtShow.setText(s);
            }
        }
    }
}
