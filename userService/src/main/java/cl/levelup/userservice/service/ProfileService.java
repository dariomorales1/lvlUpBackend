package cl.levelup.userservice.service;

import cl.levelup.userservice.model.Profile;
import cl.levelup.userservice.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileService {

    private final ProfileRepository repo;

    public ProfileService(ProfileRepository repo) {
        this.repo = repo;
    }

    public Optional<Profile> getMyProfile(String userId, String bearerJwt) {
        return repo.findMyProfileByUserId(userId, bearerJwt);
    }
}


