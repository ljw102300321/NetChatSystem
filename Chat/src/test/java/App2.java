import ChatFunction.ChatClient;
import ChatFunction.LoginJframe;

import javax.swing.*;

public class App2 {
    static LoginJframe loginJframe;
    public static void main(String[] args) {


        // 3. 显示登录窗口
        SwingUtilities.invokeLater(() -> {
            loginJframe = new LoginJframe((userId, name, password) ->
            {
                System.out.println("登录尝试 - 账号: " + userId + ", 用户名: " + name + ", 密码: " + password);

                if (1==1) {
                    System.out.println("登录成功");
                    // 关闭登录窗口
                    loginJframe.dispose();
                    //启动客户端 GUI（用户1）
                    SwingUtilities.invokeLater(() -> {
                        new ChatClient(Integer.parseInt(userId), name);
                    });
                } else {
                    JOptionPane.showMessageDialog(null, "登录失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
        });
    }
}