package cl.levelup.userservice.model;

import java.time.OffsetDateTime;

public class Profile {
    private String usuarioId;     // uuid
    private String nombreUsuario; // text
    private String emailPublico;  // text
    private Integer edad;         // integer
    private Boolean miembroDuoc;  // boolean
    private String avatarUrl;     // text
    private OffsetDateTime creadoEn; // timestamptz

    // Getters & Setters
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getEmailPublico() { return emailPublico; }
    public void setEmailPublico(String emailPublico) { this.emailPublico = emailPublico; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    public Boolean getMiembroDuoc() { return miembroDuoc; }
    public void setMiembroDuoc(Boolean miembroDuoc) { this.miembroDuoc = miembroDuoc; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(OffsetDateTime creadoEn) { this.creadoEn = creadoEn; }
}
