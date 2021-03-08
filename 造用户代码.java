package com.dunshan.mall;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.dunshan.mall.model.UmsMember;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
/**
 * @description: jdc用户地址造数代码
 * @author: 7DGroup Studio  
 * @create: 2020-12-05 21:30
 **/
public class JdbcDomeTest {
    //起始id
    private static long begin = 1;
    //每次循环插入的数据量
    private static long end = begin + 10000;
    private static String url = "jdbc:mysql://192.168.106.130:30336/mall?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowMultiQueries=true";
    private static String user = "reader";
    private static String password = "123456";
    private static String driverClass = "com.mysql.cj.jdbc.Driver";
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        pool.execute(new Runnable() {
            @Override
            public void run() {
                JdbcDomeTest.insertBigData();
            }
        });
        pool.execute(new Runnable() {
            @Override
            public void run() {
                JdbcDomeTest.insertBigData();
            }
        });
        pool.execute(new Runnable() {
            @Override
            public void run() {
                JdbcDomeTest.insertBigData();
            }
        });
       /**
       多线程存放地方
       */
        awaitAfterShutdown(pool);
    }
    /**
     * 关闭链接池
     * @param threadPool
     */
    public static void awaitAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    /**
     * 用户地址
     */
    public static void insertBigData() {
        //起始id
        long begin = 1;
        //每次循环插入的数据量
        long end = begin + 1000;
        //定义连接、statement对象
        Connection conn = null;
        PreparedStatement pstm = null;
        try {
            //加载jdbc驱动
            Class.forName(driverClass);
            //连接mysql
            conn = DriverManager.getConnection(url, user, password);
            //将自动提交关闭
            // conn.setAutoCommit(false);
            //编写sql
            String sql = "INSERT INTO ums_member_receive_address(member_id,name,phone_number,default_status,post_code,province,city,region,detail_address) VALUES (?,?,?,?,?,?,?,?,?)";
            //预编译sql
            pstm = conn.prepareStatement(sql);
            //开始总计时
            long bTime1 = System.currentTimeMillis();
            //循环10次，每次1万数据，一共10万
            for (int i = 0; i < 10; i++) {
                //开启分段计时，计1W数据耗时
                long bTime = System.currentTimeMillis();
                //开始循环
                while (begin < end) {
                    //赋值
//                    pstm.setLong(1, begin);
                    pstm.setLong(1, begin);
                    pstm.setString(2, RandomUtil.randomString(5));
                    pstm.setString(3, TestDome.getphone());
                    pstm.setInt(4, 0);
                    pstm.setString(5, RandomUtil.randomNumbers(5));
                    pstm.setString(6, "北京");
                    pstm.setString(7, "7dGruop性能实战");
                    pstm.setString(8, "7dGruop性能实战区");
                    pstm.setString(9, RandomUtil.randomString(5)+"吉地188号");
                    //添加到同一个批处理中
                    pstm.addBatch();
                    begin++;
                }
                //执行批处理
                pstm.executeBatch();
//                //提交事务
//                conn.commit();
                //边界值自增1千
                end += 1000;
                //关闭分段计时
                long eTime = System.currentTimeMillis();
                //输出
                System.out.println("成功插入1万条数据耗时：" + (eTime - bTime)/1000+"秒");
            }
            //关闭总计时
            long eTime1 = System.currentTimeMillis();
            //输出
            System.out.println("插入10W数据共耗时：" + (eTime1 - bTime1)/1000+"秒");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
    }
}
// 上面写法有问题，因为上面的member_id到1000又从1开始不会增加，需要执行下面语句就可以，这样才能把地址表的member_id与用户Id号一致
update ums_member_receive_address set member_id=id;
