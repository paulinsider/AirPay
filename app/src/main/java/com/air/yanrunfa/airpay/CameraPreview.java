package com.air.yanrunfa.airpay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.air.yanrunfa.palmprint.Palmprint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.Exception;import java.lang.Override;import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by YanRunFa on 2015/4/9.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    public Camera mCamera;
    public Bitmap bitmap;
    public boolean isSucceed=false;
    public boolean isRunning=false;
    public CameraActivity.mHandler myHandler;
    private Camera.AutoFocusCallback mAutoFocusCallback;
    private int failNum=0;

    public CameraPreview(Context context, Camera camera, CameraActivity.mHandler mHandler){
        super(context);

        mCamera=camera;
        mHolder=getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        myHandler=mHandler;

        mAutoFocusCallback = new Camera.AutoFocusCallback() {

            public void onAutoFocus(boolean success, Camera camera) {
                // TODO Auto-generated method stub
                if(success){
                    mCamera.setOneShotPreviewCallback(null);
                    //mCamera.setPreviewCallback(mPreviewCallBack);
                }

            }
        };
    }


    public void surfaceCreated(SurfaceHolder holder){
        try{
            mCamera.setPreviewDisplay(holder);
        }catch (IOException e){
            Log.d("TAG", "Error: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder){

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w,int h){
        if(mHolder.getSurface()==null){
            return;
        }
        try{
            mCamera.stopPreview();
        }catch (Exception e){
            Log.d("TAG", "Error: " + e.getMessage());
        }

        try{
            List<Camera.Size> list=mCamera.getParameters().getSupportedPreviewSizes();
            mHolder.setFixedSize(list.get(0).width, list.get(0).height);
            mCamera.setPreviewDisplay(mHolder);
            Camera.Parameters parameters=mCamera.getParameters();
            parameters.setPreviewSize(list.get(0).width, list.get(0).height);
            mCamera.setParameters(parameters);
            setTimeTask();
            mCamera.startPreview();
            mCamera.autoFocus(mAutoFocusCallback);
        }catch (Exception e){
            Log.d("TAG", "Error: " + e.getMessage());
        }
    }
    private Camera.PreviewCallback mPreviewCallBack=new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] bytes, Camera camera) {
            isRunning = true;
            bitmap = decodeToBitMap(bytes, mCamera);
            Palmprint palmprint = new Palmprint(bitmap);
            Bundle bundle = new Bundle();
            try {
                bundle.putByteArray("palmprint", palmprint.extraction());
                isSucceed = true;
                Message msg = new Message();
                msg.what = 0x112;
                msg.setData(bundle);
                myHandler.sendMessage(msg);
            } catch (IOException e) {
                failNum++;
                if (failNum == 100) {
                    bitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth() / 2 - 250, bitmap.getHeight() / 2 - 250, 500, 500);
                    bundle.putByteArray("palmprint", palmprint.extractionWithROI(bitmap));
                    Message msg = new Message();
                    msg.what = 0x112;
                    msg.setData(bundle);
                    myHandler.sendMessage(msg);
                    isSucceed = true;
                } else {
                    Message msg = new Message();
                    msg.what = 0x111;
                    myHandler.sendMessage(msg);
                }
                isRunning = false;
            }
        }


    };



    private Bitmap decodeToBitMap(byte[] data, Camera _camera) {
        Camera.Size size = mCamera.getParameters().getPreviewSize();
        Bitmap bitmap;
        try {
            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            stream.close();
            Matrix matrix = new Matrix();
            matrix.reset();
            matrix.setRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bitmap;
        } catch (Exception ex) {
            Log.e("Sys", "Error:" + ex.getMessage());
        }
        return null;
    }

    private void setTimeTask(){
        final Timer timer=new Timer();
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                if (!isSucceed){
                    if (!isRunning){
                        mCamera.setPreviewCallback(mPreviewCallBack);
                    }
                }
                else {
                    timer.cancel();
                    mCamera.stopPreview();
                }
            }
        };
        timer.schedule(timerTask,2000,500);
    }


}
