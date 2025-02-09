package me.devtec.theapi.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;

import me.devtec.theapi.TheAPI;
import me.devtec.theapi.utils.reflections.Ref;

public class Position implements Cloneable {

	public Position() {
	}

	public Position(World world) {
		w = world.getName();
	}

	public Position(String world) {
		w = world;
	}

	public Position(World world, double x, double y, double z) {
		this(world, x, y, z, 0, 0);
	}

	public Position(World world, double x, double y, double z, float yaw, float pitch) {
		w = world.getName();
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public Position(String world, double x, double y, double z) {
		this(world, x, y, z, 0, 0);
	}

	public Position(String world, double x, double y, double z, float yaw, float pitch) {
		w = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public Position(double x, double y, double z) {
		this(x, y, z, 0, 0);
	}

	public Position(double x, double y, double z, float yaw, float pitch) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public Position(Location location) {
		w = location.getWorld().getName();
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}

	public Position(Block b) {
		this(b.getLocation());
	}

	public Position(Entity b) {
		this(b.getLocation());
	}

	public static Position fromString(String stored) {
		try {
			stored = stored.substring(10, stored.length() - 1);
			String[] part = stored.replace(":", ".").split("/");
			return new Position(part[0], StringUtils.getDouble(part[1]), StringUtils.getDouble(part[2]), StringUtils.getDouble(part[3]), StringUtils.getFloat(part[4]), StringUtils.getFloat(part[5]));
		} catch (Exception notMat) {
			Location loc = StringUtils.getLocationFromString(stored);
			if (loc != null)
				return new Position(loc);
		}
		return null;
	}

	public static Position fromBlock(Block block) {
		if (block != null)
			return new Position(block.getLocation());
		return null;
	}

	public static Position fromLocation(Location location) {
		if (location != null)
			return new Position(location);
		return null;
	}

	private String w;
	private double x, y, z;
	private float yaw, pitch;

	@Override
	public String toString() {
		return ("[Position:" + w + "/" + x + "/" + y + "/" + z + "/" + yaw + "/" + pitch + ']').replace(".", ":");
	}

	public int hashCode() {
		int hashCode = 1;
		hashCode = 31 * hashCode + w.hashCode();
		hashCode = (int) (31 * hashCode + x);
		hashCode = (int) (31 * hashCode + y);
		hashCode = (int) (31 * hashCode + z);
		hashCode = (int) (31 * hashCode + yaw);
		hashCode = (int) (31 * hashCode + pitch);
		return hashCode;
	}

	public Biome getBiome() {
		return getBlock().getBiome();
	}

	public int getData() {
		return TheAPI.isOlderThan(8)?(byte)Ref.invoke(getNMSChunk(), getdata, getBlockX() & 0xF, getBlockY() & 0xF, getBlockZ() & 0xF):getType().getData();
	}

	public Material getBukkitType() {
		return getType().getType();
	}

	private static Method getdata;
	static {
		if(TheAPI.isOlderThan(8))
			getdata=Ref.method(Ref.nmsOrOld("world.level.chunk.ChunkSection","ChunkSection"), "getData", int.class, int.class, int.class);
	}
	
	public Object getIBlockData() {
		Object sc = ((Object[]) Ref.invoke(getNMSChunk(), get))[getBlockY() >> 4];
		if (sc == null)return new TheMaterial(Material.AIR, 0);
		//1.7.10
		TheAPI.isOlderThan(8);
		return Ref.invoke(sc, getType, getBlockX() & 15, getBlockY() & 15, getBlockZ() & 15);
	}
	
	private static Method getType = Ref.method(Ref.nmsOrOld("world.level.chunk.ChunkSection","ChunkSection"), "getType", int.class, int.class, int.class);
	static {
		if(getType==null)getType = Ref.method(Ref.nms("ChunkSection"), "getTypeId", int.class, int.class, int.class);
	}
	public TheMaterial getType() {
		Object sc = ((Object[]) Ref.invoke(getNMSChunk(), get))[getBlockY() >> 4];
		if (sc == null)return new TheMaterial(Material.AIR, 0);
		if(TheAPI.isOlderThan(8)) //1.7.10
			return TheMaterial.fromData(Ref.invoke(sc, getType, getBlockX() & 15, getBlockY() & 15, getBlockZ() & 15), (byte)(int)Ref.invoke(sc, getdata, getBlockX() & 15, getBlockY() & 15, getBlockZ() & 15));
		return TheMaterial.fromData(Ref.invoke(sc, getType, getBlockX() & 15, getBlockY() & 15, getBlockZ() & 15));
	}

	public Position subtract(double x, double y, double z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	public Position subtract(Position position) {
		this.x -= position.getX();
		this.y -= position.getY();
		this.z -= position.getZ();
		return this;
	}

	public Position subtract(Location location) {
		this.x -= location.getX();
		this.y -= location.getY();
		this.z -= location.getZ();
		return this;
	}

	public String getWorldName() {
		return w;
	}

	public Position setWorld(World world) {
		w = world.getName();
		return this;
	}

	public Position setX(double x) {
		this.x = x;
		return this;
	}

	public Position setY(double y) {
		this.y = y;
		return this;
	}

	public Position setZ(double z) {
		this.z = z;
		return this;
	}

	public Position setYaw(float yaw) {
		this.yaw = yaw;
		return this;
	}

	public Position setPitch(float pitch) {
		this.pitch = pitch;
		return this;
	}

	public double distance(Location location) {
		return Math.sqrt(distanceSquared(location));
	}

	public double distance(Position position) {
		return Math.sqrt(distanceSquared(position));
	}

	public Position multiply(double m) {
		x *= m;
		y *= m;
		z *= m;
		return this;
	}

	public Position zero() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
		return this;
	}

	public double length() {
		return Math.sqrt(lengthSquared());
	}

	public double lengthSquared() {
		return square(x) + square(y) + square(z);
	}

	public double distanceSquared(Location location) {
		return square(this.x - location.getX()) + square(this.y - location.getY()) + square(this.z - location.getZ());
	}

	public double distanceSquared(Position position) {
		return square(this.x - position.x) + square(this.y - position.y) + square(this.z - position.z);
	}

	private double square(double d) {
		return d * d;
	}

	public Chunk getChunk() {
		if(TheAPI.isNewVersion())
			return getWorld().getChunkAt(getBlockX() >> 4, getBlockZ() >> 4);
		return (Chunk) Ref.get(getNMSChunk(), f);
	}

	private static final int wf = StringUtils.getInt(TheAPI.getServerVersion().split("_")[1]);
	private static final Field f = Ref.field(Ref.nmsOrOld("world.level.chunk.Chunk","Chunk"), "bukkitChunk");
	private static final Field chunkProv = Ref.field(Ref.nms("World"), "chunkProviderServer");
	private static Method getOrCreate=Ref.method(Ref.nms("ChunkProviderServer"), "getOrCreateChunk", int.class, int.class);
	private static final Method handle=Ref.method(Ref.craft("CraftChunk"), "getHandle");
	static {
		if(getOrCreate==null)
			getOrCreate=Ref.method(Ref.nms("ChunkProviderServer"), "getOrLoadChunkAt", int.class, int.class);
	}
	static final Class<?> cchunk = Ref.craft("CraftChunk");
	
	public Object getNMSChunk() {
		try {
			if(TheAPI.isNewVersion())return Ref.invoke(Ref.cast(cchunk, getWorld().getChunkAt(getBlockX()>>4, getBlockZ()>>4)), handle);
			return Ref.invoke(Ref.get(Ref.world(getWorld()), chunkProv), getOrCreate, getBlockX() >> 4, getBlockZ() >> 4);
		} catch (Exception er) {
		}
		return null;
	}
	
	public Object getBlockPosition() {
		return (wf <= 7 ? Ref.newInstance(old, getBlockX(), getBlockY(), getBlockZ()) : Ref.blockPos(getBlockX(), getBlockY(), getBlockZ()));
	}

	public ChunkSnapshot getChunkSnapshot() {
		return getChunk().getChunkSnapshot();
	}

	public Block getBlock() {
		return getWorld().getBlockAt(getBlockX(), getBlockY(), getBlockZ());
	}

	public World getWorld() {
		return Bukkit.getWorld(w);
	}

	public Position add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Position add(Position position) {
		this.x += position.getX();
		this.y += position.getY();
		this.z += position.getZ();
		return this;
	}

	public Position add(Location location) {
		this.x += location.getX();
		this.y += location.getY();
		this.z += location.getZ();
		return this;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public int getBlockX() {
		int floor = (int) x;
		return (double) floor == x ? floor : floor - (int) (Double.doubleToRawLongBits(x) >>> 63);
	}

	public int getBlockY() {
		int floor = (int) y;
		return (double) floor == y ? floor : floor - (int) (Double.doubleToRawLongBits(y) >>> 63);
	}

	public int getBlockZ() {
		int floor = (int) z;
		return (double) floor == z ? floor : floor - (int) (Double.doubleToRawLongBits(z) >>> 63);
	}

	public float getYaw() {
		return yaw;
	}

	public float getPitch() {
		return pitch;
	}

	public Location toLocation() {
		return new Location(Bukkit.getWorld(w), x, y, z, yaw, pitch);
	}

	public long setType(Material with) {
		return setType(new TheMaterial(with));
	}

	public long setType(Material with, int data) {
		return setType(new TheMaterial(with, data));
	}

	public long setType(TheMaterial with) {
		return set(this, with);
	}

	public void setTypeAndUpdate(Material with) {
		setTypeAndUpdate(new TheMaterial(with));
	}

	public void setTypeAndUpdate(Material with, int data) {
		setTypeAndUpdate(new TheMaterial(with, data));
	}
	
	public void setTypeAndUpdate(TheMaterial with) {
		setType(with);
		Position.updateBlockAt(this);
		Position.updateLightAt(this);
	}

	@Override
	public boolean equals(Object a) {
		if (a instanceof Position) {
			Position s = (Position) a;
			return w.equals(s.getWorld().getName()) && s.getX() == x && s.getY() == y && s.getZ() == z
					&& s.getPitch() == pitch && s.getYaw() == yaw;
		}
		if (a instanceof Location) {
			Location s = (Location) a;
			return w.equals(s.getWorld().getName()) && s.getX() == x && s.getY() == y && s.getZ() == z
					&& s.getPitch() == pitch && s.getYaw() == yaw;
		}
		return false;
	}

	private static Constructor<?> c;
	private static int t;
	static {
		c=Ref.constructor(Ref.nmsOrOld("network.protocol.game.PacketPlayOutBlockChange","PacketPlayOutBlockChange"), Ref.nms("World"), Ref.nms("BlockPosition"));
		if(c==null)
			c=Ref.constructor(Ref.nmsOrOld("network.protocol.game.PacketPlayOutBlockChange","PacketPlayOutBlockChange"), Ref.nms("IBlockAccess"), Ref.nms("BlockPosition"));
		if(c==null) {
			c=Ref.constructor(Ref.nmsOrOld("network.protocol.game.PacketPlayOutBlockChange","PacketPlayOutBlockChange"), int.class, int.class, int.class, Ref.nms("World"));
			++t;
		}
	}
	
	public static void updateBlockAt(Position pos) {
		Ref.sendPacket(pos.getWorld().getPlayers(), t==0?Ref.newInstance(c, Ref.world(pos.getWorld()),pos.getBlockPosition()):
			Ref.newInstance(c, pos.getBlockX(),pos.getBlockY(),pos.getBlockZ(),Ref.world(pos.getWorld())));
	}

	private static final boolean aww = TheAPI.isOlderThan(8);
	private static Method updateLight = Ref.method(Ref.nmsOrOld("world.level.lighting.LightEngine","LightEngine"), "a", Ref.nmsOrOld("core.BlockPosition","BlockPosition"), int.class);
	private static Method getEngine;
	static {
		if(updateLight==null) {
			updateLight = Ref.method(Ref.nmsOrOld("world.level.chunk.Chunk","Chunk"), "initLighting");
		}else {
			getEngine=Ref.method(Ref.nmsOrOld("world.level.chunk.Chunk","Chunk"), "e");
		}
	}

	public static void updateLightAt(Position pos) {
		if (aww)
			Ref.invoke(pos.getNMSChunk(), updateLight);
		else
			Ref.invoke(Ref.invoke(pos.getNMSChunk(), getEngine), updateLight, pos.getBlockPosition(), 15);
	}

	public static long set(Position pos, TheMaterial mat) {
		if (wf <= 7)
			setOld(pos, mat.getBlock(), mat.getData());
		else
			if(TheAPI.isNewerThan(16))set(pos, mat.getIBlockData());
			else
			set(pos, wf >= 9, wf >= 14, mat.getIBlockData());
		return pos.getChunkKey();
	}
	
	public long getChunkKey() {
		long k = (getBlockX() >> 4 & 0xFFFF0000L) << 16L | (getBlockX() >> 4 & 0xFFFFL);
		k |= (getBlockZ() >> 4 & 0xFFFF0000L) << 32L | (getBlockZ() >> 4 & 0xFFFFL) << 16L;
		return k;
	}

	public void setState(BlockState state) {
		setState(this, state);
	}

	public void setBlockData(BlockData state) {
		setBlockData(this, state);
	}

	public void setStateAndUpdate(BlockState state) {
		setState(this, state);
		Position.updateBlockAt(this);
		Position.updateLightAt(this);
	}

	public void setBlockDataAndUpdate(BlockData state) {
		setBlockData(this, state);
		Position.updateBlockAt(this);
		Position.updateLightAt(this);
	}

	private static final Object air = new TheMaterial(Material.AIR).getIBlockData();
	public long setAir() {
		if (wf <= 7)
			setOld(this, 0,0);
		else
			if(TheAPI.isNewerThan(16))set(this, air);
			else
			set(this, wf >= 9, wf >= 14, air);
		return getChunkKey();
	}

	public void setAirAndUpdate() {
		setAir();
		Position.updateBlockAt(this);
		Position.updateLightAt(this);
	}
	
	public static long set(Location pos, int id, int data) {
		return set(new Position(pos), new TheMaterial(id, data));
	}

	/**
	 * 
	 * @param pos Location
	 */
	@SuppressWarnings("unchecked")
	private static synchronized void setOld(Position pos, Object block, int data) { // Uknown - 1.7.10
		Object c = pos.getNMSChunk();
		Object sc = ((Object[]) Ref.invoke(c, get))[pos.getBlockY() >> 4];
		if (sc == null) {
			sc = Ref.newInstance(aw,pos.getBlockY() >> 4 << 4, true);
			((Object[]) Ref.invoke(c, get))[pos.getBlockY() >> 4] = sc;
		}
		Object ww = Ref.world(pos.getWorld());
		//REMOVE TILE ENTITY
		for(Iterator<?> r = ((Collection<?>)Ref.get(ww, ff)).iterator(); r.hasNext();) {
			if(Ref.get(r.next(), "x").equals(pos.getBlockX()) && Ref.get(r.next(), "y").equals(pos.getBlockY()) && Ref.get(r.next(), "z").equals(pos.getBlockZ())) {
				r.remove();
				break;
			}
		}
		//CHANGE BLOCK IN CHUNKSECTION
		Ref.invoke(sc, setId, pos.getBlockX() & 0xF, pos.getBlockY() & 0xF, pos.getBlockZ() & 0xF, block);
		Ref.invoke(sc, setData, pos.getBlockX() & 0xF, pos.getBlockY() & 0xF, pos.getBlockZ() & 0xF, data);
		//ADD TILE ENTITY
		Object tt = Ref.invoke(c, cgett, pos.getBlockX() & 0xF, pos.getBlockY(), pos.getBlockZ() & 0xF);
		if(cont.isInstance(tt)) {
			tt=Ref.invoke(tt, tile, ww, 0);
			Ref.set(tt, "x", pos.getBlockX());
			Ref.set(tt, "y", pos.getBlockY());
			Ref.set(tt, "z", pos.getBlockZ());
			Ref.invoke(tt, "t");
			Ref.set(tt, "h", tt);
			Ref.set(tt, "world", ww);
			((Collection<Object>)Ref.get(ww, ff)).add(tt);
		}
	}

	private static Constructor<?> aw = Ref.constructor(Ref.nmsOrOld("world.level.chunk.ChunkSection","ChunkSection"), int.class);
	private static final Constructor<?> old = Ref.constructor(Ref.nms("ChunkPosition"), double.class, double.class, double.class);
	private static Method a;
	private static final Method get = Ref.method(Ref.nmsOrOld("world.level.chunk.Chunk","Chunk"), "getSections");
	private static Method setId;
	private static Method setData;
	private static int ii;
	static {
		a = Ref.method(Ref.nmsOrOld("world.level.chunk.ChunkSection","ChunkSection"), "setType", int.class, int.class, int.class, Ref.nmsOrOld("world.level.block.state.IBlockData","IBlockData"), boolean.class);
		if(a==null) {
			a = Ref.method(Ref.nmsOrOld("world.level.chunk.ChunkSection","ChunkSection"), "setType", int.class, int.class, int.class, Ref.nmsOrOld("world.level.block.state.IBlockData","IBlockData"));
			++ii;
		}if (aw == null)
			aw = Ref.constructor(Ref.nmsOrOld("world.level.chunk.ChunkSection","ChunkSection"), int.class, boolean.class);
		if(TheAPI.isOlderThan(8)) {
			setId=Ref.method(Ref.nmsOrOld("world.level.chunk.ChunkSection","ChunkSection"), "setTypeId", int.class, int.class, int.class, Ref.nmsOrOld("world.level.block.Block","Block"));
			setData=Ref.method(Ref.nmsOrOld("world.level.chunk.ChunkSection","ChunkSection"), "setData", int.class, int.class, int.class, int.class);
		}
	}
	private static final Method getBlock = Ref.method(Ref.craft("util.CraftMagicNumbers"), "getBlock", Material.class);
	private static final Method fromLegacyData=Ref.method(Ref.nmsOrOld("world.level.block.Block","Block"), "fromLegacyData", int.class);
	
	public static void setBlockData(Position pos, BlockData data) {
		if(data==null||!TheAPI.isNewVersion() || pos == null)return;
		if(TheAPI.isNewerThan(16))set(pos, Ref.invoke(data,"getState"));
		else
		set(pos, true, wf >= 14, Ref.invoke(data,"getState"));
	}
	
	public static void setState(Position pos, BlockState state) {
		if(state==null || pos == null)return;
		if(TheAPI.isNewerThan(16))set(pos, Ref.invoke(Ref.invokeNulled(getBlock, state.getType()),fromLegacyData,(int)state.getRawData()));
		else
		set(pos, true, wf >= 14, Ref.invoke(Ref.invokeNulled(getBlock, state.getType()),fromLegacyData,(int)state.getRawData()));
	}
	

	private static final Class<?>b =  Ref.nmsOrOld("world.level.block.Block","Block");
	private static final Method bgetBlock = Ref.method(Ref.nmsOrOld("world.level.block.state.IBlockData", "IBlockData"), "getBlock");
	private static Method cgett;
	private static Field ff = Ref.field(Ref.nmsOrOld("world.level.chunk.Chunk","Chunk"), TheAPI.isNewerThan(16)?"l":"tileEntities");
	static {
		if(TheAPI.isOlderThan(8)) {
			ff=Ref.field(Ref.nms("Chunk"), "tileEntityList");
			cgett=Ref.method(Ref.nms("Chunk"), "getType", int.class, int.class, int.class);
		}
	}
	
	/**
	 * 
	 * @param pos   Location
	 * @param palet Is server version newer than 1.8? 1.9+
	 * @param neww  Is server version newer than 1.13? 1.14+
	 */
	@SuppressWarnings("unchecked")
	public static synchronized void set(Position pos, boolean palet, boolean neww, Object cr) { // 1.8 - 1.16
		if(pos==null||cr==null)return;
		Object c = pos.getNMSChunk();
		int y = pos.getBlockY();
		//CHECK IF CHUNKSECTION EXISTS
		Object sc = ((Object[]) Ref.invoke(c, get))[y >> 4];
		if (sc == null) {
			//CREATE NEW CHUNKSECTION
			sc = neww?Ref.newInstance(aw, y >> 4 << 4):Ref.newInstance(aw, y >> 4 << 4, true);
			((Object[]) Ref.invoke(c, get))[y >> 4] = sc;
		}
		
		Object p = pos.getBlockPosition();
		Object ww = Ref.world(pos.getWorld());
		//REMOVE TILE ENTITY FROM CHUNK
		((Map<?,?>)Ref.get(c, ff)).remove(p);
		
		if (palet) {
			//CHANGE BLOCK IN CHUNKSECTION (PALLETE)
			if(ii==0)
				Ref.invoke(sc, a, pos.getBlockX() & 0xF, y & 0xF, pos.getBlockZ() & 0xF, cr, true);
			else
				Ref.invoke(sc, a, pos.getBlockX() & 0xF, y & 0xF, pos.getBlockZ() & 0xF, cr);
		}else {
			//CHANGE BLOCK IN CHUNKSECTION
			Ref.invoke(sc, a, pos.getBlockX() & 0xF, y & 0xF, pos.getBlockZ() & 0xF, cr);
		}
		//ADD TILE ENTITY
		Object tt = cr.getClass().isAssignableFrom(b)?cr:Ref.invoke(cr, bgetBlock);
		if(cont.isInstance(tt)) {
		if(ver!=-1)
			tt=ver==0?Ref.invoke(tt, tile, ww):Ref.invoke(tt, tile, ww, 0);
		else
			tt=Ref.invoke(tt, tile, ww, 0);
			((Map<Object, Object>)Ref.get(c, ff)).put(p, tt);
		}
		setup(ww,p,cr,tt);
	}
	
	static Method getY = Ref.method(Ref.nmsOrOld("world.level.LevelHeightAccessor", "LevelHeightAccessor"), "getSectionIndex", int.class);
	
	/**
	 * 
	 * @param pos   Location
	 * @param palet Is server version newer than 1.8? 1.9+
	 * @param neww  Is server version newer than 1.13? 1.14+
	 */
	@SuppressWarnings("unchecked")
	public static synchronized void set(Position pos, Object cr) { // 1.17+
		if(pos==null||cr==null)return;
		Object c = pos.getNMSChunk();
		int y = pos.getBlockY();
		
		int fixedY = (int) Ref.invoke(c, getY, y);
		//CHECK IF CHUNKSECTION EXISTS
		Object sc = ((Object[]) Ref.invoke(c, get))[fixedY];
		if (sc == null) {
			//CREATE NEW CHUNKSECTION
			sc = Ref.newInstance(aw, y >> 4 << 4);
			((Object[]) Ref.invoke(c, get))[fixedY] = sc;
		}
		
		Object p = pos.getBlockPosition();
		Object ww = Ref.world(pos.getWorld());
		//REMOVE TILE ENTITY FROM CHUNK
		((Map<?,?>)Ref.get(c, ff)).remove(p);
		
		//CHANGE BLOCK IN CHUNKSECTION (PALLETE)
		if(ii==0)
			Ref.invoke(sc, a, pos.getBlockX() & 0xF, y & 0xF, pos.getBlockZ() & 0xF, cr, true);
		else
			Ref.invoke(sc, a, pos.getBlockX() & 0xF, y & 0xF, pos.getBlockZ() & 0xF, cr);
		//ADD TILE ENTITY
		Object tt = cr.getClass().isAssignableFrom(b)?cr:Ref.invoke(cr, bgetBlock);
		if(cont.isInstance(tt)) {
		if(ver!=-1)
			tt=ver==0?Ref.invoke(tt, tile, ww):Ref.invoke(tt, tile, ww, 0);
		else
			tt=Ref.invoke(tt, tile, ww, 0);
			((Map<Object, Object>)Ref.get(c, ff)).put(p, tt);
		}
		setup(ww,p,cr,tt);
	}
		
	private static void setup(Object ww, Object p, Object cr, Object tt) {
		Ref.set(tt, "world", ww);
		Ref.set(tt, "position", p);
		if(TheAPI.isNewVersion())
			Ref.set(tt, TheAPI.isNewerThan(13)?"c":"f", cr);
		else
		Ref.set(tt, "e", Ref.invoke(cr, bgetBlock));
		Ref.set(tt, TheAPI.isNewerThan(13)?"f":"d", false);
		Ref.sendPacket(TheAPI.getOnlinePlayers(), Ref.invoke(tt, "getUpdatePacket"));
	}
	
	static int ver = 0;
	private static Method tile;
	private static final Class<?> cont = Ref.nmsOrOld("world.level.block.ITileEntity","ITileEntity") == null ? Ref.nms("IContainer") : Ref.nmsOrOld("world.level.block.ITileEntity","ITileEntity");
	static {
		if(TheAPI.isNewerThan(8)) {
			tile = Ref.method(Ref.nmsOrOld("world.level.block.ITileEntity","ITileEntity"), "createTile", Ref.nmsOrOld("world.level.IBlockAccess","IBlockAccess"));
			if(tile==null) {
				tile = Ref.method(Ref.nmsOrOld("world.level.block.ITileEntity","ITileEntity"), "a", Ref.nmsOrOld("world.level.World","World"), int.class);
				if(tile==null) {
					tile = Ref.method(Ref.nmsOrOld("world.level.block.ITileEntity","ITileEntity"), "a", Ref.nmsOrOld("world.level.IBlockAccess","IBlockAccess"));
				}else ver = 1;
			}
		}else {
			ver=-1;
			tile = Ref.method(Ref.nms("IContainer"), "a", Ref.nmsOrOld("world.level.World","World"), int.class);
		}
	}

	public Position clone() {
		return new Position(w, x, y, z, yaw, pitch);
	}
}
