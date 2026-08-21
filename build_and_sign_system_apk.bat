@echo off
chcp 65001 > nul
echo =========================================================
echo   卓策主板 APK 一键编译并生成系统包 (System App)
echo =========================================================

cd /d "%~dp0"

echo 1. 开始执行 Gradle 编译...
call gradlew.bat :app:assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo [错误] Gradle 编译失败，请检查上方日志。
    pause
    exit /b 1
)

echo.
echo 2. 开始使用卓策 Platform Key 进行系统签名...
set SIGN_JAR=%~dp0sign\signapk.jar
set KEY_FILE=%~dp0sign\platform.pk8
set CERT_FILE=%~dp0sign\platform.x509.pem
set INPUT_APK=%~dp0app\build\outputs\apk\debug\app-debug.apk
set OUTPUT_APK=%~dp0app\build\outputs\apk\debug\app-debug-system.apk

java -jar "%SIGN_JAR%" sign --key "%KEY_FILE%" --cert "%CERT_FILE%" --out "%OUTPUT_APK%" "%INPUT_APK%"
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 系统签名失败。
    pause
    exit /b 1
)

echo.
echo =========================================================
echo [成功] 系统包打包并签名完成！
echo.
echo 系统 APK 文件路径:
echo %OUTPUT_APK%
echo =========================================================
echo.
pause
