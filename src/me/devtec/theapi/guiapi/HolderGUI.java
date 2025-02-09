package me.devtec.theapi.guiapi;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.devtec.theapi.guiapi.GUI.ClickType;

public interface HolderGUI {
	public String getTitle();

	public void setTitle(String title);

	public int size();

	public Collection<Player> getPlayers();
	
	public void setItem(int slot, ItemGUI item);
	
	public ItemGUI getItemGUI(int slot);
	
	public ItemStack getItem(int slot);

	public void onClose(Player player);

	public boolean onIteractItem(Player player, ItemStack item, ClickType type, int slot, boolean gui);

	public boolean isInsertable();

	public void setInsertable(boolean value);

	public void remove(int slot);

	public void close(Player...players);

	public void close();

	public void clear();
	
	public Object getContainer(Player player);

	public void closeWithoutPacket(Player... p);

	public Inventory getInventory();
}
