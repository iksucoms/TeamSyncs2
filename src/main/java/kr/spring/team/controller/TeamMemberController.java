package kr.spring.team.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import kr.spring.team.service.TeamMemberService;
import kr.spring.team.service.TeamService;
import kr.spring.team.vo.TeamMemberVO;
import kr.spring.users.vo.PrincipalDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/team")
public class TeamMemberController {

	// 역할 코드
	private static final int ROLE_MEMBER  = 1;
	private static final int ROLE_MANAGER = 2;
	private static final int ROLE_LEADER  = 3;

	@Autowired
	private TeamMemberService teamMemberService;
	
	@Autowired
	private TeamService teamService;

	/*============================================
	 * 팀원 목록 (MB-001~003)
	 *===========================================*/
	@GetMapping("/members")
	public String members(@AuthenticationPrincipal PrincipalDetails principal,
			HttpSession session, Model model) {
		Object teamNumAttr = session.getAttribute("teamNum");
		if (teamNumAttr == null) {
			log.debug("<<팀원 목록>> 세션에 teamNum 없음 -> 더미로그인으로 리다이렉트");
			return "redirect:/users/dev/dummy-login";
		}
		long teamNum = (Long) teamNumAttr;
		long myUserNum = principal.getUsersVO().getUser_num();

		List<TeamMemberVO> members = teamMemberService.selectMembersByTeamNum(teamNum);
		TeamMemberVO me = teamMemberService.selectMemberByTeamAndUser(teamNum, myUserNum);
		int myRole = me != null ? me.getRole() : ROLE_MEMBER;

		// 통계 (전체/활동중/팀장+매니저/팀원) - 현재 목록은 JOIN_STATUS=1만 조회되므로 전체==활동중
		int total   = members.size();
		int leaders = (int) members.stream().filter(m -> m.getRole() >= ROLE_MANAGER).count();
		int plain   = total - leaders;

		model.addAttribute("currentMenu", "members");
		model.addAttribute("members", members);
		model.addAttribute("myRole", myRole);
		model.addAttribute("myUserNum", myUserNum);
		model.addAttribute("statTotal", total);
		model.addAttribute("statActive", total);
		model.addAttribute("statLeader", leaders);
		model.addAttribute("statMember", plain);

		return "thviews/team/members";
	}

	/*============================================
	 * 역할 변경 (TM-008, MB-004)
	 * - LEADER만 가능 / 본인 변경 불가 / MEMBER<->MANAGER만 가능
	 *===========================================*/
	@PostMapping("/members/role")
	@ResponseBody
	public String changeRole(@AuthenticationPrincipal PrincipalDetails principal,
			HttpSession session,
			@RequestParam long targetUserNum,
			@RequestParam int newRole) {
		long teamNum = getTeamNum(session);
		long actorUserNum = principal.getUsersVO().getUser_num();

		if (newRole != ROLE_MEMBER && newRole != ROLE_MANAGER) {
			return "INVALID_ROLE";
		}
		if (actorUserNum == targetUserNum) {
			return "CANNOT_CHANGE_SELF";
		}

		TeamMemberVO actor = teamMemberService.selectMemberByTeamAndUser(teamNum, actorUserNum);
		if (actor == null || actor.getRole() != ROLE_LEADER) {
			return "NO_PERMISSION";
		}

		TeamMemberVO target = teamMemberService.selectMemberByTeamAndUser(teamNum, targetUserNum);
		if (target == null) {
			return "NOT_FOUND";
		}
		if (target.getRole() == ROLE_LEADER) {
			return "CANNOT_CHANGE_LEADER";
		}

		TeamMemberVO updateVo = new TeamMemberVO();
		updateVo.setTeam_member_num(target.getTeam_member_num());
		updateVo.setRole(newRole);
		teamMemberService.updateRole(updateVo);

		log.debug("<<역할 변경>> teamNum={}, target={}, newRole={}", teamNum, targetUserNum, newRole);
		return "OK";
	}

