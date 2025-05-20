package clinet;

import interface_.AuthService;
import java.io.File;
import java.nio.file.Files;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class ClientMain {
    private static final Logger LOGGER = Logger.getLogger(ClientMain.class.getName());
    private static final String HOST = "localhost";
    private static final int PORT = 1099;
    
    public static void main(String[] args) {
        try {
            // RMI 보안 매니저 (Java 17+에서는 deprecated, 필요시만 사용)
            // if (System.getSecurityManager() == null) {
            //     System.setSecurityManager(new SecurityManager());
            // }
            
            // RMI 레지스트리에서 서비스 조회
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            AuthService authService = (AuthService) registry.lookup("AuthService");
            
            // 얼굴 이미지 파일 선택
            String employeeId = JOptionPane.showInputDialog("사원번호를 입력하세요:");
            if (employeeId == null || employeeId.trim().isEmpty()) {
                return;
            }
            
            File imageFile = new File("face_image.jpg"); // 실제로는 파일 선택 다이얼로그 사용
            byte[] faceImage = Files.readAllBytes(imageFile.toPath());
            
            // 얼굴 인증 수행
            boolean isAuthenticated = authService.authenticateFace(employeeId, faceImage);
            
            if (isAuthenticated) {
                // 출입 기록 저장
                long timestamp = System.currentTimeMillis();
                boolean isEntry = true; // 실제로는 사용자 입력 필요
                authService.logAccess(employeeId, timestamp, isEntry);
                JOptionPane.showMessageDialog(null, "인증 성공! 출입이 허용되었습니다.");
            } else {
                JOptionPane.showMessageDialog(null, "인증 실패! 출입이 거부되었습니다.");
            }
        } catch (Exception e) {
            LOGGER.severe("Client exception: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "오류가 발생했습니다: " + e.getMessage());
        }
    }
}
