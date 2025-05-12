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
    private JList<String> friendList;
    private DefaultListModel<String> friendsModel;
    private DatagramSocket socket;
    private Thread receiveThread;
    private int localPort;
    private InetAddress serverAddress;
    private int serverPort = 10086;
    private Map<String, Integer> friendsMap;

    public ChatClient(int localPort) {
        this.localPort = localPort;
        setTitle("聊天客户端 - 端口: " + localPort);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 设置整体风格
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        messageField.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        sendButton.addActionListener(this);

        friendsModel = new DefaultListModel<>();
        friendList = new JList<>(friendsModel);
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        friendList.addListSelectionListener(e -> {
            try {
                updateServerAddress();
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JScrollPane(friendList), BorderLayout.WEST);
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton addFriendButton = new JButton("添加好友");
        addFriendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        addFriendButton.addActionListener(e -> showAddFriendDialog());
        topPanel.add(addFriendButton);

        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        friendsMap = new HashMap<>();

        try {
            socket = new DatagramSocket(localPort); // 绑定到本地端口
            startReceivingMessages();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "无法初始化客户端：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
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
                    SwingUtilities.invokeLater(() -> chatArea.append("收到: " + receivedMessage + "\n"));
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
                JOptionPane.showMessageDialog(this, "发送失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateServerAddress() throws UnknownHostException {
        String selectedFriend = friendList.getSelectedValue();
        if (selectedFriend != null && friendsMap.containsKey(selectedFriend)) {
            serverAddress = InetAddress.getByName("127.0.0.1");
            serverPort = friendsMap.get(selectedFriend);
        }
    }

    private void addFriend(String name, int port) {
        friendsModel.addElement(name);
        friendsMap.put(name, port);
    }

    private void showAddFriendDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        JLabel nameLabel = new JLabel("好友名称:");
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        JTextField nameField = new JTextField();
        JLabel portLabel = new JLabel("端口号:");
        portLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        JTextField portField = new JTextField();

        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(portLabel);
        panel.add(portField);

        int result = JOptionPane.showConfirmDialog(this, panel, "添加好友", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String portStr = portField.getText().trim();
            if (!name.isEmpty() && !portStr.isEmpty()) {
                try {
                    int port = Integer.parseInt(portStr);
                    addFriend(name, port);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "请输入有效的端口号", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "请填写所有字段", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatClient(10086); // 第一个客户端
            new ChatClient(10087); // 第二个客户端
            new ChatClient(10088); // 第三个客户端
        });
    }
}



