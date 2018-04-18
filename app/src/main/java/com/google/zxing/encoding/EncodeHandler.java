package com.google.zxing.encoding;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.activity.Callback;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

public class EncodeHandler {
    /**
     * 根据字符串生成二维码图片
     *
     * @param text   要生成的字符串
     * @param length 生成的图片边长
     */
    @SuppressLint("StaticFieldLeak")
    public static void createQRCode(String text, int length,
                                    final Callback<Bitmap> callback) {
//        创建一个线程
        new AsyncTask<Object, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Object... params) {
                String text = (String) params[0];
                int length = (int) params[1];

                try {
                    //创建哈希表，进行文字的转换
                    Hashtable<EncodeHintType, String> hints = new Hashtable<>();
                    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

                    //图像数据转换，使用了矩阵转换
                    BitMatrix matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE,
                            length, length, hints);

                    //创建一个整形数组表示该图形的每个小格子，一个
                    // 小格子表示一个1px
                    int[] pixels = new int[length * length];

                    //下面这里按照二维码的算法，逐个生成二维码的图片，
                    //两个for循环是图片横列扫描的结果
                    for (int y = 0; y < length; y++) {
                        for (int x = 0; x < length; x++) {
                            if (matrix.get(x, y)) {  //如果转换的Matrix存在，即显示黑色
                                pixels[y * length + x] = Color.BLACK;
                            } else {
                                pixels[y * length + x] = Color.WHITE;
                            }
                        }
                    }


                    Bitmap bitmap = Bitmap.createBitmap(length, length, Bitmap.Config.RGB_565);
                    bitmap.setPixels(pixels, 0, length, 0, 0, length, length);
                    return bitmap;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (callback != null) {
                    callback.onEvent(bitmap);
                }
            }
        }.execute(text, length);
    }
}
