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

package de.bitctrl.dav.toolset.parameter.klassufd;

import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Standardimplementierung für ein ClientSenderInterface, das zum Setzen von
 * Parametern verwendet wird.
 *
 * Es wird davon ausgegangen, dass die Parametrierung für die Versorgung der
 * versendeten Parameter angemeldet ist. Auf eine Auswertung der Sendesteuerung
 * wurde daher hier verzichtet.
 *
 * @author BitCtrl Systems GmbH, Uwe Peuker
 */
public class SimpleParameterSender implements ClientSenderInterface {

	@Override
	public void dataRequest(final SystemObject object, final DataDescription dataDescription, final byte state) {
		// Rückmeldungen werden nicht erwartet und ausgewertet
	}

	@Override
	public boolean isRequestSupported(final SystemObject object, final DataDescription dataDescription) {
		return false;
	}
}
