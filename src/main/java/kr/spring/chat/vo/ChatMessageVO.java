package kr.spring.chat.vo;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChatMessageVO {
	private long message_num;
	private long channel_num;
	private long user_num;
	private String content;
	private long parent_message;
	private Date send_date;
	
	private String userName;
	private String userPhoto;
	
	private long file_num;
	private String origin_name;
	private String file_type;
	private long file_size;
}
