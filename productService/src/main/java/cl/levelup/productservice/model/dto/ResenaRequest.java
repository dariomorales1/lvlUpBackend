package cl.levelup.productservice.model.dto;

import lombok.Data;

@Data
public class ResenaRequest {
    private String comentario;
    private Integer puntuacion;
    private String usuarioId;
}
