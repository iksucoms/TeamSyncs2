package kr.spring.users.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.users.dao.UsersMapper;
import kr.spring.users.vo.UsersVO;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@Transactional
public class UsersServiceImpl implements UsersService{

	
	@Autowired
	private UsersMapper usersMapper;
	
	@Override
	public UsersVO selectByUserNum(long userNum) {
		return usersMapper.selectByUserNum(userNum);
	}

	@Override
	public UsersVO selectByEmail(String email) {
		return usersMapper.selectByEmail(email);
	}

	@Override
	public boolean isEmailDuplicated(String email) {
		return usersMapper.countByEmail(email) > 0;
	}

	@Override
	public void insertUser(UsersVO userVO) {
		usersMapper.insertUser(userVO);
	}

}
