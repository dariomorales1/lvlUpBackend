package cl.levelup.userservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DireccionRequest {

    private String alias;

    @NotBlank
    @Size(max = 200)
    private String calle;

    @NotBlank
    @Size(max = 50)
    private String numero;

    private String depto;

    @NotBlank
    @Size(max = 100)
    private String ciudad;

    private String region;

    private String pais;

    // getters & setters

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getDepto() { return depto; }
    public void setDepto(String depto) { this.depto = depto; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
}

