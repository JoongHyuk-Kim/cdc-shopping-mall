<!--
SYNC IMPACT REPORT
==================
버전 변경: 1.0.0 → 1.1.0
변경된 원칙: 없음 (기존 원칙 유지)
추가된 섹션:
  - 개발 워크플로우 > "사용자 승인 및 PR 생성" 단계 추가
  - 개발 워크플로우 > "PR 전략" 소섹션 추가
  - 개발 워크플로우 > "Git 기여 규칙" 소섹션 추가
추가된 아티팩트:
  - .github/PULL_REQUEST_TEMPLATE.md ✅ 신규 생성
업데이트된 템플릿:
  - CLAUDE.md ✅ PR 워크플로우 반영 필요
후속 TODO:
  - 없음
-->

# CDC 쇼핑몰 헌법 (Constitution)

## 핵심 원칙

### I. CDC 우선 설계 (CDC-First Design)

서비스 간 데이터 전파는 반드시 Debezium을 통한 CDC(Change Data Capture) 방식을 사용해야 한다.

- 서비스가 직접 다른 서비스의 DB에 접근하는 것을 MUST NOT 한다
- 데이터 변경 이벤트는 Debezium이 감지하여 Kafka 토픽으로 발행해야 한다
- 각 서비스는 자신의 DB만 소유하며, 타 서비스 데이터는 이벤트를 통해 수신한다
- **근거**: CDC는 서비스 간 강결합 없이 실시간 데이터 동기화를 가능하게 하며, 이것이 본 프로젝트의 핵심 학습 목표다

### II. 트랜잭셔널 아웃박스 패턴 (Transactional Outbox)

모든 도메인 이벤트 발행은 Transactional Outbox 패턴을 통해야 하며, 데이터 일관성이 MUST 보장되어야 한다.

- 도메인 이벤트와 비즈니스 데이터는 반드시 같은 트랜잭션으로 저장해야 한다
- `outbox_events` 테이블 변경만 Debezium이 감지하도록 구성한다
- Outbox 이벤트에는 `event_type`, `aggregate_id`, `payload`, `created_at`을 반드시 포함해야 한다
- **근거**: 분산 트랜잭션 없이 메시지 손실 방지 및 최소 1회 전달(at-least-once delivery)을 보장한다

### III. MSA 서비스 경계 (Service Boundary)

각 서비스는 독립적으로 배포 가능해야 하며, 서비스 간 직접 의존성을 MUST NOT 가진다.

- 새로운 기능 개발 시 기존 서비스에 무분별하게 로직을 추가하지 않는다
- 서비스 간 동기 통신(REST 직접 호출)은 최소화하며, 이벤트 기반 비동기 통신을 우선한다
- 각 모듈(`order-service`, `event-consumer`)은 독립적인 Gradle 모듈로 유지한다
- 공통 모델/DTO는 별도의 공통 모듈로 추출하여 관리한다
- **근거**: MSA의 핵심은 독립 배포와 장애 격리다

### IV. 이벤트 기반 비동기 통신 (Event-Driven Communication)

서비스 간 통신은 Kafka 이벤트를 통해 비동기로 이루어져야 한다.

- Kafka Consumer에는 반드시 DLT(Dead Letter Topic) 처리를 구현해야 한다
- 이벤트 스키마 변경 시 하위 호환성을 MUST 유지하거나 버전을 명시해야 한다
- Kafka 토픽 네이밍 컨벤션: `{db}.{schema}.{table}` (Debezium 자동 생성) 또는 `{service}.{event-type}` (직접 발행)
- Consumer Group ID는 서비스 목적을 명확히 반영해야 한다
- **근거**: 비동기 통신은 서비스 간 결합도를 낮추고 장애 전파를 방지한다

### V. 통합 테스트 필수 (Integration Testing Required)

CDC 파이프라인과 Kafka 연동은 반드시 실제 인프라(Testcontainers 또는 Docker Compose)로 검증해야 한다.

