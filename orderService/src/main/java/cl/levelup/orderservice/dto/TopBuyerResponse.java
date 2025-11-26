// order-service/src/main/java/cl/levelup/orderservice/dto/TopBuyerResponse.java
package cl.levelup.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopBuyerResponse {
    private String userId;
    private Long totalSpent;
}
