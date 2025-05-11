package Chat;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class LoginJframe extends JFrame implements KeyListener, ActionListener
{

    String username="LJW102300321";
    String userpassword="ljw102300321";
    String verification=create();
    JButton verificationcode=new JButton(verification);
    JButton login=new JButton("登录");
    JButton register=new JButton("注册");
    JTextField name=new JTextField();
    JPasswordField password=new JPasswordField();
    JTextField code=new JTextField();
    public LoginJframe()
    {
        verificationcode.addActionListener(this);
        register.addActionListener(this);
        login.addActionListener(this);
        initJFrame();
        initImage();
        this.setVisible(true);
    }
    public static String  create()
    {
        //String code = "";
        StringBuilder s = new StringBuilder();
        Random r = new Random();
        Random t = new Random();
        for (int i = 0; i < 4; i++)
        {
            int code1 = r.nextInt(26) + 65;
            int code2= r.nextInt(10)+48;
            int code3= r.nextInt(26) + 97;
            char c = (char) code1;
            char b= (char) code3;
            char a= (char) code2;
            int code4=t.nextInt(1000);
            if(code4%2==0)
                //System.out.println(c);
                s.append(c);
            else if(code4%3==0)
                s.append(a);
            else
                s.append(b);
        }
        return s.toString();
    }
    void initImage()
    {
        //将按钮添加到界面
        this.add(verificationcode);
        this.add(login);
        this.add(register);
        this.add(name);
        this.add(password);
        this.add(code);
        //设置按钮的大小位置
        verificationcode.setBounds(780, 580, 100, 50);
        login.setBounds(680, 680, 100, 50);
        register.setBounds(780, 680, 100, 50);
        name.setBounds(680, 480, 200, 50);
        password.setBounds(680, 580, 200, 50);
        code.setBounds(680, 630, 200, 50);


    }
    public void initJFrame() {
        //设置界面大小
        this.setSize(1000, 1000);
        //设置界面标题
        this.setTitle("聊天室");
        //设置界面置顶
        this.setAlwaysOnTop(true);

        //设置界面居中
        this.setLocationRelativeTo(null);
        //设置默认关闭方式
        this.setDefaultCloseOperation(3);
        //取消默认居中放置，只有取消了才会按照xy的形式添加组件
        this.setLayout(null);
        //给整个界面添加键盘监听事件
        this.addKeyListener(this);

    }
    @Override
    public void keyTyped(KeyEvent e) {

    }
    @Override

    public void keyPressed(KeyEvent e) {

    }
    @Override
    public void keyReleased(KeyEvent e) {

    }
    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object obj=e.getSource();
        if(obj==verificationcode){

        }else if(obj==login){

        }else if(obj==register){
            //关闭当前窗口，并创建RegisterJframe对象
            this.dispose();
            RegisterJframe registerFunction=new RegisterJframe();
        }
    }
    public boolean checkUser(String id) throws SQLException, ClassNotFoundException {
        Connection conn= GetConn.getConnection();
        String sql="select User_id,User_code from users where User_id=?";
        PreparedStatement pstmt=conn.prepareStatement(sql);
        pstmt.setString(1,id);
        try (ResultSet rs=pstmt.executeQuery()){
            if (rs.next()) {
                String code=rs.getString("User_code");
                char []passwordChar=password.getPassword();
                String passwordString=new String(passwordChar);
                if(code.equals(passwordString)) {
                    return true;
                }
            }
        } catch (Exception e){
            System.out.println("检查失败");
        } finally {
            pstmt.close();
            conn.close();
        }
        return false;
    }

}
