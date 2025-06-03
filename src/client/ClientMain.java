package client;

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

    // OpenCV DLL 경로 (사용자 환경에 맞게 수정 필요)
    private static final String OPENCV_DLL_PATH = "C:\\Users\\YONG\\Downloads\\open_cv\\opencv\\build\\java\\x64\\opencv_java4100.dll";

    public static void main(String[] args) {
        System.out.println("현재 작업 디렉터리: " + System.getProperty("user.dir"));

        try {
            // OpenCV DLL 직접 로드
            System.load(OPENCV_DLL_PATH);
            LOGGER.info("OpenCV Native Library 로드 성공!");
        } catch (UnsatisfiedLinkError e) {
            JOptionPane.showMessageDialog(null, "OpenCV DLL 로드 실패: " + e.getMessage());
            LOGGER.severe("OpenCV 로드 실패: " + e.getMessage());
            return;
        }

        try {
            // RMI 레지스트리에서 서비스 조회
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            AuthService authService = (AuthService) registry.lookup("AuthService");

            // 사원번호 입력
            String employeeId = JOptionPane.showInputDialog("사원번호를 입력하세요:");
            if (employeeId == null || employeeId.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "사원번호가 입력되지 않았습니다.");
                return;
            }

            // 웹캠 촬영 시작
            VideoCapture camera = new VideoCapture(0);
            if (!camera.isOpened()) {
                JOptionPane.showMessageDialog(null, "웹캠을 열 수 없습니다.");
                LOGGER.warning("웹캠 열기 실패");
                return;
            }

            Mat frame = new Mat();
            if (!camera.read(frame)) {
                JOptionPane.showMessageDialog(null, "웹캠 촬영에 실패했습니다.");
                LOGGER.warning("웹캠 프레임 읽기 실패");
                camera.release();
                return;
            }

            // 이미지 저장
            String outputPath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "captured_face.jpg";
            boolean saved = Imgcodecs.imwrite(outputPath, frame);
            camera.release();

            if (!saved) {
                JOptionPane.showMessageDialog(null, "이미지를 저장하지 못했습니다.");
                LOGGER.warning("이미지 저장 실패");
                return;
            }

            File imageFile = new File(outputPath);
            LOGGER.info("촬영 이미지 저장 위치: " + imageFile.getAbsolutePath());
            JOptionPane.showMessageDialog(null, "웹캠 촬영 완료: " + outputPath);

            byte[] faceImage = Files.readAllBytes(imageFile.toPath());

            // 얼굴 인증 요청
            boolean isAuthenticated = authService.authenticateFace(employeeId, faceImage);

            if (isAuthenticated) {
                long timestamp = System.currentTimeMillis();
                boolean isEntry = true; // 기본: 입실
                authService.logAccess(employeeId, timestamp, isEntry);
                JOptionPane.showMessageDialog(null, "인증 성공! 출입이 허용되었습니다.");
            } else {
                JOptionPane.showMessageDialog(null, "인증 실패! 출입이 거부되었습니다.");
            }

        } catch (Exception e) {
            LOGGER.severe("클라이언트 오류: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "오류 발생: " + e.getMessage());
        }
    }
}


