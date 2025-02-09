package me.devtec.theapi.worldsapi;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

import me.devtec.theapi.TheAPI;

public class voidGenerator extends ChunkGenerator {
	private static final Biome the_void = TheAPI.isNewVersion() ? Biome.valueOf("THE_VOID") : Biome.valueOf("VOID");

	@Override
	public ChunkGenerator.ChunkData generateChunkData(World world, Random random, int chunkx, int chunkz,
			ChunkGenerator.BiomeGrid biome) {
		ChunkGenerator.ChunkData data = this.createChunkData(world);
		biome.setBiome(chunkx, chunkz, the_void);
		return data;
	}

    public boolean canSpawn(World world, int x, int z) {
    	return true;
    }

    public Location getFixedSpawnLocation(World world, Random random) {
    	Location spawnLocation = new Location(world, 0, 65, 0);
    	Location blockLocation = spawnLocation.clone().subtract(0, 2, 0);
    	if(blockLocation.getBlock().getType()==Material.AIR)
    		blockLocation.getBlock().setType(Material.GLASS);
    	return spawnLocation;
    }
}
