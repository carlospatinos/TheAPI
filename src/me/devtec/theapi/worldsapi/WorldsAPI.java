package me.devtec.theapi.worldsapi;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import me.devtec.theapi.TheAPI;

public class WorldsAPI {
	/**
	 * @return boolean if world exists
	 **/
	public static boolean existsWorld(String world) {
		return Bukkit.getWorld(world) != null;
	}

	/**
	 * @apiNote This method ReCreate world, working as Import world commmand in
	 *      Multiverse
	 * @param world
	 * @param generator
	 * @param type
	 * @return boolean if world was loaded
	 */
	public static boolean load(String world, Environment generator, WorldType type) {
		return create(world, generator, type, true, 0);
	}

	/**
	 * @apiNote Create new world, return boolean if world was created
	 * @param name      Name of world (Required)
	 * @param generator World generator type (Required)
	 * @param type      set null to create Void world
	 * @return boolean if world was created
	 */
	public static boolean create(String name, Environment generator, WorldType type) {
		return create(name, generator, type, true, 0);
	}

	/**
	 * @apiNote Create new world, return boolean if world was created
	 * @param name               Name of world (Required)
	 * @param generator          World generator type (Required)
	 * @param type               set null to create Void world
	 * @param generateStructures generate in world structures ?
	 * @param seed               set 0 to generate random
	 * @return boolean if world was created
	 */
	public static boolean create(String name, Environment generator, WorldType type, boolean generateStructures,
			long seed) {
		return create(name, generator, type,
				type == null ? (TheAPI.isOlderThan(9) ? new voidGenerator_1_8() : new voidGenerator()) : null,
				generateStructures, seed);
	}

	/**
	 * @apiNote Create new world, return boolean if world was created
	 * @param name               Name of world (Required)
	 * @param generator          World generator type (Required)
	 * @param type               Nullable
	 * @param Chunkgenerator     ChunkGenerator of world
	 * @param generateStructures generate in world structures ?
	 * @param seed               set 0 to generate random
	 * @return boolean if world was created
	 */
	public static boolean create(String name, Environment generator, WorldType type, ChunkGenerator Chunkgenerator,
			boolean generateStructures, long seed) {
		if (name == null || generator == null)
			return false;
		if (Bukkit.getWorld(name) == null) {
			boolean createdFolder = new File(name).exists();
			WorldCreator c = new WorldCreator(name);
			c.generateStructures(generateStructures);
			c.environment(generator);
			if (seed != 0)
				c.seed(seed);
			if (type != null)
				c.type(type);
			if (Chunkgenerator != null)
				c.generator(Chunkgenerator);
			World w = Bukkit.createWorld(c);
			if (type == null && !createdFolder) { //void generator
				w.setSpawnLocation(0, 62, 0);
			}
			return true;
		}
		return false;
	}

	private static boolean deleteDirectory(File path) {
		if (path.exists()) {
			for (File f : path.listFiles()) {
				if (f.isDirectory()) {
					deleteDirectory(f);
				} else {
					f.delete();
				}
			}
		}
		return (path.delete());
	}

	public static boolean delete(World name, boolean safeUnloadWorld) {
		return delete(name, safeUnloadWorld, true);
	}

	public static boolean delete(World name, boolean safeUnloadWorld, boolean keepFolder) {
		if (name == null)
			return false;
		List<World> w = Bukkit.getWorlds();
		w.remove(name);
		if (!w.isEmpty()) {
			unloadWorld(name.getName(), safeUnloadWorld);
			if (keepFolder)
				return true;
			else
				return deleteDirectory(name.getWorldFolder());
		}
		return false;
	}

	public static boolean unloadWorld(String name) {
		return unloadWorld(name, true);
	}

	public static boolean unloadWorld(String name, boolean saveWorld) {
		if (name == null)
			return false;
		if (Bukkit.getWorld(name) != null) {
			List<World> w = Bukkit.getWorlds();
			w.remove(Bukkit.getWorld(name));
			if (!w.isEmpty()) {
				for (Player p : TheAPI.getOnlinePlayers())
					if (p.getWorld().getName().equals(name)) {
						p.setNoDamageTicks(30);
						p.teleport(w.get(0).getHighestBlockAt(w.get(0).getSpawnLocation()).getLocation());
					}
				Bukkit.unloadWorld(name, saveWorld);
				return true;
			}
			return false;
		}
		return false;
	}
}
