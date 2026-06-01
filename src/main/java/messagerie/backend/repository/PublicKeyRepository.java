package messagerie.backend.repository;

import messagerie.backend.model.PublicKey;
import messagerie.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublicKeyRepository extends JpaRepository<PublicKey, Long> {
    Optional<PublicKey> findByUser(User user);
    Optional<PublicKey> findByUserAndActiveTrue(User user);
}
