@echo off
java -Xmx1024m -Djna.nosys=true -Dsun.java2d.uiScale=1.0 -jar "%~dp0\ffdec.jar" %*