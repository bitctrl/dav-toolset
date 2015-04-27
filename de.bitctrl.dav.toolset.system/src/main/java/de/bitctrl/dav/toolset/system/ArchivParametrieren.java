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
package de.bitctrl.dav.toolset.system;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.Data.Array;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 * Applikation, um die Archivapplikation automatisiert zu parametrieren.
 * CSV-Datei mit 1.Spalte Typ-PID, 2.Spalte ATG-PID und 3. Spalte ASP-PID.
 *
 * @author BitCtrl Systems GmbH, Albrecht Uhlmann, Thomas Thierfelder
 * @version $Id: ParametrierungParametrieren.java 43952 2013-05-15 15:26:53Z
 *          uhlmann $
 */
public class ArchivParametrieren implements StandardApplication,
ClientSenderInterface, ClientReceiverInterface {

	private static final String ITEM_PARAMETER_SATZ = "ParameterSatz";

	private static final String ITEM_URLASSER = "Urlasser";

	private ClientDavInterface connection;

	private File parametrierungsValidatorInputFile;

	private static final int OBJ_TYP_SPALTE = 0;

	private static final int ATG_SPALTE = 1;

	private static final int ASP_SPALTE = 2;

	private static final int KB_SPALTE = 3;

	private DataDescription vorgabeDescription;

	private DataDescription receiveDescription;

	private SystemObject parametrierungsObject;

	/**
	 * Die PID der Attributgruppe, die die Archivierung parametriert.
	 */
	private static final String ATG_ARCHIV = "atg.archiv";

	private static String pidArchiv = "";

	private ResultData parametersatz;

	private final List<ObjTypAtgAspKB> typAtgAspSet = new ArrayList<>();

	/**
	 * Wenn true, dann wird der Parametersatz versendet, selbst wenn die
	 * Parametrierung bereits parametriert ist.
	 */
	private boolean force;

	private void readCsv() {
		String content;
		Logger.getLogger(getClass().getName()).info(
				"Beginne Einlesen von "
						+ parametrierungsValidatorInputFile.getPath());

		try (RandomAccessFile sourceFile = new RandomAccessFile(
				parametrierungsValidatorInputFile.getAbsolutePath(), "r")) {
			while ((content = sourceFile.readLine()) != null) {
				if (content.startsWith("#")) {
					continue;
				}
				Logger.getLogger(getClass().getName()).finer(content);
				final String[] values = content.split(";");
				String typPid = null;
				String atgPid = null;
				String aspPid = null;
				String kbPid = null;
				try {
					typPid = values[ArchivParametrieren.OBJ_TYP_SPALTE];
					if (values[ArchivParametrieren.ATG_SPALTE]
							.startsWith("atg")) {
						atgPid = values[ArchivParametrieren.ATG_SPALTE];
					}
					if (values[ArchivParametrieren.ASP_SPALTE]
							.startsWith("asp")) {
						aspPid = values[ArchivParametrieren.ASP_SPALTE];
					}
					if ((values.length > 3)
							&& values[ArchivParametrieren.KB_SPALTE]
									.startsWith("kb")) {
						kbPid = values[ArchivParametrieren.KB_SPALTE];
					}
					if ((typPid != null) || (atgPid != null)
							|| (aspPid != null) || (kbPid != null)) {
						typAtgAspSet.add(new ObjTypAtgAspKB(typPid, atgPid,
								aspPid, kbPid));
					}
				} catch (final ArrayIndexOutOfBoundsException e) {
					//
				}
			}
			sourceFile.close();
		} catch (final IOException e) {
			Logger.getLogger(getClass().getName()).warning(
					e.getLocalizedMessage());
		} catch (final ArrayIndexOutOfBoundsException e) {
			Logger.getLogger(getClass().getName()).fine(
					"Leerzeile(n) am Ende der CSV");
		}
	}

	@Override
	public void initialize(final ClientDavInterface dav) throws Exception {
		Locale.setDefault(Locale.GERMANY);
		this.connection = dav;
		final DataModel model = dav.getDataModel();
		parametrierungsObject = model.getObject(ArchivParametrieren.pidArchiv);
		if (null == parametrierungsObject) {
			Logger.getLogger(getClass().getName())
			.info("Angegebenes Parametrierungsobjekt existiert nicht, nehme AOE...");
			parametrierungsObject = dav.getLocalConfigurationAuthority();
		}
		readCsv();
		if (typAtgAspSet.isEmpty()) {
			finish("Nichts zu parametrieren.", 1);
		} else {
			Logger.getLogger(getClass().getName())
			.info(typAtgAspSet.size()
					+ " (Typ, Atg, Asp)-Kombinationen werden behandelt.");
			AttributeGroup atg;
			Logger.getLogger(getClass().getName())
			.info(typAtgAspSet.size()
					+ " (Typ, Atg, Asp)-Kombinationen werden zur Archivierung freigegeben.");
			atg = model.getAttributeGroup(ArchivParametrieren.ATG_ARCHIV);
			Aspect asp = model.getAspect("asp.parameterVorgabe");
			vorgabeDescription = new DataDescription(atg, asp);
			dav.subscribeSender(this, parametrierungsObject,
					vorgabeDescription, SenderRole.sender());

			asp = model.getAspect("asp.parameterSoll");
			receiveDescription = new DataDescription(atg, asp);
			dav.subscribeReceiver(this, parametrierungsObject,
					receiveDescription, ReceiveOptions.normal(),
					ReceiverRole.receiver());
			Logger.getLogger(getClass().getName()).fine(
					"Initialisierung erfolgreich");
		}
	}

	/** Hier findet die eigentliche Arbeit statt. */
	private void parametersatzErzeugen() {
		final Data data = connection.createData(connection.getDataModel()
				.getAttributeGroup(ArchivParametrieren.ATG_ARCHIV));
		if (data != null) {
			final Data urlasserArray = data
					.getItem(ArchivParametrieren.ITEM_URLASSER);
			urlasserArray.getReferenceValue("BenutzerReferenz")
			.setSystemObject(connection.getLocalUser());
			urlasserArray.getTextValue("Ursache").setText(
					"Ausführung des Programms "
							+ getClass().getName()
							+ " am "
							+ new SimpleDateFormat(
									CommonDefs.LOGFILE_DATE_FORMAT)
							.format(connection.getTime()));
			urlasserArray.getTextValue("Veranlasser").setText(
					connection.getLocalUser().getName());
			final Array parameterSatzArray = data
					.getArray(ArchivParametrieren.ITEM_PARAMETER_SATZ);
			parameterSatzArray.setLength(typAtgAspSet.size());
			String info = "Parametriere zur Archivierung:\n";
			int i = 0;
			for (final ObjTypAtgAspKB typAtgAsp : typAtgAspSet) {
				final Data parameterSatzInhalt = parameterSatzArray
						.getItem(i++);
				if (typAtgAsp.kb != null) {
					parameterSatzInhalt.getArray("Bereich").setLength(1);
					parameterSatzInhalt.getArray("Bereich").getItem(0)
					.asReferenceValue().setSystemObject(typAtgAsp.kb);
				}
				parameterSatzInhalt.getArray("DatenSpezifikation").setLength(1);

				final Data datenSpezifikation = parameterSatzInhalt.getArray(
						"DatenSpezifikation").getItem(0);
				if (typAtgAsp.objTyp != null) {
					datenSpezifikation.getArray("Objekt").setLength(1);
					datenSpezifikation.getArray("Objekt").getItem(0)
					.asReferenceValue()
					.setSystemObject(typAtgAsp.objTyp);
				} else {
					datenSpezifikation.getArray("Objekt").setLength(0);
				}

				if (typAtgAsp.atg != null) {
					datenSpezifikation.getArray("AttributGruppe").setLength(1);
					datenSpezifikation.getArray("AttributGruppe").getItem(0)
					.asReferenceValue().setSystemObject(typAtgAsp.atg);
				} else {
					datenSpezifikation.getArray("AttributGruppe").setLength(0);
				}

				if (typAtgAsp.asp != null) {
					datenSpezifikation.getArray("Aspekt").setLength(1);
					datenSpezifikation.getArray("Aspekt").getItem(0)
					.asReferenceValue().setSystemObject(typAtgAsp.asp);
				} else {
					datenSpezifikation.getArray("Aspekt").setLength(0);
				}
				datenSpezifikation.getUnscaledValue("SimulationsVariante").set(
						0);

				info += "  " + typAtgAsp + "\n";

				final Data einstellungen = parameterSatzInhalt
						.getItem("Einstellungen");
				einstellungen.getUnscaledValue("Archivieren").set(1);
				einstellungen.getArray("Nachfordern").setLength(0);
				einstellungen.getUnscaledValue("Sichern").set(0);
				einstellungen.getArray("Quittieren").setLength(0);
				final GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(0);
				cal.add(Calendar.YEAR, 5);
				einstellungen.getTimeValue("Vorhalten").setMillis(
						cal.getTimeInMillis());
			}
			Logger.getLogger(getClass().getName()).info(info);

			parametersatz = new ResultData(parametrierungsObject,
					vorgabeDescription, System.currentTimeMillis(), data);
			parametersatzSenden();
		} else {
			finish("Erzeugung des Datensatzes fehlgeschlagen!", 3);
		}
	}

	private void parametersatzSenden() {
		if (parametersatz != null) {
			try {
				connection.sendData(parametersatz);
				finish("Archiv wurde parametriert.", 0);
			} catch (final DataNotSubscribedException e) {
				finish("Kann Parametersatz nicht versenden: "
						+ e.getLocalizedMessage(), 4);
			} catch (final SendSubscriptionNotConfirmed e) {
				finish("Kann Parametersatz nicht versenden: "
						+ e.getLocalizedMessage(), 6);
			}
		}
	}

	@Override
	public void parseArguments(final ArgumentList argumentList)
			throws Exception {
		parametrierungsValidatorInputFile = argumentList.fetchArgument(
				"-input=../properties/ArchivParametrieren.csv")
				.asReadableFile();
		if (argumentList.hasArgument("-objekt")) {
			ArchivParametrieren.pidArchiv = argumentList.fetchArgument(
					"-objekt").asNonEmptyString();
		}
		force = argumentList.fetchArgument("-force=nein").booleanValue();
	}

	/**
	 * Haupt-Einsprungpunkt.
	 *
	 * @param args
	 *            Argumentvektor
	 */
	public static void main(final String[] args) {
		StandardApplicationRunner.run(new ArchivParametrieren(), args);
	}

	/**
	 * Beendet das Programm.
	 *
	 * @param message
	 *            Letzte Nachricht
	 * @param status
	 *            Exit-Code (0 = OK, &gt;0 Fehler)
	 */
	private void finish(final String message, final int status) {
		if (status > 0) {
			Logger.getLogger(getClass().getName()).severe(message);
		} else {
			Logger.getLogger(getClass().getName()).info(message);
		}

		if (parametrierungsObject != null) {
			connection.unsubscribeSender(this, parametrierungsObject,
					vorgabeDescription);
			connection.unsubscribeReceiver(this, parametrierungsObject,
					receiveDescription);
		}

		System.exit(status);
	}

	@Override
	public void dataRequest(final SystemObject object,
			final DataDescription dataDescription, final byte state) {
		// leer
	}

	@Override
	public boolean isRequestSupported(final SystemObject object,
			final DataDescription dataDescription) {
		return true;
	}

	@Override
	public void update(final ResultData[] results) {
		for (final ResultData result : results) {
			if (result.getDataDescription().equals(receiveDescription)
					&& (result.getData() == null)) {
				parametersatzErzeugen();
			}
			if (result.getDataDescription().equals(receiveDescription)
					&& (result.getData() != null)) {
				if (result.getData()
						.getArray(ArchivParametrieren.ITEM_PARAMETER_SATZ)
						.getLength() > 0) {
					if (force) {
						Logger.getLogger(getClass().getName()).warning(
								"Archiv-Parametrierung wird ueberschrieben!");
						parametersatzErzeugen();
					} else {
						finish("Archiv-Parametrierung ist bereits parametriert.",
								2);
					}
				} else {
					parametersatzErzeugen();
				}

			}
		}
	}

	private final class ObjTypAtgAspKB {

		private final SystemObject objTyp;

		private final AttributeGroup atg;

		private final Aspect asp;

		private final ConfigurationArea kb;

		private ObjTypAtgAspKB(final String objTypPid, final String atgPid,
				final String aspPid, final String kbPid) {
			final DataModel model = ArchivParametrieren.this.connection
					.getDataModel();

			if (objTypPid != null) {
				objTyp = model.getObject(objTypPid.trim());
				if (objTyp != null) {
					if (objTyp instanceof SystemObjectType) {
						Logger.getLogger(getClass().getName()).finer(
								"Füge Typ " + objTyp.getName() + " hinzu");
					} else {
						Logger.getLogger(getClass().getName()).finer(
								"Füge Objekt " + objTyp.getName() + " hinzu");
					}
				} else {
					throw new IllegalArgumentException("Pid " + objTypPid
							+ " ist kein Objekt oder Typ");
				}
			} else {
				objTyp = null;
			}
			if (atgPid != null) {
				atg = model.getAttributeGroup(atgPid.trim());
				if (atg != null) {
					Logger.getLogger(getClass().getName()).finer(
							"Füge Atg " + atg.getName() + " hinzu");
				} else {
					throw new IllegalArgumentException("Pid " + atgPid
							+ " ist keine Attributgruppe");
				}
			} else {
				atg = null;
			}
			if (aspPid != null) {
				asp = model.getAspect(aspPid.trim());
				if (asp != null) {
					Logger.getLogger(getClass().getName()).finer(
							"Füge Asp " + asp.getName() + " hinzu");
				} else {
					throw new IllegalArgumentException("Pid " + aspPid
							+ " ist kein Aspekt");
				}
			} else {
				asp = null;
			}
			if (kbPid != null) {
				kb = model.getConfigurationArea(kbPid.trim());
				if (kb != null) {
					Logger.getLogger(getClass().getName()).finer(
							"Füge KB " + kb.getName() + " hinzu");
				} else {
					throw new IllegalArgumentException("Pid " + kbPid
							+ " ist kein Konfigurationsbereich");
				}
			} else {
				kb = null;
			}
		}

		@Override
		public String toString() {
			return "Obj/Typ: " + (objTyp != null ? objTyp.getPid() : "*")
					+ ", Atg: " + (atg != null ? atg.getPid() : "*")
					+ ", Asp: " + (asp != null ? asp.getPid() : "*") + ", KB: "
					+ (kb != null ? kb.getPid() : "*");
		}

	}
}
