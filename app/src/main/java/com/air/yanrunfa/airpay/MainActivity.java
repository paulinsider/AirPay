package com.air.yanrunfa.airpay;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.air.network.Cdata;
import com.air.network.ExitAppliation;
import com.air.network.Login;
import com.air.network.Register;

import java.io.IOException;
import java.lang.ref.WeakReference;

import way.pattern.App;


public class MainActivity extends Activity {
    Button mDrawButton;
    Button mDepositButton;
    Button mExitButton;
    Button mQueryButton;
    Button mTransferButton;
    byte[] bytes;
    public boolean mIsLogin=false;
    mHandler handler;

    View.OnClickListener depositListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this,DepositActivity.class));
        }
    };
    View.OnClickListener drawListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this,DrawActivity.class));
        }
    };

    View.OnClickListener queryListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this,QueryActivity.class));
        }
    };

    View.OnClickListener tranferListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this,TransferActivity.class));
        }
    };

    private void initUI(){
        mDrawButton=(Button)findViewById(R.id.main_draw_money);
        mDrawButton.setOnClickListener(drawListener);
        mDepositButton=(Button)findViewById(R.id.main_deposit);
        mDepositButton.setOnClickListener(depositListener);
        mQueryButton=(Button)findViewById(R.id.main_query);
        mQueryButton.setOnClickListener(queryListener);
        mTransferButton=(Button)findViewById(R.id.main_transfer);
        mTransferButton.setOnClickListener(tranferListener);
        mExitButton=(Button)findViewById(R.id.main_exit);
        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Cdata ex=new Cdata(5);
                ex.work();
               while (Cdata.memeda==0)
               {

               }
                ExitAppliation.getInstance().exit();
                System.exit(0);
            }
        });
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ExitAppliation.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();


        if (getIntent().getBooleanExtra("isLogin",false)){
            Bundle bundle=getIntent().getBundleExtra("palmprint");
            try{
                byte[] tmp;
                tmp=getIntent().getBundleExtra("palmprint").getByteArray("palmprint");
                byte[] bytes = new byte[352];
                for (int i =0;i<352;i++)
                {
                    bytes[i] = tmp[i];
                }
                String imei=((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
                String ip="192.168.0.103";
                handler=new mHandler(this);
                Login login=new Login(bytes,imei,this,ip,handler);
                login.work();
            } catch (Exception e) {

            }
            int tmp=0;
            while (tmp == 0)
            {
                if (Login.wronflag == 1) {
                    tmp = 1;
                    Toast.makeText(getApplicationContext(), "掌纹错误，请重试！",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(MainActivity.this, CameraActivity.class);
                    intent.putExtra("login",false);
                    startActivity(intent);
                }
            }

        }

    }


    static class mHandler extends Handler {
        WeakReference<MainActivity> mActivity;

        mHandler(MainActivity activity){
            mActivity=new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity mainActivity=mActivity.get();
            if (msg.what==0x345){
                Toast.makeText(mainActivity,"登陆成功",Toast.LENGTH_SHORT).show();
            }
        }
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
}
