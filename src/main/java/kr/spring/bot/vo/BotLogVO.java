package kr.spring.bot.vo;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BotLogVO {
	private long bot_num;
	private long user_num;
	private long team_num;
	private String message;
	private int sender;
	private Date reg_date;
}
