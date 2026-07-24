package kr.spring.calendar.service;

import java.util.List;

import org.springframework.stereotype.Service;

import kr.spring.calendar.dao.CalendarMapper;
import kr.spring.calendar.vo.CalendarEventVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService{

	private final CalendarMapper calendarMapper;
	
	@Override
	public List<CalendarEventVO> selectScheduleEventList(Long team_num) {
		return calendarMapper.selectScheduleEventList(team_num);
	}

	@Override
	public List<CalendarEventVO> selectMyScheduleEventList(Long user_num) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CalendarEventVO> selectScheduleEventListByPeriod(Long team_num, String start_date, String end_date) {
		// TODO Auto-generated method stub
		return null;
	}

}
