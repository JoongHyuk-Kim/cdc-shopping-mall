
## ============================================================
## 12. 실행 & 테스트 가이드
## ============================================================
---
# ============ STEP 1: Docker 인프라 기동 ============
#
# cd cdc-shopping-mall
# docker-compose up -d
#
# 확인:
#   docker-compose ps
#   → mysql, zookeeper, kafka, debezium, kafka-ui 모두 running


# ============ STEP 2: Debezium Connector 등록 ============
#
# MySQL과 Kafka가 완전히 기동된 후 (약 30초 대기)
#
# curl -X POST http://localhost:8083/connectors \
#   -H "Content-Type: application/json" \
#   -d @debezium/register-connector.json
#
# 등록 확인:
#   curl http://localhost:8083/connectors
#   → ["shop-mysql-connector"]
#
# 상태 확인:
#   curl http://localhost:8083/connectors/shop-mysql-connector/status
#   → connector.state: "RUNNING", tasks[0].state: "RUNNING"


# ============ STEP 3: Spring Boot 앱 기동 ============
#
# 터미널 1 - Order Service:
#   cd order-service
#   ./gradlew bootRun
#
# 터미널 2 - Event Consumer:
#   cd event-consumer
#   ./gradlew bootRun


# ============ STEP 4: 주문 생성 테스트 ============
#
# curl -X POST http://localhost:8080/api/orders \
#   -H "Content-Type: application/json" \
#   -d '**{
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
    ]**
#   }'
#
# 응답 예시:
# { "orderId": 1, "status": "PENDING", "totalAmount": 3408000 }
#
# → event-consumer 로그에서 알림/재고/분석 3개 리스너 모두 동작 확인!


# ============ STEP 5: 주문 상태 변경 테스트 ============
#
# 주문 확정:
#   curl -X PATCH http://localhost:8080/api/orders/1/confirm
#   → [알림] 주문 확정 알림 발송!
#   → [재고] 재고 차감 처리 시작!
#
# 주문 취소:
#   curl -X PATCH http://localhost:8080/api/orders/1/cancel
#   → [알림] 주문 취소 알림 발송!
#   → [재고] 재고 복원 처리 시작!


# ============ STEP 6: Kafka UI에서 확인 ============
#
# 브라우저: http://localhost:8089
# → Topics 메뉴에서 "shopdb.shopdb.orders" 토픽 클릭
# → Messages 탭에서 CDC 이벤트 JSON 확인 가능


# ============ 유용한 디버깅 명령어 ============
#
# Debezium 로그 확인:
#   docker logs -f shop-debezium
#
# Kafka 토픽 목록:
#   docker exec shop-kafka kafka-topics --list --bootstrap-server localhost:9092
#
# 토픽 메시지 직접 확인:
#   docker exec shop-kafka kafka-console-consumer \
#     --bootstrap-server localhost:9092 \
#     --topic shopdb.shopdb.orders \
#     --from-beginning
#
# Connector 삭제 후 재등록:
#   curl -X DELETE http://localhost:8083/connectors/shop-mysql-connector
#
# 전체 정리:
#   docker-compose down -v