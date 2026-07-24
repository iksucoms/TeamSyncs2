package kr.spring.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import kr.spring.chat.service.ChatService;
import kr.spring.chat.vo.ChatChannelVO;
import kr.spring.chat.vo.ChatMessageVO;
import kr.spring.users.vo.PrincipalDetails;

@RestController
@RequestMapping("/chat")
public class ChatRestController {
	
	private final ChatService chatService;
	
	public ChatRestController(ChatService chatService) {
		this.chatService = chatService;
	}
	
	@GetMapping("/messages/{channel_num}")
	public ResponseEntity<List<ChatMessageVO>> getMessage(@PathVariable("channel_num") long channel_num){
		List<ChatMessageVO> list = chatService.selectMessageList(channel_num);
		
		return ResponseEntity.ok(list);
	}
	
	@PostMapping("/channel")
	public ResponseEntity<ChatChannelVO> createChannelAsync(ChatChannelVO channelVO,@AuthenticationPrincipal PrincipalDetails principal,HttpSession session){
		long user_num = principal.getUsersVO().getUser_num();
		channelVO.setCreate_by(user_num);
		
		long team_num = (Long)session.getAttribute("teamNum");
		channelVO.setTeam_num(team_num);
		chatService.insertChannel(channelVO);
		
		return ResponseEntity.ok(channelVO);
	}
	
	
}
