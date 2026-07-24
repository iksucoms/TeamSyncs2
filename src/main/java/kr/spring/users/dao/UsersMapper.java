package kr.spring.users.dao;

import org.apache.ibatis.annotations.Mapper;

import kr.spring.users.vo.UsersVO;

@Mapper
public interface UsersMapper {

	// user_num으로 조회
	public UsersVO selectByUserNum(long userNum);

	// email로 조회 (로그인용)
	public UsersVO selectByEmail(String email);

	// 이메일 중복 확인
	public int countByEmail(String email);

	// 회원가입
	public void insertUser(UsersVO userVO);
}
