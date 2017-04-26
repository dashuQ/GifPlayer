package com.dongnao.gifplayer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoFragmentActivity;
import com.jph.takephoto.app.TakePhotoImpl;
import com.jph.takephoto.model.InvokeParam;
import com.jph.takephoto.model.TContextWrap;
import com.jph.takephoto.model.TImage;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.permission.InvokeListener;
import com.jph.takephoto.permission.PermissionManager;
import com.jph.takephoto.permission.TakePhotoInvocationHandler;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends TakePhotoFragmentActivity implements InvokeListener {
    private static final String TAG = "dongnao";
    ImageView image;
    GifHandler gifHandler;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button tv = (Button) findViewById(R.id.sample_text);
        image = (ImageView) findViewById(R.id.image);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int type = 0;//相册为0 拍照为1
                boolean cropFlag = true;//false裁剪 true不裁剪
                boolean compressFlag = true;//是否压缩 false压缩 true不压缩
                //调起图片选择
                TakephotoUtils.pictureSelection(getTakePhoto(), type, cropFlag, compressFlag);


            }
        });

    }

    //===================takephoto_library=====================
    //TakePhoto
    private TakePhoto takePhoto;

    /**
     * 获取TakePhoto实例
     *
     * @return
     */
    public TakePhoto getTakePhoto() {
        if (takePhoto == null) {
            takePhoto = (TakePhoto) TakePhotoInvocationHandler.of(this).bind(new TakePhotoImpl(this, this));
        }
        return takePhoto;
    }

    //interface TakeResultListener回调3个方法
    @Override
    public void takeSuccess(TResult result) {
//        Log.i(TAG,"takeSuccess：" + result.getImage().getCompressPath());
        showImg(result.getImages());
    }

    @Override
    public void takeFail(TResult result, String msg) {//操作失败
//        Log.i(TAG, "takeFail:" + msg);
    }

    @Override
    public void takeCancel() {//操作被取消
//        Log.i(TAG, getResources().getString(com.jph.takephoto.R.string.msg_operation_canceled));
    }

    private void showImg(ArrayList<TImage> images) {
        if (null != images && images.size() > 0) {
            TImage tImage = images.get(0);
            String filepPath = TakephotoUtils.getFilepPath(tImage);
            if (null != filepPath) {
                File file = new File(filepPath);
                if (file.exists()) {
//                    File file = new File(Environment.getExternalStorageDirectory(), "demo.gif");
                    if (file.getAbsolutePath().endsWith(".gif")) {
                        loadGif(file);
                    } else {
                        Toast.makeText(MainActivity.this, "不是gif图片", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private InvokeParam invokeParam;

    @Override
    public PermissionManager.TPermissionType invoke(InvokeParam invokeParam) {
        PermissionManager.TPermissionType type = PermissionManager.checkPermission(TContextWrap.of(this), invokeParam.getMethod());
        if (PermissionManager.TPermissionType.WAIT.equals(type)) {
            this.invokeParam = invokeParam;
        }
        return type;
    }
    //===================takephoto_library=====================

    private void loadGif(File file) {
        gifHandler = GifHandler.load(file.getAbsolutePath());
        int width = gifHandler.getWidth(gifHandler.getGif_handler());
        int height = gifHandler.getHeight(gifHandler.getGif_handler());
        Log.i("david", "宽   " + width + "   高  " + height);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        long mNextFrameRenderTime = gifHandler.updateFrame(gifHandler.getGif_handler(), bitmap);
        Log.i(TAG, "onCreate   下一帧  " + mNextFrameRenderTime);
        myHandler.sendEmptyMessageDelayed(1, gifHandler.getDuration(gifHandler.getGif_handler()));
    }

    Handler myHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            long mNextFrameRenderTime = gifHandler.updateFrame(gifHandler.getGif_handler(), bitmap);
            Log.i("david", "时间  " + gifHandler.getDuration(gifHandler.getGif_handler()));
            myHandler.sendEmptyMessageDelayed(1, gifHandler.getDuration(gifHandler.getGif_handler()));
            image.setImageBitmap(bitmap);
        }

        ;
    };

}
