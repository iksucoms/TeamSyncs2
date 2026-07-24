package kr.spring.bot.service.BotService;

import java.util.List;

import kr.spring.bot.vo.BotAnswer;
import kr.spring.bot.vo.BotLogVO;

public interface BotService {
	//사용자 메시지를 저장하고, AI응답을 받아 저장한뒤 텍스트를 반환
	String ask(long user_num, long team_num, String message);
	
	// 현재 팀 안에서의 대화 이력 조회
	List<BotLogVO> getHistory(long user_num, long team_num);
	
	//비로그인 사용자용 질문 처리(팀 데이터 질문은 로그인 유도
	BotAnswer askGuest(String message);
	
}
