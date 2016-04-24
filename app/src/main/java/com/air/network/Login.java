package com.air.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


/**
 * Created by Paul Insider on 2015/4/30.
 */
public class Login {
    Socket socket;
    private final static String LOCK_PASSWORD_SALT_FILE = "password_salt";
    private final static String LOCK_PASSWORD_SALT_KEY = "lockscreen.password_salt";
    private byte[] logn={0x05,0x10,0x10,0x10,0x4C,0x4F,0x47,0x4E};
    private byte[] accp={0x06,0x10,0x10,0x10,0x41,0x43,0x43,0x50};
    private byte[] pw=new byte[320];
    private byte[] IMEI=new byte[32];
    private byte[] k=new byte[16];
    private byte[] Original=new byte[336];
    private byte[] error={0x00,0x10,0x10,0x10,0x11,0x11,0x11,0x11};
    private byte[] success={0x0c,0x10,0x10,0x10,0x53,0x55,0x43,0x45};
    public static String ip;
    private byte[] save=new byte[368];
    private Context ctx;
    private long t;
    private byte[] b=new byte[16];
    private Handler handl;
    public static String ie;
    public static char[] username=new char[15];
    public static int usermoney=0;
    public static long consumingTime=-1;
    public static int wronflag=0;
    public Login(byte[] pw1,String imei,Context cctx,String IP,Handler hand) throws IOException
    {
        wronflag=0;
        handl=hand;
        ip=IP;
        pw=pw1;
        ie=imei;
        IMEI=SM3.hash(imei.getBytes());
        ctx=cctx;
        SharedPreferences sp= ctx.getSharedPreferences(LOCK_PASSWORD_SALT_FILE,ctx.MODE_PRIVATE);
        SharedPreferences saveOriginal=ctx.getSharedPreferences("Original",ctx.MODE_PRIVATE);
        while (t==0)
            t=sp.getLong(LOCK_PASSWORD_SALT_KEY,0   );
            //t=1231;

        for (int i = 0; i < b.length; i++) {
            b[i] = new Long(t & 0xff).byteValue();// 将最低位保存在最低位
            t= t>> 8; // 向右移8位
        }
        for (int i=8;i<16;i++)
        {
            b[i]=0x01;
        }
        String Cipher=saveOriginal.getString("cipher","");
        char[] ccipher=new char[416];
        byte[] finalcipher=new byte[416];
        byte[] finabyte=new byte[416];
        ccipher=Cipher.toCharArray();
        for (int i=0;i<368;i++)
            finalcipher[i]=(byte)ccipher[i];
        SMS4 JustUse=new SMS4();
        JustUse.sms4(finalcipher,368,b,finabyte,0);
        for (int i=0;i<368;i++)
        {
            if (i<352)
            {
                Original[i]=finabyte[i];
            }
            else
            {
                k[i-352]=finabyte[i];
            }
        }
    }
    public void work()
    {
        WorkThread Wthread=new WorkThread();
        Wthread.start();
        Wthread.interrupt();
    }

