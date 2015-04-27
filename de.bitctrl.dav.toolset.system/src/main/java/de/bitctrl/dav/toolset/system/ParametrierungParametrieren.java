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
 * Applikation, um die Parametrierungsapplikation automatisiert zu
 * parametrieren, und zwar mit demselben CSV-File als Input, welches auch der
 * Datenkatalog-Validator verwendet.<br>
 * <b>TT:</b> Erweiterung des Programms dergestalt, dass auch mehrere
 * Objekte/Typen, Attributgruppen und jetzt auch Konfigurationsbereiche
 * (zusaetzliche 6.Spalte in der Tabelle) kommasepariert eingetragen werden
 * koennen.
 *
 * @author BitCtrl Systems GmbH, Albrecht Uhlmann
 * @version $Id: ParametrierungParametrieren.java 45222 2013-07-26 06:44:46Z
 *          hoesel $
 */
public class ParametrierungParametrieren implements StandardApplication,
ClientSenderInterface, ClientReceiverInterface {

	private static final String ITEM_PARAMETER_SATZ = "ParameterSatz";

	private static final String ITEM_URLASSER = "Urlasser";

	private ClientDavInterface connection;

	private File parametrierungsValidatorInputFile;

	private static final int OBJ_TYP_SPALTE = 0;

	private static final int ATG_SPALTE = 1;

	private static final int KB_SPALTE = 5;

	private DataDescription vorgabeDescription;

	private DataDescription receiveDescription;

	private SystemObject parametrierungsObject;

	/**
	 * Die PID der Attributgruppe, die die Parametrierung parametriert.
	 */
	private static final String ATG_PARAMETRIERUNG = "atg.parametrierung";

	private static String pidParametrierung = "";

	private ResultData parametersatz;

	private final ArrayList<ObjTypAtgKB> objTypAtgKBList = new ArrayList<>();

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
			int i = 0;
			while ((content = sourceFile.readLine()) != null) {
				if ((i++ == 0) || content.startsWith("#")) {
					continue;
				}
				Logger.getLogger(getClass().getName()).finer(content);
				final String[] values = content.split(";");
				final List<String> objTypPidList = new ArrayList<>();
				final List<String> atgPidList = new ArrayList<>();
				final List<String> kbPidList = new ArrayList<>();

				final String[] objTypPidArray = values[ParametrierungParametrieren.OBJ_TYP_SPALTE]
						.split(",");
				for (final String objTypPid : objTypPidArray) {
					objTypPidList.add(objTypPid);
				}
				if (values[ParametrierungParametrieren.ATG_SPALTE].startsWith("atg")) {
					final String[] atgPidArray = values[ParametrierungParametrieren.ATG_SPALTE].split(",");
					for (final String atgPid : atgPidArray) {
						atgPidList.add(atgPid);
					}
				}
				if ((values.length > 5) && values[ParametrierungParametrieren.KB_SPALTE].startsWith("kb")) {
					final String[] kbPidArray = values[ParametrierungParametrieren.KB_SPALTE].split(",");
					for (final String kbPid : kbPidArray) {
						kbPidList.add(kbPid);
					}
				}
				objTypAtgKBList.add(new ObjTypAtgKB(objTypPidList, atgPidList,
						kbPidList));
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

	/*
	 * (Kein Javadoc)
	 *
	 * @see
	 * sys.funclib.application.StandardApplication#initialize(stauma.dav.clientside
	 * .ClientDavInterface)
	 */
	@Override
	public void initialize(final ClientDavInterface dav) throws Exception {
		Locale.setDefault(Locale.GERMANY);
		this.connection = dav;
		final DataModel model = dav.getDataModel();
		parametrierungsObject = model.getObject(ParametrierungParametrieren.pidParametrierung);
		if (null == parametrierungsObject) {
			Logger.getLogger(getClass().getName())
			.info("Angegebenes Parametrierungsobjekt existiert nicht, nehme AOE...");
			parametrierungsObject = dav.getLocalConfigurationAuthority();
		}
		readCsv();
		if (objTypAtgKBList.isEmpty()) {
			finish("Nichts zu parametrieren (String)", 1);
		} else {
			Logger.getLogger(getClass().getName()).info(
					objTypAtgKBList.size()
					+ " Datenidentifikationen werden behandelt");
			AttributeGroup atg;
			for (final ObjTypAtgKB objTypAtgKB : objTypAtgKBList) {
				Logger.getLogger(getClass().getName()).finer(
						"Füge Datenidentifikationen " + objTypAtgKB + " hinzu");
			}
			Logger.getLogger(getClass().getName())
			.info(objTypAtgKBList.size()
					+ " Datenidentifikationen werden zur Parametrierung freigegeben");
			atg = model.getAttributeGroup(ParametrierungParametrieren.ATG_PARAMETRIERUNG);
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
				.getAttributeGroup(ParametrierungParametrieren.ATG_PARAMETRIERUNG));
		if (data != null) {
			final Data urlasserArray = data.getItem(ParametrierungParametrieren.ITEM_URLASSER);
			urlasserArray.getReferenceValue("BenutzerReferenz")
			.setSystemObject(connection.getLocalUser());
			urlasserArray.getTextValue("Ursache").setText(
					"Ausführung des Programms "
							+ getClass().getName()
							+ " am "
							+ new SimpleDateFormat(CommonDefs.LOGFILE_DATE_FORMAT).format(
									connection.getTime()));
			urlasserArray.getTextValue("Veranlasser").setText(
					connection.getLocalUser().getName());
			final Array parameterSatzArray = data
					.getArray(ParametrierungParametrieren.ITEM_PARAMETER_SATZ);
			parameterSatzArray.setLength(objTypAtgKBList.size());
			String infoLogText = "Erfolgreich parametrierte Datenidentifikationen:\n";
			for (int dataIdentIdx = 0; dataIdentIdx < objTypAtgKBList.size(); dataIdentIdx++) {
				infoLogText += objTypAtgKBList.get(dataIdentIdx) + "\n";
				final ObjTypAtgKB objTypAtgKBi = objTypAtgKBList.get(dataIdentIdx);
				final List<SystemObject> objTypList = objTypAtgKBi.objTypList;
				final List<AttributeGroup> atgList = objTypAtgKBi.atgList;
				final List<ConfigurationArea> kbList = objTypAtgKBi.kbList;

				final Data parameterSatzInhalt = parameterSatzArray
						.getItem(dataIdentIdx);
				parameterSatzInhalt.getArray("Bereich")
				.setLength(kbList.size());
				for (int kbIdx = 0; kbIdx < kbList.size(); kbIdx++) {
					parameterSatzInhalt.getArray("Bereich").getItem(kbIdx)
					.asReferenceValue()
					.setSystemObject(kbList.get(kbIdx));
				}

				parameterSatzInhalt.getArray("DatenSpezifikation").setLength(1);
				final Data datenSpezifikation = parameterSatzInhalt.getArray(
						"DatenSpezifikation").getItem(0);
				datenSpezifikation.getArray("Objekt").setLength(
						objTypList.size());
				for (int objTypIdx = 0; objTypIdx < objTypList.size(); objTypIdx++) {
					datenSpezifikation.getArray("Objekt").getItem(objTypIdx)
					.asReferenceValue()
					.setSystemObject(objTypList.get(objTypIdx));
				}

				final Array atgArray = datenSpezifikation.getArray("AttributGruppe");
				atgArray.setLength(atgList.size());
				for (int atgIdx = 0; atgIdx < atgList.size(); atgIdx++) {
					datenSpezifikation.getArray("AttributGruppe")
					.getItem(atgIdx).asReferenceValue()
					.setSystemObject(atgList.get(atgIdx));
				}

				datenSpezifikation.getUnscaledValue("SimulationsVariante").set(
						0);
				final Data einstellungen = parameterSatzInhalt
						.getItem("Einstellungen");
				einstellungen.getUnscaledValue("Parametrieren").set(1);
			}
			parametersatz = new ResultData(parametrierungsObject,
					vorgabeDescription, System.currentTimeMillis(), data);

			parametersatzSenden(infoLogText);
		} else {
			finish("Erzeugung des Datensatzes fehlgeschlagen", 3);
		}
	}

	private void parametersatzSenden(final String info) {
		if (parametersatz != null) {
			try {
				connection.sendData(parametersatz);
				finish(info, 0);
			} catch (final DataNotSubscribedException e) {
				finish("Kann Parametersatz nicht versenden: "
						+ e.getLocalizedMessage(), 4);
			} catch (final SendSubscriptionNotConfirmed e) {
				finish("Kann Parametersatz nicht versenden: "
						+ e.getLocalizedMessage(), 6);
			}
		}
	}

	/*
	 * (Kein Javadoc)
	 *
	 * @see
	 * sys.funclib.application.StandardApplication#parseArguments(sys.funclib
	 * .ArgumentList)
	 */
	@Override
	public void parseArguments(final ArgumentList argumentList) throws Exception {
		parametrierungsValidatorInputFile = argumentList.fetchArgument(
				"-input=../properties/ParametrierungParametrieren.csv")
				.asReadableFile();
		if (argumentList.hasArgument("-objekt")) {
			ParametrierungParametrieren.pidParametrierung = argumentList.fetchArgument("-objekt")
					.asNonEmptyString();
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
		StandardApplicationRunner.run(new ParametrierungParametrieren(), args);
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
				if (result
						.getData()
						.getArray(
								ParametrierungParametrieren.ITEM_PARAMETER_SATZ)
								.getLength() > 0) {
					if (force) {
						Logger.getLogger(getClass().getName()).warning(
								"Parametrierung wird überschrieben!");
						parametersatzErzeugen();
					} else {
						finish("Parametrierung ist bereits parametriert", 2);
					}
				} else {
					parametersatzErzeugen();
				}

			}
		}
	}

	private final class ObjTypAtgKB {

		private final List<SystemObject> objTypList = new ArrayList<>();

		private final List<AttributeGroup> atgList = new ArrayList<>();

		private final List<ConfigurationArea> kbList = new ArrayList<>();

		private ObjTypAtgKB(final List<String> objTypPidList,
				final List<String> atgPidList, final List<String> kbPidList) {
			final DataModel model = ParametrierungParametrieren.this.connection
					.getDataModel();

			if (objTypPidList != null) {
				for (final String objTypPid : objTypPidList) {
					final SystemObject objTyp = model.getObject(objTypPid.trim());
					if (objTyp != null) {
						if (objTyp instanceof SystemObjectType) {
							Logger.getLogger(getClass().getName()).finer(
									"Füge Typ " + objTyp.getName() + " hinzu");
						} else {
							Logger.getLogger(getClass().getName()).finer(
									"Füge Objekt " + objTyp.getName()
									+ " hinzu");
						}
						objTypList.add(objTyp);
					} else {
						throw new IllegalArgumentException("Pid " + objTypPid
								+ " ist kein Objekt oder Typ");
					}
				}
			}
			if (atgPidList != null) {
				for (final String atgPid : atgPidList) {
					final AttributeGroup atg = model.getAttributeGroup(atgPid.trim());
					if (atg != null) {
						Logger.getLogger(getClass().getName()).finer(
								"Füge Atg " + atg.getName() + " hinzu");
						atgList.add(atg);
					} else {
						throw new IllegalArgumentException("Pid " + atgPid
								+ " ist keine Attributgruppe");
					}
				}
			}
			if (kbPidList != null) {
				for (final String kbPid : kbPidList) {
					final ConfigurationArea kb = model.getConfigurationArea(kbPid
							.trim());
					if (kb != null) {
						kbList.add(kb);
						Logger.getLogger(getClass().getName()).finer(
								"Füge KB " + kb.getName() + " hinzu");
					} else {
						throw new IllegalArgumentException("Pid " + kbPid
								+ " ist kein Konfigurationsbereich");
					}
				}
			}
		}

		@Override
		public String toString() {
			String objTypStr = "Obj/Typ: *\n";
			if (!objTypList.isEmpty()) {
				objTypStr = "Obj/Typ:";
				for (final SystemObject objTyp : objTypList) {
					objTypStr += "\n  " + objTyp.getPid();
				}
			}
			String atgStr = "\n  Atg: *\n";
			if (!objTypList.isEmpty()) {
				atgStr = "\n  Atg:";
				for (final SystemObject atg : atgList) {
					atgStr += "\n    " + atg.getPid();
				}
			}
			String kbStr = "\n  Konfigurationsbereiche: *";
			if (!kbList.isEmpty()) {
				kbStr = "\n  Konfigurationsbereiche:";
				for (final ConfigurationArea kb : kbList) {
					kbStr += "\n    " + kb.getPid();
				}
			}
			return objTypStr + atgStr + kbStr;
		}

	}

}
