package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}
