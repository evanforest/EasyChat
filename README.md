# EasyChat-简易聊天室

作者：lzg
qq：535903063</br>
上传时间：2017.9.19
制作时间：9.10 - 9.19

简介：
  本项目是一个简单的局域网多人聊天室安卓软件。实现局域网内的聊天室创建、搜索和加入。

功能：
  必须在局域网的条件下使用(暂不完全支持热点，打开热点的用户将无法使用)。</br>
  使用时要在设置中打开软件的通知权限，本软件的提示是用Toast实现，不打开将无法看到提示。</br>
  取完昵称后可选择创建聊天室或加入局域网中他人的聊天室。</br>
  创建聊天室：创建服务器并加入到自己的聊天室中，同时向局域网可见。</br>
  加入聊天室：进入搜索界面，可搜索局域网的所有开启的聊天室，点击即可进入开始聊天。</br>
  对于客户端的加入与退出，聊天室内都有提示 “ 某某某 加入(离开)了聊天室 ”。</br>
  心跳检测：客户端连接服务器后会定时向服务器发送心跳包，服务器收到后向客户端返回心跳包。</br>
          如果当客户端因网络问题而连接不上的话，就可以检测已到离开聊天室。</br>
          服务器掉线的话就会结束聊天室。</br>
  处理了大部分异常，暂未发现大bug。</br>

运用到的知识点：
  封装、低耦合高内聚思想
  安卓基础知识(Activity,Service,各种界面布局和View，自定义View)
  Socket(Socket、ServerSocket、MulticastSocket、DatagramSocket等)
  多线程
  异常处理
  心跳检测机制
  IO流
  集合
  Handler
  Dialog和Toast
  安卓本地存储(SharedPreferences)
  获取WIFI状态
  
项目结构：
  PrepareActivity ：准备界面，
  RoomsActivity ：搜索局域网内的聊天室界面
  ChatActivity  ：聊天主界面
  SquareImageView ：自定义正方形ImageView
  ServerService ：服务器类
  ServerThread  ：服务器聊天线程
  ServerThread.ClientChannel ：客户端在服务器的映射线程
  ServerResponseThread  ：服务器响应搜索请求线程
  SearchServerThread  ：客户端搜索服务器线程
  Client  ：客户端类
  Client.Connect  ：客户端连接线程
  Client.SendMsg  ：客户端发送消息线程
  Client.ReceviceMsg ：客户端接收消息线程
  Port  ：端口号类
  CostomUtil  ：自定义工具类
  
# 欢迎下载学习交流




