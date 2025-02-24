package com.roomerang.service;

import com.roomerang.entity.ChatRoom;
import com.roomerang.entity.Message;
import com.roomerang.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    // ChatRoom, String, String을 받는 saveMessage 메서드
    public Message saveMessage(ChatRoom chatRoom, String senderId, String messageContent, LocalDateTime sentAt) {
        Message message = new Message(chatRoom, senderId, messageContent, sentAt);
        return messageRepository.save(message);
    }


    // 채팅방의 모든 메시지 조회
    public List<Message> getMessagesByRoom(ChatRoom chatRoom) {
        return messageRepository.findByChatRoom(chatRoom);
    }
}
