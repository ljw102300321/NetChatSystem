package main.java.ChatFunction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ChatClient extends JFrame implements ActionListener {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JComboBox<String> friendSelector;
    private DefaultComboBoxModel<String> friendsModel;
    private DatagramSocket socket;
    private Thread receiveThread;
    private int localPort;
    private InetAddress serverAddress;
    private int serverPort = 10086;
    private Map<String, Integer> friendsMap;

    public ChatClient(int localPort) {
        this.localPort = localPort;
        setTitle("聊天客户端 - 端口: " + localPort);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        sendButton = new JButton("发送");
        sendButton.addActionListener(this);

        friendsModel = new DefaultComboBoxModel<>();
        friendSelector = new JComboBox<>(friendsModel);
        friendSelector.addItemListener(e -> {
            try {
                updateServerAddress();
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(friendSelector, BorderLayout.WEST);
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        friendsMap = new HashMap<>();
        addFriend("客户端1", 10086);
        addFriend("客户端2", 10087);
        addFriend("客户端3", 10088);

        try {
            socket = new DatagramSocket(localPort); // 绑定到本地端口
            startReceivingMessages();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "无法初始化客户端：" + e.getMessage());
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

    @Override
    public void actionPerformed(ActionEvent e) {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                byte[] sendData = message.getBytes();
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

    private void updateServerAddress() throws UnknownHostException {
        String selectedFriend = (String) friendSelector.getSelectedItem();
        if (selectedFriend != null && friendsMap.containsKey(selectedFriend)) {
            serverAddress = InetAddress.getByName("127.0.0.1");
            serverPort = friendsMap.get(selectedFriend);
        }
    }

    private void addFriend(String name, int port) {
        friendsModel.addElement(name);
        friendsMap.put(name, port);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatClient(10086); // 第一个客户端
            new ChatClient(10087); // 第二个客户端
            new ChatClient(10088); // 第三个客户端
        });
    }
}



