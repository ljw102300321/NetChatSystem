package ChatFunction;

import javax.swing.*;
import java.io.IOException;

public class App {
    public static void main(String[] args) {
        // 1. 启动服务器（后台线程）
        new Thread(() -> {
            ChatServer.main(new String[]{});
        }).start();

        // 2. 等待服务器启动（1秒）
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 3. 显示登录窗口
        SwingUtilities.invokeLater(() -> {
            LoginJframe loginjframe = new LoginJframe((userId, name, password) -> {
                System.out.println("登录尝试 - 账号: " + userId + ", 用户名: " + name + ", 密码: " + password);


                if (userId.equals("1") && password.equals("3") && name.equals("2")) {
                    System.out.println("登录成功");

                    // 5. 启动客户端 GUI（用户1）
                    SwingUtilities.invokeLater(() -> {
                        new ChatClient(10086, "用户1");
                    });


                    // 6. 可选：启动第二个客户端（用户2）
                    SwingUtilities.invokeLater(() -> {
                        new ChatClient(10087, "用户2");
                    });
                } else {
                    JOptionPane.showMessageDialog(null, "登录失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            });

            // 7. 设置登录窗口关闭时不终止整个程序（仅关闭窗口）
            loginjframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            loginjframe.setVisible(true);
        });
    }
}