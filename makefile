PACK = gcdclogger_0.0-2_armhf
SRC = code/stage
CONFIG_LOC =  etc/gcdc
INIT_LOC = etc/init.d
CANFEST_LOC = usr/lib
BIN_LOC = usr/local/bin
JAR_LOC = usr/local/share/java
ICON_LOC = usr/share/apache2/icons
#WEB_LOC = var/www/html
steps = config init canfest bin jar icon

package: $(steps)  
	dpkg -b $(PACK)


config:
	mkdir -p $(PACK)/$(CONFIG_LOC)
	cp $(SRC)/$(CONFIG_LOC)/loggerconfig.xml $(PACK)/$(CONFIG_LOC)/loggerconfig.xml
	cp $(SRC)/$(CONFIG_LOC)/busmaster-config.xml $(PACK)/$(CONFIG_LOC)/busmaster-config.xml 
	
init:
	mkdir -p $(PACK)/$(INIT_LOC)
	cp $(SRC)/$(INIT_LOC)/can-daemon $(PACK)/$(INIT_LOC)/can-daemon
	
canfest:
	mkdir -p $(PACK)/$(CANFEST_LOC)
	cp $(SRC)/$(CANFEST_LOC)/libcanfestival* $(PACK)/$(CANFEST_LOC)/

bin:
	mkdir -p $(PACK)/$(BIN_LOC)
	cp $(SRC)/$(BIN_LOC)/DS401_Master $(PACK)/$(BIN_LOC)/DS401_Master
	cp $(SRC)/$(BIN_LOC)/busmaster $(PACK)/$(BIN_LOC)/busmaster
	cp $(SRC)/$(BIN_LOC)/can-boot $(PACK)/$(BIN_LOC)/can-boot
	cp $(SRC)/$(BIN_LOC)/can-socat-setup $(PACK)/$(BIN_LOC)/can-socat-setup
	cp $(SRC)/$(BIN_LOC)/data-logger $(PACK)/$(BIN_LOC)/data-logger

jar: 
	mkdir -p $(PACK)/$(JAR_LOC)
	cp $(SRC)/$(JAR_LOC)/jco-dl-1v1.jar $(PACK)/$(JAR_LOC)/jco-dl-1v1.jar
	
icon:
	mkdir -p $(PACK)/$(ICON_LOC)
	cp $(SRC)/$(ICON_LOC)/GCDC.png $(PACK)/$(ICON_LOC)/GCDC.png
	
#web:
#	cp /$(WEB_LOC)/index.html $(PACK)/$(WEB_LOC)/index.html
#	cp -r /$(WEB_LOC)/lib $(PACK)/$(WEB_LOC)/
#	cp -r /$(WEB_LOC)/images $(PACK)/$(WEB_LOC)/
#	cp -r /$(WEB_LOC)/css $(PACK)/$(WEB_LOC)/

install: $(steps) package
	#REQUIRES SUDO
	#run this first if changes were made that need to be implemented
	#moves working code to install locations
	dpkg -i $(PACK).deb		

clean:
	rm -rf $(PACK)/etc
	rm -rf $(PACK)/var
	rm -rf $(PACK)/usr