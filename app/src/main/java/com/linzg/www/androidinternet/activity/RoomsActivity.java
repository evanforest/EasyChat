package com.linzg.www.androidinternet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.linzg.www.androidinternet.R;
import com.linzg.www.androidinternet.SearchServerThread;
import com.linzg.www.androidinternet.ThreadCallback;
import com.linzg.www.androidinternet.util.CostomUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by asus on 2017/9/13.
 */

public class RoomsActivity extends AppCompatActivity implements View.OnClickListener,ThreadCallback {
    private static final String TAG = "RoomsActivity";
    private SearchServerThread searchThread = null;
    private Button searchBut = null;
    private Handler handler = null;
    private RelativeLayout progress = null;
    private ListView roomListView = null;
    private List<Map<String,Object>> roomList= new ArrayList<Map<String,Object>>();
    private SimpleAdapter adapter = null;
    private boolean isSearching = false;
    private String userName = "";
    private String userIp = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null)
            userName = intent.getStringExtra("userName");
        userIp = CostomUtil.getIPAddress(this);
        Log.d(TAG, "onCreate: Room:" + userName);
        if (userName == "")
            finish();
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case SearchServerThread.WHAT_END:
                        isSearching = false;
                        progress.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "handleMessage: 搜索线程结束");
                        if (roomList.size() == 0)
                            Toast.makeText(getBaseContext(),"搜索无结果，请重试",Toast.LENGTH_SHORT).show();
                        break;
                    case SearchServerThread.WHAT_EXCEPT:
                        Log.d(TAG, "handleMessage: 搜索超时或被强行结束");
                        break;
                    case SearchServerThread.WHAT_RECEIVE:
                        Log.d(TAG, "handleMessage: 接收到服务器的搜索响应");
                        addRoom2List(msg.getData().getString("name"),msg.getData().getString("ip"));
                        adapter.notifyDataSetChanged();
//                        Toast.makeText(getBaseContext(),
//                                "服务器ip：" + msg.getData().getString("ip")
//                                        + "\n房间名："+msg.getData().getString("name") + "的聊天室",
//                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("局域网内聊天室");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.layout_rooms);
        searchBut = (Button) findViewById(R.id.searchRoomBut);
        progress = (RelativeLayout) findViewById(R.id.progressRL);
        roomListView = (ListView) findViewById(R.id.roomList);
        adapter = new SimpleAdapter(this,roomList,R.layout.layout_list_item,
                new String[]{"roomImage","roomName","roomIp"},new int[]{R.id.roomImage,R.id.roomName,R.id.roomIp});
        searchBut.setOnClickListener(this);
        roomListView.setAdapter(adapter);
        roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: " + position);
                Log.d(TAG, "onItemClick: " + roomList.get(position).get("roomName"));
                joinChat((String)roomList.get(position).get("roomName"),(String)roomList.get(position).get("roomIp"));
            }
        });
        searchRooms();
    }
    @Override
    public void onThreadEnd() {
        Log.d(TAG, "onThreadEnd: 搜索线程结束");
        isSearching = false;
        progress.setVisibility(View.GONE);
    }

    private void searchRooms(){
        if (CostomUtil.isWiFiConnected(this)) {
            if (!isSearching) {
                isSearching = true;
                roomList.clear();
                adapter.notifyDataSetChanged();
                progress.setVisibility(View.VISIBLE);
                searchThread = new SearchServerThread(userIp,handler);
                searchThread.start();
            }
        }else{
            Toast.makeText(getBaseContext(),"请先加入局域网",Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void joinChat(String roomName,String roomIp){
        if (isSearching && searchThread.isAlive()){
            searchThread.stopNow();
        }
        Intent intent = new Intent(RoomsActivity.this,ChatActivity.class);
        intent.putExtra("roomName",roomName);
        intent.putExtra("roomIp",roomIp);
        intent.putExtra("userName",userName);
        startActivity(intent);
    }
    private void addRoom2List(String name,String ip){
        Map<String,Object> item = new HashMap<>();
        item.put("roomImage",R.drawable.room_default_image);
        item.put("roomName",name + " 的聊天室");
        item.put("roomIp",ip);
        roomList.add(item);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSearching && searchThread.isAlive()){
            searchThread.stopNow();
        }
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onClick(View v) {
        searchRooms();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchRooms();
        Log.d(TAG, "onResume: ");
    }
}
