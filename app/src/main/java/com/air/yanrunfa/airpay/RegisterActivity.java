package com.air.yanrunfa.airpay;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.air.network.ExitAppliation;
import com.air.network.Register;

import java.lang.ref.WeakReference;


public class RegisterActivity extends Activity {
    EditText editText;
    Button button;
    Register register;
    byte[] bytes;
    mHandler handler;

    View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Message message=new Message();
            message.what=0x123;
            Bundle bundle=new Bundle();
            bundle.putString("ss", editText.getText().toString());
            message.setData(bundle);
            register.mHandler.sendMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ExitAppliation.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        editText=(EditText)findViewById(R.id.register_id_code);
        button=(Button)findViewById(R.id.register_button);
        button.setOnClickListener(onClickListener);
        bytes=getIntent().getBundleExtra("palmprint").getByteArray("palmprint");
        String imei=((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        //String ip=getIntent().getStringExtra("ip");
        String ip="101.200.161.130";
        handler=new mHandler(this);
        register=new Register(bytes,imei,this,ip,handler);
        register.work();
    }



    static class mHandler extends Handler {
        WeakReference<RegisterActivity> mActivity;

        mHandler(RegisterActivity activity){
            mActivity=new WeakReference<RegisterActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            RegisterActivity registerActivity=mActivity.get();
            if (msg.what==0x123){
                registerActivity.startActivity(new Intent(registerActivity,MainActivity.class));
               // registerActivity.startActivity(new Intent(registerActivity,PayActivity.class));
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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
