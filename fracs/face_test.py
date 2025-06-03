import cv2
import os

print("🟢 실행 시작")

# OpenCV 설치 경로의 haarcascade XML 파일 경로
cascade_path = r"C:\Users\YONG\AppData\Local\Programs\Python\Python313\Lib\site-packages\cv2\data\haarcascade_frontalface_default.xml"

# 파일 존재 여부 확인
if not os.path.exists(cascade_path):
    print(f"❌ Haar Cascade 파일이 존재하지 않습니다: {cascade_path}")
    exit()qq

# Haar Cascade 로딩
face_cascade = cv2.CascadeClassifier(cascade_path)

if face_cascade.empty():
    print("❌ Haar Cascade 로딩 실패! 경로를 다시 확인하세요.")
    exit()
else:
    print("✅ Haar Cascade 로딩 성공!")

# 웹캠 시작
cap = cv2.VideoCapture(0)

if not cap.isOpened():
    print("❌ 웹캠을 열 수 없습니다.")
    exit()

while True:
    ret, frame = cap.read()
    if not ret:
        print("❌ 프레임을 읽을 수 없습니다.")
        break

    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray, scaleFactor=1.3, minNeighbors=5)

    for (x, y, w, h) in faces:
        cv2.rectangle(frame, (x, y), (x + w, y + h), (255, 0, 0), 2)

    cv2.imshow('Face Detection Test', frame)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        print("🔚 종료 키(Q) 입력됨. 프로그램 종료.")
        break

cap.release()
cv2.destroyAllWindows()

