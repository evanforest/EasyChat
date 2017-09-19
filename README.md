# EasyChat-简易聊天室

作者：lzg</br>
qq：535903063</br>
上传时间：2017.9.19</br>
制作时间：9.10 - 9.19</br>
</br>
简介：</br>
  本项目是一个简单的局域网多人聊天室安卓软件。实现局域网内的聊天室创建、搜索和加入。</br>
</br>
功能：</br>
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
&nbsp;</br>
运用到的知识点：</br>
  封装、低耦合高内聚思想</br>
  安卓基础知识(Activity,Service,各种界面布局和View，自定义View)</br>
  Socket(Socket、ServerSocket、MulticastSocket、DatagramSocket等)</br>
  多线程</br>
  异常处理</br>
  心跳检测机制</br>
  IO流</br>
  集合</br>
  Handler</br>
  Dialog和Toast</br>
  安卓本地存储(SharedPreferences)</br>
  获取WIFI状态</br>
</br>
项目结构：</br>
  PrepareActivity ：准备界面，</br>
  RoomsActivity ：搜索局域网内的聊天室界面</br>
  ChatActivity  ：聊天主界面</br>
  SquareImageView ：自定义正方形ImageView</br>
  ServerService ：服务器类</br>
  ServerThread  ：服务器聊天线程</br>
  ServerThread.ClientChannel ：客户端在服务器的映射线程</br>
  ServerResponseThread  ：服务器响应搜索请求线程</br>
  SearchServerThread  ：客户端搜索服务器线程</br>
  Client  ：客户端类</br>
  Client.Connect  ：客户端连接线程</br>
  Client.SendMsg  ：客户端发送消息线程</br>
  Client.ReceviceMsg ：客户端接收消息线程</br>
  Port  ：端口号类</br>
  CostomUtil  ：自定义工具类</br>
  </br>
# 欢迎下载学习交流</br>




