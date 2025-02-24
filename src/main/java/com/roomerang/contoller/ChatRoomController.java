package com.roomerang.contoller;

import com.roomerang.entity.ChatRoom;
import com.roomerang.entity.Message;
import com.roomerang.entity.Post;
import com.roomerang.entity.User;
import com.roomerang.service.ChatRoomService;
import com.roomerang.service.MessageService;
import com.roomerang.service.PostService;
import com.roomerang.service.UserService;
import com.roomerang.util.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final PostService postService;

    // 채팅방 생성
    @PostMapping("/chat/start/{postId}")
    public String startChat(@PathVariable Long postId, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute(SessionConst.LOGIN_USER) : null;

        if (loginUser == null) {
            return "redirect:/auth/login";
        }

        // ✅ 게시글 정보를 DB에서 가져옴
        Post post = postService.getPostById(postId);
        if (post == null) {
            return "redirect:/board/rooms"; // 게시글이 없으면 목록으로 리다이렉트
        }

        // ✅ 게시글 작성자 정보를 올바르게 가져옴
        String postAuthor = post.getUserId();  // 게시글 작성자 ID
        String currentUser = loginUser.getUsername();

        // ✅ 본인 글이면 채팅 불가
        if (postAuthor.equals(currentUser)) {
            return "redirect:/board/rooms";
        }

        // ✅ 올바른 사용자 정보로 채팅방 생성
        ChatRoom chatRoom = chatRoomService.createChatRoom(postAuthor, currentUser);

        return "redirect:/chat/room/" + chatRoom.getRoomId();
    }



    // 채팅방 목록 조회
    @GetMapping("/chat/list")
    public String chatList(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute(SessionConst.LOGIN_USER) : null;

        if (loginUser == null) {
            return "redirect:/auth/login";  // 로그인하지 않으면 로그인 페이지로 리다이렉트
        }

        List<ChatRoom> chatRooms = chatRoomService.getChatRooms(loginUser.getUsername());
        model.addAttribute("chatRooms", chatRooms);

        return "chat/chatList";  // 채팅 목록 페이지로 이동
    }

    // 채팅방 상세 보기
    @GetMapping("/chat/room/{roomId}")
    public String chatRoom(@PathVariable Long roomId, HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute(SessionConst.LOGIN_USER) : null;

        if (loginUser == null) {
            return "redirect:/auth/login";
        }

        // 채팅방 정보 가져오기
        ChatRoom chatRoom = chatRoomService.getChatRoom(roomId);
        if (chatRoom == null) {
            return "redirect:/board/rooms";
        }

        List<Message> messages = messageService.getMessagesByRoom(chatRoom);
        model.addAttribute("chatRoom", chatRoom);
        model.addAttribute("messages", messages);

        return "chat/chatRoom";  // 채팅방 페이지로 이동
    }

    // 메시지 전송 처리
    @PostMapping("/chat/send/{roomId}")
    public String sendMessage(@PathVariable Long roomId, HttpServletRequest request, String messageContent) {
        HttpSession session = request.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute(SessionConst.LOGIN_USER) : null;

        if (loginUser == null) {
            return "redirect:/auth/login";
        }

        // 채팅방 정보 가져오기
        ChatRoom chatRoom = chatRoomService.getChatRoom(roomId);
        if (chatRoom == null) {
            return "redirect:/board/rooms";
        }

        // 메시지 저장
        messageService.saveMessage(chatRoom, loginUser.getUsername(), messageContent, LocalDateTime.now());

        return "redirect:/chat/room/" + roomId;  // 채팅방으로 리다이렉트
    }

    // 채팅방 삭제
    @PostMapping("/chat/delete/{roomId}")
    public String deleteChatRoom(@PathVariable Long roomId, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute(SessionConst.LOGIN_USER) : null;

        if (loginUser == null) {
            return "redirect:/auth/login";  // 로그인되지 않으면 로그인 페이지로 리다이렉트
        }

        // 해당 채팅방 삭제
        chatRoomService.deleteChatRoom(roomId);

        // 채팅방 삭제 후 채팅방 목록 페이지로 리다이렉트
        return "redirect:/chat/list";
    }
}