    class WorkThread extends Thread
    {
        int RecvNumber=0;
        byte[] Recv=new byte[432];
        byte[] newkey = new byte[16];
        SMS4 JustUseFunction=new SMS4();
        int flag=0,fflag=0;
        byte[] Encode=new byte[432];
        byte[] Decode=new byte[800];
        byte[] Encode01=new byte[432];
        public void run()
        {
            try{
                long startTime = System.nanoTime();
                socket=new Socket(ip,10000);
                OutputStream out= socket.getOutputStream();
                InputStream bin=socket.getInputStream();

                while (true)
                {
                    if (wronflag>5)
                    {
                        break;
                    }
                    if (fflag==0)
                    {
                        send(out,logn,8);
                        fflag=1;
                    }
                        RecvNumber = recv(bin, Recv);
                    int flag = Recv[0];
                    byte[] sendbuff = new byte[800];
                    for (int i = 4; i < RecvNumber; i++) {
                        sendbuff[i - 4] = Recv[i];
                    }
                    if (flag==13)
                    {
                        fflag=0;
                        wronflag+=1;
                    }
                    if (flag==6)
                    {
                        /*socket.close();
                        socket = new Socket(ip, 10000);
                        out = socket.getOutputStream();
                        bin = socket.getInputStream();
                        */int hehe=0;
                        for (int i=0;i<4;i++)
                        {
                            if (accp[i+4]!=sendbuff[i])
                                hehe=1;
                        }
                        if (hehe==1)
                        {
                            fflag=0;
                        }
                        else
                        {
                            byte[] temp=new byte[36];
                            temp[0]=0x07;
                            temp[1]=0x10;
                            temp[2]=0x10;
                            temp[3]=0x10;
                            for (int i=4;i<36;i++)
                            {
                                temp[i]=IMEI[i-4];
                            }
                            send(out,temp,36);
                        }
                    }
                    if (flag==8)
                    {
                        /*socket.close();
                        socket = new Socket(ip, 10000);
                        out = socket.getOutputStream();
                        bin = socket.getInputStream();
                        */JustUseFunction.sms4(sendbuff, RecvNumber-4, k, Encode, 0);
                        newkey = sm3key(Encode);
                        Decode = encrypt(newkey);
                        byte[] temp=new byte[708];
                        temp[0]=0x09;
                        temp[1]=0x10;
                        temp[2]=0x10;
                        temp[3]=0x10;
                        for (int i=4;i<708;i++)
                        {
                            temp[i]=Decode[i-4];
                        }
                        send(out,temp,708);
                    }

                    if (flag==10) {
                        /*socket.close();
                        socket = new Socket(ip, 10000);
                        out = socket.getOutputStream();
                        bin = socket.getInputStream();
                        */
                        JustUseFunction.sms4(sendbuff, RecvNumber - 4, newkey, Encode01, 0);
                        if (save(Encode01) == 1) {
                            //SharedPreferences sp = ctx.getSharedPreferences(LOCK_PASSWORD_SALT_FILE, ctx.MODE_PRIVATE);
                            SharedPreferences saveOriginal = ctx.getSharedPreferences("Original", ctx.MODE_PRIVATE);

                            byte[] MustSave = new byte[368];
                            char[] turn = new char[368];
                            String FinalSave;
                            JustUseFunction.sms4(save, 368, b, MustSave, 1);
                            for (int i = 0; i < 368; i++) {
                                turn[i] = (char) MustSave[i];
                            }
                            FinalSave = String.valueOf(turn);
                            SharedPreferences.Editor editor = saveOriginal.edit();
                            editor.putString("cipher", FinalSave);
                            editor.commit();
                            send(out, success, 8);
                            socket.close();
                            Message msg=new Message();
                            msg.what=0x345;
                            handl.sendMessage(msg);
                            consumingTime = System.nanoTime()-startTime;
                            String hehe="";
                            byte[] haha=new byte[4];
                            Cdata cha=new Cdata(ip,ie,1,hehe,haha);
                            cha.work();
                            while (Cdata.ffflag!=1)
                            {

                            }
                            Cdata.ffflag=0;
                            username=Cdata.name;
                            usermoney=Cdata.Money;
                            return;
                        }
                        else
                        {
                            fflag=0;
                        }
                    }
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    public void send(OutputStream out,byte[] data,int len)
    {
        byte[] ans=new byte[804];
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
        try
        {

            while (true)
            {
                a=bin.read(buff);
                if (a!=0)
                    break;
            }
            //socket.shutdownInput();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return a;
    }

    public byte[] encrypt(byte[] key) throws IOException
    {
        byte[] EncrytAnswer=new byte[704];
        byte[] value=new byte[704];
        byte[] PwYS=new byte[672];
        byte[] SM3PwYS=new byte[32];
        SMS4 temp=new SMS4();
        System.arraycopy(pw,0,PwYS,0,320);
        System.arraycopy(Original,0,PwYS,320,352);
        SM3PwYS=SM3.hash(PwYS);
        //System.arraycopy(pw,0,value,0,320);
        System.arraycopy(PwYS,0,value,0,672);
        System.arraycopy(SM3PwYS,0,value,672,32);
        temp.sms4(value,704,key,EncrytAnswer,1);
        return EncrytAnswer;
    }

    public int save(byte[] value) throws IOException
    {
        byte[] Front=new byte[416];
        byte[] Down=new byte[32];
        byte[] SM3Front=new byte[32];
        byte[] NewK=new byte[16];
        for (int i=0;i<416;i++)
        {
            if (i<384)
            {
                Front[i]=value[i];
                if (i>=352&&i<384)
                {
                    NewK[i-352]=value[i];
                }
            }
            else
            {
                Down[i-384]=value[i];
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
        k= sm3key(NewK);
        Original=Front;
        byte[] tmp=new byte[416];
        System.arraycopy(Original,0,tmp,0,352);
        System.arraycopy(k,0,tmp,352,16);
        save=tmp;
        return 1;
    }

    public byte[] sm3key(byte[] x) throws IOException
    {
        byte[] sm3 = new byte[32];
        byte[] result = new byte[16];
        byte[] sm31 = new byte[16];
        byte[] sm32 = new byte[16];
        SMS4 en = new SMS4();
        sm3 = SM3.hash(x);
        for (int i=0;i < 16;i++)
        {
            sm31[i] = sm3[i];
            sm32[i] = sm3[i+16];
        }
        en.sms4(sm31, x.length, sm32, result, 0);
        return result;
    }
}
