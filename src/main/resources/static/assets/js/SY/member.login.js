$(function () {
	const csrfToken = $('meta[name="_csrf"]').attr('content');
	const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

	if (csrfToken && csrfHeader) {
		$.ajaxSetup({
			beforeSend: function(xhr) {
				xhr.setRequestHeader(csrfHeader, csrfToken);
			}
		});
	}
	
  const EMAIL_BUTTON_CHECK = '중복확인';
  const EMAIL_BUTTON_SEND = '이메일인증';

  let emailChecked = false;
  let emailCodeSent = false;
  let emailVerified = false;
  let checkedEmail = '';
  let verifiedEmail = '';
  let timerId = null;
  let remainSeconds = 0;

  $('.sy-tab').on('click', function () {
    const target = $(this).data('target');

    $('.sy-tab').removeClass('sy-on');
    $('.sy-pane').removeClass('sy-on');

    $(this).addClass('sy-on');
    $('#' + target).addClass('sy-on');
  });

  $('#sy-btn-google').on('click', function () {
    alert('추후 구현 예정입니다.');
  });

  $('.sy-term-view').on('click', function () {
    alert('약관 상세 내용은 추후 제공 예정입니다.');
  });

  $('#sy-agree-all').on('change', function () {
    const checked = $(this).is(':checked');
    $('.sy-required-term, .sy-optional-term').prop('checked', checked);
  });

  $('.sy-required-term, .sy-optional-term').on('change', function () {
    const total = $('.sy-required-term, .sy-optional-term').length;
    const checked = $('.sy-required-term:checked, .sy-optional-term:checked').length;

    $('#sy-agree-all').prop('checked', total === checked);
  });

  $('#sy-signup-email').on('input', function () {
    resetEmailVerification();
  });

  $('#sy-btn-check-email').on('click', function () {
    const email = $('#sy-signup-email').val().trim();

    if (!emailChecked) {
      checkEmailDuplicate(email);
      return;
    }

    if (checkedEmail !== email) {
      resetEmailVerification();
      checkEmailDuplicate(email);
      return;
    }

    sendEmailCode(email);
  });

  $('#sy-btn-verify-email').on('click', function () {
    const email = $('#sy-signup-email').val().trim();
    const code = $('#sy-email-code').val().trim();

    if (email.length === 0) {
      alert('이메일을 입력해주세요.');
      $('#sy-signup-email').focus();
      return;
    }

    if (!emailCodeSent) {
      alert('이메일 인증코드를 먼저 발송해주세요.');
      $('#sy-btn-check-email').focus();
      return;
    }

    if (code.length === 0) {
      alert('인증코드를 입력해주세요.');
      $('#sy-email-code').focus();
      return;
    }

    if (remainSeconds <= 0) {
      alert('인증 시간이 만료되었습니다. 인증코드를 다시 발송해주세요.');
      expireEmailVerification();
      return;
    }

    $.ajax({
      url: '/users/verifyEmailCode',
      type: 'POST',
      data: {
        email: email,
        code: code
      },
      dataType: 'json',
      success: function (res) {
        if (res.result !== 'success') {
          setCodeMessage(res.message || '이메일 인증에 실패했습니다.', 'error');

          if (res.result === 'expired') {
            expireEmailVerification();
          }

          return;
        }

        emailVerified = true;
        verifiedEmail = email;

        setCodeMessage('이메일 인증이 완료되었습니다. 남은 시간 안에 회원가입을 완료해주세요.', 'ok');
        setEmailMessage('이메일 인증이 완료되었습니다.', 'ok');

        $('#sy-email-code').prop('readonly', true);
        $('#sy-btn-verify-email').prop('disabled', true);
      },
      error: function () {
        setCodeMessage('이메일 인증 확인 중 오류가 발생했습니다.', 'error');
      }
    });
  });

  $('#sy-signup-form').on('submit', function (event) {
    const userName = $('#sy-signup-user-name').val().trim();
    const email = $('#sy-signup-email').val().trim();
    const passwd = $('#sy-signup-passwd').val().trim();
    const confirmPasswd = $('#sy-signup-confirm-passwd').val().trim();

    if (userName.length === 0) {
      alert('닉네임을 입력해주세요.');
      $('#sy-signup-user-name').focus();
      event.preventDefault();
      return;
    }

    if (email.length === 0) {
      alert('이메일을 입력해주세요.');
      $('#sy-signup-email').focus();
      event.preventDefault();
      return;
    }

    if (!isEmailFormat(email)) {
      alert('이메일 형식에 맞게 입력해주세요.');
      $('#sy-signup-email').focus();
      event.preventDefault();
      return;
    }

    if (!emailChecked || checkedEmail !== email) {
      alert('이메일 중복 확인을 해주세요.');
      $('#sy-btn-check-email').focus();
      event.preventDefault();
      return;
    }

    if (!emailCodeSent) {
      alert('이메일 인증코드를 발송해주세요.');
      $('#sy-btn-check-email').focus();
      event.preventDefault();
      return;
    }

    if (!emailVerified || verifiedEmail !== email) {
      alert('이메일 인증을 완료해주세요.');
      $('#sy-email-code').focus();
      event.preventDefault();
      return;
    }

    if (remainSeconds <= 0) {
      alert('이메일 인증 시간이 만료되었습니다. 다시 인증해주세요.');
      expireEmailVerification();
      event.preventDefault();
      return;
    }

    if (passwd.length === 0) {
      alert('비밀번호를 입력해주세요.');
      $('#sy-signup-passwd').focus();
      event.preventDefault();
      return;
    }

    if (passwd.length < 8 || passwd.length > 20) {
      alert('비밀번호는 영문, 숫자 8~20자로 입력해주세요.');
      $('#sy-signup-passwd').focus();
      event.preventDefault();
      return;
    }

    if (!/^[A-Za-z0-9]+$/.test(passwd)) {
      alert('비밀번호는 영문과 숫자만 사용할 수 있습니다.');
      $('#sy-signup-passwd').focus();
      event.preventDefault();
      return;
    }

    if (passwd !== confirmPasswd) {
      alert('비밀번호와 비밀번호 확인이 일치하지 않습니다.');
      $('#sy-signup-confirm-passwd').focus();
      event.preventDefault();
      return;
    }

    if ($('.sy-required-term:checked').length !== $('.sy-required-term').length) {
      alert('필수 약관에 동의해주세요.');
      event.preventDefault();
      return;
    }
  });

  function checkEmailDuplicate(email) {
    resetEmailVerification();

    if (email.length === 0) {
      setEmailMessage('이메일을 입력해주세요.', 'error');
      $('#sy-signup-email').focus();
      return;
    }

    if (!isEmailFormat(email)) {
      setEmailMessage('이메일 형식에 맞게 입력해주세요.', 'error');
      $('#sy-signup-email').focus();
      return;
    }

    $.ajax({
      url: '/users/checkEmail',
      type: 'GET',
      data: { email: email },
      dataType: 'json',
      success: function (res) {
        if (res.duplicated) {
          setEmailMessage('이미 가입한 이메일입니다.', 'error');
          return;
        }

        emailChecked = true;
        checkedEmail = email;

        $('#sy-btn-check-email').text(EMAIL_BUTTON_SEND);
        setEmailMessage('사용 가능한 이메일입니다. 이메일 인증을 진행해주세요.', 'ok');
      },
      error: function () {
        setEmailMessage('이메일 중복 확인 중 오류가 발생했습니다.', 'error');
      }
    });
  }

  function sendEmailCode(email) {
    if (email.length === 0) {
      setEmailMessage('이메일을 입력해주세요.', 'error');
      $('#sy-signup-email').focus();
      return;
    }

    if (!emailChecked || checkedEmail !== email) {
      alert('이메일 중복 확인을 먼저 진행해주세요.');
      resetEmailVerification();
      return;
    }

    emailCodeSent = false;
    emailVerified = false;
    verifiedEmail = '';

    stopEmailTimer();

    $('#sy-btn-check-email').prop('disabled', true);

    $.ajax({
      url: '/users/sendEmailCode',
      type: 'POST',
      data: { email: email },
      dataType: 'json',
      success: function (res) {
        $('#sy-btn-check-email').prop('disabled', false);

        if (res.result === 'duplicated') {
          resetEmailVerification();
          setEmailMessage('이미 가입한 이메일입니다.', 'error');
          return;
        }

        if (res.result !== 'success') {
          setEmailMessage(res.message || '이메일 인증코드 발송에 실패했습니다.', 'error');
          return;
        }

        emailCodeSent = true;
        emailVerified = false;
        verifiedEmail = '';

        setEmailMessage('인증코드를 발송했습니다.', 'ok');
        setCodeMessage('메일로 전송된 인증코드를 입력해주세요.', '');

        $('#sy-email-verification-box').addClass('sy-show');
        $('#sy-email-code').val('').prop('readonly', false).focus();
        $('#sy-btn-verify-email').prop('disabled', false);

        startEmailTimer(res.expireSeconds || 300);
      },
      error: function () {
        $('#sy-btn-check-email').prop('disabled', false);
        setEmailMessage('이메일 인증코드 발송 중 오류가 발생했습니다.', 'error');
      }
    });
  }

  function resetEmailVerification() {
    emailChecked = false;
    emailCodeSent = false;
    emailVerified = false;
    checkedEmail = '';
    verifiedEmail = '';

    stopEmailTimer();

    $('#sy-btn-check-email')
      .text(EMAIL_BUTTON_CHECK)
      .prop('disabled', false);

    setEmailMessage('', '');
    setCodeMessage('메일로 전송된 인증코드를 입력해주세요.', '');

    $('#sy-email-verification-box').removeClass('sy-show');
    $('#sy-email-code').val('').prop('readonly', false);
    $('#sy-btn-verify-email').prop('disabled', false);
    $('#sy-email-timer').text('05:00');
  }

  function expireEmailVerification() {
    emailCodeSent = false;
    emailVerified = false;
    verifiedEmail = '';

    stopEmailTimer();

    $('#sy-btn-check-email')
      .text(EMAIL_BUTTON_SEND)
      .prop('disabled', false);

    $('#sy-email-code').prop('readonly', false);
    $('#sy-btn-verify-email').prop('disabled', false);
    $('#sy-email-timer').text('00:00');

    setCodeMessage('인증 시간이 만료되었습니다. 이메일인증 버튼을 다시 눌러주세요.', 'error');
    setEmailMessage('인증 시간이 만료되었습니다. 이메일 인증을 다시 진행해주세요.', 'error');
  }

  function startEmailTimer(seconds) {
    stopEmailTimer();

    remainSeconds = seconds;
    renderEmailTimer();

    timerId = setInterval(function () {
      remainSeconds -= 1;
      renderEmailTimer();

      if (remainSeconds <= 0) {
        expireEmailVerification();
      }
    }, 1000);
  }

  function stopEmailTimer() {
    if (timerId !== null) {
      clearInterval(timerId);
      timerId = null;
    }
  }

  function renderEmailTimer() {
    const minutes = String(Math.floor(remainSeconds / 60)).padStart(2, '0');
    const seconds = String(remainSeconds % 60).padStart(2, '0');
    $('#sy-email-timer').text(minutes + ':' + seconds);
  }

  function setEmailMessage(message, type) {
    const $message = $('#sy-email-check-msg');

    $message
      .removeClass('sy-ok-text sy-err-text')
      .text(message);

    if (type === 'ok') {
      $message.addClass('sy-ok-text');
    }

    if (type === 'error') {
      $message.addClass('sy-err-text');
    }
  }

  function setCodeMessage(message, type) {
    const $message = $('#sy-email-code-msg');

    $message
      .removeClass('sy-ok-text sy-err-text')
      .text(message);

    if (type === 'ok') {
      $message.addClass('sy-ok-text');
    }

    if (type === 'error') {
      $message.addClass('sy-err-text');
    }
  }

  function isEmailFormat(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }
});