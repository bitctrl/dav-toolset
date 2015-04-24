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

public class Exporter extends Thread {

	private static class SummaryKey {

		private final String rolle;
		private final SystemObjectType type;
		private final AttributeGroup atg;
		private final Aspect asp;

		public SummaryKey(final String rolle, final SystemObjectType type,
				final AttributeGroup atg, final Aspect asp) {
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

	private final Set<SystemObject> exportApplications = new HashSet<SystemObject>();
	private final File file;
	private final DataDescription dataDescription;
	private final ClientDavInterface dav;

	public Exporter(final ClientDavInterface dav,
			final JList<SystemObject> applicationList, final File file) {
		exportApplications.addAll(applicationList.getSelectedValuesList());

		this.file = file;
		this.dav = dav;

		final AttributeGroup atg = dav.getDataModel().getAttributeGroup(
				"atg.angemeldeteDatenidentifikationen");
		final Aspect aspect = dav.getDataModel().getAspect("asp.standard");
		this.dataDescription = new DataDescription(atg, aspect);
	}

	@Override
	public void run() {

		try (PrintWriter writer = new PrintWriter( new FileWriter(file))) {

			for (final SystemObject exportApp : exportApplications) {

				final String appName = exportApp.toString();

				writer.println(appName);
				for( int idx = 0; idx < appName.length(); idx++) {
					writer.print('=');
				}
				writer.println("\n\n");

				final long startTime = dav.getTime();

				int versuche = 0;
				ResultData resultData = null;
				while (versuche < 10) {
					resultData = dav.getData(exportApp, dataDescription, 0);
					if (resultData.hasData()
							&& ((resultData.getDataTime() - startTime) < TimeUnit.MINUTES
									.toMillis(1))) {
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

				if ( resultData == null ) {
					writer.println("Anmeldedaten konnten nicht ermittelt werden!\n\n");
				} else {
					printResult(writer, resultData.getData());
				}
			}
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(
					null,
					"Ausgabedatei kann nicht angelegt werden!\n"
							+ e.getLocalizedMessage(), "FEHLER", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}

		JOptionPane.showMessageDialog(
				null,
				"Ausgabedatei " + file + " abgeschlossen!\n"
				, "INFO", JOptionPane.INFORMATION_MESSAGE);
	}

	private void printResult(final PrintWriter writer, final Data data) {

		if (data == null) {
			return;
		}

		final Map<SummaryKey, Long> summary = new HashMap<SummaryKey, Long>();

		final Array feld = data.getArray("angemeldeteDatenidentifikation");
		for (int idx = 0; idx < feld.getLength(); idx++) {
			final Data item = feld.getItem(idx);

			final String rolle = item.getTextValue("rolle").getText();
			final SystemObject objekt = item.getReferenceValue("objekt")
					.getSystemObject();
			final SystemObjectType type = objekt.getType();
			final AttributeGroupUsage verwendung = (AttributeGroupUsage) item
					.getReferenceValue("attributgruppenverwendung").getSystemObject();
			final AttributeGroup atg = verwendung.getAttributeGroup();
			final Aspect asp = verwendung.getAspect();

			final SummaryKey key = new SummaryKey(rolle, type, atg, asp);
			final Long counter = summary.get(key);
			if ( counter != null ) {
				summary.put(key, counter + 1);
			} else {
				summary.put(key, 1L);
			}

			writer.println(rolle + ";" + objekt + ";" + atg + ";" + asp);
		}

		writer.println("\nZusammenfassung");
		writer.println("===============\n");

		for ( final Entry<SummaryKey, Long> entry : summary.entrySet() ) {
			final SummaryKey key = entry.getKey();
			writer.println(key.rolle + ";" + key.type + ";" + key.atg + ";" + key.asp + ";" + entry.getValue());
		}

		writer.println();
	}
}
