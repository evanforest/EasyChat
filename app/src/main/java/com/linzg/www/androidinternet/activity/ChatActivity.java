package com.linzg.www.androidinternet.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.linzg.www.androidinternet.Client;
import com.linzg.www.androidinternet.R;
import com.linzg.www.androidinternet.ServerService;
import com.linzg.www.androidinternet.util.CostomUtil;


public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private Button sendBut = null;
    private EditText inputText = null;
    private TextView chatContent = null;
    private ScrollView scroller = null;
    private Client client = null;
    private Handler mHandler = null;
    private String roomName = "";
    private String roomIp = "";
    private String userName = "";
    private boolean isServer = false;
    private boolean isBackDown = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case Client.WHAT_RECEIVE:
                        //收到服务器的消息时，在textview显示
                        chatContent.append(Html.fromHtml("<font color=red>" + msg.getData().get("msgSender") + "</font>"));
                        chatContent.append(msg.getData().get("recevieMsg") + "\n\n");
                        //scroller.smoothScrollTo(0,chatContent.getBottom());
                        scrollToButtom();
                        break;
                    case Client.WHAT_CLIENT_CREATE:
                        Toast.makeText(getApplicationContext(),"成功加入聊天室",Toast.LENGTH_SHORT).show();
                        break;
                    case Client.WHAT_CLIENT_NOT_CREATE:
                        //client连接失败时，client置空，提示Toast，退出聊天界面
                        client = null;
                        Log.d(TAG, "onDie: 服务器异常，连接失败，无法进入聊天室");
                        Toast.makeText(getApplicationContext(),"该聊天室已被关闭或者您已不在该局域网中",Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case Client.WHAT_SEND_SUCCESS:
                        Log.d(TAG, "run: 发送数据数据成功");
                        inputText.setText("");
                        break;
                    case Client.WHAT_SEND_FAIL:
                        Log.d(TAG, "run: 客户端连接断开，数据发送失败");
                        break;
                    case Client.WHAT_SOCKET_CLOSE:
                        if (!isServer && !isBackDown) {
                            Toast.makeText(getApplicationContext(), "聊天室连接已断开", Toast.LENGTH_SHORT).show();
                            chatContent.append("\n*系统提示*\n\n" +
                                    "\u3000\u3000* 聊天室连接已断开，已无法接受与发送消息。\n\n" +
                                    "\u3000\u3000* 可能的原因 ： 聊天室服务器已关闭、您已不在该聊天室局域网内、您的网络连接已断开、其他未知的原因。\n\n" +
                                    "\u3000\u3000* 聊天室已不可用，请退出该聊天室。\n\n" +
                                    "*系统提示*\n");
                            client.stopNow();
                            scrollToButtom();
                        } else if (isServer && CostomUtil.isServiceRunning(getApplicationContext(),ServerService.CLASSNAME)) {
                            Toast.makeText(getApplicationContext(), "您的聊天室连接已断开", Toast.LENGTH_SHORT).show();
                            chatContent.append("\n*系统提示*\n\n" +
                                    "\u3000\u3000* 您创建的聊天室已断开，已无法接受与发送消息。\n\n" +
                                    "\u3000\u3000* 可能的原因 ： 聊天室服务器已关闭、您已不在该聊天室局域网内、您的网络连接已断开、其他未知的原因。\n\n" +
                                    "\u3000\u3000* 聊天室已不可用，请退出该聊天室。\n\n" +
                                    "*系统提示*\n");
                            scrollToButtom();
                            Intent intent = new Intent(ChatActivity.this, ServerService.class);
                            client.stopNow();
                            stopService(intent);
                        }

                        break;
                }
            }
        };
        Intent intent = getIntent();
        if (intent != null){
            roomName = intent.getStringExtra("roomName");
            roomIp = intent.getStringExtra("roomIp");
            userName = intent.getStringExtra("userName");
            Log.d(TAG, "onCreate: "+roomIp+"," + roomName + "," + userName);
        }
        isServer = CostomUtil.isServiceRunning(getApplicationContext(),ServerService.CLASSNAME);
        Log.d(TAG, "onCreate: isServer = " + isServer);
        getSupportActionBar().setTitle(roomName);
        setContentView(R.layout.activity_chat);
        scroller = (ScrollView) findViewById(R.id.scroller);
        scroller.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollToButtom();
            }
        });
        chatContent = (TextView)findViewById(R.id.chatContent);
        chatContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                clearFocusHideInputMethod();
                return false;
            }
        });
        chatContent.setMovementMethod(new ScrollingMovementMethod());
        sendBut = (Button) findViewById(R.id.chat_send_but);
        sendBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputMsg = inputText.getText().toString();
                if (inputMsg != null && !inputMsg.equals("") &&client != null){
                    client.sendMsg(inputMsg);
                }
            }
        });
        inputText = (EditText) findViewById(R.id.chat_input);
//        inputText.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    scrollToButtom();
//                }
//            }
//        });
        client = new Client(roomIp,userName,mHandler,isServer);

    }
    private void clearFocusHideInputMethod(){
        if (inputText != null) {
            inputText.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm.isActive())
                imm.hideSoftInputFromWindow(inputText.getWindowToken(), 0);
        }
    }
    public void scrollToButtom(){
        if (scroller != null) {
            scroller.post(new Runnable() {
                public void run() {
                    scroller.smoothScrollTo(0,chatContent.getBottom());
                }
            });
        }
    }
    public void onChatBack(){
        if (CostomUtil.isServiceRunning(this, ServerService.CLASSNAME)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setTitle("提示").setMessage("你创建的聊天室正在运行，退出将会关闭聊天室服务器并结束聊天。是否继续退出？");

            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Intent intent = new Intent(ChatActivity.this, ServerService.class);
                    client.stopNow();
                    stopService(intent);
                    finish();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setTitle("提示").setMessage("你确定要退出聊天室吗？");

            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    isBackDown = true;
                    client.stopNow();
                    finish();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        MenuItem item = menu.findItem(R.id.back);
        if (isServer)
            item.setTitle("结束聊天");
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.back:
                onChatBack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            onChatBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            Log.d(TAG, "onDestroy: client已销毁");
            client.stopNow();
        }
    }
}
