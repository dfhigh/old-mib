.PHONY: all clean compile package

all: compile package

clean:
	mvn clean
	rm -f release/*.tar.gz

compile:
	mvn clean package

package:
	cd target && mkdir -p mib && cp mib*.jar mib/ && cp -r ../src/main/resources/* mib/ && chmod a+x mib/bin/mib && tar zcf ../release/mib.tar.gz mib
