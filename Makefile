PACK = gcdclogger_0.0-2_armhf
SRC = ./src
CONFIG_LOC =  etc/gcdc
INIT_LOC = etc/init.d
CANFEST_LOC = usr/lib
BIN_LOC = usr/local/bin
JAR_LOC = usr/local/share/java
ICON_LOC = usr/share/apache2/icons
#WEB_LOC = var/www/html
steps = config init canfest bin jar icon
TMP_DIR=$(PACK)_temp
TARGET=$(PACK)

package: $(steps)  
#	dpkg -b $(PACK)
	@dpkg-deb --build $(TMP_DIR) .


config:
	@rm -rf $(TMP_DIR)
	-@mkdir -p $(TMP_DIR) 2>&1 1>$(NUL)
#       git pull
#	mkdir -p $(PACK)/$(CONFIG_LOC)
#	cp $(SRC)/$(CONFIG_LOC)/loggerconfig.xml $(PACK)/$(CONFIG_LOC)/loggerconfig.xml
#	cp $(SRC)/$(CONFIG_LOC)/busmaster-config.xml $(PACK)/$(CONFIG_LOC)/busmaster-config.xml 
	
init:
	rsync -a $(SRC)/* $(TMP_DIR) --exclude=.svn --exclude=*~ --exclude=./src --exclude=build*
#	cp $(SRC)/$(INIT_LOC)/can-daemon $(PACK)/$(INIT_LOC)/can-daemon
	
canfest:
#	mkdir -p $(TMP_DIR)/$(CANFEST_LOC)
#	cp $(SRC)/$(CANFEST_LOC)/libcanfestival* $(TMP_DIR)/$(CANFEST_LOC)/

bin:
#	mkdir -p $(TMP_DIR)/$(BIN_LOC)
#	cp $(SRC)/$(BIN_LOC)/DS401_Master $(TMP_DIR)/$(BIN_LOC)/DS401_Master
#	cp $(SRC)/$(BIN_LOC)/busmaster $(TMP_DIR)/$(BIN_LOC)/busmaster
#	cp $(SRC)/$(BIN_LOC)/can-boot $(TMP_DIR)/$(BIN_LOC)/can-boot
#	cp $(SRC)/$(BIN_LOC)/can-socat-setup $(TMP_DIR)/$(BIN_LOC)/can-socat-setup
#	cp $(SRC)/$(BIN_LOC)/data-logger $(TMP_DIR)/$(BIN_LOC)/data-logger

jar: 
	mkdir -p $(TMP_DIR)/$(JAR_LOC)
	cp $(SRC)/build/jco-dl-1v1.jar $(TMP_DIR)/$(JAR_LOC)/jco-dl-1v1.jar
	
icon:
#	mkdir -p $(TMP_DIR)/$(ICON_LOC)
#	cp $(SRC)/$(ICON_LOC)/GCDC.png $(TMP_DIR)/$(ICON_LOC)/GCDC.png
	

install: $(steps) package
	#REQUIRES SUDO
	#run this first if changes were made that need to be implemented
	#moves working code to install locations
	sudo dpkg -i $(PACK).deb

clean:
	rm -rf $(TMP_DIR)

