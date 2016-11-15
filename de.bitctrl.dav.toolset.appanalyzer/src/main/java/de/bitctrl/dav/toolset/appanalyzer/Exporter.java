/*
 * Allgemeine Datenverteiler-Tools
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
 *
 * This project is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This project is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this project; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA.
 *
 * Contact Information:
 * BitCtrl Systems GmbH
 * Weißenfelser Straße 67
 * 04229 Leipzig
 * Phone: +49 341-490670
 * mailto: info@bitctrl.de
 */
package de.bitctrl.dav.toolset.appanalyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JList;
import javax.swing.JOptionPane;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.Data.Array;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.AttributeGroupUsage;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;

/**
 * Hintergrund-Thread mit dem der Datensatz mit den Anmeldedaten einer Liste von
 * Applikationen in eine Datei exportiert wird.
 *
 * @author BitCtrl Systems GmbH, Uwe Peuker
 */
public class Exporter extends Thread {

	private static class SummaryKey {

		private final String rolle;
		private final SystemObjectType type;
		private final AttributeGroup atg;
		private final Aspect asp;

		public SummaryKey(final String rolle, final SystemObjectType type, final AttributeGroup atg, final Aspect asp) {
			this.rolle = rolle;
			this.type = type;
			this.atg = atg;
			this.asp = asp;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((asp == null) ? 0 : asp.hashCode());
			result = (prime * result) + ((atg == null) ? 0 : atg.hashCode());
			result = (prime * result) + ((rolle == null) ? 0 : rolle.hashCode());
			result = (prime * result) + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof SummaryKey)) {
				return false;
			}
			final SummaryKey other = (SummaryKey) obj;
			if (asp == null) {
				if (other.asp != null) {
					return false;
				}
			} else if (!asp.equals(other.asp)) {
				return false;
			}
			if (atg == null) {
				if (other.atg != null) {
					return false;
				}
			} else if (!atg.equals(other.atg)) {
				return false;
			}
			if (rolle == null) {
				if (other.rolle != null) {
					return false;
				}
			} else if (!rolle.equals(other.rolle)) {
				return false;
			}
			if (type == null) {
				if (other.type != null) {
					return false;
				}
			} else if (!type.equals(other.type)) {
				return false;
			}
			return true;
		}

	}

	private final Set<SystemObject> exportApplications = new HashSet<>();
	private final File file;
	private final DataDescription dataDescription;
	private final ClientDavInterface dav;
	private boolean onlySummary;

	Exporter(final ClientDavInterface dav, final JList<SystemObject> applicationList, final File file,
			final boolean onlySummary) {
		exportApplications.addAll(applicationList.getSelectedValuesList());

		this.file = file;
		this.dav = dav;
		this.onlySummary = onlySummary;

		final AttributeGroup atg = dav.getDataModel().getAttributeGroup("atg.angemeldeteDatenidentifikationen");
		final Aspect aspect = dav.getDataModel().getAspect("asp.standard");
		this.dataDescription = new DataDescription(atg, aspect);
	}

	@Override
	public void run() {

		try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {

			for (final SystemObject exportApp : exportApplications) {

				final String appName = exportApp.toString();
				System.err.println("Exportiere Anmeldungen für: " + appName);

				writer.println(appName);
				for (int idx = 0; idx < appName.length(); idx++) {
					writer.print('=');
				}
				writer.println("\n\n");

				final long startTime = dav.getTime();

				int versuche = 0;
				ResultData resultData = null;
				while (versuche < 10) {

					if (versuche > 0) {
						System.err.println("\tVersuch: " + versuche);
					}

					resultData = dav.getData(exportApp, dataDescription, 0);

					long dataAgeInMs = 0;

					if (!resultData.hasData()) {
						System.err.println("\tKeine Daten abrufbar");
					} else {
						dataAgeInMs = resultData.getDataTime() - startTime;
						System.err.println("Alter der Daten: " + dataAgeInMs + " ms");
					}
					if (resultData.hasData() && ((dataAgeInMs > 0) && (dataAgeInMs < TimeUnit.MINUTES.toMillis(1)))) {
						break;
					}

					try {
						Thread.sleep(10000);
					} catch (final InterruptedException ex) {
						ex.printStackTrace();
					}
					versuche++;
					resultData = null;
				}

				if (resultData == null) {
					writer.println("Anmeldedaten konnten nicht ermittelt werden!\n\n");
				} else {
					printResult(writer, resultData.getData());
				}
			}
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(null, "Ausgabedatei kann nicht angelegt werden!\n" + e.getLocalizedMessage(),
					"FEHLER", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}

		JOptionPane.showMessageDialog(null, "Ausgabedatei " + file + " abgeschlossen!\n", "INFO",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void printResult(final PrintWriter writer, final Data data) {

		if (data == null) {
			return;
		}

		final Map<SummaryKey, Long> summary = new HashMap<>();

		final Array feld = data.getArray("angemeldeteDatenidentifikation");
		System.err.println("\tPrüfe: " + feld.getLength() + " Spezifikationen");
		for (int idx = 0; idx < feld.getLength(); idx++) {
			final Data item = feld.getItem(idx);

			final String rolle = item.getTextValue("rolle").getText();
			final SystemObject objekt = item.getReferenceValue("objekt").getSystemObject();
			final SystemObjectType type = objekt.getType();
			final AttributeGroupUsage verwendung = (AttributeGroupUsage) item
					.getReferenceValue("attributgruppenverwendung").getSystemObject();
			final AttributeGroup atg = verwendung.getAttributeGroup();
			final Aspect asp = verwendung.getAspect();

			final SummaryKey key = new SummaryKey(rolle, type, atg, asp);
			final Long counter = summary.get(key);
			if (counter != null) {
				summary.put(key, counter + 1);
			} else {
				summary.put(key, 1L);
			}

			if (!onlySummary) {
				writer.println(rolle + ";" + objekt + ";" + atg + ";" + asp);
			} else {
				if (idx % 100 == 1) {
					System.err.println("\tGeprüft: " + idx);
				}
			}
		}

		writer.println("\nZusammenfassung");
		writer.println("===============\n");

		for (final Entry<SummaryKey, Long> entry : summary.entrySet()) {
			final SummaryKey key = entry.getKey();
			writer.println(key.rolle + ";" + key.type + ";" + key.atg + ";" + key.asp + ";" + entry.getValue());
		}

		writer.println();
	}
}
