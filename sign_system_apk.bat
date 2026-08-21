@echo off
chcp 65001 > nul
echo ===================================================
echo   卓策主板 APK 系统签名工具 (Platform Key)
echo ===================================================

set SIGN_JAR=%~dp0sign\signapk.jar
set KEY_FILE=%~dp0sign\platform.pk8
set CERT_FILE=%~dp0sign\platform.x509.pem

if "%~1"=="" (
    set INPUT_APK=%~dp0app\build\outputs\apk\debug\app-debug.apk
) else (
    set INPUT_APK=%~1
)

if not exist "%INPUT_APK%" (
    echo [错误] 找不到待签名的 APK 文件: %INPUT_APK%
    echo 请先编译 APK 或将 APK 拖动到此 bat 脚本上。
    pause
    exit /b 1
)

set OUTPUT_APK=%~dpn1-system%~x1
if "%~1"=="" (
    set OUTPUT_APK=%~dp0app\build\outputs\apk\debug\app-debug-system.apk
)

echo 输入 APK: %INPUT_APK%
echo 正在签名...

java -jar "%SIGN_JAR%" sign --key "%KEY_FILE%" --cert "%CERT_FILE%" --out "%OUTPUT_APK%" "%INPUT_APK%"

if %ERRORLEVEL% EQU 0 (
    echo ===================================================
    echo [成功] 系统签名完成！
    echo 输出文件: %OUTPUT_APK%
    echo ===================================================
) else (
    echo [失败] 签名失败，请检查 Java 运行环境。
)

pause
