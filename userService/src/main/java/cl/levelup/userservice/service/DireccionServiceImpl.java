package cl.levelup.userservice.service;

import cl.levelup.userservice.model.Direccion;
import cl.levelup.userservice.model.Usuario;
import cl.levelup.userservice.model.dto.DireccionRequest;
import cl.levelup.userservice.model.dto.DireccionResponse;
import cl.levelup.userservice.repository.DireccionRepository;
import cl.levelup.userservice.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DireccionServiceImpl implements DireccionService {

    private final DireccionRepository direccionRepository;
    private final UsuarioRepository usuarioRepository;

    public DireccionServiceImpl(DireccionRepository direccionRepository, UsuarioRepository usuarioRepository) {
        this.direccionRepository = direccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DireccionResponse> listarPorUsuario(String usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        return direccionRepository.findByUsuarioIdOrderByCreadoEnDesc(usuarioId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DireccionResponse crearParaUsuario(String usuarioId, DireccionRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Direccion d = new Direccion();
        d.setAlias(request.getAlias());
        d.setCalle(request.getCalle());
        d.setNumero(request.getNumero());
        d.setDepto(request.getDepto());
        d.setCiudad(request.getCiudad());
        d.setRegion(request.getRegion());
        d.setPais(request.getPais() != null ? request.getPais() : "Chile");
        d.setUsuarioId(usuario.getId());

        Direccion guardada = direccionRepository.save(d);
        return toResponse(guardada);
    }

    @Override
    public void eliminarParaUsuario(String usuarioId, Long direccionId) {
        Direccion d = direccionRepository.findById(direccionId)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));
        if (!d.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No autorizado para eliminar esta dirección");
        }
        direccionRepository.deleteById(direccionId);
    }

    @Override
    public DireccionResponse actualizarParaUsuario(String usuarioId, Long direccionId, DireccionRequest request) {
        Direccion d = direccionRepository.findById(direccionId)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));
        if (!d.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No autorizado para actualizar esta dirección");
        }

        if (request.getAlias() != null) d.setAlias(request.getAlias());
        if (request.getCalle() != null) d.setCalle(request.getCalle());
        if (request.getNumero() != null) d.setNumero(request.getNumero());
        if (request.getDepto() != null) d.setDepto(request.getDepto());
        if (request.getCiudad() != null) d.setCiudad(request.getCiudad());
        if (request.getRegion() != null) d.setRegion(request.getRegion());
        if (request.getPais() != null) d.setPais(request.getPais());

        Direccion actualizado = direccionRepository.save(d);
        return toResponse(actualizado);
    }

    private DireccionResponse toResponse(Direccion d) {
        DireccionResponse r = new DireccionResponse();
        r.setId(d.getId());
        r.setAlias(d.getAlias());
        r.setCalle(d.getCalle());
        r.setNumero(d.getNumero());
        r.setDepto(d.getDepto());
        r.setCiudad(d.getCiudad());
        r.setRegion(d.getRegion());
        r.setPais(d.getPais());
        r.setCreadoEn(d.getCreadoEn());
        r.setActualizadoEn(d.getActualizadoEn());
        return r;
    }
}

