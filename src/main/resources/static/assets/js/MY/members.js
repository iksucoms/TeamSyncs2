/* CSRF 토큰 (jQuery ajax 공통 헤더) */
var csrfToken  = $('meta[name="csrf-token"]').attr('content');
var csrfHeader = $('meta[name="csrf-header"]').attr('content');
$(document).ajaxSend(function(e, xhr) {
    if (csrfHeader) xhr.setRequestHeader(csrfHeader, csrfToken);
});

/* ====================================================
   토스트
==================================================== */
function toast(msg){
    var $t = $('#_toast');
    if ($t.length === 0) {
        $t = $('<div id="_toast"></div>').css({
            position:'fixed', bottom:'28px', right:'28px', background:'#171a2b', color:'#fff',
            borderRadius:'18px', padding:'14px 18px', fontSize:'13px', fontWeight:900,
            boxShadow:'0 16px 44px rgba(73,47,133,.12)', zIndex:9999, display:'none'
        }).appendTo('body');
    }
    $t.text(msg).stop(true,true).show();
    clearTimeout($t.data('t'));
    $t.data('t', setTimeout(function(){ $t.hide(); }, 2600));
}

function errorMessage(code){
    var map = {
        'NO_PERMISSION': '권한이 없습니다.',
        'CANNOT_CHANGE_SELF': '본인의 역할은 변경할 수 없습니다.',
        'CANNOT_CHANGE_LEADER': '팀장의 역할은 변경할 수 없습니다.',
        'CANNOT_KICK_SELF': '본인을 강퇴할 수 없습니다.',
        'CANNOT_DELEGATE_SELF': '본인에게는 위임할 수 없습니다.',
        'LEADER_CANNOT_EXIT': '팀원이 남아있어 직접 탈퇴할 수 없습니다. 먼저 팀장 위임을 진행해주세요.',
        'NOT_FOUND': '대상을 찾을 수 없습니다.',
        'INVALID_ROLE': '유효하지 않은 역할입니다.'
    };
    return map[code] || ('처리 중 오류가 발생했습니다. (' + code + ')');
}

/* ====================================================
   필터 (소속중만 조회하므로 단순 표시 토글)
==================================================== */
function setFilter(f){
    $('#f_all, #f_active').removeClass('active');
    $('#f_' + f).addClass('active');
    if (f === 'all') {
        $('.memberCard').show();
    } else {
        $('.memberCard').each(function(){
            $(this).toggle($(this).data('join') === 1);
        });
    }
}

/* ====================================================
   모달 공통
==================================================== */
function openModal(title, html){
    $('#modalTitle').text(title);
    $('#modalBody').html(html);
    $('#modalLayer').addClass('active');
}
function closeModal(){ $('#modalLayer').removeClass('active'); }

/* ====================================================
   역할 변경
==================================================== */
function openRoleModal(targetUserNum){
    var $card = $('.memberCard[data-usernum="' + targetUserNum + '"]');
    var name = $card.data('name');
    var role = $card.data('role');

    openModal('역할 변경',
        '<div class="field"><label>' + name + '님의 변경할 역할</label>' +
        '<select id="newRole">' +
        '<option value="1"' + (role === 1 ? ' selected' : '') + '>팀원 (MEMBER)</option>' +
        '<option value="2"' + (role === 2 ? ' selected' : '') + '>매니저 (MANAGER)</option>' +
        '</select></div>' +
        '<p style="font-size:12px;color:var(--muted);background:#f0eeff;border-radius:12px;padding:10px 12px;line-height:1.6">' +
        '※ 팀장 역할은 <b>팀장 위임</b> 기능을 통해서만 변경할 수 있습니다.</p>' +
        '<div class="actions">' +
        '<button class="btn" onclick="closeModal()">취소</button>' +
        '<button class="btn primary" onclick="changeRole(' + targetUserNum + ')">변경</button>' +
        '</div>'
    );
}
function changeRole(targetUserNum){
    var newRole = Number($('#newRole').val());
    $.post('/team/members/role', { targetUserNum: targetUserNum, newRole: newRole })
        .done(function(result){
            if (result === 'OK') {
                toast('역할이 변경되었습니다.');
                closeModal();
                location.reload();
            } else {
                toast(errorMessage(result));
            }
        })
        .fail(function(){ toast('요청 처리 중 오류가 발생했습니다.'); });
}

/* ====================================================
   팀장 위임
==================================================== */
function openDelegateModal(targetUserNum){
    var $card = $('.memberCard[data-usernum="' + targetUserNum + '"]');
    var name = $card.data('name');

    openModal('팀장 위임',
        '<p style="font-size:14px;line-height:1.7;margin:0 0 14px">' +
        '<b>' + name + '</b>님에게 팀장 권한을 위임합니다.<br>위임 후 나는 <b>팀원</b>으로 역할이 변경됩니다.</p>' +
        '<div style="background:var(--orangeS);border-radius:13px;padding:12px 14px;font-size:13px;color:#c46121;margin-bottom:14px;line-height:1.6">' +
        '⚠️ 팀장 위임은 <b>되돌릴 수 없습니다.</b> 신중히 결정해주세요.</div>' +
        '<div class="actions">' +
        '<button class="btn" onclick="closeModal()">취소</button>' +
        '<button class="btn orange" onclick="delegateLeader(' + targetUserNum + ')">위임 확정</button>' +
        '</div>'
    );
}
function delegateLeader(targetUserNum){
    $.post('/team/members/delegate', { targetUserNum: targetUserNum })
        .done(function(result){
            if (result === 'OK') {
                toast('팀장 위임이 완료되었습니다.');
                closeModal();
                location.reload();
            } else {
                toast(errorMessage(result));
            }
        })
        .fail(function(){ toast('요청 처리 중 오류가 발생했습니다.'); });
}

/* ====================================================
   강퇴
==================================================== */
function kickMember(targetUserNum, name){
    if (!confirm('"' + name + '"님을 강퇴하시겠습니까?\n강퇴된 팀원은 팀에 재참여할 수 없습니다(재초대 시 재가입 가능).')) return;
    $.post('/team/members/kick', { targetUserNum: targetUserNum })
        .done(function(result){
            if (result === 'OK') {
                toast(name + '님이 강퇴되었습니다.');
                location.reload();
            } else {
                toast(errorMessage(result));
            }
        })
        .fail(function(){ toast('요청 처리 중 오류가 발생했습니다.'); });
}

/* ====================================================
   팀 탈퇴
==================================================== */
function leaveTeam(){
    if (!confirm('팀을 탈퇴하시겠습니까?\n팀장은 팀원이 남아있으면 먼저 팀장 위임을 진행해야 합니다.')) return;
    $.post('/team/members/exit')
        .done(function(result){
            if (result === 'OK') {
                toast('팀에서 탈퇴했습니다.');
                location.href = '/main/home';
            } else {
                toast(errorMessage(result));
            }
        })
        .fail(function(){ toast('요청 처리 중 오류가 발생했습니다.'); });
}
