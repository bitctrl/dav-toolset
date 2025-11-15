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

/**
 * Definition der unterstützten Umfelddaten-Sensortypen, für die Parameter zur
 * Verfügung stehen.
 *
 * Momentan werden nur die Typen:
 * <ul>
 * <li>Niederschlagsintensität</li>
 * <li>Wasserfilmdicke und</li>
 * <li>Sichtweite</li>
 * </ul>
 *
 * unterstützt, da für diese Stufen für die Klassifizierung in den allgemeinen
 * Anforderungen definiert sind.
 *
 * @author BitCtrl Systems GmbH, Uwe Peuker
 */
public enum UfdsSensorType {

	/** Sensortyp "Niederschlagsintensität". */
	NI("NiederschlagsIntensität"),

	/** Sensortyp "Wasserfilmdicke". */
	WFD("WasserFilmDicke"),

	/** Sensortyp "Sichtweite". */
	SW("SichtWeite");

	private static final double[][] NI_STUFEN = { { 0.0, 0.3 }, { 0.2, 1.2 }, { 1.0, 5.0 }, { 4.0, 12.0 },
			{ 10.0, 20.0 } };

	private static final double[][] WFD_STUFEN = { { 0.0, 0.2 }, { 0.1, 0.5 }, { 0.4, 1.2 }, { 1.0, 3.0 } };

	private static final double[][] SW_STUFEN = { { 10, 60 }, { 50, 100 }, { 80, 150 }, { 120, 300 }, { 250, 500 },
			{ 400, 1000 } };

	private static final double[][] EMPTY = {};

	private final String attName;

	UfdsSensorType(final String attName) {
		this.attName = attName;
	}

	/**
	 * liefert den Name des Attributs für den Sensortyp. Der Name wird per
	 * Konvention verwendet, um die PID für Attributgruppen zu bilden.
	 *
	 * @return der Name
	 */
	public String getAttName() {
		return attName;
	}

	/**
	 * liefert ein Array von Paaren von Gernzwerten (von,bis) für die
	 * Standardstufen für die Klassifizierung der Daten eines
	 * Umfelddatensensors.
	 *
	 * @return das Array
	 */
	public double[][] getDefaultStufen() {
		return switch (this) {
			case NI -> NI_STUFEN;
			case SW -> SW_STUFEN;
			case WFD -> WFD_STUFEN;
			default -> EMPTY;
		};
	}
}
