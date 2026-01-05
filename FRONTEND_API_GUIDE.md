# Frontend API 연동 가이드

## 📌 왜 수정해야 하나요?

**현재 상황:**
- Frontend는 모든 데이터를 브라우저 `localStorage`에만 저장
- 서버(백엔드)와 통신하지 않음
- 새로고침하면 데이터가 사라지거나 다른 사용자와 공유 불가

**수정 후:**
- 서버 API를 통해 실제 데이터베이스에 저장
- 다른 사용자와 데이터 공유 가능
- 로그인 토큰(JWT) 기반 인증으로 보안 강화

---

## 🔧 수정해야 할 파일 목록

### Member-Service 관련 (6개)
1. `login.html` - 로그인
2. `signup.html` - 회원가입
3. `find_id.html` - 아이디 찾기
4. `find_password.html` - 비밀번호 찾기
5. `mypage/index.html` - 마이페이지
6. `mypage/edit.html` - 회원정보 수정

### Board-Service 관련 (4개)
7. `bbs/notice.html` - 공지사항 목록
8. `bbs/notice_detail.html` - 공지사항 상세
9. `admin/notice.html` - 관리자 공지사항 관리
10. `admin/inquiry.html` - 관리자 문의 관리

---

## 📝 수정 방법

### 1. login.html (로그인)

**경로:** `frontend-service/src/main/resources/templates/login.html`

#### 기존 코드 (350-394번 줄):
```javascript
// localStorage에서 users 배열 확인
var users = JSON.parse(localStorage.getItem('users') || '[]');

// users 배열에서 사용자 찾기
var foundUser = users.find(function(user) {
    return (user.username && user.username.toLowerCase() === loginId.toLowerCase()) ||
           (user.email && user.email.toLowerCase() === loginId.toLowerCase());
});

// 비밀번호 확인
if (foundUser.password === password) {
    // localStorage에 로그인 상태 저장
    localStorage.setItem('isLoggedIn', 'true');
    localStorage.setItem('userName', userName);
    localStorage.setItem('userRole', userRole);

    window.location.href = 'index.html';
}
```

#### 수정 후:
```javascript
// 서버에 로그인 요청
fetch('http://localhost:8000/api/auth/login', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        userId: loginId,
        password: password
    })
})
.then(response => {
    if (!response.ok) {
        throw new Error('로그인 실패');
    }
    return response.json();
})
.then(data => {
    // 서버에서 받은 토큰 저장
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('userName', data.name);
    localStorage.setItem('userRole', data.role);

    // 역할에 따라 리다이렉트
    if (data.role === 'user') {
        window.location.href = 'index.html';
    } else if (data.role === 'admin') {
        window.location.href = 'admin/user.html';
    } else if (data.role === 'owner') {
        window.location.href = 'owner/inventory.html';
    }
})
.catch(error => {
    showCustomModal('알림', '아이디 또는 비밀번호가 일치하지 않습니다.');
});
```

---

### 2. signup.html (회원가입)

**경로:** `frontend-service/src/main/resources/templates/signup.html`

#### 기존 코드:
```javascript
var newUser = {
    id: Date.now().toString(),
    username: id,
    password: password,
    name: name,
    email: email,
    role: 'user',
    createdAt: new Date().toISOString()
};
users.push(newUser);
localStorage.setItem('users', JSON.stringify(users));
```

#### 수정 후:
```javascript
fetch('http://localhost:8000/api/auth/register', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        userId: id,
        password: password,
        name: name,
        email: email,
        birthDate: birthDate || '2000-01-01',
        phone: phone || ''
    })
})
.then(response => {
    if (!response.ok) {
        throw new Error('회원가입 실패');
    }
    return response.json();
})
.then(data => {
    showCustomModal('회원가입 완료', '회원가입이 완료되었습니다.', function() {
        window.location.href = 'login.html';
    });
})
.catch(error => {
    showCustomModal('알림', '회원가입에 실패했습니다.');
});
```

---

### 3. bbs/notice.html (공지사항 목록)

**경로:** `frontend-service/src/main/resources/templates/bbs/notice.html`

