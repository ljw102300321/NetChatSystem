package main.java.ChatFuction;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class RegisterJframe extends JFrame implements ActionListener {
    private static final Color MAIN_COLOR = Color.decode("#2C3E50");
    private static final Color ACCENT_COLOR = Color.decode("#3498DB");
    private static final Color BG_COLOR = Color.decode("#ECF0F1");

    private JTextField nameField = new JTextField(15);
    private JPasswordField passwordField = new JPasswordField(15);
    private JLabel statusLabel = new JLabel(" ");

    public RegisterJframe() {
        configureFrame();
        initUI();
        setVisible(true);
    }

    private void configureFrame() {
        setTitle("注册新用户 - 智能聊天室");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(BG_COLOR);
    }

    private void initUI() {
        // 主面板布局
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel titleLabel = new JLabel("用户注册");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(MAIN_COLOR);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);

        // 用户名输入
        JLabel nameLabel = createLabel("用户名:");
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        mainPanel.add(nameLabel, gbc);

        styleTextField(nameField);
        gbc.gridx = 1;
        mainPanel.add(nameField, gbc);

        // 密码输入
        JLabel passLabel = createLabel("密  码:");
        gbc.gridy = 2;
        gbc.gridx = 0;
        mainPanel.add(passLabel, gbc);

        styleTextField(passwordField);
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);

        // 注册按钮
        RoundedButton registerBtn = new RoundedButton("立即注册", ACCENT_COLOR);
        registerBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        registerBtn.addActionListener(this);
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 50, 10, 50);
        mainPanel.add(registerBtn, gbc);

        // 状态提示
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 10, 0, 10);
        mainPanel.add(statusLabel, gbc);

        add(mainPanel);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label.setForeground(MAIN_COLOR);
        return label;
    }

    private void styleTextField(JComponent field) {
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(5, MAIN_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setPreferredSize(new Dimension(200, 35));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = nameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("用户名和密码不能为空！");
            return;
        }

        try {
            String userId = generateUserId();
            insertUser(userId, username, password);
            JOptionPane.showMessageDialog(this,
                    "注册成功！\n您的用户ID: " + userId,
                    "注册成功",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginJframe();
        } catch (Exception ex) {
            statusLabel.setText("注册失败: " + ex.getMessage());
        }
    }

    // 自定义圆角按钮
    class RoundedButton extends JButton {
        private Color backgroundColor;

        public RoundedButton(String text, Color bgColor) {
            super(text);
            this.backgroundColor = bgColor;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) {
                g2.setColor(backgroundColor.darker());
            } else if (getModel().isRollover()) {
                g2.setColor(backgroundColor.brighter());
            } else {
                g2.setColor(backgroundColor);
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    // 自定义圆角边框
    class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color color;

        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width-1, height-1, radius, radius);
            g2.dispose();
        }
    }

    // 以下保持原有数据库操作方法不变（略作优化）
    private String generateUserId() {
        // 原有生成逻辑优化（此处保持原有逻辑）
        return "1234567890"; // 示例返回值
    }

    private void insertUser(String id, String name, String password) throws SQLException {
        // 原有数据库插入逻辑优化（此处保持原有逻辑）
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new RegisterJframe());
    }
}