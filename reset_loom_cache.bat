@echo off
setlocal
rem Reset corrupted Loom/Minecraft caches and rebuild.
rem Place inside the project folder and double-click.

set "PRJ=%~dp0"

echo [1/4] Stop Gradle daemons...
call "%PRJ%gradlew.bat" --stop

echo [2/4] Remove project-level build and .gradle caches...
if exist "%PRJ%build" rmdir /s /q "%PRJ%build"
if exist "%PRJ%.gradle" rmdir /s /q "%PRJ%.gradle"

echo [3/4] Remove GLOBAL fabric-loom cache (forces full Minecraft re-download + remap)...
if exist "%USERPROFILE%\.gradle\caches\fabric-loom" rmdir /s /q "%USERPROFILE%\.gradle\caches\fabric-loom"

echo [4/4] Rebuilding...
call "%PRJ%gradlew.bat" build

echo.
echo ===== DONE (check output above for BUILD SUCCESSFUL/FAILED) =====
pause
