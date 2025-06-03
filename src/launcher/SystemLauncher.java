package launcher;

import server.AuthServiceImpl;
import interface_.AuthService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SystemLauncher {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // [1] RMI Registry 시작
            LocateRegistry.createRegistry(1099);
            System.out.println("✔ RMI Registry 시작됨");

            // [2] AuthService 서버 등록
            AuthService authService = new AuthServiceImpl();
            Naming.rebind("AuthService", authService);
            System.out.println("✔ AuthService RMI 서버 등록 완료");

            // [3] 클라이언트 코드 실행 (별도 스레드)
            executor.submit(() -> {
                try {
                    Thread.sleep(1000); // 서버 준비 대기
                    client.ClientMain.main(null); // 클라이언트 main 호출
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
