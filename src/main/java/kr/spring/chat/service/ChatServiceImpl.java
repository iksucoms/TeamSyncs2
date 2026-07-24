package kr.spring.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.chat.dao.ChatMapper;
import kr.spring.chat.vo.ChatChannelVO;
import kr.spring.chat.vo.ChatMessageVO;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {
	
	private final ChatMapper chatMapper;
	
	public ChatServiceImpl(ChatMapper chatMapper) {
		this.chatMapper = chatMapper;
	}

	@Override
	public List<ChatChannelVO> selectChannelList(long user_num,long team_num) {
		return chatMapper.selectChannelList(user_num,team_num);
	}

	@Override
	public List<ChatMessageVO> selectMessageList(long channel_num) {
		return chatMapper.selectMessageList(channel_num);
	}

	@Override
	public void insertChannel(ChatChannelVO channelVO) {
		chatMapper.insertChannel(channelVO);
	}
}
