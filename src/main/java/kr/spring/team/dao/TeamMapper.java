package kr.spring.team.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.spring.team.vo.TeamVO;

@Mapper
public interface TeamMapper {
    // 팀 생성
    void insertTeam(TeamVO team);
    // 팀 단건 조회
    TeamVO selectTeamByNum(long teamNum);
    // 팀명 중복 체크용 조회 (TM-001)
    TeamVO selectTeamByName(String teamName);
    // 유저가 만든 팀 목록 (creator_num 기준)
    List<TeamVO> selectTeamsByCreator(long userNum);
    // 팀 정보 수정 (이름, 설명, 색상, 사진)
    void updateTeam(TeamVO team);
    // 팀 삭제 (soft delete - status 변경)
    void deleteTeam(long teamNum);
}