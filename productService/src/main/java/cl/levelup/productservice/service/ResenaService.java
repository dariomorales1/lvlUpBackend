package cl.levelup.productservice.service;

import cl.levelup.productservice.client.UsuarioClient;
import cl.levelup.productservice.client.UsuarioResumen;
import cl.levelup.productservice.model.dto.ResenaRequest;
import cl.levelup.productservice.model.Product;
import cl.levelup.productservice.model.Resena;
import cl.levelup.productservice.repository.ProductRepository;
import cl.levelup.productservice.repository.ResenaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UsuarioClient usuarioClient;

    // 🔹 Obtener reseñas de un producto (enriquecidas con datos del usuario)
    public List<Resena> getResenasByProductCode(String productCode) {
        List<Resena> resenas = resenaRepository.findByProduct_Codigo(productCode);
        return enriquecerConUsuario(resenas);
    }

    // 🔹 Alias usado desde el controller para el producto
    public List<Resena> getResenasEnriquecidas(String productCode) {
        return getResenasByProductCode(productCode);
    }

    private List<Resena> enriquecerConUsuario(List<Resena> resenas) {
        for (Resena r : resenas) {
            if (r.getUsuarioId() != null) {
                Optional<UsuarioResumen> optUser =
                        usuarioClient.obtenerUsuarioPorId(r.getUsuarioId());
                optUser.ifPresent(u -> {
                    r.setUsuarioNombre(u.getNombre());
                    r.setUsuarioAvatarUrl(u.getAvatarUrl());
                });
            }
        }
        return resenas;
    }

    // 🔹 Crear nueva reseña para un producto
    public Resena addResena(String productCode, ResenaRequest request) {
        Product product = productRepository.findByCodigo(productCode);
        if (product == null) {
            throw new IllegalArgumentException("Producto no existe con código: " + productCode);
        }

        if (request.getComentario() == null || request.getComentario().isBlank()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío");
        }

        Integer punt = request.getPuntuacion();
        if (punt == null || punt < 1 || punt > 10) {
            throw new IllegalArgumentException("La puntuación debe estar entre 1 y 10");
        }

        if (request.getUsuarioId() == null || request.getUsuarioId().isBlank()) {
            throw new IllegalArgumentException("usuarioId es obligatorio");
        }

        Resena resena = new Resena();
        resena.setComentario(request.getComentario().trim());
        resena.setPuntuacion(punt);
        resena.setUsuarioId(request.getUsuarioId());
        resena.setCreatedAt(LocalDateTime.now());
        resena.setProduct(product);

        Resena saved = resenaRepository.save(resena);

        // Enriquecer solo esta reseña
        enriquecerConUsuario(List.of(saved));
        return saved;
    }

    // 🔹 Actualizar reseña existente
    public Resena updateResena(String productCode, Long resenaId, ResenaRequest request) {
        Resena existing = resenaRepository.findByIdAndProduct_Codigo(resenaId, productCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reseña no encontrada para ese producto"));

        if (request.getComentario() != null && !request.getComentario().isBlank()) {
            existing.setComentario(request.getComentario().trim());
        }
        if (request.getPuntuacion() != null) {
            Integer p = request.getPuntuacion();
            if (p < 1 || p > 10) {
                throw new IllegalArgumentException("La puntuación debe estar entre 1 y 10");
            }
            existing.setPuntuacion(p);
        }
        if (request.getUsuarioId() != null && !request.getUsuarioId().isBlank()) {
            existing.setUsuarioId(request.getUsuarioId());
        }

        Resena saved = resenaRepository.save(existing);
        enriquecerConUsuario(List.of(saved));
        return saved;
    }

    // 🔹 Eliminar reseña
    public void deleteResena(String productCode, Long resenaId) {
        Resena existing = resenaRepository.findByIdAndProduct_Codigo(resenaId, productCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reseña no encontrada para ese producto"));

        resenaRepository.delete(existing);
    }
}
