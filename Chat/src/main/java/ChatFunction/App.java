package ChatFunction;

public class App {
    public static void main(String[] args) {
    /*
    package TCPtest;

import java.io.IOException;

public class LoginTest {
    public static void main(String[] args) {
        // 先启动服务器线程
        Thread serverThread = new Thread(() -> {
            try {
                Server.main(new String[]{});
            } catch (IOException e) {
                System.err.println("服务器启动失败: " + e.getMessage());
            }
        });
        serverThread.start();

        // 等待服务器启动
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 再启动客户端线程
        Thread clientThread = new Thread(() -> {
            User.main(new String[]{});
        });
        clientThread.start();
    }
}
     */
        //LoginJframe lj= new LoginJframe();
        //String userId = "111";
        //String password = "11111";
        //String phone = "222222222222";
        LoginJframe loginjframe;
        loginjframe = new LoginJframe((userId,password,phone) -> {
            System.out.println("登录尝试 - 账号: " + userId + ", 密码: " + password+", 手机号: " + phone);
            // 这里可以添加验证逻辑
        });
        loginjframe.setVisible(true);
    }
}
