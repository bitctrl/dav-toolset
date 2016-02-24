package de.bitctrl.dav.toolset.archivcheck;

import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;

class AkkumulationKey {

	private Object atg;
	private Object asp;

	public AkkumulationKey(Object atg, Object aspect) {
		this.atg = atg;
		this.asp = aspect;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AkkumulationKey other = (AkkumulationKey) obj;
		if (asp == null) {
			if (other.asp != null)
				return false;
		} else if (!asp.equals(other.asp))
			return false;
		if (atg == null) {
			if (other.atg != null)
				return false;
		} else if (!atg.equals(other.atg))
			return false;
		return true;
	}

	public Object getAsp() {
		return asp;
	}

	public Object getAtg() {
		return atg;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((asp == null) ? 0 : asp.hashCode());
		result = prime * result + ((atg == null) ? 0 : atg.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "AkkumulationKey [atg=" + atg + ", asp=" + asp + "]";
	}

}
