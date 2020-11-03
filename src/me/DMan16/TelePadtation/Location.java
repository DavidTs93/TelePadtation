package me.DMan16.TelePadtation;

public class Location {
	final String world;
	final int x;
	final int y;
	final int z;

	public Location(org.bukkit.Location location) {
		this(location.getWorld().getName(),location.getBlockX(),location.getBlockY(),location.getBlockZ());
	}
	
	public Location(String world, int x, int y, int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (!(obj instanceof Location)) return false;
		Location location = (Location) obj;
		boolean world = this.world.equals(location.world);
		boolean x = Integer.compare(this.x,location.x) == 0;
		boolean y = Integer.compare(this.y,location.y) == 0;
		boolean z = Integer.compare(this.z,location.z) == 0;
		return world && x && y && z;
	}
	
	@Override
	public int hashCode(){
		return world.length();
	}
	
	Location add(int x, int y, int z) {
		return new Location(this.world,this.x + x,this.y + y,this.z + z);
	}
}