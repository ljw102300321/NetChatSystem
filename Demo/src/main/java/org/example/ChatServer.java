package org.example;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private ServerSocket tcpSocket;
    private final int tcpPort = 9999;
    private final Map<String, Integer> clients = new ConcurrentHashMap<>(); // 用户名 -> UDP端口
    private final Map<String, PrintWriter> clientWriters = new ConcurrentHashMap<>(); // 用户名 -> TCP输出流
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);


    public static void main(String[] args) {
        try {
            new ChatServer().start();
        } catch (IOException e) {
            System.err.println("服务器启动失败: " + e.getMessage());
        }
    }


    public void start() throws IOException {
        tcpSocket = new ServerSocket(tcpPort);
        System.out.println("TCP服务器启动，端口: " + tcpPort);

        executorService.scheduleAtFixedRate(this::checkHeartbeats, 30, 30, TimeUnit.SECONDS);

        while (true) {
            Socket clientSocket = tcpSocket.accept();
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private void handleClient(Socket clientSocket) {
        String username = null;
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true)
        ) {
            // 登录验证
            String loginMessage = in.readLine();
            if (loginMessage == null || !loginMessage.startsWith("LOGIN:")) {
                out.println("ERROR:无效的登录请求");
                return;
            }

            String[] parts = loginMessage.split(":");
            if (parts.length != 3) {
                out.println("ERROR:无效的登录格式");
                return;
            }

            username = parts[1];
            int udpPort = Integer.parseInt(parts[2]);
            synchronized (this) {
                if (clients.containsKey(username)) {
                    out.println("LOGIN_FAILED:用户名已存在");
                    return;
                }
                clients.put(username, udpPort);
                clientWriters.put(username, out);
            }

            out.println("LOGIN_SUCCESS");
            System.out.println(username + " 登录成功，UDP端口: " + udpPort);
            // 广播用户列表更新
            broadcastUserList();
            //广播在线用户列表
            broadcastOnlineUsers();
            // 处理客户端消息
            String msg;
            while ((msg = in.readLine()) != null) {
                if (msg.startsWith("SEND:")) {
                    handleSendMessage(username, msg.substring(5));
                } else if (msg.startsWith("SEND_FILE:")) {
                    handleSendFile(username, msg.substring(10));
                } else if (msg.equals("HEARTBEAT")) {
                    out.println("HEARTBEAT_ACK");
                } else if (msg.equals("REQUEST_USERLIST")) {
                    sendUserList(out);
                } else if (msg.equals("REQUEST_ONLINE_USERS")) {
                    // 新增：处理客户端请求在线用户列表
                    sendOnlineUsers(out);
                } else if (msg.startsWith("MSG_RECEIVED:")) {
                    // 处理消息接收确认
                    String[] receivedParts = msg.split(":");
                    if (receivedParts.length == 3) {
                        String from = receivedParts[1];
                        String to = receivedParts[2];
                        System.out.println(to + " 已收到来自 " + from + " 的消息");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("客户端通信错误: " + e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (username != null) {
                synchronized (this) {
                    clients.remove(username);
                    clientWriters.remove(username);
                }
                System.out.println(username + " 已下线");

                // 下线后广播用户列表和在线用户列表
                broadcastUserList();
                broadcastOnlineUsers();
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("关闭客户端连接失败: " + e.getMessage());
            }
        }
    }

    private void handleSendMessage(String from, String message) {
        String[] parts = message.split(":", 2);
        if (parts.length != 2) return;

        String to = parts[0];
        String content = parts[1];

        if (!clients.containsKey(to)) {
            PrintWriter writer = clientWriters.get(from);
            if (writer != null) {
                writer.println("USER_NOT_FOUND:" + to);
            }
            return;
        }
        try {
/*            // 通过UDP发送消息
            int toPort = clients.get(to);
            DatagramSocket udpSocket = new DatagramSocket();
            byte[] sendData = (from + ":" + content).getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length,
                    InetAddress.getByName("127.0.0.1"), toPort);
            udpSocket.send(sendPacket);
            udpSocket.close();
*/
            // 通知发送方消息已发送
            PrintWriter writer = clientWriters.get(from);
            if (writer != null) {
                writer.println("MSG_SENT:" + to + ":" + content);
            }

            System.out.println("消息转发: " + from + " -> " + to + ": " + content);
            try {
                String sendId=-1+"";
                String receiveId=-1+"";
                try {
                    sendId=JDBCUnil.selectId(from);
                    receiveId=JDBCUnil.selectId(to);
                }
                catch (Exception e){
                    System.out.println("查询失败");
                }
                System.out.println(sendId+"    "+receiveId+"     "+content);
                if(!sendId.equals(-1+"")&&!receiveId.equals(-1+"")){
                    JDBCUnil.insertChat(sendId,receiveId, content);
                } else{
                     System.out.println("插入失败");
                }
            }
            catch (Exception e){
                System.out.println("插入失败");
            }
        } catch (Exception e) {
            PrintWriter writer = clientWriters.get(from);
            if (writer != null) {
                writer.println("MSG_FAILED:" + to);
            }
        }
    }

    /**
     * 处理文件发送请求（仅记录日志）
     */
    private void handleSendFile(String from, String message) throws SQLException, ClassNotFoundException {
        String[] parts = message.split(":", 2);
        if (parts.length != 2) return;

        String to = parts[0];
        String fileName = parts[1];

        if (!clients.containsKey(to)) {
            PrintWriter writer = clientWriters.get(from);
            if (writer != null) {
                writer.println("FILE_FAILED:" + to + ":用户不存在");
            }
            return;
        }

        PrintWriter writer = clientWriters.get(from);
        if (writer != null) {
            writer.println("FILE_SENT:" + to + ":" + fileName);
        }
        System.out.println(from + " 发送文件给 " + to + ": " + fileName);
        try {
            String sendId=-1+"";
            String receiveId=-1+"";
            try {
                sendId=JDBCUnil.selectId(from);
                receiveId=JDBCUnil.selectId(to);
            }
            catch (Exception e){
                System.out.println("查询失败");
            }
            //System.out.println(sendId+"    "+receiveId+"     "+"传输文件："+fileName);
            if(!sendId.equals(-1+"")&&!receiveId.equals(-1+"")){

                JDBCUnil.insertChat(sendId,receiveId, "传输文件："+fileName);
            } else{
                System.out.println("插入失败");
            }
        }
        catch (Exception e) {
            System.out.println("传输文件超时");
        }
    }

    /**
     * 广播用户列表（兼容旧版好友列表）
     */
    private void broadcastUserList() {
        StringBuilder userList = new StringBuilder("USERLIST:");
        synchronized (this) {
            for (Map.Entry<String, Integer> entry : clients.entrySet()) {
                userList.append(entry.getKey()).append(",").append(entry.getValue()).append(";");
            }
        }

        synchronized (this) {
            for (PrintWriter writer : clientWriters.values()) {
                writer.println(userList);
            }
        }
    }

    /**
     * 发送用户列表给指定客户端（用于 REQUEST_USERLIST 请求）
     */
    private void sendUserList(PrintWriter out) {
        StringBuilder userList = new StringBuilder("USERLIST:");
        synchronized (this) {
            for (Map.Entry<String, Integer> entry : clients.entrySet()) {
                userList.append(entry.getKey()).append(",").append(entry.getValue()).append(";");
            }
        }
        out.println(userList);
    }

    /**
     * 新增：广播在线用户列表
     * 格式：ONLINE_USERS:user1,50001;user2,50002
     */
    private void broadcastOnlineUsers() {
        StringBuilder userList = new StringBuilder("ONLINE_USERS:");

        synchronized (this) {
            boolean first = true;
            for (Map.Entry<String, Integer> entry : clients.entrySet()) {
                if (!first) {
                    userList.append(";");
                }
                userList.append(entry.getKey()).append(",").append(entry.getValue());
                first = false;
            }
        }

        synchronized (this) {
            for (PrintWriter writer : clientWriters.values()) {
                writer.println(userList);
            }
        }

        System.out.println("广播在线用户列表: " + userList);
    }

    /**
     * 新增：响应客户端的 REQUEST_ONLINE_USERS 请求
     */
    private void sendOnlineUsers(PrintWriter out) {
        StringBuilder userList = new StringBuilder("ONLINE_USERS:");

        synchronized (this) {
            boolean first = true;
            for (Map.Entry<String, Integer> entry : clients.entrySet()) {
                if (!first) {
                    userList.append(";");
                }
                userList.append(entry.getKey()).append(",").append(entry.getValue());
                first = false;
            }
        }

        out.println(userList);
    }

    private void checkHeartbeats() {
        List<String> toRemove = new ArrayList<>();

        synchronized (this) {
            Iterator<Map.Entry<String, PrintWriter>> it = clientWriters.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, PrintWriter> entry = it.next();
                try {
                    entry.getValue().println("HEARTBEAT_CHECK");
                } catch (Exception e) {
                    toRemove.add(entry.getKey());
                }
            }

            for (String username : toRemove) {
                clients.remove(username);
                clientWriters.remove(username);
                System.out.println(username + " 心跳检测失败，强制下线");
            }
        }

        if (!toRemove.isEmpty()) {
            broadcastUserList();
            broadcastOnlineUsers();
        }
    }
}
