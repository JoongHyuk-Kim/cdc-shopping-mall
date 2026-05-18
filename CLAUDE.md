<!-- SPECKIT START -->
# CDC 쇼핑몰 — Claude 개발 가이드

## 프로젝트 개요

Debezium을 활용한 CDC(Change Data Capture) 기반 MSA 쇼핑몰 예제 프로젝트.
Transactional Outbox 패턴으로 데이터 변경을 Kafka에 발행하고, event-consumer가 소비한다.

## 헌법 및 원칙

**헌법 파일**: `.specify/memory/constitution.md` — 모든 개발 원칙의 근거.
개발 전 반드시 헌법을 숙지한다.

## 기술 스택

- Java 17+, Spring Boot 3.x, Gradle 8.x
- Apache Kafka, Debezium (MySQL Connector)
- MySQL, Docker Compose
- Kafka UI: http://localhost:8089

## 프로젝트 모듈 구조

```
cdc-shopping-mall/
├── order-service/      # 주문 서비스 (Outbox 이벤트 발행)
├── event-consumer/     # 이벤트 소비 서비스 (알림, 재고, 분석)
├── debezium/           # Debezium 커넥터 설정
├── mysql/              # MySQL 초기화 스크립트
├── observability/      # 모니터링 설정
└── docker-compose.yml  # 전체 인프라
```

## 주요 개발 원칙 (요약)

1. **CDC 우선**: 서비스 간 통신은 Debezium + Kafka로만
2. **Outbox 패턴**: 도메인 이벤트와 비즈니스 데이터는 같은 트랜잭션으로 저장
3. **MSA 경계**: 각 서비스는 자신의 DB만 소유, 독립 배포 가능
4. **DLT 필수**: Consumer 실패 시 Dead Letter Topic 처리 구현
5. **기능 완료 후 코드 리뷰**: `/review` 실행하여 공통 로직 추출 및 리팩토링 검토

## 개발 워크플로우

기능 개발 순서:
1. `/speckit-specify` → 기능 명세 (한글)
2. `/speckit-clarify` → 요구사항 명확화
3. `/speckit-plan` → 구현 계획
4. `/speckit-tasks` → 태스크 분리
5. `/speckit-implement` → 구현
6. `/review` → 코드 리뷰 ← **필수**
7. `/speckit-analyze` → 아티팩트 일관성 검사
8. **사용자 승인 요청** → 변경 요약 제시 후 명시적 승인 대기 ← **필수**
9. **PR 생성** → `gh pr create`로 feature → main PR 생성

> **중요**: 사용자 승인 없이 PR을 생성하거나 main에 직접 push하지 않는다.

## 문서 작성 언어

**모든 Speckit 문서(spec.md, plan.md, tasks.md, constitution.md 등)와 이 파일(CLAUDE.md)은 한국어로 작성한다.**

## 인프라 실행

```bash
# 인프라 기동
docker compose up -d

# Debezium 커넥터 등록
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @debezium/register-connector.json

# 서비스 실행
cd order-service && ./gradlew bootRun
cd event-consumer && ./gradlew bootRun
```

자세한 내용은 `README.md` 참조.
<!-- SPECKIT END -->
