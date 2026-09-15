@echo off
echo Создание EXE файла из приложения Entro...
echo.

REM Проверка наличия PyInstaller
python -m pip show pyinstaller >nul 2>&1
if errorlevel 1 (
    echo Установка PyInstaller...
    python -m pip install pyinstaller
)

REM Создание EXE файла с иконкой
echo.
echo Создание EXE файла с иконкой entro.ico...
pyinstaller --onefile --windowed --icon=entro.ico --name="Entro" --add-data "entro.ico;." entro.py

echo.
echo Готово! EXE файл находится в папке dist\
echo.
pause