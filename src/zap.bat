if exist "%HOMEDRIVE%%HOMEPATH%\OWASP ZAP\.ZAP_JVM.properties" (
	set /p jvmopts=< "%HOMEDRIVE%%HOMEPATH%\OWASP ZAP\.ZAP_JVM.properties"
) else (
	set jvmopts=-Xmx512m
)

java %jvmopts% -jar zap-dev.jar %*
