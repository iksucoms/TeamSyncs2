/**
 * TeamSync - 채팅방 연동 및 메시지 비동기 로딩 스크립트
 */

// 방 클릭 시 호출되는 함수
function switchChannel(channelNum, channelName, clickedElement) {
    
    // 1. 클릭된 방 시각적으로 활성화
    const allItems = document.querySelectorAll('.channel-item');
    allItems.forEach(item => item.classList.remove('active'));
    clickedElement.classList.add('active');

    // 2. 중앙 채팅창 상단(헤더) 이름 변경
    document.getElementById('chatRoomName').textContent = channelName;

    // 3. 로딩 상태 텍스트 표시
    const msgContainer = document.getElementById('chatMessageContainer');
    msgContainer.innerHTML = `<div style="text-align:center; padding: 48px; color: #7b7394; font-size:13px; font-weight:700;">[ ${channelName} ] 방의 대화 내역을 불러오는 중...</div>`;

    // 🌟 4. REST API 호출 (비동기 Fetch)
    fetch(`/chat/messages/${channelNum}`)
        .then(response => {
            if (!response.ok) throw new Error("API 네트워크 에러");
            return response.json(); // 서버가 보낸 JSON 데이터를 자바스크립트 배열로 변환
        })
        .then(messageList => {
            // 데이터를 성공적으로 가져오면 화면에 말풍선 그리기 함수 호출
            renderMessages(messageList, msgContainer);
        })
        .catch(error => {
            console.error("메시지 로드 에러:", error);
            msgContainer.innerHTML = `<div style="text-align:center; padding: 48px; color: #e46aa1; font-size:13px; font-weight:700;">메시지를 불러오는데 실패했습니다.</div>`;
        });
}

// 🌟 가져온 JSON 데이터를 말풍선 HTML로 만들어서 화면에 붙이는 함수
function renderMessages(messageList, container) {
    container.innerHTML = ''; // 로딩 텍스트 지우기

    if (messageList.length === 0) {
        container.innerHTML = `<div style="text-align:center; padding: 48px; color: #7b7394; font-size:13px; font-weight:700;">아직 대화 내역이 없습니다. 첫 메시지를 보내보세요!</div>`;
        return;
    }

    // 현재 로그인한 내 회원 번호 가져오기 (HTML에 숨겨둔 input 태그에서 추출)
    const loginUserEl = document.getElementById('loginUserId');
    const myUserId = loginUserEl ? parseInt(loginUserEl.value) : -1;

    // 메시지 배열을 반복하면서 HTML 태그(말풍선) 생성
    messageList.forEach(msg => {
        // 내가 보낸 메시지인지 판별 (VO에 적어둔 변수명 userId)
        const isMine = (msg.userId === myUserId);
        const mineClass = isMine ? 'mine' : ''; 
        
        const senderName = msg.userName || '알 수 없음';
        const initial = senderName.charAt(0); // 이름의 첫 글자만 따서 프로필 사진 대용
        
        // 날짜 포맷 (예: Timestamp 형식을 '오전 11:20' 형태로 자르기)
        let timeStr = msg.send_date;
        if (timeStr && timeStr.includes('T')) {
            const d = new Date(timeStr);
            timeStr = d.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
        } else if (!timeStr) {
            timeStr = '';
        }

        // 파일이 첨부된 경우의 UI 조립
        let fileHtml = '';
        if (msg.originName) { // 파일명이 존재하면
            fileHtml = `
                <div class="chat-file-attachment" style="margin-top: 8px;">
                    <span class="file-attach-icon" style="font-size: 20px;"><i class="fa-solid fa-file"></i></span>
                    <div class="file-attach-info">
                        <span class="file-attach-name" style="font-weight: bold; font-size: 13px;">${msg.originName}</span>
                        <span class="file-attach-size" style="font-size: 11px;">${msg.fileSizeStr || '용량 확인불가'}</span>
                    </div>
                </div>
            `;
        }

        // 최종 말풍선 HTML 조립
        const html = `
            <div class="msg-group ${mineClass}">
                <div class="user-avatar">${initial}</div>
                <div class="msg-content-wrapper">
                    <div class="msg-sender-info">${senderName} <span class="msg-time">${timeStr}</span></div>
                    <div class="msg-bubble">
                        ${msg.content ? msg.content : ''}
                        ${fileHtml}
                    </div>
                </div>
            </div>
        `;
        container.insertAdjacentHTML('beforeend', html);
    });
	// 모든 말풍선을 다 그렸으면 스크롤을 맨 아래로 내려줌
	   container.scrollTop = container.scrollHeight;
}

