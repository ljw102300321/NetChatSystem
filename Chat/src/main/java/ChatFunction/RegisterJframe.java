package main.java.ChatFunction;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RegisterJframe extends JFrame implements ActionListener {
    // 现代配色方案
    private static final Color GRADIENT_START = new Color(230, 240, 255);
    private static final Color GRADIENT_END = new Color(180, 210, 255);

    private JTextField accountField = new JTextField();
    private JTextField nameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JLabel statusLabel = new JLabel();

    public RegisterJframe() {
        initComponents();
        initJFrame();
        this.setVisible(true);
    }

    private void initComponents() {
        // 主面板设置 - 渐变背景
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, GRADIENT_START, getWidth(), getHeight(), GRADIENT_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // 表单面板
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // 标题
        JLabel titleLabel = new JLabel("加入智聊社区");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(50, 50, 100));
        titleLabel.setBounds(0, 10, 400, 40);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(titleLabel);

        // 账号输入
        JLabel accountLabel = createFormLabel("账  号:", 70);
        formPanel.add(accountLabel);

        accountField = createTextField(70);
        formPanel.add(accountField);

        // 用户名输入
        JLabel nameLabel = createFormLabel("用户名:", 120);
        formPanel.add(nameLabel);

        nameField = createTextField(120);
        formPanel.add(nameField);

        // 密码输入
        JLabel passwordLabel = createFormLabel("密  码:", 170);
        formPanel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setBounds(80, 170, 320, 35);
        passwordField.setBorder(createInputBorder());
        formPanel.add(passwordField);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBounds(0, 230, 400, 50);

        // 注册按钮
        JButton registerButton = createActionButton("立即加入", new Color(70, 130, 180));
        registerButton.addActionListener(this);
        buttonPanel.add(registerButton);

        formPanel.add(buttonPanel);

        // 状态提示
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setBounds(0, 290, 400, 30);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(statusLabel);

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

    public void initJFrame() {
        setSize(600, 500);
        setTitle("智能聊天室注册");
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        try {
            setIconImage(new ImageIcon("icon.png").getImage());
        } catch (Exception e) {
            System.out.println("图标加载失败，使用默认图标");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source instanceof JButton) {
            String actionCommand = ((JButton) source).getText();
            switch (actionCommand) {
                case "立即加入":
                    performRegister();
                    break;
            }
        }
    }

    private void performRegister() {
        String account = accountField.getText().trim();
        String name = nameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (validateInputs(account, name, password)) {
            // 这里仅做演示，真实应用中应将数据发送至服务器
            System.out.println("Account: " + account);
            System.out.println("Name: " + name);
            System.out.println("Password: " + new String(password));

            JOptionPane.showMessageDialog(this, "注册成功！");
            dispose();
            // 打开主界面
        }
    }

    private boolean validateInputs(String account, String name, char[] password) {
        if (account.isEmpty() || name.isEmpty() || password.length == 0) {
            statusLabel.setText("所有字段必须填写！");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new RegisterJframe());
    }
}



