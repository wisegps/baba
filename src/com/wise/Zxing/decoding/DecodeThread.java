package com.wise.Zxing.decoding;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;
import com.wise.car.BarcodeActivity;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

final class DecodeThread extends Thread {

  public static final String BARCODE_BITMAP = "barcode_bitmap";

  private final BarcodeActivity activity;
  private final Hashtable<DecodeHintType, Object> hints;
  private Handler handler;
  private final CountDownLatch handlerInitLatch;

  DecodeThread(BarcodeActivity activity,
               Vector<BarcodeFormat> decodeFormats,
               String characterSet,
               ResultPointCallback resultPointCallback) {

    this.activity = activity;
    handlerInitLatch = new CountDownLatch(1);

    hints = new Hashtable<DecodeHintType, Object>(3);

//    // The prefs can't change while the thread is running, so pick them up once here.
//    if (decodeFormats == null || decodeFormats.isEmpty()) {
//      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
//      decodeFormats = new Vector<BarcodeFormat>();
//      if (prefs.getBoolean(PreferencesActivity.KEY_DECODE_1D, true)) {
//        decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
//      }
//      if (prefs.getBoolean(PreferencesActivity.KEY_DECODE_QR, true)) {
//        decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
//      }
//      if (prefs.getBoolean(PreferencesActivity.KEY_DECODE_DATA_MATRIX, true)) {
//        decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
//      }
//    }
    if (decodeFormats == null || decodeFormats.isEmpty()) {
    	 decodeFormats = new Vector<BarcodeFormat>();
    	 decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
    	 decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
    	 decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
    	 
    }
    
    hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

    if (characterSet != null) {
      hints.put(DecodeHintType.CHARACTER_SET, characterSet);
    }

    hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
  }

  Handler getHandler() {
    try {
      handlerInitLatch.await();
    } catch (InterruptedException ie) {
      // continue?
    }
    return handler;
  }

  @Override
  public void run() {
    Looper.prepare();
    handler = new DecodeHandler(activity, hints);
    handlerInitLatch.countDown();
    Looper.loop();
  }

}
