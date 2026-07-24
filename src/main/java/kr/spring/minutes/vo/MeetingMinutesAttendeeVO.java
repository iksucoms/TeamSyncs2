package kr.spring.minutes.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MeetingMinutesAttendeeVO {

    private Long minutes_attendee_num;
    private Long minutes_num;
    private Long user_num;
}