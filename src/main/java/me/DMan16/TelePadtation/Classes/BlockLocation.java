package me.DMan16.TelePadtation.Classes;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class BlockLocation implements Cloneable {
	private final @NotNull World world;
	private final int x;
	private final int y;
	private final int z;
	
	public BlockLocation(@NotNull World world,int x,int y,int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@NotNull
	public World world() {
		return world;
	}
	
	public int x() {
		return x;
	}
	
	public int y() {
		return y;
	}
	
	public int z() {
		return z;
	}
	
	public BlockLocation(@NotNull World world,@NotNull Location location) {
		this(world,location.getBlockX(),location.getBlockY(),location.getBlockZ());
	}
	
	public BlockLocation(@NotNull Block block) throws NullPointerException {
		this(block.getWorld(),block.getX(),block.getY(),block.getZ());
	}
	
	@NotNull
	@Contract(pure = true)
	public BlockLocation add(int x,int y,int z) {
		long newX = ((long) x()) + x;
		if (newX > Integer.MAX_VALUE || newX < Integer.MIN_VALUE) throw new ArithmeticException("X overflow!");
		long newY = ((long) y()) + y;
		if (newY > Integer.MAX_VALUE || newY < Integer.MIN_VALUE) throw new ArithmeticException("Y overflow!");
		long newZ = ((long) z()) + z;
		if (newZ > Integer.MAX_VALUE || newZ < Integer.MIN_VALUE) throw new ArithmeticException("Z overflow!");
		return new BlockLocation(world(),(int) newX,(int) newY,(int) newZ);
	}
	
	@NotNull
	@Contract(pure = true)
	public BlockLocation subtract(int x,int y,int z) {
		return add(-x,-y,-z);
	}
	
	@NotNull
	@Contract("_ -> new")
	public BlockLocation withX(int x) {
		return new BlockLocation(world(),x,y(),z());
	}
	
	@NotNull
	@Contract("_ -> new")
	public BlockLocation withY(int y) {
		return new BlockLocation(world(),x(),y,z());
	}
	
	@NotNull
	@Contract("_ -> new")
	public BlockLocation withZ(int z) {
		return new BlockLocation(world(),x(),y(),z);
	}
	
	@NotNull
	@Contract("_ -> new")
	public BlockLocation withWorld(@NotNull World world) {
		return new BlockLocation(world,x(),y(),z());
	}
	
	@NotNull
	@Contract(" -> new")
	public Location toLocation() {
		return new Location(world(),x(),y(),z());
	}
	
	@NotNull
	@Contract(" -> new")
	public Block toBlock() {
		return world().getBlockAt(x(),y(),z());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BlockLocation)) return false;
		BlockLocation loc = (BlockLocation) obj;
		return loc.world().equals(world()) && loc.x() == x() && loc.y() == y() && loc.z() == z();
	}
	
	@Override
	public int hashCode() {
		int hash = 3;
		hash = 19 * hash + world().hashCode();
		hash = 19 * hash + (int)(Double.doubleToLongBits(x()) ^ Double.doubleToLongBits(x()) >>> 32);
		hash = 19 * hash + (int)(Double.doubleToLongBits(y()) ^ Double.doubleToLongBits(y()) >>> 32);
		hash = 19 * hash + (int)(Double.doubleToLongBits(z()) ^ Double.doubleToLongBits(z()) >>> 32);
		return hash;
	}
	
	@Override
	@NotNull
	public BlockLocation clone() {
		try {
			return (BlockLocation) super.clone();
		} catch (CloneNotSupportedException e) {
			return new BlockLocation(world(),x(),y(),z());
		}
	}
	
	public int compareDistance(@NotNull BlockLocation loc1,@NotNull BlockLocation loc2) {
		long diffX1 = x - loc1.x(),diffY1 = y - loc1.y(),diffZ1 = z - loc1.z(),diffX2 = x - loc2.x(),diffY2 = y - loc2.y(),diffZ2 = z - loc2.z();
		return Long.compare((diffX1 * diffX1) + (diffY1 * diffY1) + (diffZ1 * diffZ1),(diffX2 * diffX2) + (diffY2 * diffY2) + (diffZ2 * diffZ2));
	}
}