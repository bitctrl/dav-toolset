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

package de.bitctrl.dav.toolset.archivcheck;

import java.util.Objects;

class AkkumulationKey {

	private Object atg;
	private Object asp;

	AkkumulationKey(final Object atg, final Object aspect) {
		this.atg = atg;
		this.asp = aspect;
	}

	@Override
	public boolean equals(final Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj instanceof AkkumulationKey) {
			final AkkumulationKey akkKey = (AkkumulationKey) obj;
			return Objects.equals(asp, akkKey.asp) && Objects.equals(atg, akkKey.atg);
		}

		return false;
	}

	public Object getAsp() {
		return asp;
	}

	public Object getAtg() {
		return atg;
	}

	@Override
	public int hashCode() {
		return Objects.hash(asp, atg);
	}

	@Override
	public String toString() {
		return "AkkumulationKey [atg=" + atg + ", asp=" + asp + "]";
	}

}