- 단위 테스트만으로는 CDC 파이프라인 검증이 불충분하므로 통합 테스트를 MUST 포함한다
- 신규 도메인 이벤트 추가 시 end-to-end 흐름(DB 변경 → Debezium → Kafka → Consumer) 테스트를 작성해야 한다
- 테스트에서 실제 DB를 Mock으로 대체하는 것을 SHOULD NOT 한다
- **근거**: CDC 파이프라인의 정확성은 실제 인프라 없이는 검증할 수 없다

### VI. 옵저버빌리티 (Observability)

모든 서비스는 이벤트 흐름을 추적할 수 있는 구조화된 로깅과 모니터링을 MUST 제공해야 한다.

- 구조화된 로깅(JSON 포맷 권장)을 사용하며, 로그에 `eventType`, `aggregateId`, `traceId`를 포함해야 한다
- Kafka UI(`http://localhost:8089`)를 통해 토픽과 이벤트를 확인할 수 있어야 한다
- Consumer 처리 성공/실패는 반드시 로그로 기록해야 한다
- DLT(Dead Letter Topic) 메시지는 별도로 모니터링해야 한다
- **근거**: 분산 시스템에서 장애 원인 파악을 위해 이벤트 추적이 필수다

### VII. 기능 완료 후 코드 리뷰 (Post-Feature Review) — 필수

기능 개발 완료 후 코드 리뷰를 통해 공통 로직 추출과 리팩토링을 검토하는 과정이 반드시 포함되어야 한다.

- 기능 구현 완료 직후 `/review` 또는 `/speckit-analyze` 를 실행하여 코드 품질을 검사한다
- 검토 항목:
  - 중복 로직이 있는가? → 공통 모듈 또는 유틸리티 클래스로 추출
  - 서비스 경계가 명확한가? → 잘못된 레이어 배치 수정
  - 이벤트 처리 패턴이 일관적인가? → 표준 패턴으로 통일
  - 예외 처리가 적절한가? → DLT 처리 및 재시도 정책 검토
- 리팩토링 없이 다음 기능으로 넘어가는 것을 MUST NOT 한다
- **근거**: MSA 환경에서 기술 부채는 서비스 전반에 걸쳐 확산되므로 조기 해소가 중요하다

## 기술 스택 제약사항

이 프로젝트에서 사용하는 기술 스택은 아래와 같으며, 변경 시 헌법 개정이 필요하다.

| 영역 | 기술 | 버전 |
|------|------|------|
| 런타임 | Java | 17+ |
| 프레임워크 | Spring Boot | 3.x |
| 빌드 도구 | Gradle | 8.x |
| CDC 도구 | Debezium | MySQL Connector |
| 메시지 브로커 | Apache Kafka | Docker 기반 |
| 데이터베이스 | MySQL | Docker 기반 |
| 컨테이너 | Docker Compose | 전체 인프라 |
| 모니터링 | Kafka UI | localhost:8089 |

**추가 기술 도입 원칙**:
- 새로운 의존성 추가는 MSA 서비스 경계 원칙과 충돌하지 않아야 한다
- 인프라 변경은 `docker-compose.yml`에 반영하고 `README.md`를 업데이트해야 한다
- Spring Boot 버전 업그레이드는 모든 모듈에 일괄 적용한다

## 개발 워크플로우

모든 기능 개발은 아래 순서를 따른다. Speckit 명령어를 활용하여 단계별로 진행한다.

### 기능 개발 사이클

```
1. /speckit-specify   → 기능 명세 작성 (한글)
2. /speckit-clarify   → 불명확한 요구사항 명확화
3. /speckit-plan      → 구현 계획 수립
4. /speckit-tasks     → 태스크 분리
5. /speckit-implement → 구현
6. /review            → 코드 리뷰 (공통 로직, 리팩토링 검토) ← 필수
7. /speckit-analyze   → 아티팩트 일관성 검사
8. [사용자 승인 대기]  → 변경사항 검토 및 승인 확인 ← 필수
9. PR 생성            → feature 브랜치 → main 으로 PR 방식으로 병합
```

