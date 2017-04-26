package com.dongnao.gifplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.compress.CompressImageUtil;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.TImage;
import com.jph.takephoto.model.TakePhotoOptions;

import java.io.File;

/**
 * Created by lenovo on 2017/3/30.
 */

public class TakephotoUtils {

    /**
     * 是否裁剪配置
     * @param cropFlag false裁剪 true不裁剪
     * @return
     */
    public static CropOptions getCropOptions(boolean cropFlag) {
        if(cropFlag)return null;
        //是否裁剪：默认是(不裁剪就返回null)
        //是否裁剪：默认是
        int height = 800;
        int width = 800;
        //裁切工具：默认false 第三方
        boolean withWonCrop = false;//裁切工具：第三方
        CropOptions.Builder builder = new CropOptions.Builder();
        //尺寸和比例：宽x高或宽/高（默认宽x高）
        builder.setOutputX(width).setOutputY(height);//尺寸和比例：宽x高或宽/高（默认宽x高）
        builder.setWithOwnCrop(withWonCrop);
        return builder.create();
    }

    /*public static CropOptions getCropOptions() {
        //是否裁剪：默认是(不裁剪就返回null)
        //是否裁剪：默认是
        int height = 800;
        int width = 800;
        //裁切工具：默认false 第三方
        boolean withWonCrop = false;//裁切工具：第三方
        CropOptions.Builder builder = new CropOptions.Builder();
        //尺寸和比例：宽x高或宽/高（默认宽x高）
        builder.setOutputX(width).setOutputY(height);//尺寸和比例：宽x高或宽/高（默认宽x高）
        builder.setWithOwnCrop(withWonCrop);
        return builder.create();
    }*/

    public static String getFilepPath(TImage tImage) {
        boolean flag = false;
        File file;
        String filepPath = null;
        String compressPath = tImage.getCompressPath();//压缩成功后压缩路径path改为compressPath。
        if (!TextUtils.isEmpty(compressPath)) {
            filepPath = compressPath;
        } else {
            String originalPath = tImage.getOriginalPath();//压缩成功后返回原图路径(originalPath), 以便用户可以自行处理原图。
            if (!TextUtils.isEmpty(originalPath)) {
                filepPath = originalPath;
            }
        }
        return filepPath;
    }

//    public static String getFilepPath(TImage tImage) {
//        boolean flag = false;
//        File file;
//        String filepPath = null;
//        String compressPath = tImage.getCompressPath();//压缩成功后压缩路径path改为compressPath。
//        if (!TextUtils.isEmpty(compressPath)) {
//            filepPath = compressPath;
//        } else {
//            String originalPath = tImage.getOriginalPath();//压缩成功后返回原图路径(originalPath), 以便用户可以自行处理原图。
//            if (!TextUtils.isEmpty(originalPath)) {
//                filepPath = originalPath;
//            }
//        }
//        return filepPath;
//    }

