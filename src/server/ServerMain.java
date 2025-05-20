package server;

import interface_.AuthService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMain {
    private static final Logger LOGGER = Logger.getLogger(ServerMain.class.getName());
    private static final int PORT = 1099;
    
    public static void main(String[] args) {
        try {
            // RMI 보안 매니저 설정 (필요시만 사용)
            // if (System.getSecurityManager() == null) {
            //     System.setSecurityManager(new SecurityManager());
            // }
            
            // RMI 서비스 객체 생성
            AuthServiceImpl authService = new AuthServiceImpl();
            AuthService stub = authService;
            
            // RMI 레지스트리 생성 및 서비스 등록
            Registry registry = LocateRegistry.createRegistry(PORT);
            registry.rebind("AuthService", stub);
            
            LOGGER.info("Server is ready");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Server exception", e);
        }
    }
}
   