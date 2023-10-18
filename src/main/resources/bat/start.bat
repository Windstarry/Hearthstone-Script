set curdir=%cd%
for /f "delims=\" %%a in ('dir /b /a-d /o-d "%curdir%\*.jar"') do (
    start javaw -jar %%a %1 && (timeout /t 1 & exit) || (cmd /c start echo ERROR: Requires JDK environment, is downloading, after downloading can be installed! & explorer.exe "https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.exe")
)