package kr.spring.bot.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.bot.vo.BotLogVO;

@Mapper
public interface BotMapper {
	//챗봇 로그 1건 저장(사용자/챗봇)
	public void insertLog(BotLogVO botlog);
	
	//특정 팀 안에서 사용자의 대화 이력 조회
	public List<BotLogVO> selectHistory(@Param("user_num")long user_num, @Param("team_num") long team_num);
	
	
}
