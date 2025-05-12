package main.java.ChatFunction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ChatApp extends JFrame implements ActionListener {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JComboBox<String> modeSelector;
    private DatagramSocket socket;
    private Thread receiveThread;
    private InetAddress serverAddress;
    private int serverPort = 10086;

    public ChatApp() {
        setTitle("好友聊天");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        sendButton = new JButton("发送");
        sendButton.addActionListener(this);

        String[] modes = {"客户端", "服务端"};
        modeSelector = new JComboBox<>(modes);
        modeSelector.addActionListener(e -> {
            String selectedMode = (String) modeSelector.getSelectedItem();
            if ("服务端".equals(selectedMode)) {
                startServer();
            } else {
                stopServer();
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(modeSelector, BorderLayout.WEST);

        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void startServer() {
        try {
            socket = new DatagramSocket(serverPort);
            chatArea.append("已进入服务端模式，等待接收消息...\n");
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
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "无法启动服务端：" + e.getMessage());
        }
    }

    private void stopServer() {
        if (receiveThread != null && receiveThread.isAlive()) {
            receiveThread.interrupt();
        }
        if (socket != null) {
            socket.close();
        }
        chatArea.append("已退出服务端模式\n");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                byte[] sendData = message.getBytes();
                serverAddress = InetAddress.getByName("127.0.0.1"); // 目标地址
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
                socket.send(sendPacket);
                chatArea.append("发送: " + message + "\n");
                messageField.setText("");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "发送失败：" + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatApp::new);
    }
}



