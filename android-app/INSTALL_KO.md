# PACE 러닝 Android 설치 안내

## APK 받기

1. [Android Debug APK 빌드 #4](https://github.com/vz7m60/pace-running/actions/runs/36970178714)을 엽니다.
2. GitHub 계정에 로그인하고 실행 결과가 `Success`인지 확인합니다.
3. 페이지 아래 **Artifacts**에서 `PACE-Android-debug`를 내려받습니다. 이 파일은 ZIP입니다.
4. ZIP의 SHA-256을 확인합니다. Windows PowerShell에서 다운로드 폴더의 파일에 아래 명령을 실행하고 결과가 안내된 값과 같은지 비교합니다.

```powershell
Get-FileHash "$env:USERPROFILE\Downloads\PACE-Android-debug.zip" -Algorithm SHA256
```

빌드 #4의 artifact SHA-256: `32bdc5914579b7b7af2e5d463c760f321b1814776363e1e74f058cf0f2233096`

5. 휴대폰의 **내 파일** 앱에서 ZIP을 풀고 `app-debug.apk`를 찾습니다.

## Galaxy 휴대폰에 설치하기

1. `app-debug.apk`를 누릅니다.
2. Android가 출처를 알 수 없는 앱 설치를 막으면, 안내에서 **설정**을 엽니다.
3. APK를 연 앱(예: **내 파일** 또는 **Chrome**)에 대해 **이 출처의 앱 설치 허용**을 켭니다.
4. 설치 화면으로 돌아와 설치를 진행합니다.
5. 설치가 끝나면 같은 설정 화면에서 **이 출처의 앱 설치 허용**을 다시 끄는 것을 권장합니다.

기기 제조사와 One UI 버전에 따라 설정 메뉴 이름이 다를 수 있습니다. 설정에서 **출처를 알 수 없는 앱 설치** 또는 **이 출처의 앱 설치 허용**을 검색해도 됩니다.

## 보안 경고를 무시하면

Android 경고는 APK가 Play 스토어 밖에서 설치되어 Play 스토어의 사전 심사·배포 경로를 거치지 않았다는 뜻입니다. 이 경고만으로 악성 앱이라고 판정된 것은 아니지만, 설치를 계속해도 안전성이 인증되는 것은 아닙니다. 출처가 불분명하거나 위 SHA-256이 다르면 설치하지 마세요.

빌드 #4 파일은 GitHub 저장소의 공개 Actions workflow가 만든 **debug 테스트용 APK**이며 Play 스토어 심사본이 아닙니다. APK가 바뀌어 악성 코드가 들어 있다면 설치 후 허용한 기능을 악용할 수 있습니다. 이 앱이 실제 요청하는 Android 권한은 인터넷, 대략적/정확한 위치, 진동, 화면 켜짐입니다. 카메라와 갤러리는 사용자가 사진 촬영 버튼을 눌렀을 때 Android 시스템 앱을 통해 엽니다. 앱이 접근성, 기기 관리자, SMS, 연락처 권한을 요구하면 설치를 취소하세요.

설치를 진행한다면 **이 출처의 앱 설치 허용**은 APK를 연 앱(예: 내 파일)에만 켜고 설치 후 다시 끄세요. Play Protect 전체를 끄거나 다른 보안 경고를 무시하지 마세요. 확신이 서지 않으면 설치하지 않는 것이 맞습니다.

## 직접 배포용 서명 APK 준비

디버그 APK는 debug 키로 서명되어 있어, 같은 앱 ID라도 다른 서명 키로 만든 release APK로 바로 업데이트할 수 없습니다. 이미 debug APK를 설치했다면 정식 release 설치 전에 제거해야 할 수 있으며, 제거하면 앱 안에만 저장된 기록·사진·목표값이 함께 삭제됩니다. 중요한 기록은 먼저 별도 보관하세요.

### 1. 서명 키 만들기

개인 PC에서 Android Studio의 **Terminal**을 열고 아래 명령을 실행합니다. 비밀번호는 keytool이 물어볼 때 터미널에 직접 입력하고 채팅이나 저장소에는 보내지 마세요. Key password 질문에서 Enter를 누르면 keystore 비밀번호와 같은 값을 사용합니다.

```powershell
keytool -genkeypair -v -keystore pace-upload-key.jks -alias pace-upload -keyalg RSA -keysize 4096 -validity 10000
```

생성한 `.jks` 파일을 암호화된 안전한 위치에 백업하세요. 키를 잃거나 비밀번호를 잊으면 같은 서명으로 업데이트를 만들 수 없습니다. `.jks` 파일은 GitHub 저장소에 올리지 마세요. 저장소의 `.gitignore`가 해당 확장자를 제외합니다.

### 2. GitHub Secrets 등록

저장소에서 **Settings → Secrets and variables → Actions → New repository secret**으로 이동해 다음 세 항목을 등록합니다.

- `ANDROID_KEYSTORE_BASE64`: PowerShell에서 아래 명령을 실행한 결과 전체
- `ANDROID_STORE_PASSWORD`: keystore 비밀번호
- `ANDROID_KEY_ALIAS`: `pace-upload`

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\secure\pace-upload-key.jks")) | Set-Clipboard
```

예시 경로 `C:\secure\pace-upload-key.jks`는 실제 키 파일 위치로 바꿉니다. base64 결과와 비밀번호를 채팅에 붙여 넣지 말고 GitHub Secrets 입력란에 직접 붙여 넣으세요.

### 3. 초안 release 빌드

저장소 **Actions → Android Release APK → Run workflow**에서 버전 이름과 버전 코드를 입력합니다. 워크플로는 서명 APK artifact를 만들고 GitHub Release를 **초안(draft)** 상태로 생성합니다. APK를 설치해 확인한 뒤 Release 페이지에서 직접 게시해야 다른 사람이 내려받을 수 있습니다.

업데이트 때마다 `versionCode`를 이전보다 큰 값으로 지정해야 합니다. 모든 업데이트는 최초 release와 같은 keystore와 alias를 사용해야 합니다. 서명 키를 바꾸면 Android는 기존 앱의 업데이트로 인정하지 않습니다.

## 설치 후 알아둘 점

- 인터넷 연결이 필요합니다. APK 안에 웹앱 전체가 들어 있는 오프라인 앱은 아닙니다.
- 러닝 기록, 사진, 목표 및 입력한 Samsung Health 값은 이 앱의 WebView 저장소에 보관됩니다. Chrome의 PACE 기록과는 별도이며 앱 데이터를 지우거나 앱을 삭제하면 기록도 삭제될 수 있습니다.
- 위치 권한을 거부하면 GPS 거리와 경로를 기록할 수 없습니다. 필요한 권한만 허용하고, 러닝 중 안전에 유의하세요.
- 이 APK는 디버그 서명이라 Play 스토어 자동 업데이트를 받지 않습니다. 새 버전은 Actions에서 다시 받아 설치해야 합니다.
