# AudioManagerPersonal — GitHub Signed Release Build

## 1) Push this project to a GitHub repo

## 2) Create a keystore (one time)

```bash
keytool -genkeypair -v -keystore my-release-key.jks -alias release -keyalg RSA -keysize 2048 -validity 10000
```

## 3) Base64 your keystore
- macOS/Linux:
```bash
base64 my-release-key.jks > my-release-key.jks.base64
```
- Windows PowerShell:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("my-release-key.jks")) > my-release-key.jks.base64
```

## 4) Add GitHub Secrets (Repo → Settings → Secrets and variables → Actions)
- `SIGNING_KEYSTORE_BASE64` → paste file contents of `my-release-key.jks.base64`
- `SIGNING_STORE_PASSWORD` → your keystore password
- `SIGNING_KEY_ALIAS` → usually `release`
- `SIGNING_KEY_PASSWORD` → your key password

## 5) Run the workflow
- Actions → Build Signed Release APK → Run workflow
- Download artifact **AudioManagerPersonal-release** → `app-release.apk`

Package name: `com.robertarnold.audiomanager`
```
