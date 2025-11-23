package cl.levelup.productservice.model.dto;

import java.time.LocalDateTime;

public class ResenaResponse {

    private Long id;
    private String comentario;
    private Integer puntuacion;
    private LocalDateTime createdAt;

    // 👉 El usuarioId se puede mantener si quieres usarlo
    private String usuarioId;

    // 👉 Campos nuevos desde UserService
    private String usuarioNombre;
    private String usuarioAvatarUrl;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public Integer getPuntuacion() { return puntuacion; }
    public void setPuntuacion(Integer puntuacion) { this.puntuacion = puntuacion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getUsuarioAvatarUrl() { return usuarioAvatarUrl; }
    public void setUsuarioAvatarUrl(String usuarioAvatarUrl) { this.usuarioAvatarUrl = usuarioAvatarUrl; }

}
