package com.linzg.www.androidinternet;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by asus on 2017/9/16.
 */

public class ServerThread extends Thread {
    private static final String TAG = "ServerThread";
    private ServerSocket serverSocket = null;
    private List<ClientChannel> clientManager = new ArrayList<ClientChannel>();
    private Timer heartCheck = null;
    private boolean isRunning = true;
    public ServerThread() {
    }

    @Override
    public void run() {
        try {
            heartCheck = new Timer();
            heartCheck.schedule(new TimerTask(){
                @Override
                public void run() {
                    if (isRunning) {
                        Iterator<ClientChannel> list = clientManager.iterator();
                        while (list.hasNext()) {
                            ClientChannel c = list.next();
                            if (c.isConnecting == true)
                                c.isConnecting = false;
                            else {
                                Log.d(TAG, "run: c.name = " + c.name);
                                list.remove();
                                //c.dispathTip("离开了聊天室");
                                c.forcedStop();
                            }
                        }
                        Log.d(TAG, "run: 还连接的客户端个数：" + clientManager.size());
                    }
                }
            },0,4000);
            serverSocket = new ServerSocket(Port.PORT_SERVER_SOCKET);
            while (isRunning) {
                Log.d(TAG, "run: 等待客户端连接");
                ClientChannel clientChannel = new ClientChannel(serverSocket.accept());
                Log.d(TAG, "run: 一个客户端连接上了");
                clientManager.add(clientChannel);
                new Thread(clientChannel).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            isRunning = false;
            if (serverSocket != null)
                try {
                    serverSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        } finally {
            Log.d(TAG, "ServerThread已结束");
        }
    }
    public  void stopNow(){
        try {
            isRunning = false;
            heartCheck.cancel();
            serverSocket.close();
            for (ClientChannel clientChannel : clientManager){
                clientChannel.forcedStop();
            }
            clientManager.clear();
            interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ClientChannel implements Runnable{
        private BufferedReader br = null;
      //  private BufferedWriter bw = null;
        private DataOutputStream dos = null;
        private boolean isRunning = true;
        private boolean isConnecting = true;
        private Socket socket = null;
        private String name = "";
        public ClientChannel(Socket socket){
            try {
                this.socket = socket;
                Log.d(TAG, "ClientChannel: socket = "+socket.hashCode());
                br = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
             //   bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                dos = new DataOutputStream(socket.getOutputStream());
                Log.d(TAG, "ClientChannel: 客户端已连接");
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    br.close();
                    dos.close();
                    isRunning = false;
                } catch (IOException e1) {

                }
            }
        }
        @Override
        public void run() {
            Log.d(TAG, "run: 客户端线程开始");
            while (isRunning) {
//                if (socket == null || ! socket.isConnecting() || socket.isClosed()){
//                    isRunning = false;
//                    clientManager.remove(this);
//                    dispathData(name + " 离开了聊天室");
//                    Log.d(TAG, "run: " + name + "离开房间");
//                }else {
                    String s = receive();
                    Log.d(TAG, "run: 接收数据:" + s);
                if (s.equals(Client.HEART_CHEKE_CODE)){
                    send("LZG.hEaRTcHckE");
                    isConnecting = true;
                } else if (s != null && !s.equals("")) {
                    if (name == "") {
                        if (s.startsWith("lzg.chatRoom") && s.endsWith("First.in")) {
                            name = s.replaceFirst("lzg.chatRoom", "");
                            name = name.replaceFirst("First.in", "");
                            dispathTip("加入了聊天室");
                        }
                    } else {
                        Log.d(TAG, "收到客户端的的消息" + s);
                        dispathData(s);
                    }
                }
//                }
            }
            Log.d(TAG, "run: " + name + "在客户端的线程已结束");
            Log.d(TAG, "run: clientManager剩余线程：" + clientManager.size());
        }
        public String receive(){
            //String msg = "";
            StringBuffer msg = new StringBuffer();
            try {
                // 读取每一行的数据.注意大部分端口操作都需要交互数据。
                String str;
                boolean isFirst = true;
                //Log.d(TAG, "现在: "+ name + "开始读数据：a");
                while ((str = br.readLine()) != null) {
                    if (str.equals("DatA iS eNd")) {
                        break;
                    }else
                        if (!isFirst) {
                            msg.append("\n");
                        }else {
                            isFirst = false;
                        }
                        msg.append(str);
                }
               // Log.d(TAG, "现在: "+ name + "数据读完：a");
                if (msg.toString().equals("")){
                    throw new Exception();
                   // Log.d(TAG, "receive: 获取到的字符串为“”");
//                    dispathTip("离开了聊天室");
//                    clientManager.remove(this);
//                    isRunning = false;
//                    forcedStop();
                }
            } catch (Exception e) {
                e.printStackTrace();
                dispathTip("离开了聊天室");
                clientManager.remove(this);
                isRunning = false;
                Log.d(TAG, "run: " + name + "离开房间");
            }
            return msg.toString();
        }
        private void dispathData(String data){
            data = name + " :/ " + data;
            for (ClientChannel channel: clientManager){
                channel.send(data);
            }
        }
        private void dispathTip(String data){
            data = "*" + name + "*  / " + data;
            for (ClientChannel channel: clientManager){
                channel.send(data);
            }
        }
        public void send(String data){
            if (data == null || data.equals(""))
                return;
            try {
                Log.d(TAG, "send: 向客户端返回数据");
                dos.writeUTF(data);
                dos.flush();
            } catch (IOException e) {
                //e.printStackTrace();
                //clientManager.remove(this);
            }
        }
        private void releaseRec(){
            try {
                if (socket != null)
                    socket.close();
                if (br != null)
                    br.close();
                if (dos != null)
                    dos.close();
            } catch (Exception e) {
                Log.d(TAG, "ClientChannel: 资源释放异常");
            }
        }
        private void forcedStop(){
            isRunning = false;
            releaseRec();
        }
    }
}
