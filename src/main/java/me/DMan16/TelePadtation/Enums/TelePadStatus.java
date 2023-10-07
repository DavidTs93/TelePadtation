package me.DMan16.TelePadtation.Enums;

public enum TelePadStatus {
	PORTABLE,
	ACTIVE,
	OBSTRUCTED,
	INACTIVE,
	GLOBAL;
	
	public boolean isActive() {
		return !isInactive();
	}
	
	public boolean isInactive() {
		return this == INACTIVE || this == OBSTRUCTED;
	}
}