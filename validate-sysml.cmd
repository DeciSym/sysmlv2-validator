@echo off
setlocal

rem Windows executable entry point for SysML v2 Validator.
rem PowerShell handles the path and Java 21+ lookup logic.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0validate-sysml.ps1" %*
exit /b %ERRORLEVEL%
