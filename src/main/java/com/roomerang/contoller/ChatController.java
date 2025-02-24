package com.roomerang.contoller;

import com.roomerang.entity.ChatRoom;
import com.roomerang.entity.Message;
import com.roomerang.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate, MessageService messageService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }

    // ✅ 메시지를 처리하여 클라이언트에 전송하는 WebSocket 핸들러
    @MessageMapping("/chat")
    @SendTo("/topic/messages")  // 모든 클라이언트가 받을 주제
    public Message sendMessage(Message message) {
        // ✅ 메시지 전송 시간 설정
        message.setSentAt(LocalDateTime.now());

        // ✅ 메시지를 데이터베이스에 저장
        ChatRoom chatRoom = message.getChatRoom();
        String senderId = message.getSenderId();
        String messageContent = message.getMessageContent();

        messageService.saveMessage(chatRoom, senderId, messageContent, message.getSentAt());

        // ✅ 메시지를 /topic/messages로 보냄 (sentAt 포함)
        return message;
    }

    // 특정 유저에게 1:1 메시지 전송하는 메서드 (필요할 경우 사용)
    public void sendToUser(String user, String message) {
        messagingTemplate.convertAndSendToUser(user, "/queue/messages", message);
    }
}
