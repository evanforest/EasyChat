package com.linzg.www.androidinternet;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.linzg.www.androidinternet.util.CostomUtil;

import java.net.MulticastSocket;
import java.util.List;


/**
 * Created by asus on 2017/9/15.
 */

public class ServerService extends Service {
    private static final String TAG = "ServerService";
    public static final String CLASSNAME = ServerService.class.getName();
    private SearchResponseThread searchResponseTh = null;     //用于客户端搜索响应的线程
    private ServerThread serverThread = null;
    private boolean isRunning;      //服务器线程运行标志
    private String serverName ="";
    private String serverIp ="";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        isRunning = false;
        searchResponseTh = new SearchResponseThread();
        serverThread = new ServerThread();
        serverThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            Log.d(TAG, "onStartCommand: " + this.hashCode());
            isRunning = true;
            if (intent != null) {
                serverIp = intent.getStringExtra("serverIp");
                serverName = intent.getStringExtra("userName");
            }
            searchResponseTh.setIpAndName(serverIp + "/" + serverName);
            if (searchResponseTh != null && !searchResponseTh.isAlive())
                searchResponseTh.start();
            Toast.makeText(getApplicationContext(),"服务器已创建，将在局域网中可见",Toast.LENGTH_SHORT);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        searchResponseTh.stopNow();
        serverThread.stopNow();
        Log.d(TAG, "onDestroy: 销毁ServerService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
