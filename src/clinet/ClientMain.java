package clinet;

import interface_.AuthService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Files;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

public class ClientMain {
    private static final Logger LOGGER = Logger.getLogger(ClientMain.class.getName());
    private static final String HOST = "localhost";
    private static final int PORT = 1099;

    public static void main(String[] args) {
        try {
            // RMI 레지스트리에서 서비스 조회
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            AuthService authService = (AuthService) registry.lookup("AuthService");

            // 사원번호 입력
            String employeeId = JOptionPane.showInputDialog("사원번호를 입력하세요:");
            if (employeeId == null || employeeId.trim().isEmpty()) {
                return;
            }

            // 파일 선택 다이얼로그
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("얼굴 이미지 파일 선택");

            // 이미지 파일 필터 추가 (JPG, PNG만 선택 가능)
            FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
                "이미지 파일 (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png"
            );
            fileChooser.setFileFilter(imageFilter);

            int result = fileChooser.showOpenDialog(null);
            if (result != JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(null, "이미지 선택을 취소했습니다.");
                return;
            }

            File imageFile = fileChooser.getSelectedFile();
            byte[] faceImage = Files.readAllBytes(imageFile.toPath());

            // 얼굴 인증
            boolean isAuthenticated = authService.authenticateFace(employeeId, faceImage);

            if (isAuthenticated) {
                long timestamp = System.currentTimeMillis();
                boolean isEntry = true; // 추후 입/퇴사 선택 추가 가능
                authService.logAccess(employeeId, timestamp, isEntry);
                JOptionPane.showMessageDialog(null, "인증 성공! 출입이 허용되었습니다.");
            } else {
                JOptionPane.showMessageDialog(null, "인증 실패! 출입이 거부되었습니다.");
            }

        } catch (Exception e) {
            LOGGER.severe("Client exception: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "오류 발생: " + e.getMessage());
        }
    }
}

