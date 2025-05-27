import cv2
import os

# 경로 설정
cascade_path = r"C:\fracs\haarcascade_frontalface_default.xml"
face_cascade = cv2.CascadeClassifier(cascade_path)

if face_cascade.empty():
    print("❌ Cascade 로딩 실패!")
    exit()

cap = cv2.VideoCapture(0)

save_path = r"C:\fracs\dataset"
os.makedirs(save_path, exist_ok=True)

img_count = 0
max_count = 1  # 1장만 저장

while True:
    ret, frame = cap.read()
    if not ret:
        break

    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray, 1.3, 5)

    for (x, y, w, h) in faces:
        face_img = frame[y:y + h, x:x + w]
        face_img = cv2.resize(face_img, (200, 200))
        file_name = os.path.join(save_path, f"user1.jpg")
        cv2.imwrite(file_name, face_img)
        print(f"✅ 저장 완료: {file_name}")
        img_count += 1
        break  # 한 번 저장 후 종료

    cv2.imshow('Capture Face', frame)
    if cv2.waitKey(1) & 0xFF == ord('q') or img_count >= max_count:
        break

cap.release()
cv2.destroyAllWindows()
