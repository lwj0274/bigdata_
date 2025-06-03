import sys
import cv2
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity

def load_face_vector(image_path):
    img = cv2.imread(image_path)
    if img is None:
        return None
    img = cv2.resize(img, (200, 200))
    return img.flatten().reshape(1, -1)

def main():
    if len(sys.argv) != 3:
        print("Usage: python auth_face.py <input_image_path> <employee_id>")
        sys.exit(1)

    input_image_path = r"C:\Users\YONG\git\bigdata_\fracs\captured_face.jpg"
    employee_id = sys.argv[2]

    cascade_path = r"C:\Users\YONG\git\bigdata_\fracs\haarcascade_frontalface_default.xml"
    face_cascade = cv2.CascadeClassifier(cascade_path)

    db_image_path = rf"C:\Users\YONG\git\bigdata_\fracs\dataset\{employee_id}.jpg"

    db_vector = load_face_vector(db_image_path)
    if db_vector is None:
        print("False")  # DB에 사원 얼굴 없음
        sys.exit(0)

    input_img = cv2.imread(input_image_path)
    if input_img is None:
        print("False")  # 입력 이미지 없음
        sys.exit(0)

    gray = cv2.cvtColor(input_img, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray, 1.3, 5)

    if len(faces) == 0:
        print("False")  # 얼굴 없음
        sys.exit(0)

    for (x, y, w, h) in faces:
        face_img = input_img[y:y + h, x:x + w]
        face_img = cv2.resize(face_img, (200, 200))
        input_vector = face_img.flatten().reshape(1, -1)

        similarity = cosine_similarity(db_vector, input_vector)[0][0]
        print(f"Similarity: {similarity:.4f}")

        if similarity > 0.75:
            print("True")  # 인증 성공
        else:
            print("False")  # 인증 실패
        sys.exit(0)

if __name__ == "__main__":
    main()

