package ChatFunction;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
/*
public class User {
    public static void main(String[] args) {
        try (
                Socket socket = new Socket("127.0.0.1", 9999);
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), "UTF-8"));
        ) {
            System.out.println("已连接到服务器");

            // 发送登录请求
            out.println("申请登录");//服务器弹出登录界面
            System.out.println("已发送消息: 申请登录");

            //当用户发消息时：点击了



            // 接收服务器响应
            String response = in.readLine();
            System.out.println("收到服务器回复: " + response);

        } catch (IOException e) {
            System.out.println("连接服务器失败: " + e.getMessage());
        }
        //System.out.println("客户端已退出");
    }
}
 */
public class Client {
    public static void main(String[] args) throws IOException {
        Socket sock = new Socket("localhost", 6666); // 连接指定服务器和端口
        try (InputStream input = sock.getInputStream()) {
            try (OutputStream output = sock.getOutputStream()) {
                handle(input, output);
            }
        }
        sock.close();
        System.out.println("disconnected.");
    }

    private static void handle(InputStream input, OutputStream output) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        Scanner scanner = new Scanner(System.in);
        System.out.println("[server] " + reader.readLine());
        for (;;) {
            System.out.print(">>> "); // 打印提示
            String s = scanner.nextLine(); // 读取一行输入
            writer.write(s);
            writer.newLine();
            writer.flush();
            String resp = reader.readLine();
            System.out.println("<<< " + resp);
            if (resp.equals("bye")) {
                break;
            }
        }
    }
}
