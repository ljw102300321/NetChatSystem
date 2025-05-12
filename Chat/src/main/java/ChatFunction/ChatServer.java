package ChatFunction;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
/*
package TCPtest;

import Chat.LoginJFrame;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.SwingUtilities;

public class Server {
    private static LoginJFrame loginFrame;

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(9999);
        System.out.println("服务器启动，等待客户端连接...");

        Socket socket = ss.accept();
        System.out.println("客户端连接成功: " + socket.getInetAddress());

        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), "UTF-8"));
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        ) {
            String clientMessage = in.readLine();
            System.out.println("收到客户端消息: " + clientMessage);

            if ("申请登录".equals(clientMessage)) {
                // 在EDT线程中创建登录界面
                SwingUtilities.invokeLater(() -> {
                    loginFrame = new LoginJFrame((userId, password) -> {
                        System.out.println("登录尝试 - 账号: " + userId + ", 密码: " + password);
                        // 这里可以添加验证逻辑
                        if (isValidLogin(userId, password)) {
                            out.println("登录成功");
                        } else {
                            out.println("登录失败: 用户名或密码错误");
                        }
                    });
                    loginFrame.setVisible(true);
                });

                out.println("已显示登录界面");
            } else {
                out.println("未知请求");
            }
        } catch (IOException e) {
            System.out.println("客户端通信错误: " + e.getMessage());
        } finally {
            socket.close();
            ss.close();
            System.out.println("服务器已关闭");
        }
    }

    private static boolean isValidLogin(String userId, String password) {
        // 这里添加你的实际验证逻辑
        return "admin".equals(userId) && "123456".equals(password);
        //数据库检查操作
    }
}
 */
public class ChatServer {
    private DatagramSocket socket;
    private int serverPort = 10085;
    private Map<Integer, InetAddress> clients;
    private static LoginJframe loginFrame;
    public ChatServer() {
        clients = new HashMap<>();
        try {
            socket = new DatagramSocket(serverPort);
            System.out.println("服务器启动，监听端口: " + serverPort);
            startListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startListening() {
        Thread listenThread = new Thread(() -> {
            while (true) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());

                    if (receivedMessage.startsWith("[ADD_FRIEND]")) {
                        handleAddFriendRequest(packet.getAddress(), packet.getPort(), receivedMessage.substring(13));
                    } else {
                        forwardMessage(packet.getAddress(), packet.getPort(), receivedMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        listenThread.start();
    }

    private void handleAddFriendRequest(InetAddress address, int port, String data) {
        String[] parts = data.split("\\|", 3);
        if (parts.length != 3) return;

        int localPort = Integer.parseInt(parts[0]);
        String name = parts[1];
        int friendPort = Integer.parseInt(parts[2]);

        // 记录客户端地址
        clients.put(localPort, address);

        // 转发添加好友请求给目标客户端
        try {
            byte[] sendData = ("[ADD_FRIEND]" + localPort + "|" + name + "|" + friendPort).getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, friendPort);
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void forwardMessage(InetAddress address, int port, String message) {
        for (Map.Entry<Integer, InetAddress> entry : clients.entrySet()) {
            if (entry.getKey() != port) {
                try {
                    byte[] sendData = message.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, entry.getValue(), entry.getKey());
                    socket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new ChatServer();
        ServerSocket ss = new ServerSocket(9999);
        System.out.println("服务器启动，等待客户端连接...");

        Socket socket = ss.accept();
        System.out.println("客户端连接成功: " + socket.getInetAddress());

        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), "UTF-8"));
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        ) {
            String clientMessage = in.readLine();
            System.out.println("收到客户端消息: " + clientMessage);

            if ("申请登录".equals(clientMessage)) {
                // 在EDT线程中创建登录界面
                SwingUtilities.invokeLater(() -> {
                    loginFrame = new LoginJframe((id,username, password) -> {
                        System.out.println("登录尝试 - 账号: " + id + ", 密码: " + password);

                        //-----------------------------------------------------------------
                        //获取账号密码->去数据库查看是否正确
                        boolean b=true;
                        if(b){
                            SwingUtilities.invokeLater(() -> {
                                new ChatClient(10086, "1"); // 第一个客户端
                                new ChatClient(10087, "2");   // 第二个客户端
                                new ChatClient(10088, "3");// 第三个客户端
                            });
                        }








                        // 这里可以添加验证逻辑
                            out.println("登录成功");
                    });
                    loginFrame.setVisible(true);
                });

                out.println("已显示登录界面");
            } else {
                out.println("未知请求");
            }
        } catch (IOException e) {
            System.out.println("客户端通信错误: " + e.getMessage());
        } finally {
            socket.close();
            ss.close();
            System.out.println("服务器已关闭");
        }
    }
}



