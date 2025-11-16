package cl.levelup.userservice.service;

import cl.levelup.userservice.model.dto.UsuarioPublicRequest;
import cl.levelup.userservice.model.dto.UsuarioRequest;
import cl.levelup.userservice.model.dto.UsuarioResponse;
import java.util.List;

public interface UsuarioService {
    List<UsuarioResponse> findAll();
    UsuarioResponse findById(String id);
    UsuarioResponse createFromRequest(UsuarioRequest request, String uidFirebase);
    UsuarioResponse update(String id, UsuarioRequest request);
    void delete(String id);

    UsuarioResponse createFromPublicRequest(UsuarioPublicRequest request);
}

