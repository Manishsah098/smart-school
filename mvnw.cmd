@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Script for Windows
@REM ----------------------------------------------------------------------------
@echo off
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set MAVEN_HOME=%DIRNAME%\.mvn_bin\apache-maven-3.9.9
if exist "%MAVEN_HOME%\bin\mvn.cmd" (
    "%MAVEN_HOME%\bin\mvn.cmd" %*
) else (
    mvn %*
)
