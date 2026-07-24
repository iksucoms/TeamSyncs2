package kr.spring.users.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

	private final JavaMailSender mailSender;

	@Override
	public void sendSignupVerificationCode(String toEmail, String code) {
		SimpleMailMessage message = new SimpleMailMessage();

		message.setTo(toEmail);
		message.setSubject("[TeamSync] 회원가입 이메일 인증코드");
		message.setText(
			"TeamSync 회원가입 이메일 인증코드입니다.\n\n"
			+ "인증코드: " + code + "\n\n"
			+ "인증코드는 5분 동안 유효합니다."
		);

		mailSender.send(message);
	}
}