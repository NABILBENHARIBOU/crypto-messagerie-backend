package messagerie.backend.controller;

import messagerie.backend.dto.MessageDtos.*;
import messagerie.backend.exception.UnauthorizedException;
import messagerie.backend.service.AuthService;
import messagerie.backend.service.MessageService;
import messagerie.backend.util.SecurityUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@PreAuthorize("isAuthenticated()")
public class MessageController {

    private final MessageService messageService;
    private final AuthService authService;

    public MessageController(MessageService messageService, AuthService authService) {
        this.messageService = messageService;
        this.authService = authService;
    }

    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(
        @Valid @RequestBody SendMessageRequest request
    ) {
        String currentUserEmail = SecurityUtil.getCurrentUserEmail();
        if (currentUserEmail == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        Long senderId = authService.findByEmail(currentUserEmail).id();
        MessageResponse response = messageService.sendMessage(senderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<MessageResponse>> getUnreadMessages() {
        String currentUserEmail = SecurityUtil.getCurrentUserEmail();
        if (currentUserEmail == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        Long userId = authService.findByEmail(currentUserEmail).id();
        return ResponseEntity.ok(messageService.getUnreadMessages(userId));
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Long messageId) {
        String currentUserEmail = SecurityUtil.getCurrentUserEmail();
        if (currentUserEmail == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        // Verify the message belongs to the current user
        MessageResponse message = messageService.getMessageById(messageId);
        Long userId = authService.findByEmail(currentUserEmail).id();
        
        if (!message.recipientId().equals(userId) && !message.senderId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to access this message");
        }
        
        return ResponseEntity.ok(messageService.markAsRead(messageId));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        String currentUserEmail = SecurityUtil.getCurrentUserEmail();
        if (currentUserEmail == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        // Verify the message belongs to the current user
        MessageResponse message = messageService.getMessageById(messageId);
        Long userId = authService.findByEmail(currentUserEmail).id();
        
        if (!message.recipientId().equals(userId) && !message.senderId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to delete this message");
        }
        
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conversation/{userId}")
    public ResponseEntity<Page<MessageResponse>> getConversation(
        @PathVariable Long userId,
        Pageable pageable
    ) {
        String currentUserEmail = SecurityUtil.getCurrentUserEmail();
        if (currentUserEmail == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        Long currentUserId = authService.findByEmail(currentUserEmail).id();
        return ResponseEntity.ok(messageService.getConversation(currentUserId, userId, pageable));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(
        @PathVariable Long messageId,
        @Valid @RequestBody MessageUpdateRequest request
    ) {
        String currentUserEmail = SecurityUtil.getCurrentUserEmail();
        if (currentUserEmail == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        Long userId = authService.findByEmail(currentUserEmail).id();
        MessageResponse response = messageService.updateMessage(messageId, request, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{messageId}/favorite")
    public ResponseEntity<MessageResponse> toggleFavorite(@PathVariable Long messageId) {
        String currentUserEmail = SecurityUtil.getCurrentUserEmail();
        if (currentUserEmail == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        Long userId = authService.findByEmail(currentUserEmail).id();
        MessageResponse response = messageService.toggleFavorite(messageId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorites")
    public ResponseEntity<Page<MessageResponse>> getFavorites(Pageable pageable) {
        String currentUserEmail = SecurityUtil.getCurrentUserEmail();
        if (currentUserEmail == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        Long userId = authService.findByEmail(currentUserEmail).id();
        return ResponseEntity.ok(messageService.getFavoriteMessages(userId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MessageResponse>> searchMessages(
        @RequestParam String query,
        Pageable pageable
    ) {
        String currentUserEmail = SecurityUtil.getCurrentUserEmail();
        if (currentUserEmail == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        Long userId = authService.findByEmail(currentUserEmail).id();
        return ResponseEntity.ok(messageService.searchMessages(userId, query, pageable));
    }

    @GetMapping("/sent")
    public ResponseEntity<Page<MessageResponse>> getSentMessages(Pageable pageable) {
        String currentUserEmail = SecurityUtil.getCurrentUserEmail();
        if (currentUserEmail == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        Long userId = authService.findByEmail(currentUserEmail).id();
        return ResponseEntity.ok(messageService.getSentMessages(userId, pageable));
    }

    @GetMapping("/inbox")
    public ResponseEntity<Page<MessageResponse>> getInboxMessages(Pageable pageable) {
        String currentUserEmail = SecurityUtil.getCurrentUserEmail();
        if (currentUserEmail == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        Long userId = authService.findByEmail(currentUserEmail).id();
        return ResponseEntity.ok(messageService.getInboxMessages(userId, pageable));
    }
}
