package com.example.consumer.service;

import com.example.consumer.dto.OrderEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void deductStock(OrderEventPayload event) {
        validateItems(event);

        for (OrderEventPayload.OrderItemPayload item : event.getItems()) {
            InventoryRow inventory = findInventoryForUpdate(item.getProductId());
            if (inventory.stockQuantity() < item.getQuantity()) {
                throw new IllegalStateException(
                        "재고 부족: productId=" + item.getProductId()
                                + ", requested=" + item.getQuantity()
                                + ", available=" + inventory.stockQuantity()
                );
            }

            int updated = jdbcTemplate.update(
                    """
                    UPDATE product_inventory
                    SET stock_quantity = stock_quantity - ?
                    WHERE product_id = ?
                    """,
                    item.getQuantity(),
                    item.getProductId()
            );

            if (updated != 1) {
                throw new IllegalStateException("재고 차감 업데이트 실패: productId=" + item.getProductId());
            }

            log.info("  productId={}, name={}, before={}, deducted={}, after={}",
                    inventory.productId(), inventory.productName(), inventory.stockQuantity(),
                    item.getQuantity(), inventory.stockQuantity() - item.getQuantity());
        }
    }

    @Transactional
    public void restoreStock(OrderEventPayload event) {
        validateItems(event);

        for (OrderEventPayload.OrderItemPayload item : event.getItems()) {
            InventoryRow inventory = findInventoryForUpdate(item.getProductId());

            int updated = jdbcTemplate.update(
                    """
                    UPDATE product_inventory
                    SET stock_quantity = stock_quantity + ?
                    WHERE product_id = ?
                    """,
                    item.getQuantity(),
                    item.getProductId()
            );

            if (updated != 1) {
                throw new IllegalStateException("재고 복원 업데이트 실패: productId=" + item.getProductId());
            }

            log.info("  productId={}, name={}, before={}, restored={}, after={}",
                    inventory.productId(), inventory.productName(), inventory.stockQuantity(),
                    item.getQuantity(), inventory.stockQuantity() + item.getQuantity());
        }
    }

    private void validateItems(OrderEventPayload event) {
        List<OrderEventPayload.OrderItemPayload> items = event.getItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("주문 품목이 비어 있습니다. orderId=" + event.getOrderId());
        }
    }

    private InventoryRow findInventoryForUpdate(Long productId) {
        List<InventoryRow> rows = jdbcTemplate.query(
                """
                SELECT product_id, product_name, stock_quantity
                FROM product_inventory
                WHERE product_id = ?
                FOR UPDATE
                """,
                (rs, rowNum) -> new InventoryRow(
                        rs.getLong("product_id"),
                        rs.getString("product_name"),
                        rs.getInt("stock_quantity")
                ),
                productId
        );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("재고 정보를 찾을 수 없습니다: productId=" + productId);
        }

        return rows.get(0);
    }

    private record InventoryRow(Long productId, String productName, int stockQuantity) {
    }
}
