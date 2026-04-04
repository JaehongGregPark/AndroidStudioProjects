# Memo Flow Android

Android Studio에서 바로 실행 가능한 네이티브 안드로이드 메모 앱입니다.

## 실행 방법

1. Android Studio에서 `C:\Users\USER\Documents\codex\memo-app` 폴더를 엽니다.
2. Gradle Sync가 끝나면 `app` 실행 구성을 선택합니다.
3. 에뮬레이터 또는 연결된 기기를 선택하고 실행합니다.

## 기능

- 메모 추가
- 메모 수정
- 메모 검색
- 선택 메모 삭제
- 전체 메모 삭제
- 앱 재실행 후에도 데이터 유지

## 저장 방식

- 메모는 `SharedPreferences`에 JSON 형태로 저장됩니다.
- 별도 데이터베이스 없이 바로 실행 가능합니다.
