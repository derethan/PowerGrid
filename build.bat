@echo off
cd /d "%~dp0"
echo Building Power Grid JAR...
call gradlew.bat jar
if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful! Copying to CurseForge mods folder...
    copy /y "build\libs\powergrid-1.0.1.jar" "C:\Users\andg_\curseforge\minecraft\Instances\Andrew\mods\powergrid-1.0.1.jar"
    echo Done! You can now launch or restart Minecraft.
) else (
    echo.
    echo Build failed with error code %ERRORLEVEL%.
)
pause
