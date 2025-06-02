package util;

import java.sql.*;

public class DBUtil {
	private static final String URL = "jdbc:mysql://localhost:3306/employee_access_db";
    private static final String USER = "project_user";
    private static final String PASSWORD = "1837";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try { if(rs != null) rs.close(); } catch (Exception ignored) {}
        try { if(stmt != null) stmt.close(); } catch (Exception ignored) {}
        try { if(conn != null) conn.close(); } catch (Exception ignored) {}
    }
}
