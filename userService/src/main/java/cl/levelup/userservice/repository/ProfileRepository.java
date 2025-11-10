package cl.levelup.userservice.repository;

import cl.levelup.userservice.model.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProfileRepository {

    private final JdbcTemplate jdbc;

    public ProfileRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Profile> PROFILE_MAPPER = new RowMapper<Profile>() {
        @Override
        public Profile mapRow(ResultSet rs, int rowNum) throws SQLException {
            Profile p = new Profile();
            p.setUsuarioId(rs.getString("usuario_id"));
            p.setNombreUsuario(rs.getString("nombre_usuario"));
            p.setEmailPublico(rs.getString("email_publico"));
            p.setEdad(rs.getObject("edad") != null ? rs.getInt("edad") : null);
            p.setMiembroDuoc(rs.getObject("miembro_duoc") != null ? rs.getObject("miembro_duoc", Boolean.class) : null);
            p.setAvatarUrl(rs.getString("avatar_url"));
            p.setCreadoEn(rs.getObject("creado_en", OffsetDateTime.class));
            return p;
        }
    };

    @Transactional(readOnly = true)
    public Optional<Profile> findMyProfileByUserId(String userId, String bearerJwt) {
        try {
            UUID uid = UUID.fromString(userId);

            final String sql =
                    "select usuario_id, nombre_usuario, email_publico, edad, " +
                            "       miembro_duoc, avatar_url, creado_en " +
                            "from public.perfiles " +
                            "where usuario_id = ? " +
                            "limit 1";

            return jdbc.query(sql, PROFILE_MAPPER, uid).stream().findFirst();
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }
}
