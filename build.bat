@echo off
cd /d "%~dp0"
echo Building Power Grid JAR...
call gradlew.bat jar
if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful! Copying to CurseForge mods folder...
    for /f "delims=" %%F in ('dir /b /o:-d "build\libs\powergrid-*.jar"') do (
        echo Copying build\libs\%%F...
        copy /y "build\libs\%%F" "C:\Users\andg_\curseforge\minecraft\Instances\Andrew\mods\%%F"
        goto :copied
    )
    :copied
    echo Done! You can now launch or restart Minecraft.
) else (
    echo.
    echo Build failed with error code %ERRORLEVEL%.
)
pause
