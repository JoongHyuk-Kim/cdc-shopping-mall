# 실행 및 테스트 가이드

이 문서는 `cdc-shopping-mall` 프로젝트의 Docker 인프라 기동, Debezium 커넥터 등록, 애플리케이션 실행, CDC 이벤트 확인 절차를 정리한 가이드입니다.

## 1. Docker 인프라 기동

프로젝트 루트에서 아래 명령을 실행합니다.

```bash
cd cdc-shopping-mall
docker compose up -d
```

상태를 확인합니다.

```bash
docker compose ps
```

정상 상태:

- `mysql`
- `zookeeper`
- `kafka`
- `debezium`
- `kafka-ui`

위 컨테이너가 모두 `running` 상태여야 합니다.

## 2. Debezium Connector 등록

MySQL과 Kafka가 완전히 기동된 뒤, 약 30초 정도 기다린 후 커넥터를 등록합니다.

```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @debezium/register-connector.json
```

등록 여부를 확인합니다.

```bash
curl http://localhost:8083/connectors
```

예상 결과:

```json
["shop-mysql-connector"]
```

커넥터 상태를 확인합니다.

```bash
curl http://localhost:8083/connectors/shop-mysql-connector/status
```

정상 상태:

- `connector.state`: `RUNNING`
- `tasks[0].state`: `RUNNING`

## 3. Spring Boot 애플리케이션 실행

### Order Service

```bash
cd order-service
./gradlew bootRun
```

### Event Consumer

다른 터미널에서 실행합니다.

```bash
cd event-consumer
./gradlew bootRun
```

## 4. 주문 생성 테스트

아래 요청으로 주문을 생성합니다.

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "홍길동",
    "customerEmail": "hong@example.com",
    "items": [
      {
        "productId": 1001,
        "productName": "맥북 프로 14인치",
        "quantity": 1,
        "unitPrice": 2690000
      },
      {
        "productId": 1002,
        "productName": "에어팟 프로",
        "quantity": 2,
        "unitPrice": 359000
      }
    ]
  }'
```

응답 예시:

```json
{ "orderId": 1, "status": "PENDING", "totalAmount": 3408000 }
```

확인 포인트:

- `event-consumer` 로그에서 알림 리스너 동작 확인
- `event-consumer` 로그에서 재고 리스너 동작 확인
- `event-consumer` 로그에서 분석 리스너 동작 확인

## 5. 주문 상태 변경 테스트

### 주문 확정

```bash
curl -X PATCH http://localhost:8080/api/orders/1/confirm
```

예상 동작:

- `[알림]` 주문 확정 알림 발송
- `[재고]` 재고 차감 처리 시작

### 주문 취소

```bash
curl -X PATCH http://localhost:8080/api/orders/1/cancel
```

예상 동작:

- `[알림]` 주문 취소 알림 발송
- `[재고]` 재고 복원 처리 시작

## 6. Kafka UI에서 CDC 이벤트 확인

브라우저에서 아래 주소로 접속합니다.

```text
http://localhost:8089
```

확인 순서:

1. `Topics` 메뉴로 이동
2. `shopdb.shopdb.orders` 토픽 선택
3. `Messages` 탭에서 CDC 이벤트 JSON 확인

## 디버깅 명령어

### Debezium 로그 확인

```bash
docker logs -f shop-debezium
```

### Kafka 토픽 목록 확인

```bash
docker exec shop-kafka kafka-topics --list --bootstrap-server localhost:9092
```

### 토픽 메시지 직접 확인

```bash
docker exec shop-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic shopdb.shopdb.orders \
  --from-beginning
```

### Connector 삭제 후 재등록

```bash
curl -X DELETE http://localhost:8083/connectors/shop-mysql-connector
```

이후 다시 커넥터 등록 명령을 실행합니다.

### 전체 정리

```bash
docker compose down -v
```
