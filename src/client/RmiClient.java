package client;

import interface_.AuthService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RmiClient {
    private static final Logger LOGGER = Logger.getLogger(RmiClient.class.getName());

    public static void main(String[] args) {
        String empId = "user1";
        String imagePath = "C:/fracs/dataset/user1.jpg";

        try {
            // RMI 객체 찾기
            AuthService authService = (AuthService) Naming.lookup("rmi://localhost:1099/AuthService");
            LOGGER.info("RMI 서비스에 연결되었습니다.");

            // 이미지 파일 확인
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                LOGGER.severe("이미지 파일이 존재하지 않습니다: " + imageFile.getAbsolutePath());
                return;
            }

            // byte[] 변환
            byte[] imageData = Files.readAllBytes(imageFile.toPath());
            LOGGER.info("이미지 파일 로드 완료: " + imageFile.getAbsolutePath());

            // 인증 요청
            boolean isAuthenticated = authService.authenticateFace(empId, imageData);
            if (isAuthenticated) {
                LOGGER.info("✅ 얼굴 인증 성공!");
            } else {
                LOGGER.info("❌ 얼굴 인증 실패!");
            }

        } catch (NotBoundException e) {
            LOGGER.log(Level.SEVERE, "RMI 바인딩 실패 - 서비스 이름 확인 필요", e);
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "RMI 통신 오류 발생", e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "파일 입출력 오류 발생", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "예기치 못한 오류 발생", e);
        }
    }
}

