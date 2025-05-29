package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import util.DBUtil;

public class DBTest {
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) {
            System.out.println("✅ DB 연결 성공!");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM employinfo");

            while (rs.next()) {
                String empId = rs.getString("emp_id");
                String name = rs.getString("name");
                String dept = rs.getString("department");
                System.out.println(empId + " | " + name + " | " + dept);
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.out.println("❌ DB 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
