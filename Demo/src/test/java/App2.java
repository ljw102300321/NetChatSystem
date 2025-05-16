import org.example.ChatClient;
import org.example.JDBCUnil;
import org.example.LoginJframe;

import javax.swing.*;

public class App2 {
    static LoginJframe loginJframe;
    public static void main(String[] args) {


        SwingUtilities.invokeLater(() -> {
            loginJframe = new LoginJframe((userId, name, password, writeCode, code) -> {
                SwingUtilities.invokeLater(() -> {  // 确保对话框在EDT上显示
                    if(writeCode.equals(code)){
                        try {
                            if (JDBCUnil.isExist(userId, name, password)) {
                                System.out.println("登录成功");
                                loginJframe.dispose();
                                new ChatClient(Integer.parseInt(userId), name);
                            } else {
                                JOptionPane.showMessageDialog(loginJframe,
                                        "登录失败！该用户还未注册",
                                        "错误",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(loginJframe,
                                    "发生错误: " + e.getMessage(),
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(loginJframe,
                                "验证码错误！",
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            });
        });
    }
}