package ChatFunction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
/*
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

public class ChatClient extends JFrame implements ActionListener {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton fileSendButton;
    private JList<String> friendList;
    private DefaultListModel<String> friendsModel;
    private DatagramSocket socket;
    private Thread receiveThread;
    private int localPort;
    private InetAddress serverAddress;
    private int serverPort = 10086;
    private Map<String, Integer> friendsMap;
    private String clientName; // 客户端名称

    public ChatClient(int localPort, String clientName) {
        this.localPort = localPort;
        this.clientName = clientName;
        setTitle("聊天客户端 - 端口: " + localPort);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 设置整体风格
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            showErrorDialog("无法设置系统外观", e.getMessage());
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

        fileSendButton = new JButton("发送文件");
        fileSendButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        fileSendButton.addActionListener(e -> sendFile());

        friendsModel = new DefaultListModel<>();
        friendList = new JList<>(friendsModel);
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        friendList.addListSelectionListener(e -> {
            try {
                updateServerAddress();
            } catch (UnknownHostException ex) {
                showErrorDialog("未知主机地址", ex.getMessage());
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JScrollPane(friendList), BorderLayout.WEST);
        bottomPanel.add(messageField, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(fileSendButton);
        buttonPanel.add(sendButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

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
            showErrorDialog("无法初始化客户端", e.getMessage());
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
                    //System.out.println("Received: " + receivedMessage);
                    if (receivedMessage.startsWith("[FILE]")) {
                        handleFileReceive(receivedMessage.substring(6));
                    } else {
                        String sender = getSenderFromMessage(receivedMessage);
                        String message = getMessageContent(receivedMessage);
                        SwingUtilities.invokeLater(() -> chatArea.append(sender + ": " + message + "\n"));
                    }
                } catch (IOException e) {
                    showErrorDialog("接收消息失败", e.getMessage());
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
                String fullMessage = clientName + ": " + message;
                byte[] sendData = fullMessage.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
                socket.send(sendPacket);
                chatArea.append(fullMessage + "\n");
                messageField.setText("");
            } catch (IOException ex) {
                showErrorDialog("发送消息失败", ex.getMessage());
            }
        }
    }

    private void updateServerAddress() throws UnknownHostException {
        String selectedFriend = friendList.getSelectedValue();
        if (selectedFriend != null && friendsMap.containsKey(selectedFriend)) {
            serverAddress = InetAddress.getByName("127.0.0.1");
            serverPort = friendsMap.get(selectedFriend);
        } else {
            throw new UnknownHostException("未选择有效的好友");
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
                    showErrorDialog("无效的端口号", "请输入有效的端口号");
                }
            } else {
                showErrorDialog("缺少必要信息", "请填写所有字段");
            }
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[(int) file.length()];
                fis.read(buffer);

                String fileName = file.getName();
                String header = "[FILE]" + clientName + ":" + fileName + "|";
                byte[] headerBytes = header.getBytes();
                byte[] dataToSend = new byte[headerBytes.length + buffer.length];
                System.arraycopy(headerBytes, 0, dataToSend, 0, headerBytes.length);
                System.arraycopy(buffer, 0, dataToSend, headerBytes.length, buffer.length);

                DatagramPacket sendPacket = new DatagramPacket(dataToSend, dataToSend.length, serverAddress, serverPort);
                socket.send(sendPacket);
                chatArea.append(clientName + " 发送文件: " + fileName + "\n");
            } catch (IOException ex) {
                showErrorDialog("发送文件失败", ex.getMessage());
            }
        }
    }

    private void handleFileReceive(String data) {
        String[] parts = data.split("\\|", 2);
        if (parts.length != 2) return;

        String senderAndFileName = parts[0];
        String[] senderParts = senderAndFileName.split(":");
        if (senderParts.length != 2) return;

        String sender = senderParts[0];
        String fileName = senderParts[1];
        byte[] fileData = parts[1].getBytes();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(fileName));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileData);
                chatArea.append(sender + " 接收文件: " + fileName + "\n");
            } catch (IOException ex) {
                showErrorDialog("保存文件失败", ex.getMessage());
            }
        }
    }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private String getSenderFromMessage(String message) {
        int colonIndex = message.indexOf(':');
        if (colonIndex > 0) {
            return message.substring(0, colonIndex).trim();
        }
        return "未知用户";
    }

    private String getMessageContent(String message) {
        int colonIndex = message.indexOf(':');
        if (colonIndex > 0) {
            return message.substring(colonIndex + 1).trim();
        }
        return message;
    }

    public static void main(String[] args) {
/*       11

 */
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



