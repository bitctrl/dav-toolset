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
package de.bitctrl.dav.toolset.appanalyzer;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.AbstractGUIApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 * Tool zum Export der Daten der Attributgruppe mit den angemeledeten
 * Datenspezifikationen einer Applikation in eine Datei.
 *
 * @author BitCtrl Systems GmbH, Uwe Peuker
 */
public class AppAnalyzer extends AbstractGUIApplication {

	private boolean onlySummary;

	@Override
	public void parseArguments(final ArgumentList argumentList)
			throws Exception {
		onlySummary = argumentList.fetchArgument("-onlySummary=true").booleanValue();
	}

	@Override
	public void initialize(final ClientDavInterface connection)
			throws Exception {
		final MainView mainView = new MainView(connection, onlySummary);
		mainView.pack();
		mainView.setVisible(true);
		System.err.println("Fertig");
	}

	@Override
	protected String getApplicationName() {
		return "BitCtrl AppAnalyzer";
	}

	public static void main(final String[] args) {
		StandardApplicationRunner.run(new AppAnalyzer(), args);
	}
}