    /**
     * 图片选择
     *
     * @param takePhoto
     * @param type      0相册选择，1拍照
     */
    public static void pictureSelection(TakePhoto takePhoto, int type,boolean cropFlag,boolean compressFlag) {
        String temp = Constants.CACHE_FILE_NAME;
        File file = new File(Environment.getExternalStorageDirectory(), File.separator + temp + File.separator + System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        Uri imageUri = Uri.fromFile(file);
        //压缩配置：4
        //是否压缩：是4-2
        configCompress(takePhoto,compressFlag);
        //选择图片配置：
        TakePhotoOptions.Builder builder = new TakePhotoOptions.Builder();
        //选择图片配置：使用TakePhoto自带相册：是
        builder.setWithOwnGallery(true);
        //其它配置：
        //纠正拍照的照片旋转角度：默认否
        takePhoto.setTakePhotoOptions(builder.create());
        if (0 == type) {//
            //最多选择：5张
            //是否裁切：默认是
            takePhoto.onPickMultipleWithCrop(1, getCropOptions(cropFlag));//裁切 相册选择 默认参数
            return;
        } else if (1 == type) {
            //是否裁切：默认是
            takePhoto.onPickFromCaptureWithCrop(imageUri, getCropOptions(cropFlag));
        }
    }

    /**
     * 默认配置
     *
     * @param takePhoto
     * @param compressFlag 是否压缩 false压缩 true不压缩
     */
    public static void configCompress(TakePhoto takePhoto,boolean compressFlag) {
        //压缩配置：4
////        if(rgCompress.getCheckedRadioButtonId()!=R.id.rbCompressYes){//是否压缩：是4-2
//        takePhoto.onEnableCompress(null, false);
////            return ;
////        }
        if(compressFlag){
            takePhoto.onEnableCompress(null, false);
            return;
        }
        int maxSize = Integer.parseInt(maxSizeStr);//大小不超过：102400B   4-4
//        int maxSize = Integer.parseInt("512000");//大小不超过：102400B   4-4
        int width = Integer.parseInt(widthStr);//裁切配置：尺寸比例：宽X高800x800  etCropWidth是宽800
        int height = Integer.parseInt(widthStr);//压缩配置：大小不超过... etHeightPx是高800
        boolean showProgressBar = true;//显示压缩进度条：是4-3
        CompressConfig config;
//        if(rgCompressTool.getCheckedRadioButtonId()==R.id.rbCompressWithOwn){//压缩工具：自带4-1
        config = new CompressConfig.Builder()
                .setMaxSize(maxSize)
                .setMaxPixel(width >= height ? width : height)
                .enableReserveRaw(enableRawFile)
                .create();
//        }else {//压缩工具：Luban
//            LubanOptions option=new LubanOptions.Builder()
//                    .setMaxHeight(height)
//                    .setMaxWidth(width)
//                    .setMaxSize(maxSize)
//                    .create();
//            config=CompressConfig.ofLuban(option);
//            config.enableReserveRaw(enableRawFile);
//        }
        takePhoto.onEnableCompress(config, showProgressBar);
    }

    public static String maxSizeStr = "25600";
    public static int mMaxSize = 25600;
    public static String widthStr = "500";
    public static int width = 500;
    public static boolean enableRawFile = false;//拍照压缩后是否保存原图：默认是


    /**
     * 对指定图片进行压缩，调用的是TakePhoto中的方法
     * @param context
     * @param imagePath
     * @param listener
     */
    public static void compress(Context context, String imagePath, CompressImageUtil.CompressListener listener) {
        new CompressImageUtil(context, new CompressConfig.Builder()
                .setMaxSize(TakephotoUtils.mMaxSize)
                .setMaxPixel(TakephotoUtils.width >= TakephotoUtils.width ? TakephotoUtils.width : TakephotoUtils.width)
                .create()).compress(imagePath, listener);
    }














    /*
    *//**
     * 是否启用像素压缩
     *//*
    private boolean enablePixelCompress = true;


    public void compress(Context context, String imagePath, CompressImageUtil.CompressListener listener) {
        if (enablePixelCompress) {
            try {
                compressImageByPixel(context, imagePath, listener);
            } catch (FileNotFoundException e) {
                listener.onCompressFailed(imagePath, String.format("图片压缩失败,%s", e.toString()));
                e.printStackTrace();
            }
        } else {
            compressImageByQuality(context,BitmapFactory.decodeFile(imagePath), imagePath, listener);
        }
    }

    *//**
     * 多线程压缩图片的质量
     *
     * @param bitmap  内存中的图片
     * @param imgPath 图片的保存路径
     * @author JPH
     * @date 2014-12-5下午11:30:43
     *//*
    private void compressImageByQuality(final Context context, final Bitmap bitmap, final String imgPath, final CompressImageUtil.CompressListener listener) {
        if (bitmap == null) {
            sendMsg(false, imgPath, "像素压缩失败,bitmap is null", listener);
            return;
        }
        new Thread(new Runnable() {//开启多线程进行压缩处理
            @Override
            public void run() {
                // TODO Auto-generated method stub
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int options = 100;
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//质量压缩方法，把压缩后的数据存放到baos中 (100表示不压缩，0表示压缩到最小)
                while (baos.toByteArray().length > mMaxSize) {//循环判断如果压缩后图片是否大于指定大小,大于继续压缩
                    baos.reset();//重置baos即让下一次的写入覆盖之前的内容
                    options -= 5;//图片质量每次减少5
                    if (options <= 5) options = 5;//如果图片质量小于5，为保证压缩后的图片质量，图片最底压缩质量为5
                    bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//将压缩后的图片保存到baos中
                    if (options == 5) break;//如果图片的质量已降到最低则，不再进行压缩
                }
//				if(bitmap!=null&&!bitmap.isRecycled()){
//					bitmap.recycle();//回收内存中的图片
//				}
                try {
                    File thumbnailFile = getThumbnailFile(context,new File(imgPath));
                    FileOutputStream fos = new FileOutputStream(thumbnailFile);//将压缩后的图片保存的本地上指定路径中
                    fos.write(baos.toByteArray());
                    fos.flush();
                    fos.close();
                    sendMsg(true, thumbnailFile.getPath(), null, listener);
                } catch (Exception e) {
                    sendMsg(false, imgPath, "质量压缩失败", listener);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    *//**
     * 按比例缩小图片的像素以达到压缩的目的
     *
     * @param imgPath
     * @return
     * @author JPH
     * @date 2014-12-5下午11:30:59
     *//*
    private void compressImageByPixel(Context context, String imgPath, CompressImageUtil.CompressListener listener) throws FileNotFoundException {
        if (imgPath == null) {
            sendMsg(false, imgPath, "要压缩的文件不存在", listener);
            return;
        }
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;//只读边,不读内容
        BitmapFactory.decodeFile(imgPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int width = newOpts.outWidth;
        int height = newOpts.outHeight;
        float maxSize = mMaxSize;
        int be = 1;
        if (width >= height && width > maxSize) {//缩放比,用高或者宽其中较大的一个数据进行计算
            be = (int) (newOpts.outWidth / maxSize);
            be++;
        } else if (width < height && height > maxSize) {
            be = (int) (newOpts.outHeight / maxSize);
            be++;
        }
        newOpts.inSampleSize = be;//设置采样率
        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;//该模式是默认的,可不设
        newOpts.inPurgeable = true;// 同时设置才会有效
        newOpts.inInputShareable = true;//。当系统内存不够时候图片自动被回收
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        if (enablePixelCompress) {
            compressImageByQuality(context,bitmap, imgPath, listener);//压缩好比例大小后再进行质量压缩
        } else {
            File thumbnailFile = getThumbnailFile(context, new File(imgPath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(thumbnailFile));

            listener.onCompressSuccess(thumbnailFile.getPath());
        }
    }

    Handler mhHandler = new Handler();

    *//**
     * 发送压缩结果的消息
     *
     * @param isSuccess 压缩是否成功
     * @param imagePath
     * @param message
     *//*
    private void sendMsg(final boolean isSuccess, final String imagePath, final String message, final CompressImageUtil.CompressListener listener) {
        mhHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isSuccess) {
                    listener.onCompressSuccess(imagePath);
                } else {
                    listener.onCompressFailed(imagePath, message);
                }
            }
        });
    }

    private File getThumbnailFile(Context context, File file) {
        if (file == null || !file.exists()) return file;
        return TFileUtils.getPhotoCacheDir(context, file);
    }

    *//**
     * 压缩结果监听器
     *//*
    public interface CompressListener {
        *//**
     * 压缩成功
     *
     * @param imgPath 压缩图片的路径
     *//*
        void onCompressSuccess(String imgPath);

        *//**
     * 压缩失败
     *
     * @param imgPath 压缩失败的图片
     * @param msg     失败的原因
     *//*
        void onCompressFailed(String imgPath, String msg);
    }
*/

}

