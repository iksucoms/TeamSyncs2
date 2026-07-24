package kr.spring.chat.service;

import java.util.List;

import kr.spring.chat.vo.ChatChannelVO;
import kr.spring.chat.vo.ChatMessageVO;

public interface ChatService {
	List<ChatChannelVO> selectChannelList(long user_num,long team_num);
	List<ChatMessageVO> selectMessageList(long channel_num);
	public void insertChannel(ChatChannelVO channelVO);
}
