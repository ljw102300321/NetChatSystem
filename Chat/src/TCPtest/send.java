package TCPtest;

import main.java.ChatFuction.GetConn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class send {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        String s="send message 1023003210 to 1023003200 你好你好你好";
        Pattern pattern = Pattern.compile("(\\d{10}) to (\\d{10}) (.*)");
        Matcher matcher = pattern.matcher(s);
        String sender="";
        String receiver="";
        String message="";
        if(matcher.find()){
            sender=matcher.group(1);
            receiver = matcher.group(2);
            message=matcher.group(3);
        }

        //System.out.println(sender+" "+receiver+" "+message);
        Connection conn= GetConn.getConnection();
        String sql="insert into chatdata values(?,?,?,?)";
        PreparedStatement pstmt=conn.prepareStatement(sql);
        //获取当前的时间 年月日时分秒

        pstmt.setInt(1,Integer.parseInt(sender));
        pstmt.setInt(2,Integer.parseInt(receiver));
        pstmt.setString(3,"2025-05-04 12:00:00");
        pstmt.setString(4,message);

        try {
            conn.setAutoCommit(false);
            pstmt.executeUpdate();
            conn.commit();
        } catch (Exception e){
            System.out.println("插入聊天记录失败");
            conn.rollback();
        } finally {
            pstmt.close();
            conn.close();
        }
    }
}
