package cl.levelup.userservice.service;

import cl.levelup.userservice.model.dto.DireccionRequest;
import cl.levelup.userservice.model.dto.DireccionResponse;

import java.util.List;

public interface DireccionService {
    List<DireccionResponse> listarPorUsuario(String usuarioId);
    DireccionResponse crearParaUsuario(String usuarioId, DireccionRequest request);
    void eliminarParaUsuario(String usuarioId, Long direccionId);
    DireccionResponse actualizarParaUsuario(String usuarioId, Long direccionId, DireccionRequest request);
}
