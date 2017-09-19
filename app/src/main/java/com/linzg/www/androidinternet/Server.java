package com.linzg.www.androidinternet;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;

/**
 * Created by asus on 2017/9/13.
 */

public class Server{
    private static final String TAG = "Server";
    public  static final int WHAT_SERVER_OK = 1;
    public  static final int WHAT_SERVER_INITING = 2;
    private ServerSocket mServerSocket = null;      //单播socket(TCP协议)
    private DatagramSocket sendSoket = null;       //向发出组播的客户端发送UDP数据
    private MulticastSocket mMs = null;     //接受客户端的组播
    private static volatile Server mServer;     //单例实例
    private byte[] serverIPAndName = null;     //服务器的ip地址 + 服务器主昵称
    private DatagramPacket packet = null;       //接收客户端组播的数据包
    private DatagramPacket ipPacket = null;
    private boolean isAleady = false;
    private Handler mHandler = null;
    private Server(String ip,String userName,Handler handler){
        try {
            mHandler = handler;
            StringBuffer sb = new StringBuffer();
            serverIPAndName = sb.append(ip).append("/").append(userName).toString().getBytes();
            sendSoket = new DatagramSocket(Port.PORT_SERVER_RESPOND);
            mMs = new MulticastSocket(Port.PORT_MULTICAST_SOCKET);
            mMs.joinGroup(InetAddress.getByName(SearchServerThread.HOST_MULTICAST_LISTEN));
            byte[] buf = new byte[512];
            packet = new DatagramPacket(buf,buf.length);
            Log.d(TAG, "Server: 构造完成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Server getServer(String ip,String userName,Handler handler){
        if (mServer == null){
            synchronized (Server.class) {
                if (mServer == null) {
                    mServer = new Server(ip,userName,handler);
                }
            }
        }
        return mServer;
    }

    public void runa() {
        int i = 0;
        try {
            while (true) {
                if (mMs == null)
                    Log.d(TAG, "run: mMs为空");
                if (packet == null)
                    Log.d(TAG, "run: packet为空");
                Log.d(TAG, "run: 等待客户端连接");
                mMs.receive(packet);
                byte[] data = packet.getData();
                String s = new String(data,0,packet.getLength());
                Log.d(TAG, "run: 收到客户端的ip：" + s);
                //向客户端回复服务器的ip地址
                ipPacket = new DatagramPacket(serverIPAndName,serverIPAndName.length,InetAddress.getByName(s),8888);
                sendSoket.send(ipPacket);
                Log.d(TAG, "run: 收到第" + (i++) +"次请求");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
