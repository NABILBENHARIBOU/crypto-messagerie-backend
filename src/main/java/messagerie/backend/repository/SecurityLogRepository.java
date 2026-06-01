package messagerie.backend.repository;

import messagerie.backend.model.SecurityLog;
import messagerie.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityLogRepository extends JpaRepository<SecurityLog, Long> {
    Page<SecurityLog> findByUser(User user, Pageable pageable);
    List<SecurityLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    Page<SecurityLog> findByLogType(SecurityLog.LogType logType, Pageable pageable);
}
