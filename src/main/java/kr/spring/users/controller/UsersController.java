package kr.spring.users.controller;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.spring.users.service.UsersService;
import kr.spring.users.vo.PrincipalDetails;
import kr.spring.users.vo.UsersVO;
import kr.spring.users.service.EmailService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/users")
public class UsersController {

    private final PasswordEncoder passwordEncoder;
	@Autowired
    private UsersService usersService;
	@Autowired
	private EmailService emailService;
	
	private static final int EMAIL_CODE_EXPIRE_MINUTES = 5;

	UsersController(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

    /*============================================
     * 개발용 더미 로그인
     * DB의 USER_NUM=1 유저로 Security 인증 주입
     *===========================================*/
    @GetMapping("/dev/dummy-login")
    public String dummyLogin(HttpServletRequest request) {
        UsersVO user = usersService.selectByUserNum(1L);
        if (user == null) {
            log.error("<<더미 로그인 실패>> USER_NUM=1 유저 없음");
            return "redirect:/";
        }

        // UsersVO를 PrincipalDetails로 감싸서 principal로 사용
        PrincipalDetails principalDetails = new PrincipalDetails(user);

        // Spring Security 인증 객체 생성
        Authentication auth = new UsernamePasswordAuthenticationToken(
            principalDetails,
            null,
            List.of(new SimpleGrantedAuthority(user.getAuth()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 세션에 Security 컨텍스트 저장
        HttpSession session = request.getSession(true);
        session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.getContext()
        );
        session.setAttribute("loginUser", user);
        session.setAttribute("teamNum", 2L);

        log.debug("<<더미 로그인 성공>> : {}", user);
        return "redirect:/team/dashboard";
    }
    
    
    /*============================================
     * 이메일 중복 확인
     *===========================================*/
    @GetMapping("/checkEmail")
    @ResponseBody
    public Map<String,Object> checkEmail(@RequestParam("email") String email){
    	boolean duplicated = false;
    	
    	if(email != null && !email.trim().isEmpty()) {
    		duplicated = usersService.isEmailDuplicated(email.trim());
    	}
    	
    	return Map.of(
    				"duplicated",duplicated,
    				"available",!duplicated
    			);
    }
    
    /*============================================
     * 회원가입 이메일 인증코드 발송
     *===========================================*/
    @PostMapping("/sendEmailCode")
    @ResponseBody
    public Map<String, Object> sendEmailCode(@RequestParam("email") String email,
                                             HttpSession session) {
        String targetEmail = email == null ? "" : email.trim();

        if (targetEmail.isEmpty()) {
            return Map.of(
                "result", "empty",
                "message", "이메일을 입력해주세요."
            );
        }

        if (usersService.isEmailDuplicated(targetEmail)) {
            return Map.of(
                "result", "duplicated",
                "message", "이미 가입한 이메일입니다."
            );
        }

        String code = createEmailCode();

        session.setAttribute("signupEmail", targetEmail);
        session.setAttribute("signupEmailCode", code);
        session.setAttribute("signupEmailExpireTime", LocalDateTime.now().plusMinutes(EMAIL_CODE_EXPIRE_MINUTES));
        session.setAttribute("signupEmailVerified", false);

        emailService.sendSignupVerificationCode(targetEmail, code);

        return Map.of(
            "result", "success",
            "message", "인증코드를 이메일로 전송했습니다.",
            "expireSeconds", EMAIL_CODE_EXPIRE_MINUTES * 60
        );
    }
    
    /*============================================
     * 회원가입 이메일 인증코드 확인
     *===========================================*/
    @PostMapping("/verifyEmailCode")
    @ResponseBody
    public Map<String, Object> verifyEmailCode(@RequestParam("email") String email,
                                               @RequestParam("code") String code,
                                               HttpSession session) {
        String targetEmail = email == null ? "" : email.trim();
        String inputCode = code == null ? "" : code.trim();

        String sessionEmail = (String) session.getAttribute("signupEmail");
        String sessionCode = (String) session.getAttribute("signupEmailCode");
        LocalDateTime expireTime = (LocalDateTime) session.getAttribute("signupEmailExpireTime");

        if (sessionEmail == null || sessionCode == null || expireTime == null) {
            return Map.of(
                "result", "none",
                "message", "인증코드를 먼저 발송해주세요."
            );
        }

        if (LocalDateTime.now().isAfter(expireTime)) {
            session.removeAttribute("signupEmailCode");
            session.removeAttribute("signupEmailExpireTime");
            session.setAttribute("signupEmailVerified", false);

            return Map.of(
                "result", "expired",
                "message", "인증 시간이 만료되었습니다. 인증코드를 다시 발송해주세요."
            );
        }

        if (!sessionEmail.equals(targetEmail)) {
            return Map.of(
                "result", "emailChanged",
                "message", "인증을 요청한 이메일과 현재 이메일이 다릅니다."
            );
        }

        if (!sessionCode.equals(inputCode)) {
            return Map.of(
                "result", "invalid",
                "message", "인증코드가 일치하지 않습니다."
            );
        }

        session.setAttribute("signupEmailVerified", true);

        return Map.of(
            "result", "success",
            "message", "이메일 인증이 완료되었습니다."
        );
    }
    
    /*============================================
     * 일반 회원가입
     *===========================================*/
    @PostMapping("/signup")
    public String signup(UsersVO userVO, RedirectAttributes redirectAttributes, HttpSession session) {
    	  String email = userVO.getEmail() == null ? "" : userVO.getEmail().trim();
          String passwd = userVO.getPasswd() == null ? "" : userVO.getPasswd().trim();
          String confirmPasswd = userVO.getConfirm_passwd() == null ? "" : userVO.getConfirm_passwd().trim();
          String userName = userVO.getUser_name() == null ? "" : userVO.getUser_name().trim();
          
          /*
           * UsersVO는 일반 회원가입과 추후 구글 소셜 회원가입에서 함께 사용할 수 있음
           * 구글 전용 회원은 PASSWD가 NULL일 수 있으므로 UsersVO.passwd에는 @NotBlank를 두지 않았음
           * 대신 일반 회원가입에서는 여기서 비밀번호 필수 입력 여부를 직접 검증
           */
          if (email.isEmpty() || passwd.isEmpty() || userName.isEmpty()) {
              redirectAttributes.addFlashAttribute("signupError", "이메일, 비밀번호, 닉네임은 필수입니다.");
              return "redirect:/member/login";
          }

          if (!passwd.equals(confirmPasswd)) {
              redirectAttributes.addFlashAttribute("signupError", "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
              return "redirect:/member/login";
          }

          if (usersService.isEmailDuplicated(email)) {
              redirectAttributes.addFlashAttribute("signupError", "이미 사용 중인 이메일입니다.");
              return "redirect:/member/login";
          }
          
          Boolean emailVerified = (Boolean) session.getAttribute("signupEmailVerified");
          String verifiedEmail = (String) session.getAttribute("signupEmail");
          LocalDateTime expireTime = (LocalDateTime) session.getAttribute("signupEmailExpireTime");

          if (!Boolean.TRUE.equals(emailVerified) || !email.equals(verifiedEmail)) {
              redirectAttributes.addFlashAttribute("signupError", "이메일 인증을 완료해주세요.");
              return "redirect:/member/login";
          }

          if (expireTime == null || LocalDateTime.now().isAfter(expireTime)) {
              session.removeAttribute("signupEmailCode");
              session.removeAttribute("signupEmailExpireTime");
              session.setAttribute("signupEmailVerified", false);

              redirectAttributes.addFlashAttribute("signupError", "이메일 인증 시간이 만료되었습니다. 다시 인증해주세요.");
              return "redirect:/member/login";
          }
          
          
          userVO.setEmail(email);
          userVO.setPasswd(passwordEncoder.encode(passwd));
          userVO.setUser_name(userName);
          userVO.setAuth("USER_MEMBER");
          userVO.setLogin_type(1);
          userVO.setGoogle_id(null);
          userVO.setStatus(1);

          usersService.insertUser(userVO);
          session.removeAttribute("signupEmail");
          session.removeAttribute("signupEmailCode");
          session.removeAttribute("signupEmailExpireTime");
          session.removeAttribute("signupEmailVerified");

          redirectAttributes.addFlashAttribute("signupSuccess", "회원가입이 완료되었습니다. 로그인해주세요.");
          return "redirect:/member/login";
    }
    
    private String createEmailCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}