	/*============================================
	 * 강퇴 (TM-009, MB-005)
	 *===========================================*/
	@PostMapping("/members/kick")
	@ResponseBody
	public String kickMember(@AuthenticationPrincipal PrincipalDetails principal,
			HttpSession session,
			@RequestParam long targetUserNum) {
		long teamNum = getTeamNum(session);
		long actorUserNum = principal.getUsersVO().getUser_num();

		if (actorUserNum == targetUserNum) {
			return "CANNOT_KICK_SELF";
		}

		TeamMemberVO actor = teamMemberService.selectMemberByTeamAndUser(teamNum, actorUserNum);
		if (actor == null || actor.getRole() != ROLE_LEADER) {
			return "NO_PERMISSION";
		}

		TeamMemberVO target = teamMemberService.selectMemberByTeamAndUser(teamNum, targetUserNum);
		if (target == null) {
			return "NOT_FOUND";
		}

		teamMemberService.kickMember(teamNum, targetUserNum);

		log.debug("<<강퇴>> teamNum={}, target={}", teamNum, targetUserNum);
		return "OK";
	}

	/*============================================
	 * 팀장 위임 (TM-011)
	 *===========================================*/
	@PostMapping("/members/delegate")
	@ResponseBody
	public String delegateLeader(@AuthenticationPrincipal PrincipalDetails principal,
			HttpSession session,
			@RequestParam long targetUserNum) {
		long teamNum = getTeamNum(session);
		long actorUserNum = principal.getUsersVO().getUser_num();

		if (actorUserNum == targetUserNum) {
			return "CANNOT_DELEGATE_SELF";
		}

		TeamMemberVO currentLeader = teamMemberService.selectMemberByTeamAndUser(teamNum, actorUserNum);
		if (currentLeader == null || currentLeader.getRole() != ROLE_LEADER) {
			return "NO_PERMISSION";
		}

		TeamMemberVO target = teamMemberService.selectMemberByTeamAndUser(teamNum, targetUserNum);
		if (target == null) {
			return "NOT_FOUND";
		}

		TeamMemberVO demote = new TeamMemberVO();
		demote.setTeam_member_num(currentLeader.getTeam_member_num());
		demote.setRole(ROLE_MEMBER);
		teamMemberService.updateRole(demote);

		TeamMemberVO promote = new TeamMemberVO();
		promote.setTeam_member_num(target.getTeam_member_num());
		promote.setRole(ROLE_LEADER);
		teamMemberService.updateRole(promote);

		log.debug("<<팀장 위임>> teamNum={}, from={} to={}", teamNum, actorUserNum, targetUserNum);
		return "OK";
	}

	/*============================================
	 * 팀 탈퇴 (TM-010)
	 * - 팀장이 혼자 남은 상태로 탈퇴하면 팀 자체도 삭제 처리 (soft delete)
	 *===========================================*/
	@PostMapping("/members/exit")
	@ResponseBody
	public String exitTeam(@AuthenticationPrincipal PrincipalDetails principal,
			HttpSession session) {
		long teamNum = getTeamNum(session);
		long userNum = principal.getUsersVO().getUser_num();

		TeamMemberVO member = teamMemberService.selectMemberByTeamAndUser(teamNum, userNum);
		if (member == null) {
			return "NOT_FOUND";
		}

		boolean isLastLeader = false;
		if (member.getRole() == ROLE_LEADER) {
			int remaining = teamMemberService.countMembers(teamNum) - 1;
			if (remaining > 0) {
				return "LEADER_CANNOT_EXIT";
			}
			isLastLeader = true;
		}

		teamMemberService.exitMember(teamNum, userNum);

		// 팀장 혼자 남은 상태에서 탈퇴 -> 팀 자체도 삭제 처리 (TM-010)
		if (isLastLeader) {
			teamService.deleteTeam(teamNum);
			log.debug("<<팀 삭제(팀장 단독 탈퇴)>> teamNum={}", teamNum);
		}

		log.debug("<<팀 탈퇴>> teamNum={}, userNum={}", teamNum, userNum);
		return "OK";
	}

	private long getTeamNum(HttpSession session) {
		Object teamNum = session.getAttribute("teamNum");
		if (teamNum == null) {
			throw new IllegalStateException("선택된 팀이 없습니다.");
		}
		return (Long) teamNum;
	}
}