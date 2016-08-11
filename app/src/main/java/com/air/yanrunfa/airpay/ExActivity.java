package com.air.yanrunfa.airpay;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.air.network.ExitAppliation;
import com.air.network.Register;
import com.air.network.SM3;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;

public class ExActivity extends Activity {
    //EditText editText;
   // Button button;

    /*View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences saveOriginal=getSharedPreferences("Original",ExActivity.this.MODE_PRIVATE);
            if (saveOriginal.getString("cipher",null)==null){
                Intent intent=new Intent(ExActivity.this, CameraActivity.class);
                intent.putExtra("register",true);
                intent.putExtra("ip",editText.getText().toString());

                startActivity(intent);
            }
            else if (!getIntent().getBooleanExtra("isLogin",false)){
                Intent intent=new Intent(ExActivity.this, CameraActivity.class);
                intent.putExtra("register",false);
                intent.putExtra("ip",editText.getText().toString());
                startActivity(intent);
            }
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ExitAppliation.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        /*editText=(EditText)findViewById(R.id.ex_ip_address);
        button=(Button)findViewById(R.id.ex_ip_button);
        button.setOnClickListener(onClickListener);*/
        SharedPreferences saveOriginal=getSharedPreferences("Original",ExActivity.this.MODE_PRIVATE);
        byte[] a = new byte[736];
        for (int i=0;i<736;i++)
        {
            a[i] = 0x01;
        }
        byte[] b = new byte[32];
        try{
            b = SM3.hash(a);
            a[0] =0x01;
        } catch (IOException e)
        {

        }

        if (saveOriginal.getString("cipher",null)==null){
            Intent intent=new Intent(ExActivity.this, CameraActivity.class);
            intent.putExtra("register",true);
            //Intent intent=new Intent(ExActivity.this, RegisterActivity.class);
            //intent.putExtra("ip",editText.getText().toString());

            startActivity(intent);
        }
        else if (!getIntent().getBooleanExtra("isLogin",false)){
            Intent intent=new Intent(ExActivity.this, CameraActivity.class);
            intent.putExtra("login",false);
            //intent.putExtra("ip",editText.getText().toString());
            //Intent intent=new Intent(ExActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
    }

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("TAG", "OpenCV loaded successfully");

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){ //默认加载opencv_java.so库
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);//加载依赖opencv_java.so的jni库
            //System.load("/app/src/main/jniLibs/armeabi-v7a/libopencv_java.so");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ex, menu);
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
}
