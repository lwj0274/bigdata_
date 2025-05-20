package interface_;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthService extends Remote {
    /**
     * 얼굴 인증을 수행하는 메서드
     * @param employeeId 사원번호
     * @param faceImage 얼굴 이미지 데이터
     * @return 인증 성공 여부
     * @throws RemoteException RMI 통신 오류 발생 시
     */
    boolean authenticateFace(String employeeId, byte[] faceImage) throws RemoteException;
    
    /**
     * 출입 기록을 저장하는 메서드
     * @param employeeId 사원번호
     * @param timestamp 출입 시간
     * @param isEntry 입사/퇴사 여부 (true: 입사, false: 퇴사)
     * @throws RemoteException RMI 통신 오류 발생 시
     */
    void logAccess(String employeeId, long timestamp, boolean isEntry) throws RemoteException;
}
