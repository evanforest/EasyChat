package com.linzg.www.androidinternet;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by asus on 2017/9/15.
 */

public class SearchResponseThread extends Thread {
    private static final String TAG = "SearchResponseThread";
    private MulticastSocket receive= null;
    private DatagramSocket sendSoket = null;       //向发出组播的客户端发送UDP数据
    private DatagramPacket receivePacket = null;
    private DatagramPacket sendPacket = null;
    private byte[] serverIPAndName = null;     //服务器的ip地址 + 服务器主昵称
    private boolean isRunning = true;
    public SearchResponseThread(){
        try {
            receive = new MulticastSocket(Port.PORT_MULTICAST_SOCKET);
            receive.joinGroup(InetAddress.getByName(SearchServerThread.HOST_MULTICAST_LISTEN));
            sendSoket = new DatagramSocket(Port.PORT_SERVER_RESPOND);
        } catch (IOException e) {
            e.printStackTrace();
            isRunning = false;
        }
    }

    @Override
    public void run() {
        int i = 0;
        byte[] receiveBytes = new byte[256];
        receivePacket = new DatagramPacket(receiveBytes,receiveBytes.length);
        try {
            while (isRunning) {
                Log.d(TAG, "run: 等待客户端搜索");
                receive.receive(receivePacket);
                String s = new String(receiveBytes,0,receivePacket.getLength());
                Log.d(TAG, "run: 收到客户端的ip：" + s);
                //向客户端回复服务器的ip地址
                sendPacket = new DatagramPacket(serverIPAndName,serverIPAndName.length,InetAddress.getByName(s),Port.PORT_SERVER_RESPOND);
                if (sendSoket == null)
                    Log.d(TAG, "run: sendSocket为空 ");
                for (int a = 0 ;a < 2 ;a++) //发送两次
                    sendSoket.send(sendPacket);
                Log.d(TAG, "run: 收到第" + (i++) +"次请求");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sendSoket != null)
                sendSoket.close();
            if (receive != null)
                sendSoket.close();
            Log.d(TAG, "run: searchResponseThread已结束！！");
        }
    }

    public void stopNow(){
        this.isRunning = false;
        receive.close();
        sendSoket.close();
        interrupt();
    }
    public void setIpAndName(String ipAndName){
        this.serverIPAndName = ipAndName.getBytes();
    }
}
