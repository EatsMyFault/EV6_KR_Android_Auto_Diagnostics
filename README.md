# EV6 유선 Android Auto TBT 진단 v0.1.0

한국 내수형 EV6 표준형 5W `CVEV / KOR / 260902`와 HMG-API `1.0.2`용 테스트입니다. 차량의 Android Auto 프로세스가 받는 안내 상태, 회전, 거리를 HMG-API 로그에 기록합니다. 도로명, 목적지, 좌표, 회전 이미지는 수집하지 않습니다. 이 모드는 계기판이나 HUD에 안내를 보내지 않습니다.

설치 ZIP은 [GitHub Releases](https://github.com/EatsMyFault/EV6_KR_Android_Auto_Diagnostics/releases)에서 받으세요. 실차에서 유선 Android Auto 콜백이 들어오는지는 테스트 로그로 확인합니다.

## USB로 설치하기

1. 주차 상태에서 설치 USB에 `HMGAPI/updates/` 폴더를 만듭니다.
2. `EV6_KR_Android_Auto_Diagnostics_v0.1.0_TEST.zip`을 압축을 풀지 않고 아래 위치에 넣습니다.

   ```text
   USB:/HMGAPI/updates/EV6_KR_Android_Auto_Diagnostics_v0.1.0_TEST.zip
   ```

3. 설치 USB를 차량의 데이터 USB 포트에 꽂고 HMG-API에서 **Check USB updates**를 누릅니다.
4. **Mods**에서 **EV6 KOR Android Auto Diagnostics**를 켠 뒤 **Apply changes**를 누릅니다. 적용 계획에서 재시작이 필요하다고 나오면 화면 안내를 따릅니다.
5. 설치 USB와 캐빈프로 같은 어댑터를 빼고, 같은 데이터 포트에 안드로이드폰을 데이터 케이블로 직접 연결합니다. 차량 화면에 **Android Auto**가 열리는지 확인합니다.

## 로그 남기기

1. 티맵이나 네이버지도에서 경로 안내를 시작하고 첫 회전 안내가 뜰 때까지 기다립니다. 경로 안내를 종료한 뒤 휴대폰 케이블을 뺍니다.
2. HMG-API **Mods → EV6 KOR Android Auto Diagnostics → Log**에서 `AA_DIAG_010` 줄을 캡처합니다. Log 화면이 이미 열려 있었다면 진단 모드를 다시 선택해 최신 로그를 불러옵니다.
3. 캡처에 `GPM_READY`, `AA_NAV_INIT`, `AA_STATUS`, `AA_TURN`, `AA_DISTANCE`, `AA_DISCONNECTED` 중 보이는 줄을 포함합니다. 없던 줄은 `없음`으로 적고, 사용한 내비 앱과 USB 직결 여부를 함께 [GitHub Issues](https://github.com/EatsMyFault/EV6_KR_Android_Auto_Diagnostics/issues)에 올려주세요.

| 로그 | 의미 |
| --- | --- |
| `GPM_READY hooks=7/7` | 진단 후크가 모두 등록됐습니다. 안내 데이터 수신 여부는 아래 항목으로 확인합니다. |
| `GPM_HOOK_FAILED` | 등록하지 못한 후크 이름이 기록됩니다. |
| `AA_SENSOR_INIT` | Android Auto 센서 세션 초기화 결과입니다. |
| `AA_NAV_INIT` | 차량이 협상에 제시한 계기판 안내 형식과 초기화 결과입니다. |
| `AA_STATUS` | `1` 안내 시작, `2` 안내 종료, `3` 재탐색입니다. |
| `AA_TURN`, `AA_DISTANCE` | 다음 회전 코드와 회전까지의 거리입니다. 반복 수신은 일부만 화면에 기록합니다. |
| `AA_DISCONNECTED` | 연결이 종료되면 상태·회전·거리 콜백 수신 합계를 기록합니다. |

`GPM_READY`가 보이지 않으면 휴대폰을 연결해 Android Auto를 연 상태에서 진단 모드와 적용 상태를 다시 확인합니다. 로그가 계속 없으면 주차 상태에서 **Restart unit** 후 다시 직결해 확인합니다. `GPM_READY hooks=7/7`이 뜨는데 `AA_TURN` 또는 `AA_DISTANCE`가 없어도 그대로 알려주세요. 이 로그에는 케이블 연결과 무선 연결을 구별하는 값이 없으므로 테스트 환경을 함께 적어야 합니다.

테스트 후 **Mods**에서 진단 모드를 끄거나 제거한 뒤 **Apply changes**를 누릅니다.

## 빌드

[HMG-API 1.0.2](https://github.com/Open-HMG/HMG-API/tree/v1.0.2)의 컴파일된 `out/classes`, Android SDK 플랫폼 29 및 build-tools 35.0.0, JDK, Python과 로컬 서명 키가 필요합니다.

```sh
bash build.sh /path/to/HMG-API/out/classes /path/to/author.key
```
