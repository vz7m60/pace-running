# PACE 러닝 Android 배포 안내문

아래 문구를 테스트 사용자에게 전달할 수 있습니다. 현재 연결된 다운로드는 debug 테스트 APK입니다. 정식 서명본으로 배포할 때는 링크를 GitHub Release의 APK 링크로 교체하세요.

## 공유 문구

안녕하세요. PACE 러닝 Android 테스트 앱을 공유드립니다.

APK 다운로드: https://github.com/vz7m60/pace-running/actions/runs/36970178714
설치 안내: https://github.com/vz7m60/pace-running/blob/main/android-app/INSTALL_KO.md

다운로드 페이지에 GitHub 로그인 후 **Artifacts**에서 `PACE-Android-debug`를 받아 주세요. 이 빌드 ZIP의 SHA-256은 `32bdc5914579b7b7af2e5d463c760f321b1814776363e1e74f058cf0f2233096`입니다. 값이 다르면 설치하지 말고 알려 주세요. ZIP을 풀어 `app-debug.apk`를 설치하면 됩니다.

Android의 **출처를 알 수 없는 앱** 경고는 Play 스토어 밖에서 설치하기 때문에 표시됩니다. 이 경고만으로 악성 앱이라는 뜻은 아니지만, 설치 안전성을 보증하지도 않습니다. 이 파일은 공개 GitHub Actions에서 만든 debug 테스트 빌드이며 Play 스토어 심사본이 아닙니다. 출처나 SHA-256이 다르거나 추가로 접근성·기기 관리자·SMS·연락처 권한을 요구하면 설치하지 마세요. 진행할 경우 APK를 연 앱에만 **이 출처의 앱 설치 허용**을 켜고, 설치 후 다시 끄세요. Play Protect 전체를 끌 필요는 없습니다.

PACE 웹앱과 GPS 지도를 열 때 인터넷 연결이 필요하고, 위치 권한을 허용해야 거리와 경로를 기록할 수 있습니다. 앱이 요청하는 권한은 인터넷, 대략적/정확한 위치, 진동, 화면 켜짐입니다. 카메라와 사진은 Android 시스템 카메라/사진 선택 화면을 통해 엽니다. 기록과 사진은 설치한 기기의 앱 저장소에 보관됩니다. 앱을 삭제하면 앱 안의 저장 데이터도 삭제될 수 있습니다.

질문이나 오류를 보내실 때는 휴대폰 모델, Android 버전, 발생 단계를 함께 알려주세요. 비밀번호나 서명 키는 보내지 마세요.

### ZIP 검사 (Windows)

다운로드한 ZIP 파일의 SHA-256 확인 명령:

```powershell
Get-FileHash "$env:USERPROFILE\Downloads\PACE-Android-debug.zip" -Algorithm SHA256
```

출력된 `Hash`가 위의 빌드 #4 값과 다르면 설치하지 마세요.

## 정식 배포 전 확인

- 저장소 Settings → Secrets and variables → Actions에 `ANDROID_KEYSTORE_BASE64`, `ANDROID_STORE_PASSWORD`, `ANDROID_KEY_ALIAS`를 등록합니다.
- 키 저장소 파일은 저장소나 채팅에 올리지 말고 안전하게 백업합니다.
- Actions의 **Android Release APK** 워크플로를 실행하고 버전명과 이전보다 큰 versionCode를 입력합니다.
- 생성된 draft Release의 서명 APK를 실제 Android 기기에 설치해 확인한 뒤 Release를 게시합니다.
- debug APK를 설치한 테스트 사용자는 서명이 다르므로 정식 버전 설치 전에 debug 앱을 제거해야 할 수 있습니다. 제거 시 앱에만 저장된 러닝 기록이 삭제될 수 있으니 먼저 백업하도록 안내합니다.
