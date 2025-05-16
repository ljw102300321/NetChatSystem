import org.example.ChatServer;

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
/*
CREATE DATABASE chat_app;

USE chat_app;
drop table chat_history;
drop table friends;
drop table users;

-- 用户表
CREATE TABLE users (
                       id INT  PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       status INT default 0
);

INSERT INTO users(id, username, password) VALUES(1, '2', '3');
-- 好友关系表
CREATE TABLE friends (
                         user_id INT NOT NULL,
                         friend_id INT NOT NULL,
                         FOREIGN KEY (user_id) REFERENCES users(id),
                         FOREIGN KEY (friend_id) REFERENCES users(id),
                         PRIMARY KEY (user_id, friend_id)
);

-- 聊天记录表
CREATE TABLE chat_history (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              sender_id INT NOT NULL,
                              receiver_id INT NOT NULL,
                              message TEXT,
                              timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (sender_id) REFERENCES users(id),
                              FOREIGN KEY (receiver_id) REFERENCES users(id)
);
 */