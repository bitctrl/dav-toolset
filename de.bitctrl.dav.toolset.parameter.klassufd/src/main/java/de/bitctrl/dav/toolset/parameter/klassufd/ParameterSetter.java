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
 * Weiﬂenfelser Straﬂe 67
 * 04229 Leipzig
 * Phone: +49 341-490670
 * mailto: info@bitctrl.de
 */

package de.bitctrl.dav.toolset.parameter.klassufd;

import java.util.LinkedHashSet;
import java.util.Set;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList.Argument;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Hilfsapplikation zum Setzen der Standardparameter innerhalb des Projekts VRZ
 * NRW II.
 *
 * @author BitCtrl Systems GmbH, Uwe Peuker
 */
public class ParameterSetter implements StandardApplication {

	private final Set<String> kbPids = new LinkedHashSet<>();
	private boolean force;
	
	@Override
	public void parseArguments(final ArgumentList argumentList) throws Exception {
		Argument pids = argumentList.fetchArgument("-kbPids");
		if( pids != null) {
			String[] parts = pids.asString().split(",");
			for( String part : parts ) {
				String trimmedPart = part.trim();
				if( !trimmedPart.isEmpty()) {
					kbPids.add(trimmedPart);
				}
			}
		}
		
		force = Boolean.parseBoolean(argumentList.fetchArgument("-force=false").asString());
		
		Argument[] unusedArguments = argumentList.fetchUnusedArguments();
		if( unusedArguments != null ) {
			Debug.getLogger().info("Unbenutzte Argumente", unusedArguments);
		}
	}

	@Override
	public void initialize(final ClientDavInterface connection) throws Exception {

		for (final UfdsSensorType sensorType : UfdsSensorType.values()) {
			final UfdsSensorenSetter umfeldDatenSensorenSetter = new UfdsSensorenSetter(connection, sensorType, kbPids, force);
			umfeldDatenSensorenSetter.run();
		}

		System.exit(0);
	}

	/**
	 * Startpunkt der Applikation.
	 *
	 * Neden den Standard-Datenverteiler-Argumenten zum Herstellen der
	 * Verbindung werden keine zus‰tzlichen Argumente erwartet.
	 *
	 * @param args
	 *            die Kommandozeilenargumente
	 */
	public static void main(final String[] args) {
		StandardApplicationRunner.run(new ParameterSetter(), args);
	}
}
