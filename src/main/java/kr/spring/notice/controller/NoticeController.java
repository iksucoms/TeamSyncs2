package kr.spring.notice.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.spring.users.vo.PrincipalDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/notice")
public class NoticeController {

	@GetMapping("/list")
	public String list(@AuthenticationPrincipal PrincipalDetails principal, Model model) {
		model.addAttribute("currentMenu", "notice");

		// 공지사항 목록 넘기기

		return "thviews/notice/list";
	}
}