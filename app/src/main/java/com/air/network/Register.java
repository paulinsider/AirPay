package com.air.network;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Paul Insider on 2015/4/29.
 */
public class Register {
    private final static String LOCK_PASSWORD_SALT_FILE = "password_salt";
    private final static String LOCK_PASSWORD_SALT_KEY = "lockscreen.password_salt";
    private byte[] pw=new byte[352];
    private String ip;
    private byte[] s0=new byte[16];
    private byte[] IMEI=new byte[32];
    private byte[] k;
    private byte[] Original;
    private byte[] error={0x00,0x10,0x10,0x10,0x11,0x11,0x11,0x11};
    private byte[] success={0x0b,0x10,0x10,0x10,0x53,0x55,0x43,0x45};
    private int fffflag=0;
    private byte[] save=new byte[400];
    private Context ctx;
    private int hah=0;
        private String ie;
    Handler handl;
    public Register(byte[] pw0,String imei,Context cctx,String IP, Handler hand) throws IOException
    {
        handl=hand;
        ip=IP;
        pw=pw0;
        ctx=cctx;
        ie=imei;
        IMEI=SM3.hash(imei.getBytes());
        s0[0]=0;
    }
    public void work()
    {
        WorkThread Wthread=new WorkThread();
        Wthread.start();
        Wthread.interrupt();
    }

