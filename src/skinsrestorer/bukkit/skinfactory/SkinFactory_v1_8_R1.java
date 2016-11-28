package skinsrestorer.bukkit.skinfactory;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.EnumGamemode;
import net.minecraft.server.v1_8_R1.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R1.PacketPlayOutHeldItemSlot;
import net.minecraft.server.v1_8_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R1.PacketPlayOutRespawn;
import net.minecraft.server.v1_8_R1.PlayerConnection;
import skinsrestorer.bukkit.SkinsRestorer;

public class SkinFactory_v1_8_R1 extends SkinFactory {

	@SuppressWarnings("deprecation")
	@Override
	public void updateSkin(Player p) {
		try {
			if (!p.isOnline())
				return;

			CraftPlayer cp = (CraftPlayer) p;
			EntityPlayer ep = cp.getHandle();
			int entId = ep.getId();

			PacketPlayOutPlayerInfo removeInfo = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, ep);

			PacketPlayOutEntityDestroy removeEntity = new PacketPlayOutEntityDestroy(entId);

			PacketPlayOutNamedEntitySpawn addNamed = new PacketPlayOutNamedEntitySpawn(ep);

			PacketPlayOutPlayerInfo addInfo = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, ep);

			PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(ep.getWorld().worldProvider.getDimension(),
					ep.getWorld().getDifficulty(), ep.getWorld().worldData.getType(),
					EnumGamemode.getById(p.getGameMode().getValue()));

			PacketPlayOutEntityEquipment itemhand = new PacketPlayOutEntityEquipment(entId, 0,
					CraftItemStack.asNMSCopy(p.getItemInHand()));

			PacketPlayOutEntityEquipment helmet = new PacketPlayOutEntityEquipment(entId, 4,
					CraftItemStack.asNMSCopy(p.getInventory().getHelmet()));

			PacketPlayOutEntityEquipment chestplate = new PacketPlayOutEntityEquipment(entId, 3,
					CraftItemStack.asNMSCopy(p.getInventory().getChestplate()));

			PacketPlayOutEntityEquipment leggings = new PacketPlayOutEntityEquipment(entId, 2,
					CraftItemStack.asNMSCopy(p.getInventory().getLeggings()));

			PacketPlayOutEntityEquipment boots = new PacketPlayOutEntityEquipment(entId, 1,
					CraftItemStack.asNMSCopy(p.getInventory().getBoots()));

			PacketPlayOutHeldItemSlot slot = new PacketPlayOutHeldItemSlot(p.getInventory().getHeldItemSlot());

			for (Player pOnline : ((CraftServer) Bukkit.getServer()).getOnlinePlayers()) {
				final CraftPlayer craftOnline = (CraftPlayer) pOnline;
				PlayerConnection con = craftOnline.getHandle().playerConnection;
				if (pOnline.getName().equals(p.getName())) {
					con.sendPacket(removeInfo);
					con.sendPacket(addInfo);
					con.sendPacket(respawn);
					con.sendPacket(slot);
					craftOnline.updateScaledHealth();
					craftOnline.getHandle().triggerHealthUpdate();
					craftOnline.updateInventory();
					Bukkit.getScheduler().runTask(SkinsRestorer.getInstance(), new Runnable() {

						@Override
						public void run() {
							craftOnline.getHandle().updateAbilities();
						}

					});
					continue;
				}
				if (pOnline.canSee(p)) {
					con.sendPacket(removeEntity);
					con.sendPacket(removeInfo);
					con.sendPacket(addInfo);
					con.sendPacket(addNamed);
					con.sendPacket(itemhand);
					con.sendPacket(helmet);
					con.sendPacket(chestplate);
					con.sendPacket(leggings);
					con.sendPacket(boots);
				}
			}
		} catch (Exception e) {
		}

	}

}
