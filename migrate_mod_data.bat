@echo off
setlocal
rem ASCII-only script: paths are derived from this file's own folder, so no CJK in the batch.
rem Place this .bat inside the NEW project folder (ssc_furniture) and run it from there.

set "NEW=%~dp0"
set "OLD=%NEW%..\textmod-template-1.20.1"

echo NEW project : %NEW%
echo OLD project : %OLD%
echo.

if not exist "%OLD%\src\main\java\ly\ssc_furniture" (
	echo [ERROR] old source not found, expected: %OLD%\src\main\java\ly\ssc_furniture
	pause
	exit /b 1
)

echo [1/5] Copy main Java code (ly\ssc_furniture)
robocopy "%OLD%\src\main\java\ly\ssc_furniture" "%NEW%src\main\java\ly\ssc_furniture" /E /IS /NFL /NDL /NJH
if errorlevel 8 goto fail

echo [2/5] Copy client Java code (ly\ssc_furniture)
robocopy "%OLD%\src\client\java\ly\ssc_furniture" "%NEW%src\client\java\ly\ssc_furniture" /E /IS /NFL /NDL /NJH
if errorlevel 8 goto fail

echo [3/5] Copy main resources (overwrite fabric.mod.json / mixins / icon, add assets+data)
robocopy "%OLD%\src\main\resources" "%NEW%src\main\resources" /E /IS /NFL /NDL /NJH
if errorlevel 8 goto fail

echo [4/5] Copy client resources
robocopy "%OLD%\src\client\resources" "%NEW%src\client\resources" /E /IS /NFL /NDL /NJH
if errorlevel 8 goto fail

echo [5/5] Copy SSC dependency jar into libs
if not exist "%NEW%libs" mkdir "%NEW%libs"
copy /Y "%OLD%\libs\shape-shifter-curse-fabric-1.10.0.jar" "%NEW%libs\" >nul
if errorlevel 1 goto fail

echo Remove template placeholder sources
del /Q "%NEW%src\main\java\ly\Ssc_furniture.java" 2>nul
del /Q "%NEW%src\main\java\ly\mixin\ExampleMixin.java" 2>nul
del /Q "%NEW%src\client\java\ly\client\Ssc_furnitureClient.java" 2>nul
del /Q "%NEW%src\client\java\ly\client\Ssc_furnitureDataGenerator.java" 2>nul
del /Q "%NEW%src\client\java\ly\client\mixin\ExampleClientMixin.java" 2>nul
rmdir "%NEW%src\main\java\ly\mixin" 2>nul
rmdir "%NEW%src\client\java\ly\client\mixin" 2>nul
rmdir "%NEW%src\client\java\ly\client" 2>nul

echo.
echo ===== MIGRATION DONE =====
echo Verify: src\main\java\ly\ssc_furniture and src\client\java\ly\ssc_furniture now exist.
echo Then build with:  cd /d "%NEW%"  ^&^&  gradlew build
pause
exit /b 0

:fail
echo.
echo [ERROR] robocopy/copy failed (see messages above).
pause
exit /b 1
