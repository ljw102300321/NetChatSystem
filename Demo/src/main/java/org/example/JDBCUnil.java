package org.example;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JDBCUnil {
    public static boolean isExist(String id,String username,String password) throws SQLException, ClassNotFoundException {
        boolean b=false;
        Connection conn = GetConn.getConnection();
        String sql="select status from users where id=? and username=? and password=?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1,id);
        pstmt.setString(2,username);
        pstmt.setString(3,password);
        try (ResultSet rs=pstmt.executeQuery()){
            if (rs.next()){
                b=true;
            }
        }catch (Exception e){
            System.out.println("查询记录出错");
        }finally {
            conn.close();
            pstmt.close();
        }
        return b;
    }


    public static boolean isFriend(String id,String friend_id) throws SQLException, ClassNotFoundException {
        boolean b=false;
        Connection conn = GetConn.getConnection();
        String sql="select status from friends where id=? and friend_id=?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1,id);
        pstmt.setString(2,friend_id);
        try (ResultSet rs=pstmt.executeQuery()){
            if (rs.next()){
                b=true;
            }
        }
        catch (Exception e){
            System.out.println("查询记录出错");
        }
        finally {
            conn.close();
        }
        return b;
    }

    public static int insertFriend(String id,String friend_id) throws SQLException, ClassNotFoundException {
        int insert=0;
        Connection conn = GetConn.getConnection();
String sql="insert into friends (user_id, friend_id) values(?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1,id);
        pstmt.setString(2,friend_id);
        try {
            conn.setAutoCommit(false);
            pstmt.executeUpdate();
            conn.commit();
        }
        catch (Exception e){
            conn.rollback();
            throw new RuntimeException();
        }
        finally {
            conn.close();
            pstmt.close();
        }
        return insert;
    }


    public static String selectId(String username) throws SQLException, ClassNotFoundException {
        int id=-1;
            Connection conn = GetConn.getConnection();
            String sql="select id from users where username=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,username);
            try (ResultSet rs=pstmt.executeQuery()){
                if (rs.next()){
                    id=rs.getInt("id");
                }
            }
            catch (Exception e){
                System.out.println("查询记录出错");
            }
            finally {
                conn.close();
                pstmt.close();
            }
        return id+"";
    }
    public static int insertChat(String sender_id,String receiver_id,String message )throws SQLException, ClassNotFoundException {
        System.out.println(sender_id+receiver_id+message);
        int insert;
        Connection conn = GetConn.getConnection();
        String sql="insert into chat_history (sender_id, receiver_id, message) value (?,?,?);";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1,sender_id);
        pstmt.setString(2,receiver_id);
        pstmt.setString(3,message);
        try {
            conn.setAutoCommit(false);
            pstmt.executeUpdate();
            conn.commit();
            insert=1;
        }
        catch (Exception e){
            conn.rollback();
            throw new RuntimeException();
        }
        finally {
            conn.close();
            pstmt.close();
        }
        return insert;
    }

    public static int insertUser(String id,String username,String password) throws SQLException, ClassNotFoundException {
        int insert;
        Connection conn = GetConn.getConnection();
        String sql="insert into users (id, username, password) values(?,?,?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1,id);
        pstmt.setString(2,username);
        pstmt.setString(3,password);
        try {
            conn.setAutoCommit(false);
            pstmt.executeUpdate();
            conn.commit();
            insert=1;
        }
        catch (Exception e){
            conn.rollback();
            throw new RuntimeException();
        }
        finally {
            conn.close();
            pstmt.close();
        }
        return insert;
    }

    /**
     * 获取指定用户的所有好友名
     */
    public static List<String> getFriends(String username) throws SQLException, ClassNotFoundException {
        List<String> friends = new ArrayList<>();

        String userId = selectId(username);
        if (userId == null || userId.equals("-1")) {
            return friends;
        }

        Connection conn = GetConn.getConnection();
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userId);

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String friendId = rs.getString("friend_id");
                String friendName = getIdToUsername(friendId);
                if (friendName != null) {
                    friends.add(friendName);
                }
            }
        } finally {
            conn.close();
            pstmt.close();
        }

        return friends;
    }

    /**
     * 根据用户ID获取用户名
     */
    private static String getIdToUsername(String id) throws SQLException, ClassNotFoundException {
        String name = null;
        Connection conn = GetConn.getConnection();
        String sql = "SELECT username FROM users WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);

        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                name = rs.getString("username");
            }
        } finally {
            conn.close();
            pstmt.close();
        }

        return name;
    }

    /**
     * 判断某个用户是否在线（本地模型判断）
     */
    public static boolean isOnline(String username, DefaultListModel<String> onlineFriendsModel) {
        for (int i = 0; i < onlineFriendsModel.getSize(); i++) {
            if (username.equals(onlineFriendsModel.getElementAt(i))) {
                return true;
            }
        }
        return false;
    }

}
