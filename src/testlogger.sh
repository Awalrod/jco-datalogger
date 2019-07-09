#!/bin/sh
#java -jar build/jco-dl-1v1.jar --address '10.10.3.1' $*	#When pi is a hotspot
#java -jar build/jco-dl-1v1.jar --stdout --start --xml-config "config.xml" --address '192.168.1.54' $*	#When pi is connected to ethernet
java -jar build/jco-dl-1v1.jar --xml-config "stage/etc/gcdc/loggerconfig.xml" --address 'localhost'  $*	#When pi is connected to ethernet
