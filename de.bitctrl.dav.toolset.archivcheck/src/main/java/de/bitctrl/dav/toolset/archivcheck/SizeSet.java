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

class SizeSet {

	private long setCount;

	public long getSetCount() {
		return setCount;
	}

	void incSetCount() {
		setCount++;
	}

	private long count;
	private long idxSize;
	private long datSize;
	private long otherSize;

	SizeSet() {
		this(0, 0, 0, 0);
	}

	SizeSet(final int count, final long idxSize, final long datSize, final long otherSize) {
		this.count = count;
		this.idxSize = idxSize;
		this.datSize = datSize;
		this.otherSize = otherSize;
	}

	public void add(final SizeSet sizeSet) {
		count += sizeSet.count;
		idxSize += sizeSet.idxSize;
		datSize += sizeSet.datSize;
		otherSize += sizeSet.otherSize;
	}

	public long getCount() {
		return count;
	}

	public long getDatSize() {
		return datSize;
	}

	public long getIdxSize() {
		return idxSize;
	}

	public long getOtherSize() {
		return otherSize;
	}

	public long getSize() {
		return idxSize + otherSize + datSize;
	}

	@Override
	public String toString() {
		return "SizeSet [count=" + count + ", idxSize=" + idxSize + ", datSize=" + datSize + ", otherSize=" + otherSize
				+ "]";
	}

	public long getDatRelation() {
		final long divisor = otherSize + idxSize;
		if (divisor <= 0) {
			return -1;
		}
		return datSize / divisor;
	}
}
