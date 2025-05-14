package ChatFunction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

public class ChatClient extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton fileSendButton;
    private JList<String> friendList;
    private DefaultListModel<String> friendsModel;
    private DatagramSocket udpSocket;
    private final int udpPort;
    private Socket tcpSocket;
    private PrintWriter tcpOut;
    private BufferedReader tcpIn;
    private final String username;
    private final Map<String, Integer> friendsMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService heartbeatExecutor;

    public ChatClient(int udpPort, String username) {
        this.udpPort = udpPort;
        this.username = username;
        initializeGUI();
        initializeNetwork();
    }

    private void initializeGUI() {
        setTitle("聊天客户端 - " + username + " (UDP端口:" + udpPort + ")");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            showError("外观设置错误", e.getMessage());
        }

        // 主面板布局
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));

        // 聊天区域
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(600, 400));
        mainPanel.add(chatScroll, BorderLayout.CENTER);

        // 好友列表
        friendsModel = new DefaultListModel<>();
        friendList = new JList<>(friendsModel);
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane friendScroll = new JScrollPane(friendList);
        friendScroll.setPreferredSize(new Dimension(150, 400));
        mainPanel.add(friendScroll, BorderLayout.WEST);

        // 底部面板
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        // 消息输入框
        messageField = new JTextField();
        messageField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        messageField.addActionListener(e -> sendMessage());
        bottomPanel.add(messageField, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));

        // 文件发送按钮
        fileSendButton = new JButton("发送文件");
        fileSendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        fileSendButton.addActionListener(e -> sendFile());
        buttonPanel.add(fileSendButton);

        // 发送按钮
        sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        sendButton.addActionListener(e -> sendMessage());
        buttonPanel.add(sendButton);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // 顶部面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton refreshButton = new JButton("刷新好友列表");
        refreshButton.addActionListener(e -> tcpOut.println("REQUEST_USERLIST"));
        topPanel.add(refreshButton);

        // 添加面板到主窗口
        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void initializeNetwork() {
        try {
            // 初始化UDP Socket
            udpSocket = new DatagramSocket(udpPort);
            startUdpReceiver();

            // 连接TCP服务器
            tcpSocket = new Socket("127.0.0.1", 9999);
            tcpOut = new PrintWriter(
                    new OutputStreamWriter(tcpSocket.getOutputStream(), "UTF-8"), true);
            tcpIn = new BufferedReader(
                    new InputStreamReader(tcpSocket.getInputStream(), "UTF-8"));

            // 发送登录信息
            tcpOut.println("LOGIN:" + username + ":" + udpPort);

            // 启动心跳
            startHeartbeat();

            // 启动服务器消息接收线程
            new Thread(this::receiveServerMessages).start();

        } catch (IOException e) {
            showError("网络初始化失败", e.getMessage());
            System.exit(1);
        }
    }

    private void startUdpReceiver() {
        new Thread(() -> {
            byte[] buffer = new byte[65507]; // UDP最大包大小
            while (!udpSocket.isClosed()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);

                    String received = new String(packet.getData(), 0, packet.getLength(), "UTF-8");

                    if (received.startsWith("FILE:")) {
                        handleFileTransfer(packet, received);
                    } else {
                        handleTextMessage(received);
                    }
                } catch (IOException e) {
                    if (!udpSocket.isClosed()) {
                        showError("接收UDP消息失败", e.getMessage());
                    }
                }
            }
        }).start();
    }

    private void handleFileTransfer(DatagramPacket packet, String header) throws IOException {
        String[] parts = header.split(":", 3);
        if (parts.length != 3) return;

        String from = parts[1];
        String fileName = parts[2];

        // 接收文件内容
        byte[] fileData = Arrays.copyOf(packet.getData(), packet.getLength());

        SwingUtilities.invokeLater(() -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(fileName));
            fileChooser.setDialogTitle("保存接收的文件");

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    Files.write(selectedFile.toPath(), fileData);
                    appendToChat(from + " 发送的文件已保存: " + selectedFile.getName());
                } catch (IOException e) {
                    showError("保存文件失败", e.getMessage());
                }
            }
        });
    }

    private void handleTextMessage(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String from = parts[0];
            String content = parts[1];
            appendToChat(from + ": " + content);
            // 通知服务器已收到消息
            tcpOut.println("MSG_RECEIVED:" + from + ":" + username);
        }
    }

    private void receiveServerMessages() {
        try {
            String message;
            while ((message = tcpIn.readLine()) != null) {
                if (message.startsWith("USERLIST:")) {
                    updateFriendList(message.substring(9));
                } else if (message.startsWith("MSG_SENT:")) {
                    String[] parts = message.split(":", 3);
                    if (parts.length == 3) {
                        appendToChat("消息已发送给 " + parts[1] + ": " + parts[2]);
                    }
                } else if (message.startsWith("USER_NOT_FOUND:")) {
                    appendToChat("用户 " + message.substring(15) + " 不存在");
                } else if (message.startsWith("FILE_SENT:")) {
                    String[] parts = message.split(":", 3);
                    if (parts.length == 3) {
                        appendToChat("文件发送给 " + parts[1] + ": " + parts[2]);
                    }
                } else if (message.startsWith("FILE_FAILED:")) {
                    String[] parts = message.split(":", 3);
                    if (parts.length == 3) {
                        appendToChat("文件发送失败: " + parts[2]);
                    }
                } else if (message.equals("HEARTBEAT_CHECK")) {
                    tcpOut.println("HEARTBEAT");
                } else {
                    appendToChat("系统: " + message);
                }
            }
        } catch (IOException e) {
            showError("服务器连接错误", e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void updateFriendList(String userListStr) {
        SwingUtilities.invokeLater(() -> {
            friendsModel.clear();
            friendsMap.clear();

            String[] users = userListStr.split(";");
            for (String userInfo : users) {
                if (!userInfo.isEmpty()) {
                    String[] parts = userInfo.split(",");
                    if (parts.length == 2) {
                        String name = parts[0];
                        int port = Integer.parseInt(parts[1]);
                        friendsModel.addElement(name);
                        friendsMap.put(name, port);
                    }
                }
            }
        });
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;

        String selected = friendList.getSelectedValue();
        if (selected == null || !friendsMap.containsKey(selected)) {
            showError("发送失败", "请选择有效的好友");
            return;
        }

        try {
            // 直接通过UDP发送消息
            int toPort = friendsMap.get(selected);
            DatagramSocket udpSocket = new DatagramSocket();
            byte[] sendData = (username + ":" + message).getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length,
                    InetAddress.getByName("127.0.0.1"), toPort);
            udpSocket.send(sendPacket);
            udpSocket.close();

            // 通知服务器消息详情
            tcpOut.println("SEND:" + selected + ":" + message);
            appendToChat("我 -> " + selected + ": " + message);
            messageField.setText("");
        } catch (IOException ex) {
            showError("发送消息失败", ex.getMessage());
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择要发送的文件");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String selectedFriend = friendList.getSelectedValue();

            if (selectedFriend == null || !friendsMap.containsKey(selectedFriend)) {
                showError("发送失败", "请选择有效的好友");
                return;
            }

            // 通知服务器文件发送动作
            tcpOut.println("SEND_FILE:" + selectedFriend + ":" + file.getName());
            appendToChat("我 -> " + selectedFriend + ": [发送文件] " + file.getName());

            // 实际文件通过UDP直接发送
            new Thread(() -> {
                try {
                    byte[] fileData = Files.readAllBytes(file.toPath());

                    // 发送文件头信息
                    String header = "FILE:" + username + ":" + file.getName();
                    byte[] headerData = header.getBytes("UTF-8");

                    DatagramSocket udpSocket = new DatagramSocket();
                    DatagramPacket headerPacket = new DatagramPacket(
                            headerData, headerData.length,
                            InetAddress.getByName("127.0.0.1"), friendsMap.get(selectedFriend));
                    udpSocket.send(headerPacket);

                    // 发送文件内容
                    DatagramPacket filePacket = new DatagramPacket(
                            fileData, fileData.length,
                            InetAddress.getByName("127.0.0.1"), friendsMap.get(selectedFriend));
                    udpSocket.send(filePacket);

                    udpSocket.close();
                    appendToChat("文件 " + file.getName() + " 已发送");
                } catch (IOException ex) {
                    showError("发送文件失败", ex.getMessage());
                }
            }).start();
        }
    }

    private void startHeartbeat() {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                tcpOut.println("HEARTBEAT");
            } catch (Exception e) {
                showError("心跳发送失败", e.getMessage());
                disconnect();
            }
        }, 0, 25, TimeUnit.SECONDS);
    }

    private void disconnect() {
        try {
            if (heartbeatExecutor != null) {
                heartbeatExecutor.shutdownNow();
            }
            if (udpSocket != null) {
                udpSocket.close();
            }
            if (tcpSocket != null) {
                tcpSocket.close();
            }
        } catch (IOException e) {
            System.err.println("断开连接时出错: " + e.getMessage());
        }
    }

    private void appendToChat(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void showError(String title, String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatClient(10086, "用户1");
            // 可以启动多个客户端测试
            new ChatClient(10087, "用户2");
        });
    }
}
/*
package ChatFunction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatClient extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> friendList;
    private DefaultListModel<String> friendsModel;
    private DatagramSocket udpSocket;
    private int udpPort;
    private Socket tcpSocket;
    private PrintWriter tcpOut;
    private BufferedReader tcpIn;
    private String username;
    private Map<String, Integer> friendsMap;
    private ScheduledExecutorService heartbeatExecutor;

    public ChatClient(int udpPort, String username) {
        this.udpPort = udpPort;
        this.username = username;
        initializeGUI();
        initializeNetwork();
    }

    private void initializeGUI() {
        setTitle("聊天客户端 - " + username + " (UDP端口:" + udpPort + ")");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            showError("无法设置系统外观", e.getMessage());
        }

        // 聊天区域
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane chatScroll = new JScrollPane(chatArea);

        // 消息输入框
        messageField = new JTextField();
        messageField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        messageField.addActionListener(e -> sendMessage());

        // 发送按钮
        sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        sendButton.addActionListener(e -> sendMessage());

        // 好友列表
        friendsModel = new DefaultListModel<>();
        friendList = new JList<>(friendsModel);
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane friendScroll = new JScrollPane(friendList);

        // 底部面板
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(friendScroll, BorderLayout.WEST);
        bottomPanel.add(messageField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(sendButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // 顶部面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("刷新好友列表");
        refreshButton.addActionListener(e -> tcpOut.println("REQUEST_USERLIST"));
        topPanel.add(refreshButton);

        // 主界面布局
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(chatScroll, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        friendsMap = new ConcurrentHashMap<>();
        setVisible(true);
    }

    private void initializeNetwork() {
        try {
            // 初始化UDP Socket
            udpSocket = new DatagramSocket(udpPort);
            startUdpReceiver();

            // 连接TCP服务器
            tcpSocket = new Socket("127.0.0.1", 9999);
            tcpOut = new PrintWriter(
                    new OutputStreamWriter(tcpSocket.getOutputStream(), "UTF-8"), true);
            tcpIn = new BufferedReader(
                    new InputStreamReader(tcpSocket.getInputStream(), "UTF-8"));

            // 发送登录信息
            tcpOut.println("LOGIN:" + username + ":" + udpPort);

            // 启动心跳
            startHeartbeat();

            // 启动服务器消息接收线程
            new Thread(this::receiveServerMessages).start();

        } catch (IOException e) {
            showError("网络初始化失败", e.getMessage());
            System.exit(1);
        }
    }

    private void startUdpReceiver() {
        new Thread(() -> {
            while (!udpSocket.isClosed()) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);

                    String received = new String(packet.getData(), 0, packet.getLength());
                    String[] parts = received.split(":", 2);

                    if (parts.length == 2) {
                        String from = parts[0];
                        String message = parts[1];
                        appendToChat(from + ": " + message);

                        // 通知服务器已收到消息
                        tcpOut.println("MSG_RECEIVED:" + from + ":" + username);
                    }
                } catch (IOException e) {
                    if (!udpSocket.isClosed()) {
                        showError("接收UDP消息失败", e.getMessage());
                    }
                }
            }
        }).start();
    }

    private void receiveServerMessages() {
        try {
            String message;
            while ((message = tcpIn.readLine()) != null) {
                if (message.startsWith("USERLIST:")) {
                    updateFriendList(message.substring(9));
                } else if (message.startsWith("MSG_SENT:")) {
                    appendToChat("消息已发送给 " + message.substring(9));
                } else if (message.startsWith("USER_NOT_FOUND:")) {
                    appendToChat("用户 " + message.substring(15) + " 不存在");
                } else if (message.startsWith("ADD_FRIEND_SUCCESS:")) {
                    String[] parts = message.split(":");
                    if (parts.length == 3) {
                        addFriend(parts[1], Integer.parseInt(parts[2]));
                    }
                } else if (message.equals("HEARTBEAT_CHECK")) {
                    tcpOut.println("HEARTBEAT");
                } else {
                    appendToChat("系统: " + message);
                }
            }
        } catch (IOException e) {
            showError("服务器连接错误", e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void updateFriendList(String userListStr) {
        friendsModel.clear();
        friendsMap.clear();

        String[] users = userListStr.split(";");
        for (String userInfo : users) {
            if (!userInfo.isEmpty()) {
                String[] parts = userInfo.split(",");
                if (parts.length == 2) {
                    addFriend(parts[0], Integer.parseInt(parts[1]));
                }
            }
        }
    }

    private void addFriend(String name, int port) {
        if (!friendsMap.containsKey(name)) {
            friendsModel.addElement(name);
            friendsMap.put(name, port);
            appendToChat("已添加好友: " + name + " (端口:" + port + ")");
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;

        String selected = friendList.getSelectedValue();
        if (selected == null || !friendsMap.containsKey(selected)) {
            showError("发送失败", "请选择有效的好友");
            return;
        }

        tcpOut.println("SEND:" + selected + ":" + message);
        appendToChat("我 -> " + selected + ": " + message);
        messageField.setText("");
    }

    private void startHeartbeat() {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                tcpOut.println("HEARTBEAT");
            } catch (Exception e) {
                showError("心跳发送失败", e.getMessage());
                disconnect();
            }
        }, 0, 25, TimeUnit.SECONDS);
    }

    private void disconnect() {
        try {
            if (heartbeatExecutor != null) {
                heartbeatExecutor.shutdown();
            }
            if (udpSocket != null) {
                udpSocket.close();
            }
            if (tcpSocket != null) {
                tcpSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendToChat(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void showError(String title, String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE));
    }

    public static void main(String[] args) {
        // 示例启动代码
        SwingUtilities.invokeLater(() -> {
            new ChatClient(10086, "用户1");
            // 可以启动多个客户端测试
             new ChatClient(10087, "用户2");
        });
    }
}
*/