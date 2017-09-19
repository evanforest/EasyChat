package com.linzg.www.androidinternet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by asus on 2017/9/13.
 */

public class Client{
    private static final String TAG = "Client";
    public static final int WHAT_RECEIVE = 6;
    public static final int WHAT_CLIENT_CREATE = 5;
    public static final int WHAT_CLIENT_NOT_CREATE = 4;
    public static final int WHAT_SOCKET_CLOSE = 3;
    public static final int WHAT_SEND_SUCCESS = 2;
    public static final int WHAT_SEND_FAIL = 1;
    public static final String HEART_CHEKE_CODE = "LZG.hEaRTcHckE";
    private Socket clientSocket = null;
    private DataInputStream dis = null;
    private BufferedReader br = null;
    private BufferedWriter bw = null;

    private DataOutputStream dos = null;
    private Handler mHandler = null;
    private RecevieMsg recevieMsg  = null;
    private Timer heartCheck = null;
    private boolean isRunning = false;
    private boolean isServer = false;
    private boolean isConnecting = true;
    private Thread heart = null;
    private String serverIp = "";
    private String userName = "";
    public Thread connect = null;
    public Client(String ip,String userName,Handler handler,boolean isServer){
        Log.d(TAG, "Client: 构造ip:" + ip);
        mHandler = handler;
        this.serverIp = ip;
        this.userName = userName;
        this.isServer = isServer;
        connect(serverIp,this.userName,false); //连接服务器
    }

    /**
     *  客户端连接线程
     *  创建客户端对象时运行一次，连接失败则 创建Client失败
     */
    class Connect extends Thread{
        private String ip;
        private String name;
        //private boolean isReconnect = false;
        public Connect(String ip,String userName,boolean isReconnect){
            this.ip = ip;
            this.name = userName;
            //this.isReconnect = isReconnect;
        }
        @Override
        public void run() {
            try {
//                if (isReconnect)
//                    sleep(2000);
                isRunning = true;
                clientSocket = new Socket(ip,Port.PORT_SERVER_SOCKET);
                dis = new DataInputStream(clientSocket.getInputStream());
                br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                dos = new DataOutputStream(clientSocket.getOutputStream());

                recevieMsg = new RecevieMsg();
                recevieMsg.start();
                //if (!isServer) {
                    heart = new Thread() {
                        @Override
                        public void run() {
                            try {
                                while (isRunning) {
                                    sendHeartMsg(HEART_CHEKE_CODE);
                                    sleep(2500);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    heart.start();
            //    }

                //成功连接后向服务器发送自己的昵称，服务器将提示“ 某某某 加入了聊天室”
                sendMsg("lzg.chatRoom" + name+ "First.in");
                mHandler.sendEmptyMessage(WHAT_CLIENT_CREATE);
                heartCheck = new Timer();
                heartCheck.schedule(new TimerTask(){
                    @Override
                    public void run() {
                        if (isRunning) {
                            if (isConnecting)
                                isConnecting = false;
                            else {
                               // mHandler.sendEmptyMessage(WHAT_SOCKET_CLOSE);
                                stopNow();
                            }
                        }
                    }
                },0,4000);
                Log.d(TAG, "run: " + name);
                Log.d(TAG, "client已连接");
            } catch (Exception e) {
                //连接过程中出现异常说明默认连接失败
                e.printStackTrace();
                Log.d(TAG, "run: 连接异常，客户端创建失败");
                //停止发送与接收线程，释放资源
                stopNow();
                //通知activity
                mHandler.sendEmptyMessage(WHAT_CLIENT_NOT_CREATE);
            }
        }
    }

    /**
     * 释放Client资源方法，在client创建失败或关闭client时调用
     */
    private void releaseRec(){
        try {
            if (clientSocket != null)
                clientSocket.close();
            if (dis != null)
                dis.close();
            if (dis != null)
                dos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ReleaseRec: 资源释放异常");
        }
    }

    /**
     * 向服务器发送数据方法
     * @param data
     */
    public void sendMsg(String data){
        new Thread(new SendMsg(data + "\nDatA iS eNd\n",false)).start();
    }

    public void sendHeartMsg(String data){
        new Thread(new SendMsg(data + "\nDatA iS eNd\n",true)).start();
    }
    /**
     * 发送数据线程，每次点击发送创建一个新线程
     */
    class SendMsg implements Runnable {
        private String s;
        private boolean isHeart;
        public SendMsg(String s,boolean isHeart){
            this.s = s;
            this.isHeart = isHeart;
        }
        @Override
        public void run() {
//            Log.d(TAG, "run: client.isRunning = " + isRunning);
//            Log.d(TAG, "run: client.sengMsg.isAlive = " + recevieMsg.isAlive());
//            Log.d(TAG, "run: client.connect.isAlive = " + connect.isAlive());
            if (isRunning) {
                if (!clientSocket.isClosed() && clientSocket.isConnected()) {
                    try {
                        Log.d(TAG, "run: 客户端要发送的数据 = "+ s);
                        bw.write(s);
                        bw.flush();
//                        dos.writeUTF(s);
//                        dos.flush();
                        if (!isHeart)
                            mHandler.sendEmptyMessage(WHAT_SEND_SUCCESS);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "run: 发送数据异常");
                        if (!isHeart)
                            mHandler.sendEmptyMessage(WHAT_SEND_FAIL);
                    }
                } else {
                    if (!isHeart)
                        mHandler.sendEmptyMessage(WHAT_SEND_FAIL);
                }
            }else{
                Log.d(TAG, "run: client.isRunning = " + isRunning +",已无法发送");
            }

        }
    }

    class RecevieMsg extends Thread{
        @Override
        public void run() {
            while(isRunning){
                if (!clientSocket.isClosed() && clientSocket.isConnected()){
                    Log.d(TAG, "receviceMsg：准备从服务器获取数据");
                    try {
                        String msg = dis.readUTF();
                        Log.d(TAG, "run: msg = " + msg);
                        if (msg.equals(HEART_CHEKE_CODE)){
                            Log.d(TAG, "run: "+ userName +"收到心跳回应");
                            isConnecting = true;
                        } else if (msg != null && !msg.equals("")) {
                            Log.d(TAG, "run: 获取到服务器发回的数据:" + msg);
                            String[] s = msg.split("/",2);
                            Message message = new Message();
                            message.what = WHAT_RECEIVE;
                            Bundle bundle = new Bundle();
                            bundle.putString("msgSender", s[0]);
                            if (s.length >1)
                                bundle.putString("recevieMsg", s[1]);
                            message.setData(bundle);
                            mHandler.sendMessage(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "receviceMsg：服务器获取数据异常 ");
                        mHandler.sendEmptyMessage(WHAT_SOCKET_CLOSE);
                        stopNow();
                    }
                } //else {
//                    mHandler.sendEmptyMessage(WHAT_SOCKET_CLOSE);
//                    isRunning = false;
//                    connect(serverIp,userName,true);
//                }
            }
            Log.d(TAG, "client接收线程结束");
        }
    }
    public void connect(String ip,String userName,boolean isReconnect){

        connect = new Connect(ip,userName,isReconnect);
        connect.start();  //开始连接服务器
    }
    public void stopNow(){
        Log.d(TAG, "stopNow: client" + userName);
        isRunning = false;
//        heart.interrupt();
//        heartCheck.cancel();
        releaseRec();
    }

}
