package junkai.com.qrcode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.activity.Callback;
import com.google.zxing.encoding.EncodeHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements OnClickListener{

    private ImageView imageView;
    private EditText et_input;
    private Bitmap mBitmap;
    private ProgressDialog mProgressDialog;
    private Button btn_createCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_input = (EditText) findViewById(R.id.et_input);
        imageView = (ImageView) findViewById(R.id.iv);
        btn_createCode = (Button) findViewById(R.id.btn_createCode);

        btn_createCode.setOnClickListener(this);

        // 文本输入框的监听事件
        et_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String trim = et_input.getText().toString().trim();
                if (trim.equals("")) {
                    btn_createCode.setEnabled(false);
                } else {
                    btn_createCode.setEnabled(true);
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        showProgress();   //调用正在生成二维码的对话框

        String text = et_input.getText().toString().trim();
        mBitmap = null;

        /*
        * text ： 获取文本信息
        * length ： 生成的位图大小
        * 调用com.google.zxing底下的方法EncodeHandler.createQRCode
        * 生成二维码
        * */
        EncodeHandler.createQRCode(text, 500, new Callback<Bitmap>() {
            @Override
            public void onEvent(Bitmap bitmap) {
                cancelProgress();   //生成后，撤销进程对话框
                mBitmap = bitmap;
                imageView.setImageBitmap(mBitmap);
                // View.GONE : mBitmap为null 的时候，也就说不存在
                imageView.setVisibility((mBitmap == null) ? View.GONE : View.VISIBLE);
            }
        });
    }

    //  二维码保存
    public void save_image(View view) {
        new AlertDialog.Builder(this)
                .setTitle("温馨提醒")
                .setMessage("是否保存二维码？")
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                check();
                            }
                        }
                )
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    //    检查SD卡是否可用，是否可写入
    private void check() {
        if (!Utils.hasSDCard()) {
            Toast.makeText(getApplicationContext(), "保存失败，SD卡不可以用！", Toast.LENGTH_SHORT).show();

            return;   //return的作用： 假如运行到这里的时候，就跳出该方法
        }

        PermissionReq.with(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .result(new PermissionReq.Result() {
                    //                    SD卡请求保存数据成功，调用以下方法
                    @Override
                    public void onGranted() {
                        save();
                    }

                    //                    SD卡请求保存数据失败，调用以下方法
                    @Override
                    public void onDenied() {
                        Toast.makeText(getApplicationContext(),  getString(R.string.no_permission,
                                "读写存储", "保存二维码图片"), Toast.LENGTH_SHORT).show();


                    }
                })
                .request();
    }

    //    图像的保存过程
    @SuppressLint("StringFormatInvalid")
    private void save() {
        /*
        * sdf:文件创建时间
        * fileName：文件名，QRCode_%s（+ sdf.format(new Date())）.jpg
        * */
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault());
        String fileName = getString(R.string.qrcode_file_name, sdf.format(new Date()));

        /*
        * Utils.getPictureDir()： 获取二维码位图 + 文件名
        *
        * */
        File file = new File(Utils.getPictureDir() + fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);

            //对生成的二维码的位图，进行compress（压缩）保存
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.qrcode_save_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        // 刷新相册
        /*
        * ACTION_MEDIA_SCANNER_SCAN_FILE： 想media加入数据，发送广播，更新
        * media里面的数据
        *
        * */
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        sendBroadcast(intent);

        Toast.makeText(getApplicationContext(), getString(R.string.qrcode_save_success, fileName), Toast.LENGTH_SHORT).show();

    }


    //    显示正在加载的对话框
    private void showProgress() {
        //第一次生成二维码的时候
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
        }

        //已经存在，但是没有显示
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    //    撤销正在加载的对话框
    private void cancelProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
