package kr.spring.bot.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BotAnswer {
	private String answer;
	private boolean loginRequired;
	
}
