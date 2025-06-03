package server;

import interface_.AuthService;
import util.DBUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthServiceImpl extends UnicastRemoteObject implements AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthServiceImpl.class.getName());
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    // 생성자 접근 제어자를 public 으로 변경
    public AuthServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public boolean authenticateFace(String employeeId, byte[] faceImage) throws RemoteException {
        // 1. DB 인증 (사원번호 존재 여부)
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

        // 2. 이미지 저장 및 멀티스레드 얼굴 인식 작업
        Callable<Boolean> task = () -> {
            File outputFile = null;
            try {
                BufferedImage image = byteArrayToImage(faceImage);
                File dir = new File("C:\\Users\\YONG\\git\\bigdata_\\output");
                if (!dir.exists()) dir.mkdirs();

                // 절대 경로로 임시 이미지 저장
                outputFile = new File(dir, employeeId + "_received.jpg");
                ImageIO.write(image, "jpg", outputFile);
                LOGGER.info("이미지 저장 완료: " + outputFile.getAbsolutePath());

                // Python 실행 경로 및 스크립트 경로
                String pythonPath = "C:\\Users\\YONG\\AppData\\Local\\Programs\\Python\\Python313\\python.exe";
                String scriptPath = "C:\\Users\\YONG\\git\\bigdata_\\fracs\\auth_face.py";

                ProcessBuilder pb = new ProcessBuilder(
                        pythonPath,
                        scriptPath,
                        outputFile.getAbsolutePath(),
                        employeeId
                );
                pb.redirectErrorStream(true);

                Process process = pb.start();

                boolean resultFound = false;
                boolean finalResult = false;

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LOGGER.info("[Python] " + line);
                        if ("True".equalsIgnoreCase(line.trim())) {
                            finalResult = true;
                            resultFound = true;
                        } else if ("False".equalsIgnoreCase(line.trim())) {
                            finalResult = false;
                            resultFound = true;
                        }
                    }
                    process.waitFor();
                }

                if (!resultFound) {
                    LOGGER.warning("Python 결과에서 True/False를 찾지 못함");
                }

                return finalResult;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "얼굴 인식 처리 중 오류", e);
                return false;
            } finally {
                if (outputFile != null && outputFile.exists()) {
                    boolean deleted = outputFile.delete();
                    LOGGER.info("임시 이미지 파일 삭제됨: " + deleted);
                }
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
    LOGGER.info("출입 기록 저장 요청: " + employeeId + ", " + timestamp + ", 입실 여부=" + isEntry);
    try (Connection conn = DBUtil.getConnection()) {
        String sql = "INSERT INTO accesslog (emp_id, access_time, access_result) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, employeeId);
            pstmt.setTimestamp(2, new Timestamp(timestamp));
            // isEntry 가 true면 "success", false면 "fail"로 저장 (필요에 맞게 바꾸세요)
            pstmt.setString(3, isEntry ? "success" : "fail");
            pstmt.executeUpdate();
            LOGGER.info("출입 기록 DB 저장 완료");
        }
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "출입 기록 저장 중 오류", e);
    }
}


    public void shutdown() {
        executor.shutdown();
    }

    private BufferedImage byteArrayToImage(byte[] data) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(data));
    }
}
