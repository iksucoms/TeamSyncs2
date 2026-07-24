package kr.spring.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import kr.spring.member.service.MemberService;
import kr.spring.member.vo.MemberVO;
import kr.spring.users.service.UsersService;
import kr.spring.users.vo.PrincipalDetails;
import kr.spring.users.vo.UsersVO;
import kr.spring.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/member")
public class MemberUserController {
	@Autowired
	private UsersService usersService;

	//자바빈(VO) 초기화
	@ModelAttribute
	public UsersVO initCommand() {
		return new UsersVO();
	}

	/*============================================
	 * 회원로그인
	 *===========================================*/
	//로그인 폼
	@GetMapping("/login")
	public String formLogin() {
		return "thviews/member/memberLogin";
	}	
	
	/*============================================
	 * 프로필 사진 출력
	 *===========================================*/
	//프로필 사진 출력(로그인 전용)
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/photoView")
	public String getProfile(@AuthenticationPrincipal PrincipalDetails principal,HttpServletRequest request,Model model) {
		try {
			UsersVO user = principal.getUsersVO();
			log.debug("<<photoView>> : {}", user);
			UsersVO usersVO = usersService.selectByUserNum(1L);
			viewProfile(usersVO,request,model);
		}catch(Exception e) {
			getBasicProfileImage(request,model);
		}
		return "imageView";
	}

	//프로필 사진 출력(회원번호 지정)
	@GetMapping("/viewProfile")
	public String getProfileByMem_num(long user_num,
			HttpServletRequest request,
			Model model) {
		UsersVO usersVO = usersService.selectByUserNum(user_num);

		viewProfile(usersVO,request,model);

		return "imageView";
	}

	//프로필 사진 처리를 위한 공통 코드
	public void viewProfile(UsersVO usersVO,HttpServletRequest request, Model model) {
		if(usersVO==null || usersVO.getPhoto_name()==null) {
			//DB에 저장된 프로필 이미지가 없기 때문에 기본 이미지 호출
			getBasicProfileImage(request,model);
		}else {//업로드한 프로필 이미지 읽기
			//속성명       속성값(byte[]의 데이터)
			model.addAttribute("imageFile", usersVO.getPhoto());
			model.addAttribute("filename", usersVO.getPhoto_name());
		}
	}
	//기본 이미지 읽기
	public void getBasicProfileImage(HttpServletRequest request,Model model) {
		byte[] readbyte = FileUtil.getBytes(request.getServletContext().getRealPath("/assets/image_bundle/face.png"));
		//속성명       속성값(byte[]의 데이터)
		model.addAttribute("imageFile", readbyte);
		model.addAttribute("filename", "face.png");
	}
}
