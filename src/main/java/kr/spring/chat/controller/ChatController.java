package kr.spring.chat.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import kr.spring.chat.service.ChatService;
import kr.spring.chat.vo.ChatChannelVO;
import kr.spring.users.vo.PrincipalDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/chat")
public class ChatController {
	
	private final ChatService chatService;
	
	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	@GetMapping("/list")
	public String chat(@AuthenticationPrincipal PrincipalDetails principal, Model model,HttpSession session) {
		
		//사용자의 회원번호 가져오기
		long user_num = 1;
		long team_num = 2;
		
		List<ChatChannelVO> channelList = chatService.selectChannelList(user_num,team_num);
		// Controller에서 처리
		Map<Integer, List<ChatChannelVO>> groupedChannels =
		        channelList.stream()
		            .sorted(Comparator.comparing(ChatChannelVO::getChannel_name))
		            .collect(Collectors.groupingBy(
		                ChatChannelVO::getCategory,   // int → Integer로 자동 박싱
		                TreeMap::new,
		                Collectors.toList()
		            ));
		model.addAttribute("groupedChannels", groupedChannels);
		
		int teamRole = 1;
		model.addAttribute("teamRole",teamRole);
		model.addAttribute("currentMenu", "chat");
		
		return "thviews/chat/list";
	}
}