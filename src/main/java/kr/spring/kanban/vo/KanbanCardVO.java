package kr.spring.kanban.vo;

import java.sql.Date;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KanbanCardVO {
	private long card_num;
	private long team_num;
	private long writer_num;
	@NotBlank
	private String title;
	private String content;
	private String tag;
	private int kanban_status;
	@NotBlank
	private Date deadline;
	private Date reg_date;
	private Date modify_date;
	private int status;
}
