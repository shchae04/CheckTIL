# .gitignore에 추가했는데도 git status에 계속 관리되는 파일 처리하기

## 발단

Git을 사용하다 보면 `.gitignore`에 파일이나 폴더를 추가했는데도 `git status` 명령어 실행하면 여전히 그 파일이 추적(tracked) 상태로 보이는 경우. Git이 이미 추적 중인 파일은 `.gitignore`에 넣어도 무시하지 않기 때문입니다.

## 왜?

Git은 한번 추적(track)을 시작한 파일은 `.gitignore`에 넣어도 계속 추적해요. 이건 Git의 설계 의도. 이미 버전 관리 중인 파일을 실수로 무시하게 되는 상황을 막기 위해서

## How to solve

이미 Git이 추적 중인 파일을 더 이상 추적하지 않게 하고 싶다면, 다음과 같이 해보세요:

### 1. 파일은 그대로 두고 Git 추적만 제외하기

```bash
git rm --cached <file_path>
```

여러 파일이나 폴더 한번에 처리
```bash
git rm --cached -r <directory_path>
```

`--cached` 옵션을 쓰면 Git 추적 목록에서만 파일을 빼고, 실제 파일은 그대로 남긴다.

### 2. 변경사항 커밋


```bash
git commit -m "Untracked files"
```

### 3. 변경사항 푸시

```bash
git push
```

## 주의ㅣ

1. `git rm --cached` 실행하면 다음 커밋부터 그 파일은 추적되지 않음. 하지만 이전 커밋 기록에는 남아있음.

2. 비밀번호나 API 키 같은 민감한 정보가 담긴 파일이 이미 원격 저장소에 올라갔다면, 이미 노출됨. 바로 변경 필요.

## 이런 상황을 미리 막으려면?

앞으로는 이런 문제를 안 겪으려면:

1. 프로젝트 시작할 때 `.gitignore` 파일부터 생성
2. 민감한 정보는 환경 변수나 별도 설정 파일로 관리하고, `.gitignore`에 추가.
3. 커밋하기 전  `git status`로 확인.

## 결론

Git에서 이미 추적 중인 파일을 무시하려면 `git rm --cached` 명령어로 Git 추적 목록에서 제외, 변경사항을 커밋. 파일은 그대로 Git 추적에서만 제외.
