# Tool zur Analyse eines Datenverteilerarchivs

Version: 0.0.2

## Übersicht

Das Paket enthält ein Tool zur Analyse eines Datenverteiler-Archivs.
Eingebunden ist:

### ArchivSizer - ein Tool zur Größenanalyse auf Basis einer Filesystem-Analyse
  
Das Tool durchläuft ausgehend vom Root-Verzeichnis des Archivs das Dateisystems und rekonstruiert aus der Verzeichnisstruktur die Datenspezifikationen der im Archiv hinterlegten Daten.  
Für jede Datenspezifikation wird die Anzahl der gespeicherten Dateien und die Gesamtgröße ermittelt.
  
Aufrufparameter sind neben den Standard-Datenverteiler-Parametern:

- **-baseDir=<verzeichnis>** das Basisverzeichnis des Archivsystems (notwendig)
- **-outputFile=<ausgabedatei>** die Ausgabedatei, Standardwert ist *archivsize.txt* (optional)
  
## Versionsgeschichte

### 1.0.0

- erstes Release

## Kontakt

BitCtrl Systems GmbH  
Weißenfelser Straße 67  
04229 Leipzig  
Phone: +49 341-490670  
mailto: info@bitctrl.de  
