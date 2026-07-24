package kr.spring.team.controller;

import java.io.IOException;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import kr.spring.team.service.TeamMemberService;
import kr.spring.team.service.TeamService;
import kr.spring.team.vo.TeamMemberVO;
import kr.spring.team.vo.TeamVO;
import kr.spring.users.vo.PrincipalDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/team")
public class TeamController {

	private static final int ROLE_LEADER = 3;

	// 색상 HEX 코드 검증 (#RRGGBB)
	private static final Pattern COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

	@Autowired
	private TeamService teamService;

	@Autowired
	private TeamMemberService teamMemberService;

	@GetMapping("/dashboard")
	public String dashboard(@AuthenticationPrincipal PrincipalDetails principal, Model model) {
		model.addAttribute("currentMenu", "dashboard");

		// 사용자 정보 넘기기
		// 팀 정보 넘기기

		return "thviews/team/dashboard";
	}

	@GetMapping("/settings")
	public String settings(@AuthenticationPrincipal PrincipalDetails principal, Model model) {
		model.addAttribute("currentMenu", "settings");

		// 팀 설정 정보 넘기기

		return "thviews/team/settings";
	}

	/*============================================
	 * 팀 생성 (TM-001)
	 * - 생성자는 자동으로 LEADER 역할로 TEAM_MEMBER 등록
	 * - 팀명 중복 불가 (DB TEAM_NAME UK 제약과 별개로 사전 체크하여 친절한 에러 반환)
	 * - 팀 프로필 이미지는 선택 사항 (미업로드 시 기본 이미지로 처리 - 프론트에서 default 이미지 표시)
	 *===========================================*/
	@PostMapping("/create")
	@ResponseBody
	public String createTeam(@AuthenticationPrincipal PrincipalDetails principal,
			@RequestParam String teamName,
			@RequestParam(required = false) String description,
			@RequestParam String color,
			@RequestParam(value = "upload", required = false) MultipartFile upload) {

		// ---- 입력값 검증 ----
		if (teamName == null || teamName.trim().isEmpty()) {
			return "INVALID_NAME";
		}
		String trimmedName = teamName.trim();
		if (trimmedName.length() < 2 || trimmedName.length() > 30) {
			return "INVALID_NAME_LENGTH";
		}
		if (color == null || !COLOR_PATTERN.matcher(color.trim()).matches()) {
			return "INVALID_COLOR";
		}
		String trimmedDesc = (description == null) ? null : description.trim();
		if (trimmedDesc != null && trimmedDesc.length() > 200) {
			return "INVALID_DESCRIPTION_LENGTH";
		}

		// ---- 이름 중복 체크 (TM-001) ----
		if (teamService.selectTeamByName(trimmedName) != null) {
			return "DUPLICATE_NAME";
		}

		long creatorNum = principal.getUsersVO().getUser_num();

		TeamVO team = new TeamVO();
		team.setTeam_name(trimmedName);
		team.setDescription(trimmedDesc);
		team.setColor(color.trim());
		team.setCreator_num(creatorNum);

		// 팀 프로필 이미지 (선택) - 미업로드 시 team_photo는 null로 유지 -> 프론트에서 기본 이미지 처리
		if (upload != null && !upload.isEmpty()) {
			try {
				team.setUpload(upload);
			} catch (IOException e) {
				log.error("<<팀 프로필 이미지 업로드 실패>> : {}", e.toString());
				return "UPLOAD_FAIL";
			}
		}

		teamService.insertTeam(team);

		// 생성자를 LEADER로 TEAM_MEMBER 등록
		TeamMemberVO leader = new TeamMemberVO();
		leader.setTeam_num(team.getTeam_num());
		leader.setUser_num(creatorNum);
		leader.setRole(ROLE_LEADER);
		teamMemberService.insertTeamMember(leader);

		// TODO: 채팅 모듈 완성 후 기본 채팅 채널 자동 개설 (TM-001)
		// 채팅 담당자와 CHAT_CHANNEL 컬럼값(CHANNEL_NAME 기본값, IS_DEFAULT 처리) 협의 필요
		// 예) chatChannelService.createDefaultChannel(team.getTeam_num(), creatorNum);

		log.debug("<<팀 생성>> teamNum={}, creator={}", team.getTeam_num(), creatorNum);
		return "OK:" + team.getTeam_num();
	}

	/*============================================
	 * 팀 입장 (사용자 홈 -> 팀 클릭)
	 * - 해당 팀의 JOINED 멤버인지 확인 후 세션에 teamNum 저장
	 *===========================================*/
	@GetMapping("/enter/{teamNum}")
	public String enterTeam(@AuthenticationPrincipal PrincipalDetails principal,
			@PathVariable long teamNum,
			HttpSession session) {
		long userNum = principal.getUsersVO().getUser_num();

		TeamMemberVO member = teamMemberService.selectMemberByTeamAndUser(teamNum, userNum);
		if (member == null) {
			log.debug("<<팀 입장 거부>> 소속 아님: teamNum={}, userNum={}", teamNum, userNum);
			return "redirect:/main/home";
		}

		session.setAttribute("teamNum", teamNum);
		log.debug("<<팀 입장>> teamNum={}, userNum={}", teamNum, userNum);
		return "redirect:/team/dashboard";
	}
}