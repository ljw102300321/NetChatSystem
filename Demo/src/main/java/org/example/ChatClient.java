package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Lenovo
 */
public class ChatClient extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JList<String> friendList;
    private final DefaultListModel<String> friendsModel;
    private DatagramSocket udpSocket;
    private final int udpPort;
    private Socket tcpSocket;
    private PrintWriter tcpOut;
    private BufferedReader tcpIn;
    private final String username;
    private final Map<String, Integer> friendsMap;
    private ScheduledExecutorService heartbeatExecutor;

    // 新增：在线好友相关
    private DefaultListModel<String> onlineFriendsModel;
    private Set<String> userFriends = new HashSet<>();

    public ChatClient(int udpPort, String username) {
        this.udpPort = udpPort;
        this.username = username;
        friendsModel = new DefaultListModel<>(); // 初始化为空好友列表
        onlineFriendsModel = new DefaultListModel<>();
        userFriends = new HashSet<>();
        friendsMap = new ConcurrentHashMap<>();
        // 新增：从数据库加载已有好友
        loadFriendsFromDatabase();
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

        // 好友列表（初始为空）
        friendList = new JList<>(friendsModel);
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane friendScroll = new JScrollPane(friendList);
        friendScroll.setPreferredSize(new Dimension(150, 400));
        mainPanel.add(friendScroll, BorderLayout.WEST);

        // 在线好友列表
        onlineFriendsModel = new DefaultListModel<>();
        JList<String> onlineFriendList = new JList<>(onlineFriendsModel);
        onlineFriendList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        onlineFriendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 双击添加好友
        onlineFriendList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) { // 双击
                    String selected = onlineFriendList.getSelectedValue();
                    if (selected != null && !userFriends.contains(selected)) {
                        userFriends.add(selected);
                        friendsModel.addElement(selected);

                        // 同步添加到 friendsMap，默认使用服务器提供的UDP端口
                        if (friendsMap.containsKey(selected)) {
                            // 如果已经存在，直接保留已有端口
                            appendToChat("已添加好友: " + selected);
                        } else {
                            // 如果不存在，先设为默认值，后续通过刷新获取真实端口
                            friendsMap.put(selected, 0); // 默认设为0
                            appendToChat("已添加好友: " + selected + "（端口待更新）");
                        }
                    }
                }
            }
        });

        JScrollPane onlineFriendScroll = new JScrollPane(onlineFriendList);
        JLabel onlineLabel = new JLabel("在线用户(双击对话）");
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setPreferredSize(new Dimension(150, 400));
        rightPanel.add(onlineLabel, BorderLayout.NORTH);
        rightPanel.add(onlineFriendScroll, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        // 底部面板
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        // 消息输入框
        messageField = new JTextField();
        messageField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        messageField.addActionListener(_ -> sendMessage());
        bottomPanel.add(messageField, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));

        // 文件发送按钮
        JButton fileSendButton = new JButton("发送文件");
        fileSendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        fileSendButton.addActionListener(_ -> sendFile());
        buttonPanel.add(fileSendButton);

        // 发送按钮
        JButton sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        sendButton.addActionListener(_ -> sendMessage());
        buttonPanel.add(sendButton);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // 顶部面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton refreshButton = new JButton("刷新好友列表");
        refreshButton.addActionListener(_ -> tcpOut.println("REQUEST_USERLIST"));
        topPanel.add(refreshButton);

        JButton refreshOnlineButton = new JButton("刷新在线用户");
        refreshOnlineButton.addActionListener(_ -> tcpOut.println("REQUEST_ONLINE_USERS"));
        topPanel.add(refreshOnlineButton);

        // 添加面板到主窗口
        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        // 新增：添加好友的输入框和按钮
        JPanel addFriendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JLabel friendLabel = new JLabel("添加好友:");
        friendLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        addFriendPanel.add(friendLabel);

        JTextField friendInputField = new JTextField(15);
        friendInputField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        addFriendPanel.add(friendInputField);

        JButton addFriendButton = new JButton("添加");
        addFriendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        addFriendButton.addActionListener(_ -> {
            String friendName = friendInputField.getText().trim();
            if (friendName.isEmpty()) {
                showError("添加失败", "请输入要添加的好友用户名");
                return;
            }

            if (userFriends.contains(friendName)) {
                showError("添加失败", "该好友已存在");
                return;
            }

            // 查询好友是否存在
            String friendId;
            try {
                friendId = JDBCUnil.selectId(friendName);
            } catch (Exception ex) {
                showError("数据库错误", "无法查询好友信息: " + ex.getMessage());
                return;
            }

            if ("-1".equals(friendId)) {
                // 数据库中没有该用户
                showError("添加失败", "该用户不存在，请确认用户名正确");
                return;
            }

            // 用户存在，继续添加
            userFriends.add(friendName);
            friendsModel.addElement(friendName);
            friendsMap.put(friendName, 0); // 端口后续通过服务器获取
            appendToChat("已添加好友: " + friendName + "（端口待更新）");
            friendInputField.setText("");

            // 插入数据库（可选）
            try {
                JDBCUnil.insertFriend(String.valueOf(udpPort), friendId);
            } catch (Exception ex) {
                showError("数据库错误", "保存好友失败: " + ex.getMessage());
            }
        });

        addFriendPanel.add(addFriendButton);

        topPanel.add(addFriendPanel); // 将新面板加入顶部面板

        JButton removeFriendButton = new JButton("删除好友");
        removeFriendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        removeFriendButton.addActionListener(_ -> {
            String selected = friendList.getSelectedValue();
            if (selected != null && userFriends.contains(selected)) {
                userFriends.remove(selected);
                friendsModel.removeElement(selected);
                friendsMap.remove(selected);
                appendToChat("已删除好友: " + selected);
            }
        });
        addFriendPanel.add(removeFriendButton); // buttonPanel 是你放置按钮的容器

        //好友在线信息
        friendList.setCellRenderer(new DefaultListCellRenderer() {
            private final JLabel label = new JLabel();

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                String friendName = (String) value;
                label.setText(friendName);

                if (isUserOnline(friendName)) {
                    label.setForeground(Color.GREEN);
                } else {
                    label.setForeground(Color.RED);
                }

                label.setOpaque(true);
                if (isSelected) {
                    label.setBackground(list.getSelectionBackground());
                    label.setForeground(list.getSelectionForeground());
                } else {
                    label.setBackground(list.getBackground());
                }

                return label;
            }

            // 检查用户是否在线（根据 onlineFriendsModel）
            private boolean isUserOnline(String name) {
                return JDBCUnil.isOnline(name, onlineFriendsModel);
            }
        });

        friendList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = friendList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        friendList.setSelectedIndex(index);
                        JPopupMenu menu = new JPopupMenu();
                        JMenuItem removeItem = new JMenuItem("删除好友");
                        removeItem.addActionListener(_ -> {
                            String selected = friendList.getSelectedValue();
                            if (selected != null && userFriends.contains(selected)) {
                                userFriends.remove(selected);
                                friendsModel.removeElement(selected);
                                friendsMap.remove(selected);
                                appendToChat("已删除好友: " + selected);
                            }
                        });
                        menu.add(removeItem);
                        menu.show(friendList, e.getX(), e.getY());
                    }
                }
            }
        });




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
                    new OutputStreamWriter(tcpSocket.getOutputStream(), StandardCharsets.UTF_8), true);
            tcpIn = new BufferedReader(
                    new InputStreamReader(tcpSocket.getInputStream(), StandardCharsets.UTF_8));

            // 发送登录信息
            tcpOut.println("LOGIN:" + username + ":" + udpPort);

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

                    String received = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

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
        if (parts.length != 3) {
            return;
        }

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
                } else if (message.startsWith("ONLINE_USERS:")) {
                    updateOnlineFriends(message.substring(13));
                } else if (message.startsWith("MSG_SENT:")) {
                    String[] parts = message.split(":", 3);
/*                    if (parts.length == 3) {
                        //appendToChat("消息已发送给 " + parts[1] + ": " + parts[2]);
                    }
 */
                } else if (message.startsWith("USER_NOT_FOUND:")) {
                    appendToChat("用户 " + message.substring(15) + " 不存在");
                } else if (message.startsWith("FILE_SENT:")) {
                    String[] parts = message.split(":", 3);
                /*    if (parts.length == 3) {
                        //appendToChat("文件发送给 " + parts[1] + ": " + parts[2]);
                    }
                 */
                } else if (message.startsWith("FILE_FAILED:")) {
                    String[] parts = message.split(":", 3);
                    if (parts.length == 3) {
                        appendToChat("文件发送失败: " + parts[2]);
                    }
                } else if ("HEARTBEAT_CHECK".equals(message)) {
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
        // 不再自动填充本地好友列表
    }

    private void updateOnlineFriends(String userListStr) {
        SwingUtilities.invokeLater(() -> {
            Set<String> currentOnlineUsers = new HashSet<>();
            String[] users = userListStr.split(";");

            for (String userInfo : users) {
                if (!userInfo.isEmpty()) {
                    String[] parts = userInfo.split(",");
                    if (parts.length >= 2) {
                        String name = parts[0].trim();
                        int port = Integer.parseInt(parts[1].trim());

                        if (!name.equals(username)) {
                            currentOnlineUsers.add(name);
                            friendsMap.put(name, port); // 更新好友端口
                        }
                    }
                }
            }

            // 只更新在线列表，避免全部删除再添加
            for (int i = 0; i < onlineFriendsModel.getSize(); i++) {
                String user = onlineFriendsModel.getElementAt(i);
                if (!currentOnlineUsers.contains(user)) {
                    onlineFriendsModel.removeElement(user);
                }
            }

            for (String user : currentOnlineUsers) {
                if (!containsUser(onlineFriendsModel, user)) {
                    onlineFriendsModel.addElement(user);
                }
            }
        });
    }

    // 辅助方法：检查ListModel是否包含指定用户
    private boolean containsUser(DefaultListModel<String> model, String user) {
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).equals(user)) {
                return true;
            }
        }
        return false;
    }


    // 在 sendMessage() 方法中添加刷新逻辑
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        String selected = friendList.getSelectedValue();
        if (selected == null) {
            showError("发送失败", "未选择好友");
            return;
        }

        if (!friendsMap.containsKey(selected)) {
            if (!JDBCUnil.isOnline(selected, onlineFriendsModel)) {
                showError("发送失败", "好友 " + selected + " 当前离线");
            } else {
                showError("发送失败", "无法获取好友的UDP端口");
            }
            return;
        }


        // 刷新好友端口
        tcpOut.println("REQUEST_ONLINE_USERS");

        int toPort = friendsMap.getOrDefault(selected, 0);
        if (toPort == 0) {
            showError("发送失败", "好友离线或尚未获取该好友的UDP端口，请重试或等待好友上线");
            return;
        }

        try {
            DatagramSocket udpSocket = new DatagramSocket();
            byte[] sendData = (username + ":" + message).getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length,
                    InetAddress.getByName("127.0.0.1"), toPort);
            udpSocket.send(sendPacket);
            udpSocket.close();

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
            if (selectedFriend == null) {
                showError("发送失败", "未选择好友");
                return;
            }

            if (!friendsMap.containsKey(selectedFriend)) {
                if (!JDBCUnil.isOnline(selectedFriend, onlineFriendsModel)) {
                    showError("发送失败", "好友 " + selectedFriend + " 当前离线");
                } else {
                    showError("发送失败", "无法获取好友的UDP端口");
                }
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

    private void loadFriendsFromDatabase() {
        try {
            // 假设 JDBCUnil.getFriends(username) 返回 List<String> 类型的好友名列表
            // 需要你实现这个方法
            List<String> friends = JDBCUnil.getFriends(username);
            for (String friend : friends) {
                userFriends.add(friend);
                friendsModel.addElement(friend);
                // 先设置为默认端口，后续刷新获取真实值
                friendsMap.put(friend, 0);
            }
            appendToChat("已从数据库加载 " + friends.size() + " 位好友");
        } catch (Exception e) {
            showError("加载好友失败", "无法从数据库中读取好友列表: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String username = JOptionPane.showInputDialog(null, "请输入用户名:");
            if (username == null || username.trim().isEmpty()) {
                System.exit(0);
            }
            new ChatClient(8000 + new Random().nextInt(1000), username);
        });
    }
}