    class WorkThread extends Thread {
        Socket socket;
        byte[] RegSignal = {0x01, 0x01, 0x01, 0x01, 0x52, 0x45, 0x47, 0x54};
        int RecvNumber = 0;
        SMS4 JustUseFunction = new SMS4();
        byte[] Encode = new byte[800];
        byte[] Decode01 = new byte[476];
        public void run() {
            try {
                int fflag=0;
                socket = new Socket(ip, 10000);
                OutputStream out = socket.getOutputStream();
                InputStream bin = socket.getInputStream();
                while (true) {
                    if (fflag==0) {
                        send(out, RegSignal, 8);
                        fflag=1;
                        fffflag=0;
                    }
                    byte[] Recv = new byte[1000];
                    RecvNumber = recv(bin, Recv);
                    int flag = Recv[0];
                    byte[] sendbuff = new byte[1000];
                    for (int i = 4; i < RecvNumber; i++) {
                        sendbuff[i - 4] = Recv[i];
                    }
                    if (flag==13)
                    {
                        fflag=0;
                        fffflag=0;
                    }
                    if (flag == 2) {
                        while(s0[0]==0||fffflag==0)
                        {

                        }
                        byte[] key = new byte[16];
                        key = sm3key(s0, 0);
                        JustUseFunction.sms4(sendbuff, RecvNumber - 4, key, Encode, 0);
                        k = sm3key(Encode, 1);
                        Decode01 = Encryt(k);
                        sendbuff[0] = 0x03;
                        sendbuff[1] = 0x10;
                        sendbuff[2] = 0x10;
                        sendbuff[3] = 0x10;
                        for (int i = 4; i < 420; i++) {
                            sendbuff[i] = Decode01[i - 4];
                        }
                        send(out, sendbuff, 420);
                    }
                    if (flag == 4) {
                        JustUseFunction.sms4(sendbuff, RecvNumber - 4, k, Encode, 0);
                        if (save(Encode) == 1) {
                            SharedPreferences sp = ctx.getSharedPreferences(LOCK_PASSWORD_SALT_FILE, ctx.MODE_PRIVATE);
                            SharedPreferences saveOriginal = ctx.getSharedPreferences("Original", ctx.MODE_PRIVATE);
                            long t=0;
                            while(t==0)
                            {
                                t= sp.getLong(LOCK_PASSWORD_SALT_KEY, 0);
                                //t=1231;
                            }
                            byte[] MustSave = new byte[400];
                            char[] turn = new char[400];
                            byte[] b = new byte[16];
                            String FinalSave;

                            for (int i = 0; i < b.length; i++) {
                                b[i] = new Long(t & 0xff).byteValue();// 将最低位保存在最低位
                                t = t >> 8; // 向右移8位
                            }
                            for (int i=8;i<16;i++)
                            {
                                b[i]=0x01;
                            }
                            JustUseFunction.sms4(save, 400, b, MustSave, 1);
                            for (int i = 0; i < 400; i++) {
                                turn[i] = (char) MustSave[i];
                            }
                            FinalSave = String.valueOf(turn);
                            SharedPreferences.Editor editor = saveOriginal.edit();
                            editor.putString("cipher", FinalSave);
                            editor.commit();
                            send(out, success, 8);
                            socket.close();
                            hah=1;
                            Message msg=new Message();
                            msg.what=0x123;
                            handl.sendMessage(msg);
                            Login hehe=new Login(pw,ie,ctx,ip,handl);
                            hehe.work();
                            return;
                        } else {
                            fflag=0;
                            fffflag=0;
                        }
                    }

                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void send(OutputStream out,byte[] data,int len)
    {
        byte[] ans=new byte[1000];
        byte[] temp=new byte[4];
        try
        {

            for (int i=0;i<len;i++)
                ans[i]=data[i];
            for (int i=len;i<len+4;i++)
                ans[i]=0x10;
            out.write(ans,0,len+4);
            out.flush();
            /*for (int i=0;i<len+4;i++)
            {
                temp[i%4]=ans[i];
                if ((i+1)%4==0)
                {
                    out.write(temp);
                    out.flush();
                }
            }*/
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public int recv(InputStream bin,byte[] buff)
    {

        int a=0;
        int flag=1;
        try
        {

            while (true)
            {
                a=bin.read(buff);
                if(a>0)
                    break;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return a;
    }

    public byte[] Encryt(byte[] key) throws IOException
    {
        byte[] EncrytAnswer=new byte[416];
        byte[] value=new byte[416];
        byte[] PwIMEI=new byte[384];
        byte[] SM3PwIMEI=new byte[16];
        SMS4 temp=new SMS4();
        System.arraycopy(pw,0,PwIMEI,0,352);
        System.arraycopy(IMEI,0,PwIMEI,352,32);
        SM3PwIMEI=SM3.hash(PwIMEI);
        System.arraycopy(pw,0,value,0,352);
        System.arraycopy(IMEI,0,value,352,32);
        System.arraycopy(SM3PwIMEI,0,value,384,32);
        temp.sms4(value,416,key,EncrytAnswer,1);
        return EncrytAnswer;
    }

    public int save(byte[] value) throws IOException
    {
        byte[] Front=new byte[416];
        byte[] Down=new byte[32];
        byte[] SM3Front=new byte[32];
        byte[] NewK=new byte[32];
        for (int i=0;i<448;i++)
        {
            if (i<416)
            {
                Front[i]=value[i];
                if (i>=384&&i<416)
                {
                    NewK[i-384]=value[i];
                }
            }
            else
            {
                Down[i-416]=value[i];
            }
        }
        SM3Front=SM3.hash(Front);
        for (int i=0;i<32;i++)
        {
            if (SM3Front[i]!=Down[i])
            {
                return 0;
            }
        }
        k= sm3key(NewK, 1);
        Original=Front;
        byte[] tmp=new byte[400];
        System.arraycopy(Original,0,tmp,0,384);
        System.arraycopy(k,0,tmp,384,16);
        save=tmp;
        return 1;
    }


    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x123:
                    s0=msg.getData().getString("ss").getBytes();
                    fffflag=1;
                    break;
            }
        }
    };

    public byte[] sm3key(byte[] x, int type) throws IOException
    {
        byte[] sm3 = new byte[32];
        byte[] result = new byte[16];
        byte[] sm31 = new byte[16];
        byte[] sm32 = new byte[16];
        SMS4 en = new SMS4();
        if (type == 0)
        {
            sm3 = SM3.hash(x);
        }else {
            sm3=x;
        }
        for (int i=0;i < 16;i++)
        {
            sm31[i] = sm3[i];
            sm32[i] = sm3[i+16];
        }
        en.sms4(sm31, 16, sm32, result, 1);
        return result;
    }
}
