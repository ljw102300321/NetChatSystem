import ChatFunction.ChatServer;

public class App1 {
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
    }
}
