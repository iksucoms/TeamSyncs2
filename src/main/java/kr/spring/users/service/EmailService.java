package kr.spring.users.service;

public interface EmailService {

	// 회원가입 이메일 인증코드 발송
	public void sendSignupVerificationCode(String toEmail, String code);
}