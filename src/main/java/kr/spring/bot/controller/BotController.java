package kr.spring.bot.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import kr.spring.bot.service.BotService.BotService;
import kr.spring.bot.vo.BotAnswer;
import kr.spring.bot.vo.BotLogVO;
import kr.spring.users.vo.PrincipalDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/bot")
public class BotController {

	private final BotService botService;

	public BotController(BotService botService) {
		this.botService = botService;
	}

	// 챗봇에게 메시지 전송 (로그인 사용자, 팀 컨텍스트 필요)
	@PostMapping(value = "/ask", produces = "application/json;charset=UTF-8")
	public Map<String, String> ask(@AuthenticationPrincipal PrincipalDetails principal,
			HttpSession session,
			@RequestBody Map<String, String> body) {

		if (principal == null) {
			return Map.of("answer", "로그인이 필요합니다.");
		}

		Long teamNum = (Long) session.getAttribute("teamNum");
		if (teamNum == null) {
			return Map.of("answer", "팀을 먼저 선택해 주세요.");
		}

		String message = body.get("message");
		if (message == null || message.trim().isEmpty()) {
			return Map.of("answer", "메시지를 입력해 주세요.");
		}

		long userNum = principal.getUsersVO().getUser_num();
		String answer = botService.ask(userNum, teamNum, message.trim());

		log.debug("<<챗봇 응답>> userNum={}, teamNum={}", userNum, teamNum);
		return Map.of("answer", answer);
	}

	// 비로그인 사용자용 챗봇 (랜딩/로그인 페이지). DB 저장 없음, 팀 데이터 질문은 로그인 유도.
	@PostMapping(value = "/guest-ask", produces = "application/json;charset=UTF-8")
	public BotAnswer guestAsk(@RequestBody Map<String, String> body) {
		String message = body.get("message");
		if (message == null || message.trim().isEmpty()) {
			return new BotAnswer("메시지를 입력해 주세요.", false);
		}
		return botService.askGuest(message.trim());
	}

	// 현재 팀 안에서의 대화 이력 조회 (챗봇 패널 열릴 때 이전 대화 복원용)
	@GetMapping("/history")
	public List<BotLogVO> history(@AuthenticationPrincipal PrincipalDetails principal, HttpSession session) {
		if (principal == null) {
			return List.of();
		}
		Long teamNum = (Long) session.getAttribute("teamNum");
		if (teamNum == null) {
			return List.of();
		}
		long userNum = principal.getUsersVO().getUser_num();
		return botService.getHistory(userNum, teamNum);
	}
}