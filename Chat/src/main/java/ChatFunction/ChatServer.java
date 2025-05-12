package main.java.ChatFunction;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ChatServer extends JFrame {
    private JTextArea chatArea;
    private DatagramSocket socket;
    private Thread receiveThread;

    public ChatServer() {
        setTitle("聊天服务端");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        getContentPane().add(scrollPane, BorderLayout.CENTER);

        try {
            socket = new DatagramSocket(10086); // 绑定到端口10086
            startReceivingMessages();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "无法启动服务端：" + e.getMessage());
            System.exit(1);
        }

        setVisible(true);
    }

    private void startReceivingMessages() {
        receiveThread = new Thread(() -> {
            while (true) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                    chatArea.append("收到: " + receivedMessage + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        receiveThread.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatServer::new);
    }
}



