// order-service/src/main/java/cl/levelup/orderservice/dto/PointsResponse.java
package cl.levelup.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PointsResponse {
    private String userId;
    private Long totalPoints;
}