function openCreateChannelModal() {
	    const modal = document.getElementById('createChannelModal');
	    if (modal) {
	        modal.classList.add('active');
	        document.getElementById('newChannelName').focus(); // 모달 열리면 이름칸에 자동 커서 깜빡임
	    }
	}

	// 🌟 모달 창 닫기 및 초기화 함수
	function closeCreateChannelModal() {
	    const modal = document.getElementById('createChannelModal');
	    if (modal) {
	        modal.classList.remove('active');
	        document.getElementById('newChannelName').value = ''; // 입력값 초기화
	        document.getElementById('newChannelDesc').value = ''; // 설명 초기화
	    }
	}

	// 모달 바깥 영역 클릭 시 닫기 및 비동기 폼 전송 이벤트 등록
	document.addEventListener("DOMContentLoaded", () => {
	    // 0. 모달을 body로 이동 (레이아웃 overflow에 잘리지 않도록)
	    const modalOverlay = document.getElementById('createChannelModal');
	    if (modalOverlay && modalOverlay.parentElement !== document.body) {
	        document.body.appendChild(modalOverlay);
	    }
	    // 1. 모달 배경 클릭 시 닫기
	    if (modalOverlay) {
	        modalOverlay.addEventListener('click', function(e) {
	            if (e.target === modalOverlay) {
	                closeCreateChannelModal();
	            }
	        });
	    }

	    // 🌟 2. 폼 전송(Submit) 가로채서 비동기(Fetch)로 처리하기
	    const createForm = document.getElementById('createChannelForm');
	    if (createForm) {
	        createForm.addEventListener('submit', function(e) {
	            e.preventDefault(); // 기본 기능인 '화면 새로고침'을 차단!

	            // FormData를 쓰면 input 데이터와 Thymeleaf가 숨겨둔 CSRF 토큰까지 한 번에 싹 긁어옵니다.
	            const formData = new FormData(this);
	            const channelNameInput = document.getElementById('newChannelName').value;

	            fetch(this.action, { // action="/chat/channel" 주소로 요청
	                method: 'POST',
	                body: formData
	            })
	            .then(response => {
	                if (!response.ok) throw new Error("네트워크 응답 에러");
	                return response.json(); // 서버가 돌려준 생성된 방 정보(JSON)
	            })
				.then(newChannel => {
				    // 1) 성공했으니 모달창 닫기
				    closeCreateChannelModal();

				    const listScroll = document.querySelector('.channel-list-scroll');

				    // "참여 중인 대화방이 없습니다" 메시지가 있다면 제거
				    const emptyMsg = listScroll.querySelector('div[style*="padding: 20px"]');
				    if (emptyMsg) emptyMsg.remove();

				    // 새 방 태그 생성
				    const newItem = document.createElement('div');
				    newItem.className = 'channel-item';
				    newItem.setAttribute('onclick', `switchChannel(${newChannel.channel_num}, '${newChannel.channel_name}', this)`);
				    newItem.innerHTML = `
				      <div class="channel-info">
				        <span class="channel-icon">#</span>
				        <span class="channel-name">${newChannel.channel_name}</span>
				      </div>
				    `;

				    // 🌟 2) 서버가 준 category 코드로 알맞은 카테고리 그룹 찾기 (Thymeleaf th:switch와 동일하게 유지)
				    const categoryLabels = { 1: '일반', 2: '프로젝트', 3: '공지사항', 4: '기타' };
				    const catCode = Number(newChannel.category);

				    let group = listScroll.querySelector(`.category-group[data-category="${catCode}"]`);

				    if (!group) {
				        // 이 카테고리의 첫 방이면 그룹(제목 포함)을 새로 생성
				        group = document.createElement('div');
				        group.className = 'category-group';
				        group.setAttribute('data-category', catCode);

				        const title = document.createElement('div');
				        title.className = 'list-section-title';
				        title.textContent = categoryLabels[catCode] || '기타';
				        group.appendChild(title);

				        // 카테고리 코드 오름차순(1,2,3,4) 위치에 맞춰 삽입
				        const existingGroups = Array.from(listScroll.querySelectorAll('.category-group'));
				        const nextGroup = existingGroups.find(g => Number(g.dataset.category) > catCode);
				        if (nextGroup) {
				            listScroll.insertBefore(group, nextGroup);
				        } else {
				            listScroll.appendChild(group);
				        }
				    }

				    // 3) 찾은(혹은 새로 만든) 그룹 안에 새 방 추가
				    group.appendChild(newItem);

				    // 4) 방금 만든 방을 사용자가 직접 클릭한 것처럼 자동 클릭 이벤트 발생!
				    newItem.click();
				})
	            .catch(error => {
	                console.error("방 생성 에러:", error);
	                alert("채팅방 생성에 실패했습니다.");
	            });
	        });
	    }
	});