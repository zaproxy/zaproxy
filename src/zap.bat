if exist %HOMEPATH\.ZAP_JVM.properties (
	set /p jvmopts=< %HOMEPATH\.ZAP_JVM.properties
) else (
	set jvmopts=-Xmx512m
)

java %jvmopts% -jar zap-dev.jar %*
