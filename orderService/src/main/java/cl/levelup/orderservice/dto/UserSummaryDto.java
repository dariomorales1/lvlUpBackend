package cl.levelup.orderservice.dto;

import lombok.Data;

@Data
public class UserSummaryDto {
    private String id;
    private String nombre;
    private String email;
    private String rol;
}
