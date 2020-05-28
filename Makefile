PACK = jcodatalogger
VERSION = 1.0
TARGET = $(PACK)-$(VERSION)
SRC = ./src
CONFIG_LOC =  etc/gcdc
INIT_LOC = etc/init.d
CANFEST_LOC = usr/lib
BIN_LOC = usr/local/bin
JAR_LOC = usr/local/share/java
ICON_LOC = usr/share/apache2/icons
#WEB_LOC = var/www/html
steps = config init jar
TMP_DIR=$(TARGET)_temp

package: $(steps)  
#	dpkg -b $(TARGET)
	@dpkg-deb --build $(TMP_DIR) .


config:
	@rm -rf $(TMP_DIR)
	-@mkdir -p $(TMP_DIR) 2>&1
	
init:
	rsync -a $(SRC)/* $(TMP_DIR) --exclude=.svn --exclude=*~ --exclude=./src --exclude=build*
	
jar: 
	mkdir -p $(TMP_DIR)/$(JAR_LOC)
	cp $(SRC)/build/jco-dl-1v1.jar $(TMP_DIR)/$(JAR_LOC)/jco-dl-1v1.jar
	
install: $(steps) package
	#REQUIRES SUDO
	#run this first if changes were made that need to be implemented
	#moves working code to install locations
	sudo dpkg -i $(PACK)_$(VERSION)_armhf.deb
	
uninstall:
	sudo dpkg -r $(PACK)

clean:
	rm -rf $(TMP_DIR)

