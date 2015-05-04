package com.wise.baba.biz;

import java.util.Hashtable;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.wise.baba.R;

/**
 * @author honesty
 **/
public class ShowErWeiMa {
	Context context;
	View anchorView;
	int width;
	String text;

	public ShowErWeiMa(Context context, View anchorView, int width, String text) {
		this.context = context;
		this.anchorView = anchorView;
		this.width = width;
		this.text = text;
	}

	/** 弹出二维码界面 **/
	public void openErWeiMa() {
		// 得到二维码
		Bitmap bitmap = createImage();
		if (bitmap == null) {
			Toast.makeText(context, "生成二维码失败", Toast.LENGTH_SHORT).show();
			return;
		}
		LayoutInflater mLayoutInflater = LayoutInflater.from(context);
		View popunwindwow = mLayoutInflater.inflate(R.layout.pop_erweima, null);
		ImageView iv_erweima = (ImageView) popunwindwow.findViewById(R.id.iv_erweima);
		iv_erweima.setImageBitmap(bitmap);
		PopupWindow mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
	}

	private Bitmap createImage() {
		try {
			int QR_WIDTH = width;
			int QR_HEIGHT = width;
			// 需要引入core包
			QRCodeWriter writer = new QRCodeWriter();
			if (text == null || "".equals(text) || text.length() < 1) {
				return null;
			}
			// 把输入的文本转为二维码
			BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);

			System.out.println("w:" + martix.getWidth() + "h:" + martix.getHeight());

			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
			int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
			for (int y = 0; y < QR_HEIGHT; y++) {
				for (int x = 0; x < QR_WIDTH; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * QR_WIDTH + x] = 0xff000000;
					} else {
						pixels[y * QR_WIDTH + x] = 0xffffffff;
					}

				}
			}

			Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);

			bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
			return bitmap;

		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}
}
