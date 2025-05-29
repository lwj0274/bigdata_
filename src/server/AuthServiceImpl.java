package server;

import interface_.AuthService;
import util.DBUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.*;

import javax.imageio.ImageIO;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthServiceImpl extends UnicastRemoteObject implements AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthServiceImpl.class.getName());

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    protected AuthServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public boolean authenticateFace(String employeeId, byte[] faceImage) throws RemoteException {
        // 1. DB 인증
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT emp_id FROM employinfo WHERE emp_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, employeeId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        LOGGER.info("인증 실패: DB에 없는 사원번호 " + employeeId);
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DB 인증 중 오류", e);
            return false;
        }

        // 2. 이미지 저장 및 멀티스레드 처리
        Callable<Boolean> task = () -> {
            try {
                BufferedImage image = byteArrayToImage(faceImage);
                File dir = new File("output");
                if (!dir.exists()) dir.mkdirs();

                File outputFile = new File(dir, employeeId + "_received.jpg");
                ImageIO.write(image, "jpg", outputFile);
                LOGGER.info("이미지 저장 완료: " + outputFile.getAbsolutePath());

                // TODO: 얼굴 인식 알고리즘 추가
                return true;
            } catch (IOException e) {
                LOGGER.severe("이미지 처리 실패: " + e.getMessage());
                return false;
            }
        };

        Future<Boolean> future = executor.submit(task);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.log(Level.SEVERE, "멀티스레드 얼굴 인증 중 예외", e);
            throw new RemoteException("멀티스레드 얼굴 인증 중 예외", e);
        }
    }

    @Override
    public void logAccess(String employeeId, long timestamp, boolean isEntry) throws RemoteException {
        LOGGER.info("출입 기록: " + employeeId + ", " + timestamp + ", " + isEntry);
    }

    public void shutdown() {
        executor.shutdown();
    }

    private BufferedImage byteArrayToImage(byte[] data) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(data));
    }
}
