package com.air.yanrunfa.airpay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;

import android.widget.FrameLayout;
import android.widget.Toast;

import com.air.network.ExitAppliation;

import java.lang.ref.WeakReference;
public class CameraActivity extends Activity {
    private final static int EXTRACTION_FAIL = 0x111;
    private final static int EXTRACTION_SUCCEED=0x112;




    private Camera mCamera;
    private CameraPreview mPreview;
    private RectDrawer rectDrawer;
    //===================================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ExitAppliation.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mCamera=getCameraInstance();
        mCamera.setDisplayOrientation(90);
        mPreview=new CameraPreview(this,mCamera,new mHandler(this));
        FrameLayout preview=(FrameLayout)findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        rectDrawer=new RectDrawer(this);
        preview.addView(rectDrawer);
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Camera getCameraInstance(){
        Camera camera=null;
        try{
            camera=Camera.open();
        }catch (Exception e) {
            Log.d("TAG", "Error: " + e.getMessage());
        }
        return camera;
    }

    private boolean checkCameraHardware(Context context) {
        return (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA));
    }

    static class mHandler extends Handler{
        WeakReference<CameraActivity> mActivity;

        mHandler(CameraActivity activity){
            mActivity=new WeakReference<CameraActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CameraActivity cameraActivity=mActivity.get();
            Log.v("aaa",String.valueOf(msg.what));
            if (msg.what==EXTRACTION_SUCCEED){
                Toast toast=Toast.makeText(cameraActivity,"掌纹提取成功",Toast.LENGTH_SHORT);
                toast.show();
                if (cameraActivity.getIntent().getBooleanExtra("register",false)){
                    Intent intent=new Intent(cameraActivity, RegisterActivity.class);
                    intent.putExtra("palmprint",msg.getData());
                    intent.putExtra("ip",cameraActivity.getIntent().getStringExtra("ip"));
                    cameraActivity.startActivity(intent);
                    //cameraActivity.mCamera.release();

                }else {
                    Intent intent=new Intent(cameraActivity, MainActivity.class);
                    //Intent intent=new Intent(cameraActivity, PayActivity.class);
                    intent.putExtra("isLogin",true);
                    intent.putExtra("palmprint",msg.getData());
                  //  intent.putExtra("ip",cameraActivity.getIntent().getStringExtra("ip"));
                    cameraActivity.startActivity(intent);
                    //cameraActivity.mCamera.release();
                }
            }
            else if (msg.what==EXTRACTION_FAIL){
                //Toast toast=Toast.makeText(cameraActivity,"掌纹提取失败",Toast.LENGTH_SHORT);
                //toast.show();
            }
        }
    }


}


