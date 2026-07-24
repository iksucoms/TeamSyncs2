package kr.spring.calendar.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.calendar.vo.CalendarEventVO;

@Mapper
public interface CalendarMapper {

    // 팀 캘린더 일정 조회
    List<CalendarEventVO> selectScheduleEventList(@Param("team_num") Long team_num);

    // 홈 캘린더용, 내가 속한 팀 전체 일정 조회
    List<CalendarEventVO> selectMyScheduleEventList(@Param("user_num") Long user_num);

    // 기간 조건까지 받을 때
    List<CalendarEventVO> selectScheduleEventListByPeriod(@Param("team_num") Long team_num,
                                                          @Param("start_date") String start_date,
                                                          @Param("end_date") String end_date);
}