#### 기존 코드:
```javascript
// 정적 데이터 또는 localStorage 사용
```

#### 수정 후:
```javascript
// 페이지 로드 시 공지사항 목록 가져오기
function loadNotices(page = 1, keyword = '') {
    let url = `http://localhost:8000/api/notices?page=${page}&limit=10`;
    if (keyword) {
        url += `&keyword=${encodeURIComponent(keyword)}`;
    }

    fetch(url)
        .then(response => response.json())
        .then(data => {
            // data.notices: 공지사항 배열
            // data.currentPage: 현재 페이지
            // data.totalPages: 전체 페이지 수

            displayNotices(data.notices);
            displayPagination(data.currentPage, data.totalPages);
        })
        .catch(error => {
            console.error('공지사항 로드 실패:', error);
        });
}

// 페이지 로드 시 실행
$(document).ready(function() {
    loadNotices();

    // 검색 버튼 클릭 시
    $('#searchBtn').click(function() {
        const keyword = $('#bbs_search').val();
        loadNotices(1, keyword);
    });
});
```

---

### 4. bbs/notice_detail.html (공지사항 상세)

**경로:** `frontend-service/src/main/resources/templates/bbs/notice_detail.html`

#### 기존 코드:
```javascript
const urlParams = new URLSearchParams(window.location.search);
const noticeId = urlParams.get('noticeId');
const title = urlParams.get('title') || '';
```

#### 수정 후:
```javascript
const urlParams = new URLSearchParams(window.location.search);
const noticeId = urlParams.get('noticeId');

fetch(`http://localhost:8000/api/notices/${noticeId}`)
    .then(response => response.json())
    .then(notice => {
        // 공지사항 정보 표시
        $('#noticeTitle').text(notice.title);
        $('#noticeContent').html(notice.content);
        $('#noticeAuthor').text(notice.author);
        $('#noticeDate').text(notice.createdAt);
        $('#noticeViews').text(notice.viewCount);
    })
    .catch(error => {
        console.error('공지사항 로드 실패:', error);
        alert('공지사항을 불러올 수 없습니다.');
    });
```

---

### 5. admin/notice.html (관리자 공지사항 관리)

**경로:** `frontend-service/src/main/resources/templates/admin/notice.html`

#### 공지사항 작성:
```javascript
$('#noticeForm').submit(function(e) {
    e.preventDefault();

    const token = localStorage.getItem('accessToken');

    fetch('http://localhost:8000/api/notices/admin', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            title: $('#noticeTitle').val(),
            content: $('#noticeContent').val(),
            isImportant: $('#noticeImportant').prop('checked')
        })
    })
    .then(response => response.json())
    .then(data => {
        showCustomModal('성공', '공지사항이 등록되었습니다.', function() {
            location.reload();
        });
    })
    .catch(error => {
        showCustomModal('오류', '공지사항 등록에 실패했습니다.');
    });
});
```

#### 공지사항 수정:
```javascript
function updateNotice(noticeId) {
    const token = localStorage.getItem('accessToken');

    fetch(`http://localhost:8000/api/notices/admin/${noticeId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            title: $('#editTitle').val(),
            content: $('#editContent').val(),
            isImportant: $('#editImportant').prop('checked')
        })
    })
    .then(response => response.json())
    .then(data => {
        showCustomModal('성공', '공지사항이 수정되었습니다.', function() {
            location.reload();
        });
    })
    .catch(error => {
        showCustomModal('오류', '공지사항 수정에 실패했습니다.');
    });
}
```

#### 공지사항 삭제:
```javascript
function deleteNotice(noticeId) {
    const token = localStorage.getItem('accessToken');

    fetch(`http://localhost:8000/api/notices/admin/${noticeId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(data => {
        showCustomModal('성공', '공지사항이 삭제되었습니다.', function() {
            location.reload();
        });
    })
    .catch(error => {
        showCustomModal('오류', '공지사항 삭제에 실패했습니다.');
    });
}
```

---

### 6. admin/inquiry.html (관리자 문의 관리)

**경로:** `frontend-service/src/main/resources/templates/admin/inquiry.html`

#### 문의 목록 로드:
```javascript
function loadInquiries() {
    const token = localStorage.getItem('accessToken');

    fetch('http://localhost:8000/api/boards', {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(inquiries => {
        displayInquiries(inquiries);
    })
    .catch(error => {
        console.error('문의 목록 로드 실패:', error);
    });
}
```

#### 문의 답변:
```javascript
function answerInquiry(inquiryId) {
    const token = localStorage.getItem('accessToken');
    const answer = $('#inquiryAnswer').val();

    fetch(`http://localhost:8000/api/boards/${inquiryId}/answer`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            answer: answer
        })
    })
    .then(response => response.json())
    .then(data => {
        showCustomModal('성공', '답변이 등록되었습니다.', function() {
            location.reload();
        });
    })
    .catch(error => {
        showCustomModal('오류', '답변 등록에 실패했습니다.');
    });
}
```

---

## 🔑 주요 변경사항 요약

### 1. localStorage 사용 → API 호출
```javascript
// 기존
localStorage.getItem('users')

