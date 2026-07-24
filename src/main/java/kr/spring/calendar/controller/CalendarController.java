package kr.spring.calendar.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.spring.calendar.service.CalendarService;
import kr.spring.calendar.vo.CalendarEventVO;
import kr.spring.users.vo.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/calendar")
public class CalendarController {

	private final CalendarService calendarService;

	
	
	// 캘린더 화면 이동
	@GetMapping("/list")
	public String list(@AuthenticationPrincipal PrincipalDetails principal, Model model) {
		model.addAttribute("currentMenu", "calendar");

		// FullCalendar를 쓸 거라면 여기서 일정 목록을 직접 model에 담을 필요 없음
		// 일정 데이터는 아래 /calendar/events에서 JSON으로 따로 가져감

		return "thviews/calendar/list";
	}

	// FullCalendar에 전달할 일정 목록 JSON
	@GetMapping("/events")
	@ResponseBody
	public List<CalendarEventVO> getCalendarEvents(@RequestParam("team_num") Long team_num) {
		
		log.debug("캘린더 일정 조회 team_num = {}", team_num);
		
		return calendarService.selectScheduleEventList(team_num);
	}
	
	
}