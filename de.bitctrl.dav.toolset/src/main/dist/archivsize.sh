#!/bin/bash

java -cp de.bitctrl.dav.toolset-runtime.jar \
 -Xmx768m \
 de.bitctrl.dav.toolset.ArchivSizer \
 -datenverteiler=192.168.1.219:8083 \
 -benutzer=vrz \
 -authentifizierung=../../bin/dos/passwd \
 -baseDir=Hier_muss_das_Basedir_her \
 -outputFile=archivsize.txt \
 -debugLevelStdErrText=INFO \
 -debugLevelFileText=CONFIG

 