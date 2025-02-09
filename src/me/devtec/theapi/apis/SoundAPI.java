package me.devtec.theapi.apis;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.devtec.theapi.utils.Position;
import me.devtec.theapi.utils.theapiutils.Validator;

public class SoundAPI {
	public static boolean existSound(String sound) {
		return valueOf(sound) != null;
	}

	public static void playSound(Player player, String sound, float volume, float pitch) {
		playSound(new Position(player.getLocation()), sound, volume, pitch);
	}

	public static void playSound(Location where, String sound, float volume, float pitch) {
		playSound(new Position(where), sound, volume, pitch);
	}

	public static void playSound(Position where, String sound, float volume, float pitch) {
		Validator.validate(where == null || where.getWorld() == null, "Location is null");
		Validator.validate(sound == null || getByName(sound) == null, "Sound is null");
		where.getWorld().playSound(where.toLocation(), getByName(sound.toUpperCase()), volume, pitch);
	}

	public static void playSound(Player player, Sound sound, float volume, float pitch) {
		playSound(new Position(player.getLocation()), sound.name(), volume, pitch);
	}

	public static void playSound(Position where, Sound sound, float volume, float pitch) {
		playSound(where, sound.name(), volume, pitch);
	}

	public static void playSound(Location where, Sound sound, float volume, float pitch) {
		playSound(new Position(where), sound.name(), volume, pitch);
	}

	public static void playSound(Player player, String sound) {
		playSound(player, sound, 1, 1);
	}

	public static void playSound(Location where, String sound) {
		playSound(where, sound, 1, 1);
	}

	public static void playSound(Player player, Sound sound) {
		playSound(player, sound, 1, 1);
	}

	public static void playSound(Location where, Sound sound) {
		playSound(where, sound, 1, 1);
	}

	public static Sound[] values() {
		return Sound.values();
	}

	public static List<Sound> valuesInList() {
		return Arrays.asList(values());
	}

	/**
	 * 
	 * @param sound Name of sound
	 * @return Sound
	 */
	public static Sound valueOf(String sound) {
		return getByName(sound);
	}

	/**
	 * 
	 * @param sound Name of sound
	 * @return Sound
	 */
	public static Sound getByName(String sound) {
		String a = null, c = null;
		for (Sound s : values()) {
			if (s.name().toLowerCase().equals(sound.toLowerCase())) {
				a = s.name();
				break;
			} else if (s.name().toLowerCase().contains(sound.toLowerCase()))
				c = s.name();
		}
		if (a == null && c != null)
			a = c;
		try {
			if (Sound.valueOf(a.toUpperCase()) != null)
				return Sound.valueOf(a.toUpperCase());
		} catch (Exception find) {

		}
		return null;
	}
}
