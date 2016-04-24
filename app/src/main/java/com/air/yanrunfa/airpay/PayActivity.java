package com.air.yanrunfa.airpay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.air.network.Cdata;
import com.air.network.ExitAppliation;
import com.air.network.Login;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by Paul Insider on 2015/5/27.
 */
public class PayActivity extends Activity{
    byte[] bytes;
    public boolean mIsLogin=false;
    mHandler handler;
    Button mExitButton;
    Button mPayButton;
    EditText tx1;
    EditText tx2;
    View.OnClickListener payListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String ie2=tx1.getText().toString();
            String tmp=tx2.getText().toString();
            byte[] Money=new byte[4];
            int num=0;
            num=Integer.valueOf(tmp).intValue();
            num-=num%100;
            Money[3]=(byte)(num&0xff);
            Money[2]=(byte)((num>>8)&0xff);
            Money[1]=(byte)((num>>16)&0xff);
            Money[0]=(byte)(num>>24);
            Cdata h=new Cdata(Login.ip,Login.ie,4,ie2,Money);
            h.work();
            while (Cdata.ffflag==0)
            {

            }
            Cdata.ffflag=0;
            Toast.makeText(getApplicationContext(),"转账成功",Toast.LENGTH_SHORT).show();
        }
    };
    private void initUI(){
        tx1=(EditText)findViewById(R.id.pay_id);
        tx2=(EditText)findViewById(R.id.pay_money);
        mPayButton=(Button)findViewById(R.id.pay_button);
        mPayButton.setOnClickListener(payListener);
        mExitButton=(Button)findViewById(R.id.exit);
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
        setContentView(R.layout.activity_pay);
        initUI();


        if (getIntent().getBooleanExtra("isLogin",false)){
            Bundle bundle=getIntent().getBundleExtra("palmprint");
            bytes=getIntent().getBundleExtra("palmprint").getByteArray("palmprint");
            String imei=((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
            //String ip=getIntent().getStringExtra("ip");
            String ip="101.200.161.130";
            handler=new mHandler(this);
            try{
                Login login=new Login(bytes,imei,this,ip,handler);
                login.work();
            } catch (IOException E) {

            }

            while(Login.consumingTime==-1)
            {
                if (Login.wronflag>5)
                {
                    Toast.makeText(getApplicationContext(), "登陆失败重新注册！", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PayActivity.this,CameraActivity .class));
                    break;
                }
            }
            Login.consumingTime=-1;
            String hehe=String.valueOf(Login.consumingTime);
            Toast.makeText(getApplicationContext(),(CharSequence)hehe,Toast.LENGTH_SHORT).show();
        }

    }

    static class mHandler extends Handler {
        WeakReference<PayActivity> mActivity;

        mHandler(PayActivity activity){
            mActivity=new WeakReference<PayActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PayActivity payActivity=mActivity.get();
            if (msg.what==0x345){
                Toast.makeText(payActivity,"登陆成功",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
