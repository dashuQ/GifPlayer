package com.dongnao.gifplayer;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2017/4/25.
 */

public class GifHandler {
    private  long  gif_handler;
    public native static long loadGif(String path);
    public native static int updateFrame(long gif_handler,Bitmap bitmap);

    public native static int getDuration(long gif_handler);
    public native static int getWidth(long gif_handler);
    public native static int getHeight(long gif_handler);
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public  long getGif_handler() {
        return gif_handler;
    }

    public GifHandler(long gif_handler) {
        this.gif_handler = gif_handler;
    }

    public static GifHandler load(String filepath) {
        int[] size = new int[2];
        long gif_handle = loadGif(filepath);
        if (gif_handle != 0L) {
            return new GifHandler(gif_handle);
        }
        return null;
    }
}
