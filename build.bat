@echo off
cd /d "%~dp0"
echo Building Power Grid JAR...
call gradlew.bat jar
if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful! Output jar located in build\libs\
) else (
    echo.
    echo Build failed with error code %ERRORLEVEL%.
)
pause
