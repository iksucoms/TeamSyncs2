package kr.spring.minutes.vo;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MeetingMinutesVO {

    private Long minutes_num;
    private Long team_num;
    private Long schedule_num;
    private Long writer_num;

    private String title;
    private Date meeting_date;

    private String content;
    private String pdf_path;

    private Integer status;

    private Date reg_date;
    private Date modify_date;

    // DB 컬럼 아님. 화면에서 참석자 여러 명 받을 때 사용
    private List<Long> attendeeList;
}