// 수정 후
fetch('http://localhost:8000/api/...')
```

### 2. 인증 토큰 추가
```javascript
headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
}
```

### 3. 응답 데이터 처리
```javascript
fetch(url)
    .then(response => response.json())
    .then(data => {
        // 서버에서 받은 데이터 사용
    })
    .catch(error => {
        // 에러 처리
    });
```

---

## 📌 API 엔드포인트 정리

### Member-Service (포트: 8004, Gateway: 8000)

| 기능 | 메서드 | URL | 인증 필요 |
|------|--------|-----|-----------|
| 로그인 | POST | /api/auth/login | ❌ |
| 회원가입 | POST | /api/auth/register | ❌ |
| 아이디 찾기 | POST | /api/auth/find-id | ❌ |
| 비밀번호 찾기 | POST | /api/auth/find-password | ❌ |
| 내 정보 조회 | GET | /api/member/me | ✅ |
| 회원정보 수정 | PUT | /api/member/update | ✅ |

### Board-Service (포트: 8002, Gateway: 8000)

| 기능 | 메서드 | URL | 인증 필요 |
|------|--------|-----|-----------|
| 공지사항 목록 | GET | /api/notices?page=1&limit=10 | ❌ |
| 공지사항 검색 | GET | /api/notices?keyword=검색어 | ❌ |
| 공지사항 상세 | GET | /api/notices/{id} | ❌ |
| 공지사항 작성 | POST | /api/notices/admin | ✅ (관리자) |
| 공지사항 수정 | PUT | /api/notices/admin/{id} | ✅ (관리자) |
| 공지사항 삭제 | DELETE | /api/notices/admin/{id} | ✅ (관리자) |
| 문의 목록 | GET | /api/boards | ✅ |
| 문의 작성 | POST | /api/boards | ✅ |
| 문의 수정 | PUT | /api/boards/{id} | ✅ |
| 문의 삭제 | DELETE | /api/boards/{id} | ✅ |

---

## ⚠️ 주의사항

1. **모든 API 호출은 `http://localhost:8000`을 기본 URL로 사용** (Gateway 경유)
2. **인증이 필요한 API는 반드시 `Authorization` 헤더에 토큰 포함**
3. **에러 처리 필수** (`.catch()`로 에러 처리)
4. **기존 localStorage 사용 코드는 모두 제거**
5. **토큰은 `localStorage.getItem('accessToken')`으로 가져오기**

---

## 🚀 테스트 방법

1. Gateway 서비스 실행 (포트 8000)
2. Member-Service 실행 (포트 8004)
3. Board-Service 실행 (포트 8002)
4. Frontend-Service 실행
5. 브라우저에서 테스트
   - 회원가입 → 로그인 → 공지사항 조회
   - 관리자 로그인 → 공지사항 작성/수정/삭제
   - 일반 사용자 로그인 → 문의 작성

---

## 📞 문의

수정 중 문제가 발생하면 백엔드 담당자에게 문의하세요.
