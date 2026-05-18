## 변경 요약

<!-- 이 PR에서 변경된 내용을 간략하게 설명하세요 (2-3줄 이내) -->

## 변경 유형

- [ ] 신규 기능 (New Feature)
- [ ] 버그 수정 (Bug Fix)
- [ ] 리팩토링 (Refactoring)
- [ ] 문서 업데이트 (Documentation)
- [ ] 인프라/설정 변경 (Infrastructure/Config)

## 관련 스펙

<!-- 관련 스펙 파일이나 이슈 링크 -->
- Spec: `specs/###-feature-name/spec.md`
- Plan: `specs/###-feature-name/plan.md`

## 주요 변경 내용

<!-- 구체적인 변경사항을 나열하세요 -->
-
-
-

## 헌법 원칙 준수 체크리스트

- [ ] **CDC 원칙**: 서비스 간 직접 DB 접근 없이 Debezium + Kafka 사용
- [ ] **Outbox 패턴**: 도메인 이벤트와 비즈니스 데이터 동일 트랜잭션 저장
- [ ] **MSA 경계**: 각 서비스가 자신의 책임 범위만 처리, 독립 배포 가능
- [ ] **DLT 처리**: Consumer 실패 시 Dead Letter Topic 적재 구현
- [ ] **옵저버빌리티**: 구조화된 로깅(`eventType`, `aggregateId` 포함)
- [ ] **코드 리뷰 완료**: 공통 로직 추출 및 리팩토링 검토 (`/review` 실행)

## 테스트 방법

```bash
# 인프라 기동
docker compose up -d

# Debezium 커넥터 등록
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @debezium/register-connector.json

# 테스트 시나리오
# 1.
# 2.
# 3.
```

## 확인 포인트

- [ ] `event-consumer` 로그에서 이벤트 수신 확인
- [ ] Kafka UI(`http://localhost:8089`)에서 토픽 메시지 확인
- [ ] DLT 토픽에 불필요한 메시지 없음 확인

## 스크린샷 / 로그 (선택)

<!-- 필요한 경우 Kafka UI 스크린샷 또는 로그를 첨부하세요 -->
