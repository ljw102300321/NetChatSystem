package main.java.ChatFunction;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
    private DatagramSocket socket;
    private int serverPort = 10085;
    private Map<Integer, InetAddress> clients;

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

    public static void main(String[] args) {
        new ChatServer();
    }
}