### 사용자 승인 및 PR 생성 규칙 — 필수

기능 구현 및 코드 리뷰가 완료된 후, git에 반영하기 전에 반드시 아래 절차를 따른다:

1. **사용자 승인 요청**: 구현된 내용을 요약하여 사용자에게 승인을 요청한다
   - 변경 파일 목록, 주요 변경 내용, 헌법 준수 체크리스트 결과를 제시한다
   - 사용자의 명시적 승인(예: "승인", "PR 만들어줘", "OK") 없이는 PR을 생성하지 않는다

2. **PR 생성**: 승인 후 `feature` 브랜치에서 `main`으로 Pull Request를 생성한다
   - `gh pr create` 명령어를 사용한다
   - PR 제목: `[기능명] 간단한 요약` 형식으로 작성한다
   - PR 본문: `.github/PULL_REQUEST_TEMPLATE.md` 템플릿을 기반으로 작성한다

3. **직접 push 금지**: `main` 브랜치에 직접 push하는 것을 MUST NOT 한다

### PR 전략

```
main
 └── feature/{기능명}  ← 개발
       └── PR → main  ← 사용자 승인 후 병합
```

- 모든 변경은 `feature` 브랜치에서 작업하며 PR로 병합한다
- PR 제목은 한국어로 작성한다 (예: `주문 취소 이벤트 처리 구현`)
- PR 병합 후 `feature` 브랜치는 삭제한다
- Squash merge 또는 Merge commit 방식 모두 허용하되, 팀 내 일관성을 유지한다

### 코드 리뷰 체크리스트 (기능 완료 후 필수)

기능 완료 후 아래 항목을 반드시 확인하고, PR 설명에 결과를 기재한다:

- [ ] CDC 원칙 준수: 서비스 간 직접 DB 접근 없음
- [ ] Outbox 패턴: 도메인 이벤트와 비즈니스 데이터의 동일 트랜잭션 저장
- [ ] DLT 처리: Consumer 실패 시 Dead Letter Topic 적재
- [ ] 로깅: 구조화된 로그, 이벤트 타입 및 ID 포함
- [ ] 중복 로직: 공통 로직은 공유 모듈로 추출
- [ ] 서비스 경계: 각 서비스가 자신의 책임만 처리
- [ ] 이벤트 스키마 호환성: 기존 Consumer에 영향 없음

### 브랜치 전략

- `main`: 안정 버전 (직접 push 금지)
- `feature/{기능명}`: 기능 개발 브랜치 (Speckit이 자동 생성)

### 문서 작성 언어

**모든 Speckit 문서(spec.md, plan.md, tasks.md 등)와 CLAUDE.md는 한국어로 작성한다.**

## 거버넌스

- 이 헌법은 모든 개발 관행보다 우선한다
- 원칙 위반 사항은 반드시 코드 리뷰 단계에서 지적하고 수정 후 병합한다
- 헌법 개정은 아래 절차를 따른다:
  1. 개정 이유와 영향 범위를 문서화
  2. 버전 번호 업데이트 (Semantic Versioning: MAJOR.MINOR.PATCH)
  3. `LAST_AMENDED_DATE` 갱신
  4. 관련 템플릿 및 CLAUDE.md 동기화

**버전 번호 정책**:
- MAJOR: 기존 원칙의 제거 또는 상호 불호환 변경
- MINOR: 새로운 원칙 추가 또는 기존 원칙의 실질적 확장
- PATCH: 명확화, 표현 수정, 오타 수정

모든 PR 및 리뷰는 이 헌법의 원칙 준수 여부를 확인해야 한다.
복잡성은 반드시 정당화되어야 하며, 단순한 해결책이 가능하다면 복잡한 방법을 선택하지 않는다.

**Version**: 1.1.0 | **Ratified**: 2026-05-18 | **Last Amended**: 2026-05-18
