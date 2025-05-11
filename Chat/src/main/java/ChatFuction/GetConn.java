package main.java.ChatFuction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class GetConn {
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/chatData";
        String username = "root";
        String password = "123456";
        return DriverManager.getConnection(url,username, password);
    }
}