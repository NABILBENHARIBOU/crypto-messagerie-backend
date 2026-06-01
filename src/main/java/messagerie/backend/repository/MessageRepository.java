package messagerie.backend.repository;

import messagerie.backend.model.Message;
import messagerie.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findBySenderOrRecipient(User sender, User recipient, Pageable pageable);
    Page<Message> findBySenderAndRecipient(User sender, User recipient, Pageable pageable);
    List<Message> findByRecipientAndReadAtIsNull(User recipient);
    List<Message> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Messages envoyés (soft delete)
    Page<Message> findBySenderAndIsDeletedFalse(User sender, Pageable pageable);
    
    // Messages reçus (soft delete)
    Page<Message> findByRecipientAndIsDeletedFalse(User recipient, Pageable pageable);
    
    // Messages favoris
    Page<Message> findByRecipientAndIsFavoriteTrueAndIsDeletedFalse(User recipient, Pageable pageable);
    Page<Message> findBySenderAndIsFavoriteTrueAndIsDeletedFalse(User sender, Pageable pageable);
    
    // Recherche de messages
    @Query("SELECT m FROM Message m WHERE m.isDeleted = false AND " +
           "(m.sender.id = :userId OR m.recipient.id = :userId) AND " +
           "m.encryptedContent LIKE %:query%")
    Page<Message> searchMessages(@Param("userId") Long userId, @Param("query") String query, Pageable pageable);
    
    // Conversation entre deux utilisateurs
    @Query("SELECT m FROM Message m WHERE m.isDeleted = false AND " +
           "((m.sender.id = :userId AND m.recipient.id = :otherUserId) OR " +
           "(m.sender.id = :otherUserId AND m.recipient.id = :userId)) " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findConversation(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId, Pageable pageable);
}
