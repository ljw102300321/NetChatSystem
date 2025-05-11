package ChatFuction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Random;

public class LoginJframe extends JFrame implements ActionListener {
    private String verification = create();
    // 组件声明
    private JPanel mainPanel;
    private JPanel formPanel;
    private JLabel titleLabel;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel codeLabel;
    private JButton refreshCodeButton;
    private JButton loginButton;
    private JButton registerButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField codeField;
    private JLabel verificationCodeLabel;

    public LoginJframe() {
        initComponents();
        initJFrame();
        this.setVisible(true);
    }

    private void initComponents() {
        // 主面板设置 - 使用渐变背景
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(230, 240, 255);
                Color color2 = new Color(180, 210, 255);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // 表单面板 - 白色半透明背景
        formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // 标题
        titleLabel = new JLabel("欢迎登录聊天室");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(new Color(50, 50, 100));
        titleLabel.setBounds(0, 0, 400, 50);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(titleLabel);

        // 用户名标签和输入框
        usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        usernameLabel.setForeground(new Color(80, 80, 80));
        usernameLabel.setBounds(0, 70, 80, 30);
        formPanel.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        usernameField.setBounds(80, 70, 320, 35);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(usernameField);

        // 密码标签和输入框
        passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        passwordLabel.setForeground(new Color(80, 80, 80));
        passwordLabel.setBounds(0, 120, 80, 30);
        formPanel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setBounds(80, 120, 320, 35);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(passwordField);

        // 验证码标签和输入框
        codeLabel = new JLabel("验证码:");
        codeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        codeLabel.setForeground(new Color(80, 80, 80));
        codeLabel.setBounds(0, 170, 80, 30);
        formPanel.add(codeLabel);

        codeField = new JTextField();
        codeField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        codeField.setBounds(80, 170, 150, 35);
        codeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(codeField);

        // 验证码显示区域
        verificationCodeLabel = new JLabel(verification, SwingConstants.CENTER);
        verificationCodeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        verificationCodeLabel.setOpaque(true);
        verificationCodeLabel.setBackground(Color.WHITE);
        verificationCodeLabel.setForeground(new Color(70, 130, 180));
        verificationCodeLabel.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 200)));
        verificationCodeLabel.setBounds(240, 170, 100, 35);
        formPanel.add(verificationCodeLabel);

        // 刷新验证码按钮
        refreshCodeButton = new JButton("换一个");
        refreshCodeButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        refreshCodeButton.setBounds(350, 170, 80, 35);
        refreshCodeButton.setBackground(new Color(70, 130, 180));
        refreshCodeButton.setForeground(Color.WHITE);
        refreshCodeButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        refreshCodeButton.setFocusPainted(false);
        refreshCodeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshCodeButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                refreshCodeButton.setBackground(new Color(90, 150, 200));
            }
            public void mouseExited(MouseEvent e) {
                refreshCodeButton.setBackground(new Color(70, 130, 180));
            }
        });
        refreshCodeButton.addActionListener(this);
        formPanel.add(refreshCodeButton);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBounds(0, 230, 400, 50);

        // 登录按钮
        loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(150, 40));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(90, 150, 200));
            }
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(70, 130, 180));
            }
        });
        loginButton.addActionListener(this);
        buttonPanel.add(loginButton);

        // 注册按钮
        registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        registerButton.setPreferredSize(new Dimension(150, 40));
        registerButton.setBackground(new Color(100, 180, 100));
        registerButton.setForeground(Color.WHITE);
        registerButton.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                registerButton.setBackground(new Color(120, 200, 120));
            }
            public void mouseExited(MouseEvent e) {
                registerButton.setBackground(new Color(100, 180, 100));
            }
        });
        registerButton.addActionListener(this);
        buttonPanel.add(registerButton);

        formPanel.add(buttonPanel);

        // 设置表单面板大小并添加到主面板
        formPanel.setPreferredSize(new Dimension(480, 320));
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(formPanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        this.add(mainPanel);
    }

    public static String create() {
        StringBuilder s = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < 4; i++) {
            int type = r.nextInt(3);
            switch (type) {
                case 0: // 大写字母
                    s.append((char)(r.nextInt(26) + 65));
                    break;
                case 1: // 数字
                    s.append((char)(r.nextInt(10) + 48));
                    break;
                case 2: // 小写字母
                    s.append((char)(r.nextInt(26) + 97));
                    break;
            }
        }
        return s.toString();
    }

    public void initJFrame() {
        // 设置界面大小
        this.setSize(600, 500);
        // 设置界面标题
        this.setTitle("聊天室登录");
        // 设置界面置顶
        this.setAlwaysOnTop(true);
        // 设置界面居中
        this.setLocationRelativeTo(null);
        // 设置默认关闭方式
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 禁止调整窗口大小
        this.setResizable(false);
        // 设置窗口图标
        try {
            ImageIcon icon = new ImageIcon("icon.png");
            this.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.out.println("图标加载失败，使用默认图标");
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == refreshCodeButton) {
            verification = create();
            verificationCodeLabel.setText(verification);
        } else if (obj == loginButton) {
            // 登录逻辑
        } else if (obj == registerButton) {
            this.dispose();
            new RegisterJframe();
        }
    }

    public boolean checkUser(String id) throws SQLException, ClassNotFoundException {
        // 原有检查逻辑
        return false;
    }
}