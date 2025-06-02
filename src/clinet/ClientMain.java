package clinet;

import interface_.AuthService;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

public class ClientMain {
    private static final Logger LOGGER = Logger.getLogger(ClientMain.class.getName());
    private static final String HOST = "localhost";
    private static final int PORT = 1099;

    public static void main(String[] args) {
        System.out.println("현재 작업 디렉터리: " + System.getProperty("user.dir")); // 작업 디렉터리 출력

        try {
            // RMI 레지스트리에서 서비스 조회
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            AuthService authService = (AuthService) registry.lookup("AuthService");

            // 사원번호 입력
            String employeeId = JOptionPane.showInputDialog("사원번호를 입력하세요:");
            if (employeeId == null || employeeId.trim().isEmpty()) {
                return;
            }

            // 웹캠으로 촬영
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            VideoCapture camera = new VideoCapture(0);
            if (!camera.isOpened()) {
                JOptionPane.showMessageDialog(null, "웹캠을 열 수 없습니다.");
                return;
            }
            Mat frame = new Mat();

            if (camera.read(frame)) {
                // 바탕화면 경로 예시 (Windows 기준)
                String outputPath = System.getProperty("user.home") + "/Desktop/captured_face.jpg";

                Imgcodecs.imwrite(outputPath, frame);

                File imageFile = new File(outputPath);

                if (imageFile.exists()) {
                    System.out.println("파일이 생성됨: " + imageFile.getAbsolutePath());
                } else {
                    System.out.println("파일 생성 실패");
                }

                JOptionPane.showMessageDialog(null, "웹캠 촬영 완료: " + outputPath);

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
            } else {
                JOptionPane.showMessageDialog(null, "웹캠 촬영에 실패했습니다.");
                camera.release();
                return;
            }
            camera.release();
        } catch (Exception e) {
            LOGGER.severe("Client exception: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "오류 발생: " + e.getMessage());
        }
    }
}


