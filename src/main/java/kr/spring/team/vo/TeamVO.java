package kr.spring.team.vo;

import java.io.IOException;
import java.sql.Date;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"team_photo"})
public class TeamVO {
	private long team_num;
	private String team_name;
	private String description;
	private byte[] team_photo;
	private String team_photo_name;
	private String color;
	private int status;
	private Date created_at;
	private Date updated_at;
	private Date deleted_at;
	//팀을 만든 사람 팀장, 만약 위임 등 팀장이 바뀌면 여기도 변경 됨
	private long creator_num;

	//============이미지 BLOB 처리=====================//
	//(주의)폼에서 파일업로드 파라미터네임은 반드시 upload로 지정해야 함
	public void setUpload(MultipartFile upload) throws IOException {
		if (upload == null || upload.isEmpty()) return;
		//MultipartFile -> byte[]
		setTeam_photo(upload.getBytes());
		//파일 이름
		setTeam_photo_name(upload.getOriginalFilename());
	}
	//============이미지 BLOB 처리=====================//
}