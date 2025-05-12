package main.java.ChatFunction;

import javax.swing.*;
import javax.swing.border.Border;
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
    private JLabel accountLabel;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel codeLabel;
    private JButton refreshCodeButton;
    private JButton loginButton;
    private JButton registerButton;
    private JTextField accountField;
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
        // 主面板设置 - 渐变背景
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

        // 表单面板
        formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // 标题
        titleLabel = new JLabel("欢迎登录智能聊天室");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(50, 50, 100));
        titleLabel.setBounds(0, 10, 400, 40);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(titleLabel);

        // 账号输入
        accountLabel = createFormLabel("账  号:", 70);
        formPanel.add(accountLabel);

        accountField = createTextField(70);
        formPanel.add(accountField);

        // 用户名输入
        usernameLabel = createFormLabel("用户名:", 120);
        formPanel.add(usernameLabel);

        usernameField = createTextField(120);
        formPanel.add(usernameField);

        // 密码输入
        passwordLabel = createFormLabel("密  码:", 170);
        formPanel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setBounds(80, 170, 320, 35);
        passwordField.setBorder(createInputBorder());
        formPanel.add(passwordField);

        // 验证码输入
        codeLabel = createFormLabel("验证码:", 220);
        formPanel.add(codeLabel);

        codeField = createTextField(220);
        codeField.setBounds(80, 220, 150, 35);
        formPanel.add(codeField);

        // 验证码显示
        verificationCodeLabel = new JLabel(verification, SwingConstants.CENTER);
        verificationCodeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        verificationCodeLabel.setOpaque(true);
        verificationCodeLabel.setBackground(Color.WHITE);
        verificationCodeLabel.setForeground(new Color(70, 130, 180));
        verificationCodeLabel.setBorder(createInputBorder());
        verificationCodeLabel.setBounds(240, 220, 100, 35);
        formPanel.add(verificationCodeLabel);

        // 刷新验证码按钮
        refreshCodeButton = createStyledButton("换一个", 350, 220, 80, 35);
        refreshCodeButton.addActionListener(this);
        formPanel.add(refreshCodeButton);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBounds(0, 280, 400, 50);

        // 登录按钮
        loginButton = createActionButton("登  录", new Color(70, 130, 180));
        loginButton.addActionListener(this);
        buttonPanel.add(loginButton);

        // 注册按钮
        registerButton = createActionButton("注  册", new Color(100, 180, 100));
        registerButton.addActionListener(this);
        buttonPanel.add(registerButton);

        formPanel.add(buttonPanel);

        // 主布局设置
        formPanel.setPreferredSize(new Dimension(480, 360));
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(formPanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        this.add(mainPanel);
    }

    private JLabel createFormLabel(String text, int yPos) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        label.setForeground(new Color(80, 80, 80));
        label.setBounds(0, yPos, 80, 30);
        return label;
    }

    private JTextField createTextField(int yPos) {
        JTextField field = new JTextField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBounds(80, yPos, 320, 35);
        field.setBorder(createInputBorder());
        return field;
    }

    private Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );
    }

    private JButton createStyledButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        button.setBounds(x, y, width, height);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new HoverEffect(button));
        return button;
    }

    private JButton createActionButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(150, 40));
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new HoverEffect(button));
        return button;
    }

    // 鼠标悬停效果处理
    private static class HoverEffect extends MouseAdapter {
        private final JButton button;
        private final Color originalColor;

        public HoverEffect(JButton button) {
            this.button = button;
            this.originalColor = button.getBackground();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            button.setBackground(originalColor.brighter());
        }

        @Override
        public void mouseExited(MouseEvent e) {
            button.setBackground(originalColor);
        }
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
        setSize(600, 500);
        setTitle("智能聊天室登录");
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == refreshCodeButton) {
            verification = create();
            verificationCodeLabel.setText(verification);
        } else if (source == loginButton) {
            openAccountion();
        } else if (source == registerButton) {
            openRegistration();
        }
    }

    private void openAccountion() {
        this.dispose();
        new ChatClient(Integer.parseInt(accountField.getText()));
    }

    private void performLogin() {
        String account = accountField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String inputCode = codeField.getText().trim();

        if (validateInputs(account, username, password, inputCode)) {
            try {
                if (authenticateUser(account, username, password)) {
                    JOptionPane.showMessageDialog(this, "登录成功！");
                    dispose();
                    // 打开主界面
                } else {
                    showError("账号或密码错误");
                }
            } catch (SQLException | ClassNotFoundException ex) {
                showError("数据库连接失败");
            }
        }
    }

    private boolean validateInputs(String... inputs) {
        for (String input : inputs) {
            if (input.isEmpty()) {
                showError("所有字段必须填写！");
                return false;
            }
        }
        if (!codeField.getText().equalsIgnoreCase(verification)) {
            showError("验证码错误！");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    private void openRegistration() {
        this.dispose();
        new RegisterJframe();
    }

    private boolean authenticateUser(String account, String username, String password)
            throws SQLException, ClassNotFoundException {
        // 实现数据库验证逻辑
        return false; // 示例返回值
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new LoginJframe());
    }
}