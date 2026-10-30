# ESS_Guard_server
EMS 제어명령 로그를 실시간으로 분석해 ESS를 노리는 이상 명령을 탐지하는 규칙 기반 보안 모니터링 시스템

## EC2 배포

`main`에 push되면 GitHub Actions가 이미지를 Docker Hub에 올리고 EC2에서 재기동한다.

사전 조건: EC2(Ubuntu)에 Docker와 Compose plugin 설치, 보안 그룹에서 22, 8080 인바운드 허용

GitHub Secrets: `DOCKER_USERNAME`, `DOCKER_PASSWORD`, `DOCKER_REPO`, `AWS_EC2_IP`, `AWS_EC2_USERNAME`, `AWS_EC2_KEY`, `AWS_EC2_PORT`, `MYSQL_DATABASE`, `MYSQL_USERNAME`, `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD`

- 로그 확인: `docker compose logs -f app`
- MySQL은 컨테이너 내부 네트워크에서만 접근할 수 있고, 데이터는 `mysql-data` 볼륨에 유지된다
