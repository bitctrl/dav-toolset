# Tool zur Analyse eines Datenverteilerarchivs

Version: 0.0.2

## �bersicht

Das Paket enth�lt ein Tool zur Analyse eines Datenverteiler-Archivs.
Eingebunden ist:

### ArchivSizer - ein Tool zur Gr��enanalyse auf Basis einer Filesystem-Analyse
  
Das Tool durchl�uft ausgehend vom Root-Verzeichnis des Archivs das Dateisystems und rekonstruiert aus der Verzeichnisstruktur die Datenspezifikationen der im Archiv hinterlegten Daten.  
F�r jede Datenspezifikation wird die Anzahl der gespeicherten Dateien und die Gesamtgr��e ermittelt.
  
Aufrufparameter sind neben den Standard-Datenverteiler-Parametern:

- **-baseDir=<verzeichnis>** das Basisverzeichnis des Archivsystems (notwendig)
- **-outputFile=<ausgabedatei>** die Ausgabedatei, Standardwert ist *archivsize.txt* (optional)
  
## Versionsgeschichte

### 1.0.0

- erstes Release

## Kontakt

BitCtrl Systems GmbH  
Wei�enfelser Stra�e 67  
04229 Leipzig  
Phone: +49 341-490670  
mailto: info@bitctrl.de  
