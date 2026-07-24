package kr.spring.chat.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.chat.vo.ChatChannelVO;
import kr.spring.chat.vo.ChatMessageVO;

@Mapper
public interface ChatMapper {
	//채팅방생성
	public void insertChannel(ChatChannelVO channelVO);
	//내 채팅방 목록 조회
	List<ChatChannelVO> selectChannelList(@Param("user_num") long user_num,@Param("team_num") long team_num);
	// 특정 채널의 메시지 내역 가져오기
	List<ChatMessageVO> selectMessageList(long channel_num);
	//채널 참여자 목록 조회
		
	//메시지 전송
	//메시지 내역 조회
		
	//읽음 처리
	//안 읽은 메시지 개수 계산
		
	//파일 정보 저장
	//채널별 파일 모아보기
		
	//멘션 등록
	//나의 멘션 알림
}
