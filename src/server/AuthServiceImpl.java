package server;

import interface_.AuthService;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthServiceImpl extends UnicastRemoteObject implements AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthServiceImpl.class.getName());
    private static final String DB_URL = "jdbc:mysql://localhost:3306/employee_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";
    
    // 허용된 사원번호 목록
    private static final Set<String> ALLOWED_EMPLOYEE_IDS = new HashSet<>();
    static {
        ALLOWED_EMPLOYEE_IDS.add("202310241");
        ALLOWED_EMPLOYEE_IDS.add("202212876");
        ALLOWED_EMPLOYEE_IDS.add("202111629");
        ALLOWED_EMPLOYEE_IDS.add("202111596");
        ALLOWED_EMPLOYEE_IDS.add("202111620");
    }
    
    // 멀티스레드 처리를 위한 ExecutorService (고정 스레드풀 4개)
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    protected AuthServiceImpl() throws RemoteException {
        super();
    }
    
    @Override
    public boolean authenticateFace(String employeeId, byte[] faceImage) throws RemoteException {
        // 사원번호가 허용된 목록에 없으면 인증 실패
        if (!ALLOWED_EMPLOYEE_IDS.contains(employeeId)) {
            LOGGER.info("인증 실패: 허용되지 않은 사원번호 " + employeeId);
            return false;
        }
        // 멀티스레드로 얼굴 인증 처리
        Callable<Boolean> task = () -> {
            // TODO: 얼굴 인식 알고리즘 구현 (employeeId, faceImage 활용)
            LOGGER.info("멀티스레드에서 얼굴 인증 처리: " + employeeId);
            return true; // 임시로 항상 성공 처리
        };
        Future<Boolean> future = executor.submit(task);
        try {
            return future.get(); // 결과 반환
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.log(Level.SEVERE, "멀티스레드 얼굴 인증 중 예외", e);
            throw new RemoteException("멀티스레드 얼굴 인증 중 예외", e);
        }
    }
    
    @Override
    public void logAccess(String employeeId, long timestamp, boolean isEntry) throws RemoteException {
        LOGGER.info("출입 기록: " + employeeId + ", " + timestamp + ", " + isEntry);
        // DB 연동이 필요하다면 여기에 추가
    }

    // 서버 종료 시 ExecutorService 종료를 위한 메서드 (필요시 호출)
    public void shutdown() {
        executor.shutdown();
    }
}
