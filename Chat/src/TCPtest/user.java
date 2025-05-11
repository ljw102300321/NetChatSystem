package TCPtest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class user {
    public static void main(String[] args) throws IOException {
        //TCP协议，发送数据

        //1.创建客户端Socket对象，指明服务器端的ip和端口号
        Socket socket=new Socket("127.0.0.1",9999);

        //2.可以从连接通道中获取输出流
        OutputStream os=socket.getOutputStream();

        //3.使用输出流发送数据
        os.write("你哈".getBytes());
        os.flush(); // 确保数据被发送

        //4.关闭资源
        os.close();
        socket.close();

    }
}
