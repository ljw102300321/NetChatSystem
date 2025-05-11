package ChatFuction;

import Chat.LoginJframe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.*;
import java.util.Random;

public class RegisterJframe extends JFrame implements KeyListener, ActionListener {
    String userid = createUserid();
    JButton register = new JButton("注册");
    JTextField name = new JTextField();
    JPasswordField password = new JPasswordField();
    JLabel nameLabel = new JLabel("用户名：");
    JLabel passwordLabel = new JLabel("密码：");
    JLabel tipLabel = new JLabel();

    public String createUserid() {
        Random random = new Random();
        StringBuilder userId = new StringBuilder();
        userId.append("1234567890");
        boolean b = true;
        while (b) {
            // 第一位不能是0，随机生成1到9之间的数字
            int firstDigit = random.nextInt(9) + 1;
            userId.append(firstDigit);
            // 生成剩余的9位数字
            for (int i = 1; i < 10; i++) {
                int num = random.nextInt(10);
                userId.append(num);
            }
            // 检查是否所有数字都相同
            while (userId.toString().chars().distinct().count() == 1) {
                // 如果所有数字都相同，重新生成
                userId = new StringBuilder();
                userId.append(random.nextInt(9) + 1); // 第一位不能是0
                for (int i = 1; i < 10; i++) {
                    userId.append(random.nextInt(10));
                }
            }
            try {
                b = checkUser(userId.toString());
            } catch (Exception e1) {
                System.out.println("检查用户名失败");
                b = false;
            }
        }
        return userId.toString();
    }

    public RegisterJframe() {
        register.addActionListener(this);
        initJFrame();
        initImage();
        this.setVisible(true);
    }

    void initImage() {
        // 设置组件字体
        Font font = new Font("楷体", Font.BOLD, 20);
        nameLabel.setFont(font);
        passwordLabel.setFont(font);
        name.setFont(font);
        password.setFont(font);
        register.setFont(font);
        tipLabel.setFont(font);

        // 设置组件颜色
        nameLabel.setForeground(Color.BLUE);
        passwordLabel.setForeground(Color.BLUE);
        tipLabel.setForeground(Color.RED);

        // 将组件添加到界面
        this.add(nameLabel);
        this.add(name);
        this.add(passwordLabel);
        this.add(password);
        this.add(register);
        this.add(tipLabel);

        // 设置组件的大小位置
        nameLabel.setBounds(600, 400, 100, 50);
        name.setBounds(700, 400, 200, 50);
        passwordLabel.setBounds(600, 500, 100, 50);
        password.setBounds(700, 500, 200, 50);
        register.setBounds(700, 600, 100, 50);
        tipLabel.setBounds(600, 700, 300, 50);
    }

    public void initJFrame() {
        // 设置界面大小
        this.setSize(1000, 1000);
        // 设置界面标题
        this.setTitle("聊天室注册");
        // 设置界面置顶
        this.setAlwaysOnTop(true);
        // 设置界面居中
        this.setLocationRelativeTo(null);
        // 设置默认关闭方式
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 取消默认居中放置，只有取消了才会按照xy的形式添加组件
        this.setLayout(null);
        // 给整个界面添加键盘监听事件
        this.addKeyListener(this);
        // 设置界面背景颜色
        this.getContentPane().setBackground(Color.WHITE);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == register) {
            // 如果用户名为空
            if (name.getText().equals("")) {
                tipLabel.setText("用户名不能为空");
            } else if (password.getText().equals("")) {
                tipLabel.setText("用户密码不能为空");
            } else {
                try {
                    insertUser(userid, name.getText(), password.getText());
                    tipLabel.setText("注册成功");
                    this.dispose();
                    new LoginJframe();
                } catch (Exception exception) {
                    tipLabel.setText("注册失败，请稍后再试");
                }
            }
        }
    }

    public void insertUser(String id, String name, String password) throws SQLException, ClassNotFoundException {
        Connection conn = GetConn.getConnection();
        String sql = "insert into users(User_id,User_name,User_code) values(?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        pstmt.setString(2, name);
        pstmt.setString(3, password);
        try {
            conn.setAutoCommit(false);
            pstmt.executeUpdate();
            conn.commit();
        } catch (Exception e) {
            System.out.println("插入失败");
            conn.rollback();
            throw new RuntimeException();
        } finally {
            pstmt.close();
            conn.close();
        }
    }

    public static boolean checkUser(String id) throws SQLException, ClassNotFoundException {
        Connection conn = GetConn.getConnection();
        String sql = "select User_name from users where User_id=?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("检查失败");
            throw new BatchUpdateException();
        } finally {
            pstmt.close();
            conn.close();
        }
        return false;
    }
}