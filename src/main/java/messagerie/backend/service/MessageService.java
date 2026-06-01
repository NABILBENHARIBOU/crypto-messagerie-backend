package messagerie.backend.service;

import messagerie.backend.dto.MessageDtos.SendMessageRequest;
import messagerie.backend.dto.MessageDtos.MessageResponse;
import messagerie.backend.dto.MessageDtos.MessageUpdateRequest;
import messagerie.backend.exception.ResourceNotFoundException;
import messagerie.backend.model.Message;
import messagerie.backend.model.User;
import messagerie.backend.repository.MessageRepository;
import messagerie.backend.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Send a message
     */
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new ResourceNotFoundException("Sender not found: " + senderId));
        
        User recipient = userRepository.findById(request.recipientId())
            .orElseThrow(() -> new ResourceNotFoundException("Recipient not found: " + request.recipientId()));

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setEncryptedContent(request.encryptedContent());
        message.setEncryptionAlgorithm(request.encryptionAlgorithm());
        message.setIv(request.iv());
        message.setAuthTag(request.authTag());
        message.setMediaUrl(request.mediaUrl());
        message.setMediaType(request.mediaType());

        Message savedMessage = messageRepository.save(message);
        return mapToMessageResponse(savedMessage);
    }

    /**
     * Get message by ID
     */
    public MessageResponse getMessageById(Long messageId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));
        return mapToMessageResponse(message);
    }

    /**
     * Get unread messages for a user
     */
    public List<MessageResponse> getUnreadMessages(Long userId) {
        User recipient = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        
        return messageRepository.findByRecipientAndReadAtIsNull(recipient).stream()
            .map(this::mapToMessageResponse)
            .toList();
    }

    /**
     * Mark message as read
     */
    public MessageResponse markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));
        
        message.setReadAt(LocalDateTime.now());
        message.setStatus(Message.MessageStatus.READ);
        
        Message updated = messageRepository.save(message);
        return mapToMessageResponse(updated);
    }

    /**
     * Delete message (soft delete)
     */
    public void deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));
        
        message.setDeletedAt(LocalDateTime.now());
        message.setIsDeleted(true);
        message.setStatus(Message.MessageStatus.DELETED);
        messageRepository.save(message);
    }

    /**
     * Update a message
     */
    public MessageResponse updateMessage(Long messageId, MessageUpdateRequest request, Long userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));
        
        // Vérifier que l'utilisateur est l'expéditeur
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("Only the sender can edit this message");
        }
        
        // Vérifier que le message n'est pas supprimé
        if (message.getIsDeleted()) {
            throw new RuntimeException("Cannot edit a deleted message");
        }
        
        message.setEncryptedContent(request.encryptedContent());
        message.setEncryptionAlgorithm(request.encryptionAlgorithm());
        message.setIv(request.iv());
        message.setAuthTag(request.authTag());
        message.setMediaUrl(request.mediaUrl());
        message.setMediaType(request.mediaType());
        message.setEditedAt(LocalDateTime.now());
        
        Message updated = messageRepository.save(message);
        return mapToMessageResponse(updated);
    }

    /**
     * Toggle favorite status
     */
    public MessageResponse toggleFavorite(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));
        
        // Vérifier que l'utilisateur a accès au message
        if (!message.getSender().getId().equals(userId) && !message.getRecipient().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to access this message");
        }
        
        message.setIsFavorite(!message.getIsFavorite());
        Message updated = messageRepository.save(message);
        return mapToMessageResponse(updated);
    }

    /**
     * Get favorite messages for a user
     */
    public Page<MessageResponse> getFavoriteMessages(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        
        // Combiner les messages favoris reçus et envoyés
        Page<Message> receivedFavorites = messageRepository.findByRecipientAndIsFavoriteTrueAndIsDeletedFalse(user, pageable);
        return receivedFavorites.map(this::mapToMessageResponse);
    }

    /**
     * Search messages
     */
    public Page<MessageResponse> searchMessages(Long userId, String query, Pageable pageable) {
        return messageRepository.searchMessages(userId, query, pageable)
            .map(this::mapToMessageResponse);
    }

    /**
     * Get sent messages
     */
    public Page<MessageResponse> getSentMessages(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        
        return messageRepository.findBySenderAndIsDeletedFalse(user, pageable)
            .map(this::mapToMessageResponse);
    }

    /**
     * Get inbox messages
     */
    public Page<MessageResponse> getInboxMessages(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        
        return messageRepository.findByRecipientAndIsDeletedFalse(user, pageable)
            .map(this::mapToMessageResponse);
    }

    /**
     * Get conversation between two users
     */
    public Page<MessageResponse> getConversation(Long userId1, Long userId2, Pageable pageable) {
        User user1 = userRepository.findById(userId1)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId1));
        User user2 = userRepository.findById(userId2)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId2));
        
        return messageRepository.findBySenderAndRecipient(user1, user2, pageable)
            .map(this::mapToMessageResponse);
    }

    private MessageResponse mapToMessageResponse(Message message) {
        return new MessageResponse(
            message.getId(),
            message.getSender().getId(),
            message.getRecipient().getId(),
            message.getEncryptedContent(),
            message.getEncryptionAlgorithm(),
            message.getStatus().name(),
            message.getCreatedAt(),
            message.getReadAt(),
            message.getEditedAt(),
            message.getIsFavorite(),
            message.getIsDeleted()
        );
    }
}
