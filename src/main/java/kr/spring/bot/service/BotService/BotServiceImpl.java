package kr.spring.bot.service.BotService;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.bot.dao.BotMapper;
import kr.spring.bot.vo.BotAnswer;
import kr.spring.bot.vo.BotLogVO;

@Service
@Transactional
public class BotServiceImpl implements BotService {

	private static final int SENDER_USER = 1;
	private static final int SENDER_BOT = 2;

	// 중국어 한자(CJK 통합 한자/확장A) + 일본어 히라가나/가타카나 범위
	// 시스템 프롬프트만으로는 Qwen 계열 모델의 한자 혼입을 100% 막지 못해서 코드로 한 번 더 필터링
	private static final Pattern NON_KOREAN_CJK =
			Pattern.compile("[\u4E00-\u9FFF\u3400-\u4DBF\u3040-\u309F\u30A0-\u30FF\uFF66-\uFF9D]");

	// 비로그인 사용자가 이 단어들을 포함해서 질문하면 실제 팀 데이터가 필요한 질문으로 보고
	// AI 호출 없이 바로 로그인 유도 응답을 반환 (LLM 판단에 맡기지 않고 결정론적으로 차단)
	private static final Set<String> TEAM_DATA_KEYWORDS = Set.of(
			"일정", "캘린더", "스케줄", "회의록", "회의", "미팅",
			"칸반", "카드", "보드", "업무", "할일", "할 일", "태스크", "마감", "데드라인",
			"채팅", "대화", "메시지", "멘션",
			"파일", "보관함", "폴더", "첨부",
			"공지", "알림", "notification",
			"내 팀", "우리 팀", "팀원", "팀장", "마이페이지", "프로필"
	);

	private final BotMapper botMapper;
	private final ChatClient chatClient;

	// 비로그인(게스트) 응답용 시스템 프롬프트 (팀 데이터 언급 금지, 서비스 소개 위주)
	private static final String GUEST_SYSTEM_PROMPT = """
			당신은 TeamSync 서비스의 AI 어시스턴트입니다. 지금은 특정 팀 컨텍스트가 없는 상태입니다.
다음 규칙을 반드시 지키세요:
1. 기본적으로 한국어(한글)로 답변합니다.
2. 중국어 한자, 일본어 히라가나/가타카나는 단어의 한자 어원 설명, 일본어 표현 번역/인용처럼
   꼭 필요한 경우에만 최소한으로 사용하고, 사용한 즉시 괄호 등으로 한국어 뜻을 함께 적어주세요.
3. 한자나 가나를 쓸 때는 실제 글자를 그대로 적으세요. 절대로 ****, XX, ○○ 같은
   별표나 자리표시자로 가리거나 검열하지 마세요. 요청받으면 반드시 실제 문자로 답합니다.
4. 그 외의 불필요한 상황에서는 외국 문자를 섞지 말고 한국어로만 답변합니다.
5. 실제 팀의 일정, 회의록, 칸반, 채팅, 파일 등 개인/팀 데이터는 절대 언급하거나 지어내지 않습니다.
6. TeamSync 서비스 소개, 기능 설명, 일반적인 질문에는 친절하고 실용적으로 답변하세요.
			""";

	public BotServiceImpl(BotMapper botMapper, ChatClient.Builder builder) {
		this.botMapper = botMapper;
		this.chatClient = builder
				.defaultSystem("""
						당신은 TeamSync 서비스의 AI 어시스턴트입니다.
다음 규칙을 반드시 지키세요:
1. 기본적으로 한국어(한글)로 답변합니다.
2. 중국어 한자, 일본어 히라가나/가타카나는 단어의 한자 어원 설명, 일본어 표현 번역/인용처럼
   꼭 필요한 경우에만 최소한으로 사용하고, 사용한 즉시 괄호 등으로 한국어 뜻을 함께 적어주세요.
3. 한자나 가나를 쓸 때는 실제 글자를 그대로 적으세요. 절대로 ****, XX, ○○ 같은
   별표나 자리표시자로 가리거나 검열하지 마세요. 요청받으면 반드시 실제 문자로 답합니다.
4. 그 외의 불필요한 상황에서는 외국 문자를 섞지 말고 한국어로만 답변합니다.
5. 아직 팀의 회의록, 일정, 칸반 등 실제 팀 데이터에는 접근할 수 없습니다.
   구체적인 팀 데이터를 묻는 질문에는 해당 기능이 곧 추가될 예정이라고 안내하고,
   그 외에는 최대한 친절하고 실용적으로 도와주세요.
						""")
				.build();
	} 

	@Override
	public String ask(long user_num, long team_num, String message) {
		// 1. 사용자 메시지 저장
		BotLogVO userLog = new BotLogVO();
		userLog.setUser_num(user_num);
		userLog.setTeam_num(team_num);
		userLog.setMessage(message);
		userLog.setSender(SENDER_USER);
		botMapper.insertLog(userLog);

		// 2. AI 응답 생성
		// TODO(RAG 확장): 여기서 팀 데이터(회의록/일정/칸반 등)를 벡터 검색으로 조회해
		// context 로 프롬프트에 추가하면 RagChatService 형태로 확장 가능
		String raw = chatClient.prompt()
				.user(message)
				.call()
				.content();

		String answer = cleanAnswer(raw);

		// 3. 챗봇 응답 저장
		BotLogVO botLog = new BotLogVO();
		botLog.setUser_num(user_num);
		botLog.setTeam_num(team_num);
		botLog.setMessage(answer);
		botLog.setSender(SENDER_BOT);
		botMapper.insertLog(botLog);

		return answer;
	}

	@Override
	public List<BotLogVO> getHistory(long user_num, long team_num) {
		return botMapper.selectHistory(user_num, team_num);
	}

	@Override
	public BotAnswer askGuest(String message) {
		// 팀/개인 데이터 관련 키워드가 포함되면 AI 호출 없이 즉시 로그인 유도
		String normalized = message.replaceAll("\\s+", "");
		for (String keyword : TEAM_DATA_KEYWORDS) {
			if (normalized.contains(keyword.replaceAll("\\s+", ""))) {
				return new BotAnswer(
						"팀 일정, 회의록, 칸반 같은 팀 데이터는 로그인 후에 확인하실 수 있어요. 아래 버튼으로 로그인해 주세요 🙂",
						true
				);
			}
		}

		// 일반 질문은 게스트용 프롬프트로 답변 (DB 저장 안 함)
		String raw = chatClient.prompt()
				.system(GUEST_SYSTEM_PROMPT)
				.user(message)
				.call()
				.content();

		return new BotAnswer(cleanAnswer(raw), false);
	}

	// <think> 추론 태그 제거 + 한자/가나 필터링을 한 번에 처리
	private String cleanAnswer(String raw) {
		String noThink = raw.replaceAll("(?s)<think>.*?</think>", "").trim();
		return stripNonKorean(noThink);
	}

	// 한자/가나 문자만 제거하고 나머지(한글/영문/숫자/문장부호/이모지)는 그대로 둠
	private String stripNonKorean(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}
		return NON_KOREAN_CJK.matcher(text)
				.replaceAll("")
				.replaceAll(" {2,}", " ")
				.trim();
	}
}