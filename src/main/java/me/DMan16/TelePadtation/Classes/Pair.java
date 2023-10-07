package me.DMan16.TelePadtation.Classes;

public final class Pair<V,U> {
	private final V first;
	private final U second;
	
	public Pair(V first,U second) {
		this.first = first;
		this.second = second;
	}
	
	public V first() {
		return this.first;
	}
	
	public U second() {
		return this.second;
	}
}