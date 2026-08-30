# TODO: 멀티스테이지 빌드
#   1) build 스테이지: gradle:8-jdk21 (또는 wrapper 사용) 이미지에서 ./gradlew bootJar
#   2) run 스테이지: eclipse-temurin:21-jre 등 경량 JRE 이미지에 jar만 복사
#   - ONNX Runtime 네이티브 라이브러리가 리눅스 컨테이너에서 정상 동작하는지 확인 필요
#     (onnxruntime jar에 OS별 네이티브 바이너리가 번들되어 있음 - 별도 설치 불필요할 가능성 높음, 검증 필요)
#   - models/ 디렉터리(ONNX 파일)를 이미지에 COPY할지, 볼륨/외부 스토리지에서 마운트할지 결정
#     (이미지 용량 문제로 볼륨 마운트 권장)
#   - JVM 힙 사이즈 등 컨테이너 환경에 맞는 옵션 설정 (-XX:MaxRAMPercentage 등)
#   - EXPOSE 포트, ENTRYPOINT 정의
