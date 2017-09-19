package com.linzg.www.androidinternet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2017/9/14.
 */

public class SearchServerThread extends Thread {
    private static final String TAG = "SearchServerThread";
    private MulticastSocket mSocket = null;
    private DatagramSocket udpSocket = null;
    private DatagramPacket searchPacket = null;
    private DatagramPacket receivePacket = null;
    private Handler mHandler = null;
    public static final int WHAT_END= 1;
    public static final int WHAT_EXCEPT= 2;
    public static final int WHAT_RECEIVE= 3;
    private byte[] ipByte;
    public final static String HOST_MULTICAST_LISTEN = "233.233.233.233";
    public SearchServerThread(String ip, Handler handler){
        ipByte = ip.getBytes();
        mHandler = handler;
    }
    @Override
    public void run() {
        List<String> recevieIps = new ArrayList<>();
        try {
            searchPacket = new DatagramPacket(ipByte, ipByte.length,
                    InetAddress.getByName(HOST_MULTICAST_LISTEN),Port.PORT_MULTICAST_SOCKET);
            mSocket = new MulticastSocket();
            mSocket.setTimeToLive(3);
            udpSocket = new DatagramSocket(Port.PORT_SERVER_RESPOND);
            udpSocket.setSoTimeout(3000);
            byte[] buf = new byte[512];
            receivePacket = new DatagramPacket(buf, 0, buf.length);
            int count = 0;
            while (recevieIps.size() < 200) {
                if (count <= 5) {
                    count++;
                    mSocket.send(searchPacket);
                }
                Log.d(TAG, "run: 开始接受等待接受服务器相应");
                udpSocket.receive(receivePacket);
                Log.d(TAG, "run: 接收到服务器响应");
                byte[] data = receivePacket.getData();
                String[] s = new String(data, 0, receivePacket.getLength()).split("/", 2);
                //如果ip没有获取过
                if (!isRecevied(recevieIps,s[0])) {
                    recevieIps.add(s[0]);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("ip", s[0]);
                    bundle.putString("name", s[1]);
                    msg.setData(bundle);
                    msg.what = WHAT_RECEIVE;
                    mHandler.sendMessage(msg);
                }
            }
        } catch (IOException e) {
            mHandler.sendEmptyMessage(WHAT_EXCEPT);
            //e.printStackTrace();
        } finally {
            mSocket.close();
            udpSocket.close();
            mHandler.sendEmptyMessage(WHAT_END);
        }
    }
    public void stopNow(){
        if (mSocket != null)
            mSocket.close();
        if (udpSocket != null)
            udpSocket.close();
    }
    /**
     * 判断该ip地址是否已经获取过了
     * @param list  存放获取过的所有ip
     * @param ip    要判断的ip
     * @return  是否获取过
     */
    private boolean isRecevied(List list,String ip){
        boolean isExist = false;
        for (int a = 0;a < list.size() ;a++){
            if (list.get(a).equals(ip)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }
}
