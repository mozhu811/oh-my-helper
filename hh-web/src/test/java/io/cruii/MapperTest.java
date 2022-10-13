package io.cruii;

import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import io.cruii.mapper.BilibiliUserMapper;
import io.cruii.pojo.po.BilibiliUser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

/**
 * @author cruii
 * Created on 2022/8/24
 */
//@MybatisPlusTest
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class MapperTest {

    @Autowired
    private BilibiliUserMapper bilibiliUserMapper;

    @Autowired
    private DataSource dataSource;

    @Test
    public void testQueryList() {
        List<BilibiliUser> users = bilibiliUserMapper.selectList(null);
        Assert.assertFalse(users.isEmpty());
    }

    @Test
    public void testCreateDataSource() {
        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://115.28.161.73:3306/sanji?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowMultiQueries=true",
                    "xubo","xubo");

            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT id FROM log_request";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                int id  = rs.getInt("id");
                //String name = rs.getString("name");
                //String url = rs.getString("url");

                // 输出数据
                System.out.print("ID: " + id);
                //System.out.print(", 站点名称: " + name);
                //System.out.print(", 站点 URL: " + url);
                System.out.print("\n");
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        System.out.println("Goodbye!");
    }
}
