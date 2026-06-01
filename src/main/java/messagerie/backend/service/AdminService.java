package messagerie.backend.service;

import messagerie.backend.dto.AdminDtos.AdminStatsResponse;
import messagerie.backend.dto.AdminDtos.SecurityLogResponse;
import messagerie.backend.exception.ResourceNotFoundException;
import messagerie.backend.model.SecurityLog;
import messagerie.backend.model.User;
import messagerie.backend.repository.MessageRepository;
import messagerie.backend.repository.SecurityLogRepository;
import messagerie.backend.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final SecurityLogRepository securityLogRepository;

    public AdminService(UserRepository userRepository, MessageRepository messageRepository, SecurityLogRepository securityLogRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.securityLogRepository = securityLogRepository;
    }

    public AdminStatsResponse stats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
            .filter(u -> u.getLastLoginAt() != null)
            .filter(u -> u.getLastLoginAt().isAfter(LocalDateTime.now().minusDays(7)))
            .count();
        long totalMessages = messageRepository.count();
        long criticalLogs = securityLogRepository.findByLogType(SecurityLog.LogType.SUSPICIOUS_ACTIVITY, Pageable.unpaged()).getTotalElements();

        return new AdminStatsResponse(
            (int) totalUsers,
            (int) activeUsers,
            (int) totalMessages,
            (int) criticalLogs
        );
    }

    /**
     * Get security logs
     */
    public Page<SecurityLogResponse> logs(Pageable pageable) {
        return securityLogRepository.findAll(pageable)
            .map(this::mapToSecurityLogResponse);
    }

    /**
     * Log security event
     */
    public void logSecurityEvent(Long userId, SecurityLog.LogType logType, String description, String ipAddress, SecurityLog.LogSeverity severity) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                .orElse(null);
        }

        SecurityLog log = new SecurityLog();
        log.setUser(user);
        log.setLogType(logType);
        log.setDescription(description);
        log.setIpAddress(ipAddress);
        log.setSeverity(severity);
        securityLogRepository.save(log);
    }

    /**
     * Delete user (admin only)
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        userRepository.delete(user);
    }

    private SecurityLogResponse mapToSecurityLogResponse(SecurityLog log) {
        return new SecurityLogResponse(
            log.getId(),
            log.getDescription(),
            log.getDescription(),
            log.getIpAddress(),
            log.getUserAgent(),
            log.getSeverity().name(),
            log.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()
        );
    }
}

