package cl.levelup.userservice.service;

import cl.levelup.userservice.model.Usuario;
import cl.levelup.userservice.model.UsuarioRequest;

import java.util.List;

public interface UsuarioService {

    List<Usuario> findAll();
    Usuario findById(String id);
    Usuario createFromRequest(UsuarioRequest request, String idDesdeToken);
    Usuario update(String id, UsuarioRequest request);
    void delete(String id);
}
