import cv2
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity

# 경로
cascade_path = r"C:\fracs\haarcascade_frontalface_default.xml"
face_cascade = cv2.CascadeClassifier(cascade_path)

# 얼굴 DB 불러오기
db_image = cv2.imread(r"C:\fracs\dataset\user1.jpg")
db_image = cv2.resize(db_image, (200, 200))
db_vector = db_image.flatten().reshape(1, -1)  # 1D 벡터화

# 카메라 ON
cap = cv2.VideoCapture(0)

while True:
    ret, frame = cap.read()
    if not ret:
        break

    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray, 1.3, 5)

    result_text = "No Face"

    for (x, y, w, h) in faces:
        face_img = frame[y:y + h, x:x + w]
        face_img = cv2.resize(face_img, (200, 200))
        input_vector = face_img.flatten().reshape(1, -1)

        # 코사인 유사도 계산
        similarity = cosine_similarity(db_vector, input_vector)[0][0]

        # 임계값 설정 (0.90 이상이면 승인)
        if similarity > 0.90:
            result_text = "Approved (Similarity: {:.2f})".format(similarity)
            color = (0, 255, 0)
        else:
            result_text = "Denied (Similarity: {:.2f})".format(similarity)
            color = (0, 0, 255)

        cv2.rectangle(frame, (x, y), (x + w, y + h), color, 2)
        cv2.putText(frame, result_text, (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.7, color, 2)
        break  # 한 사람만 처리

    cv2.imshow('Authentication', frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()
