package com.linzg.www.androidinternet.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.linzg.www.androidinternet.R;
import com.linzg.www.androidinternet.ServerService;
import com.linzg.www.androidinternet.util.CostomUtil;

/**
 * Created by asus on 2017/9/13.
 */

public class PrepareActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PrepareActivity";
    private Button createBut = null;
    private Button joinBut = null;
//    private Button start = null;
//    private Button close = null;
    private View baseLayout = null;
    private EditText userNameText = null;
    private String userName;
    SharedPreferences p;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        p = getSharedPreferences("chatData", Context.MODE_PRIVATE);
        userName = p.getString("user","");
        setContentView(R.layout.activity_perpare);
        createBut = (Button) findViewById(R.id.createRoom);
        joinBut = (Button) findViewById(R.id.joinRoom);
//        start = (Button) findViewById(R.id.start);
//        start.setOnClickListener(listener);
//        close = (Button) findViewById(R.id.close);
//        close.setOnClickListener(listener);
        baseLayout = findViewById(R.id.baseLayout);
        userNameText = (EditText) findViewById(R.id.nameText);
        userNameText.setText(userName);
        baseLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                clearFocusHideInputMethod();
                return true;
            }
        });
        createBut.setOnClickListener(this);
        joinBut.setOnClickListener(this);
        new Thread(){
            @Override
            public void run() {
                super.run();
                Log.d(TAG, "onCreate: " + CostomUtil.getIPAddress(getBaseContext()));
            }
        }.start();
    }
    private void clearFocusHideInputMethod(){
        userNameText.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isActive())
            imm.hideSoftInputFromWindow(userNameText.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        clearFocusHideInputMethod();
        userName = userNameText.getText().toString();
        if (userName.equals("")) {
            Log.d(TAG, "onClick: 昵称不能为空");
            Toast.makeText(getApplicationContext(), "昵称不能为空",
                    Toast.LENGTH_SHORT).show();
        }else {
            if (CostomUtil.isWiFiConnected(this)) {
                if (v.getId() == createBut.getId()) {
                    Log.d(TAG, "onClick: 创建服务器");
//                    Log.d(TAG, "onClick: 服务是否运行：" + CostomUtil.isServiceRunning(this,ServerService.CLASSNAME));
                    startServer();
                    joinChat(userName + " 的聊天室",CostomUtil.getIPAddress(this));
                } else if (v.getId() == joinBut.getId()) {
                    intoRoom();
                }
            } else {
                    Log.d(TAG, "onClick: 未加入局域网");
                    Toast.makeText(getApplicationContext(), "未加入局域网",
                            Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void joinChat(String roomName,String roomIp){
        Intent intent = new Intent(PrepareActivity.this,ChatActivity.class);
        intent.putExtra("roomName",roomName);
        intent.putExtra("roomIp",roomIp);
        intent.putExtra("userName", userName);
        startActivity(intent);
    }
    private void startServer(){
        Intent intent = new Intent(PrepareActivity.this, ServerService.class);
        intent.putExtra("userName",userName);
        intent.putExtra("serverIp",CostomUtil.getIPAddress(this));
        startService(intent);
    }
//    private void stopServer(){
//        Intent intent = new Intent(PrepareActivity.this, ServerService.class);
//        stopService(intent);
//    }
    private void intoRoom(){
        Intent intent = new Intent(PrepareActivity.this, RoomsActivity.class);
        intent.putExtra("userName", userName);
        startActivity(intent);
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        userName = userNameText.getText().toString();
        if (!userName.equals("")) {
            SharedPreferences.Editor editor = p.edit();
            editor.putString("user",userName);
            editor.apply();
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.info:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                        .setTitle("关于聊天室").setMessage("\u3000*本聊天室仅可在局域网的条件下使用。" +
                                "\u3000*请在设置中打开本软件的通知权限，否则将会无法看到所有的操作响应和提示。\n" +
                                "\u3000*用户可选择创建聊天室或者加入局域网中的聊天室，聊天室被创建后将在局域网内可见。\n" +
                                "\u3000*存在问题：暂不完全支持热点，打开热点的用户无法创建或加入局域网。\n\n作者: LZG\nQQ : 535903063");

                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
//    class MyThread extends Thread{
//        boolean flag = true;
//        @Override
//        public void run() {
//            int i = 0;
//            while(flag){
//                Log.d(TAG, "run: "+ i++);
//                try {
//                    sleep(10000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//    private MyThread th = new MyThread();
//    private View.OnClickListener listener= new View.OnClickListener(){
//
//        @Override
//        public void onClick(View v) {
//            if (v.getId() == start.getId()){
//                if (th.isAlive()){
//                    Log.d(TAG, "onClick: 线程正在运行，不用启动");
//                    return;
//                }else{
//                    th = new MyThread();
//                    th.start();
//                    Log.d(TAG, "onClick: 开启新线程");
//                }
//            }else if (v.getId() == close.getId()){
//                stopServer();
//                if(th.isAlive()) {
//                    th.stopByFlag();
//                    th.interrupt();
//                    Log.d(TAG, "onClick: 停止线程");
//                }else
//                    Log.d(TAG, "onClick: 线程未在运行，不用停止");
//            }
//        }
//    };
}
