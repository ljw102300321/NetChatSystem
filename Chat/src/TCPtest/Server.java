package TCPtest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        //创建ServerSocket对象，指明端口号
        ServerSocket ss=new ServerSocket(9999);
        //监听客户端的连接，返回一个Socket对象
        Socket socket=ss.accept();

        //从连接通道中获取输入流读取数据
        InputStream is=socket.getInputStream();
        //用转换流把is变成字符流，以读取中文
        InputStreamReader isr=new InputStreamReader(is);
        int b;
        while((b=isr.read())!=-1){
            System.out.println((char)b);
        }
        //关闭资源

        socket.close();
        ss.close();
        isr.close();
        is.close();



    }
}
