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

	public SizeSet() {
		this(0, 0, 0, 0);
	}

	public SizeSet(int count, long idxSize, long datSize, long otherSize) {
		this.count = count;
		this.idxSize = idxSize;
		this.datSize = datSize;
		this.otherSize = otherSize;
	}

	public void add(SizeSet sizeSet) {
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