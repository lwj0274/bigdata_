package server;

import interface_.AuthService;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class AuthServiceImpl extends UnicastRemoteObject implements AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthServiceImpl.class.getName());

    private static final Set<String> ALLOWED_EMPLOYEE_IDS = new HashSet<>();
    static {
        ALLOWED_EMPLOYEE_IDS.add("202310241");
        ALLOWED_EMPLOYEE_IDS.add("202212876");
        ALLOWED_EMPLOYEE_IDS.add("202111629");
        ALLOWED_EMPLOYEE_IDS.add("202111596");
        ALLOWED_EMPLOYEE_IDS.add("202111620");
    }

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    protected AuthServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public boolean authenticateFace(String employeeId, byte[] faceImage) throws RemoteException {
        if (!ALLOWED_EMPLOYEE_IDS.contains(employeeId)) {
            LOGGER.info("인증 실패: 허용되지 않은 사원번호 " + employeeId);
            return false;
        }

        Callable<Boolean> task = () -> {
            try {
                // byte[] → BufferedImage
                BufferedImage image = byteArrayToImage(faceImage);

                // output 디렉토리 생성
                File dir = new File("output");
                if (!dir.exists()) dir.mkdirs();

                // 이미지 저장
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

    // 🔽 유틸 함수 직접 포함 (ImageUtils 필요 없음)
    private BufferedImage byteArrayToImage(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        return ImageIO.read(bais);
    }
}

