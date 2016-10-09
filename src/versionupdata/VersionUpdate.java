package versionupdata;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.wise.baba.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import versionupdata.download.DownloadProgressListener;
import versionupdata.download.FileDownloader;

import java.io.File;
import java.util.logging.Logger;

/**
 * 检查更新
 * Created by Administrator on 2016/9/27.
 */
public  class VersionUpdate {

    final String TAG = "UPDATE_TEST";
    private  Context mContext;
    private  BaseVolley volley = null;

    private double vsersionLast;//服务器版本
    private double vsersionNative;//本地版本
    private String updateLogs;//版本更新日志
    private String updateApkUrl;//版本下载地址

    private final int UPDATA_PROGRESS = 0;
    private final int DOWNLOAD_FAILE  = 1;
    private final int DOWNLOAD_START  = 2;
    private int fileSize;//文件大小
    private int DOWNLOAD_THREAD_NUM = 4;

    int REQUEST_EXTERNAL_STORAGE = 1;
    String[]PERMISSIONS_STORAGE={
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
    };


    //下载包安装路径
    private static final String savePath = Environment
            .getExternalStorageDirectory().getPath() + "/updateAppPath/";
    private static final String fileName = "UpdateApp.apk";
    private static final String saveFileName = savePath + fileName;



    public VersionUpdate(Activity context){
        this.mContext = context;
        vsersionNative = Double.valueOf(WUtils.getVersion(context,context.getPackageName()));
//        if(Build.VERSION.SDK_INT>=23){
//            int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            if (permission != PackageManager.PERMISSION_GRANTED) {
//                // We don't have permission so prompt the user
//                ActivityCompat.requestPermissions(
//                        context,
//                        PERMISSIONS_STORAGE,
//                        REQUEST_EXTERNAL_STORAGE
//                );
//            }
//        }
    }

    /**
     * 检查更新
     * @param url
     * @param listener
     */
    public void check(String url,final UpdateListener listener){
        getVersionInfo(url, new OnSuccess() {
            @Override
            protected void onSuccess(String response) {
                Log.d(TAG,response);
                try {
                    JSONObject object = new JSONObject(response);
                    vsersionLast = object.getDouble("version");
                    updateApkUrl = object.getString("app_path");
                    String updateMsg = object.getString("logs");
                    Log.d(TAG, updateMsg);
                    if(vsersionLast>vsersionNative){
                        JSONArray jsonArray = new JSONArray(updateMsg);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            double logVersion = Double.valueOf(jsonArray.getJSONObject(i).getString("version"));
                            if (logVersion > vsersionNative) {
                                updateLogs = jsonArray.getJSONObject(i).getString("log").replaceAll("\\\\r\\\\n", "\n");//替换\r\n
                                showDialog(mContext,updateLogs,updateApkUrl);
                                listener.hasNewVersion(true,updateLogs,updateApkUrl);
                            }else{
                                listener.hasNewVersion(false,"nothing update","nothing update");
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new OnFailure() {
            @Override
            protected void onFailure(VolleyError error) {
                listener.hasNewVersion(false,"nothing update","nothing update");
            }
        });
    }

    /**
     * @param url 更新的地址
     * @param onSuccess 请求成功
     * @param onFailure 请求失败
     */
    private void getVersionInfo(String url, OnSuccess onSuccess, OnFailure onFailure){
        if(volley == null){
            volley = new BaseVolley();
        }
        volley.request(url, onSuccess,onFailure);
    }


    public interface UpdateListener {
        void hasNewVersion(boolean isHad,String updateMsg,String apkUrl);
    }


    /**
     * 显示dialog
     *
     * @param context
     * @param updateLogs
     * @param downUrl
     */
    private void showDialog(final Context context,String updateLogs,final String downUrl){
        CustomerDialog.Builder builder = new CustomerDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.has_new_version));
        builder.setMessage(updateLogs);
        builder.setPositiveButton(context.getResources().getString(R.string.update_now), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            if(!TextUtils.isEmpty(downUrl)){
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//                    File saveDir = Environment.getExternalStorageDirectory();
                	if(WUtils.isWifiActive(context)){
                		download(downUrl,savePath,fileName);
                	}else{
                		dialogInterface.dismiss();
                		CustomerDialog.Builder builder = new CustomerDialog.Builder(context);
                        builder.setTitle("警告");
                        builder.setMessage("当前非wifi网络环境，是否要下载？");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
								download(downUrl,savePath,fileName);
							}
						});
                        builder.setNegativeButton(new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});
                        builder.create().show();
                	}
                }else {
                    showToast(mContext,"SD卡不存在");
                }
            }else{
                showToast(mContext,"下载链接不能为空");
            }

            }
        });
        builder.setNegativeButton( new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }


    /**
     * @param path
     * @param saveDir
     */
    private void download(final String path,final String saveDir,final String fileName) {
        DownloadTask downloadTask = new DownloadTask(path,saveDir,fileName);
        new Thread(downloadTask).start();
    }


    /**
     * 下载任务
     */
    private final class DownloadTask implements Runnable{
        private String path;
        private String saveDir;
        private String fileName;
        public DownloadTask (String path,String saveDir,String fileName){
            this.path = path;
            this.saveDir = saveDir;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            try {
                FileDownloader loader = new FileDownloader(mContext,path,saveDir,fileName,DOWNLOAD_THREAD_NUM);
                fileSize =  loader.getFileSize();
//                pProgress.setMax(loader.getFileSize());//设置进度条的最大刻度
                Message message = mHandler.obtainMessage();
                message.what = DOWNLOAD_START;
                mHandler.sendMessage(message);
                loader.download(progressListener);//这是个耗时的操作，在主线程会阻塞，要开子线程
            } catch (Exception e) {
                e.printStackTrace();
                Message message = mHandler.obtainMessage();
                message.what = DOWNLOAD_FAILE;
                mHandler.sendMessage(message);
            }
        }
    }


    /**
     * 监听当前的已经下载的数量
     */
    private DownloadProgressListener progressListener = new DownloadProgressListener() {
        @Override
        public void onDownloadSize(int size) {
            Log.d("DOWNLOAD","下载进度： " + size);
            Message message = new Message();
            message.what = UPDATA_PROGRESS;
            message.getData().putInt("size",size);
            mHandler.sendMessage(message);
        }
    };


    /**
     * Handler 更新ui
     */
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATA_PROGRESS:
                    int size = msg.getData().getInt("size");
                    if(size == fileSize){
//                        showToast(mContext,"文件下载完成");
                        showInstallDialog(mContext);
                    }
                    break;
                case DOWNLOAD_FAILE:
                    showToast(mContext,mContext.getResources().getString(R.string.new_version_download_failed));
                    break;
                case DOWNLOAD_START:
                    showToast(mContext,mContext.getResources().getString(R.string.download_app_now));
                    break;
            }
        }
    };


    /**
     * 马上安装对话框
     * @param context
     */
    private void showInstallDialog(Context context){
        CustomerDialog.Builder builder = new CustomerDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.new_version_install));
        builder.setMessage(context.getResources().getString(R.string.if_install));
        builder.setPositiveButton(context.getResources().getString(R.string.install_now), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                installApk();
            }
        });
        builder.setNegativeButton( new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 弹框
     * @param context
     * @param msg
     */
    private void showToast(Context context,String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }


    /**
     * 安装apk
     *
     */
    private void installApk() {
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }
}
