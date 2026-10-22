@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script for VS Code
@REM ----------------------------------------------------------------------------
@echo off
setlocal

SET JAVA_EXE="C:\Users\Administrator\.antigravity\extensions\redhat.java-1.55.0-win32-x64\jre\21.0.11-win32-x86_64\bin\java.exe"
IF NOT EXIST %JAVA_EXE% SET JAVA_EXE=java

IF NOT EXIST bin mkdir bin

echo Building with Java 21...
"C:\Users\Administrator\.antigravity\extensions\redhat.java-1.55.0-win32-x64\jre\21.0.11-win32-x86_64\bin\javac.exe" -d bin -encoding UTF-8 (Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName)

IF %ERRORLEVEL% EQU 0 (
    echo Starting Currency Converter Server...
    %JAVA_EXE% -cp bin com.currencyconverter.CurrencyConverterServer %*
)
