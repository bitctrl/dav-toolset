package de.bitctrl.dav.toolset.archivcheck;

class SizeSet {

	private long count;
	private long size;

	public SizeSet() {
		this(0, 0);
	}
	
	public SizeSet(int count, long size) {
		this.count = count;
		this.size = size;
	}

	public void add(SizeSet sizeSet) {
		count += sizeSet.count;
		size += sizeSet.size;
	}

	public long getCount() {
		return count;
	}

	public long getSize() {
		return size;
	}

	@Override
	public String toString() {
		return "Size [count=" + count + ", size=" + size + "]";
	}
}