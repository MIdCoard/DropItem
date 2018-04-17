package com.focess.dropitem.item;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import com.focess.dropitem.Debug;
import com.focess.dropitem.DropItem;
import com.focess.dropitem.event.DropItemDeathEvent;
import com.focess.dropitem.event.DropItemDeathEvent.DeathCause;
import com.focess.dropitem.event.DropItemSpawnEvent;
import com.focess.dropitem.util.AnxiCode;
import com.focess.dropitem.util.Array;

public class CraftDropItem {
    static class ItemStackAngle {
        private final Material material;
        private final int px;
        private final int py;
        private final int pz;

        ItemStackAngle(final Material material, final int px, final int py, final int pz) {
            this.material = material;
            this.px = px;
            this.py = py;
            this.pz = pz;
        }

        Material getMaterial() {
            return this.material;
        }

        int getPx() {
            return this.px;
        }

        int getPy() {
            return this.py;
        }

        int getPz() {
            return this.pz;
        }
    }

    private static int anxiCode;
    private static DropItem drop;
    private static Array<EntityDropItem> droppedItems = new Array<>();
    private static String PickForm;
    private static int pitchX;
    private static int pitchY;
    private static int pitchZ;
    private static double height;
    private static Array<String> uuids = new Array<>();

    private static Array<ItemStackAngle> isas = new Array<>();

    public static EntityDropItem getDropItem(final Entity entity) {
        for (final EntityDropItem d : CraftDropItem.droppedItems)
            if (entity.getUniqueId().equals(d.getUniqueId()))
                return d;
        return null;
    }

    public static Array<EntityDropItem> getDropItems(final int anxiCode) {
        try {
            if (CraftDropItem.anxiCode == anxiCode)
                return CraftDropItem.droppedItems;
            AnxiCode.shut(CraftDropItem.class);
            return null;
        } catch (final Exception e) {
            Debug.debug(e, "Something wrong in getting ArmorStands.");
            return null;
        }
    }

    public static boolean include(final Entity dropItem) {
        for (final EntityDropItem d : CraftDropItem.droppedItems)
            if (d.getUniqueId().equals(dropItem.getUniqueId()))
                return true;
        return false;
    }

    @SuppressWarnings("deprecation")
    public static void loadItem(final DropItem dropItem) {
        try {
            CraftDropItem.anxiCode = AnxiCode.getCode(CraftDropItem.class, dropItem);
            CraftDropItem.drop = dropItem;
            CraftDropItem.pitchX = CraftDropItem.drop.getConfig().getInt("PitchX");
            CraftDropItem.pitchY = CraftDropItem.drop.getConfig().getInt("PitchY");
            CraftDropItem.pitchZ = CraftDropItem.drop.getConfig().getInt("PitchZ");
            CraftDropItem.PickForm = CraftDropItem.drop.getConfig().getString("PickForm");
            CraftDropItem.height = CraftDropItem.drop.getConfig().getDouble("Height");
            final File drops = new File(CraftDropItem.drop.getDataFolder(), "drops");
            final File[] files = drops.listFiles();
            for (final File file : files)
                CraftDropItem.uuids.add(file.getName());
            final List<String> angles = CraftDropItem.drop.getConfig().getStringList("Angles");
            for (final String angle : angles) {
                final String[] temp = angle.trim().split(" ");
                if (temp.length != 4)
                    continue;
                try {
                    int id = Integer.parseInt(temp[0]);
                    if (Material.getMaterial(id) == null)
                        continue;
                    CraftDropItem.isas.add(new ItemStackAngle(Material.getMaterial(id), Integer.parseInt(temp[1]),
                            Integer.parseInt(temp[2]), Integer.parseInt(temp[3])));
                } catch (Exception e) {
                    if (Material.getMaterial(temp[0]) == null)
                        continue;
                    CraftDropItem.isas.add(new ItemStackAngle(Material.getMaterial(temp[0]), Integer.parseInt(temp[1]),
                            Integer.parseInt(temp[2]), Integer.parseInt(temp[3])));
                }

            }
        } catch (final Exception e) {
            Debug.debug(e, "Something wrong in loading config.");
        }
    }

    public static void loadItem(final Entity dropItem, final int anxiCode) {
        if (anxiCode == CraftDropItem.anxiCode) {
            if (CraftDropItem.uuids.contains(dropItem.getUniqueId().toString())) {
                final EntityDropItem entityDropItem = EntityDropItem.getEntityDropItem(dropItem);
                CraftDropItem.droppedItems.add(entityDropItem);
                DropItemInfo.registerInfo(entityDropItem);
                CraftDropItem.uuids.remove(dropItem.getUniqueId().toString());
            }
        } else
            AnxiCode.shut(CraftDropItem.class);
    }

    public static void remove(final Entity dropItem, final DeathCause death) {
        final EntityDropItem d = CraftDropItem.getDropItem(dropItem);
        CraftDropItem.remove(d, death);
    }

    public static void remove(final EntityDropItem dropItem, final boolean iscalled) {
        try {
            if (CraftDropItem.include(dropItem.getEntity()) && !iscalled) {
                final File uuidFile = new File(
                        CraftDropItem.drop.getDataFolder() + "/drops/" + dropItem.getUniqueId().toString());
                uuidFile.delete();
                dropItem.remove();
                CraftDropItem.droppedItems.remove(dropItem);
            } else
                CraftDropItem.remove(dropItem, DropItemDeathEvent.DeathCause.UNKNOWN);
        } catch (final Exception e) {
            Debug.debug(e,
                    "Something wrong in removing EntityDropItem(Name = " + dropItem.getCustomName() + ",Type = "
                            + dropItem.getItemInHand().getType().name() + ",Count = "
                            + dropItem.getItemInHand().getAmount() + ").");
        }
    }

    public static void remove(final EntityDropItem dropItem, final DropItemDeathEvent.DeathCause death) {
        try {
            if (CraftDropItem.include(dropItem.getEntity())) {
                final DropItemDeathEvent event = new DropItemDeathEvent(dropItem, death);
                CraftDropItem.drop.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled())
                    return;
                final File uuidFile = new File(
                        CraftDropItem.drop.getDataFolder() + "/drops/" + dropItem.getUniqueId().toString());
                uuidFile.delete();
                dropItem.remove();
                CraftDropItem.droppedItems.remove(dropItem);
            }
        } catch (final Exception e) {
            Debug.debug(e,
                    "Something wrong in removing EntityDropItem(Name = " + dropItem.getCustomName() + ",Type = "
                            + dropItem.getItemInHand().getType().name() + ",Count = "
                            + dropItem.getItemInHand().getAmount() + ").");
        }
    }

    public static void spawnItem(final Item item) {
        try {
            final ItemStack itemStack = item.getItemStack();
            final Location location = item.getLocation();
            if (!item.isDead()) {
                item.remove();
                CraftDropItem.spawnItem(itemStack, location);
            }
        } catch (final Exception e) {
            Debug.debug(e, "Something wrong in spawning ItemStack(Type = " + item.getItemStack().getType().name()
                    + ",Count = " + item.getItemStack().getAmount() + ").");
        }
    }

    public static EntityDropItem spawnItem(final ItemStack itemStack, final Location location) {
        return CraftDropItem.spawnItem(itemStack, location, true);
    }

    public static EntityDropItem spawnItem(final ItemStack itemStack, final Location location, final boolean iscalled) {
        try {
            location.setY((location.getBlockY() - 1) + CraftDropItem.height);
            EntityDropItem dropItem = null;
            if (!CraftDropItem.drop.islower)
                dropItem = EntityDropItem.getEntityDropItem(
                        location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND), itemStack);
            dropItem.setUp();
            if (!CraftDropItem.drop.islower) {
                boolean flag = false;
                for (final ItemStackAngle isa : CraftDropItem.isas)
                    if (isa.getMaterial().equals(itemStack.getType())) {
                        flag = true;
                        final EulerAngle eulerAngle = new EulerAngle(isa.getPx(), isa.getPy(), isa.getPz());
                        dropItem.setRightArmPose(eulerAngle);
                    }
                if (!flag) {
                    final EulerAngle eulerAngle = new EulerAngle(CraftDropItem.pitchX, CraftDropItem.pitchY,
                            CraftDropItem.pitchZ);
                    dropItem.setRightArmPose(eulerAngle);
                }
            }
            if (CraftDropItem.PickForm.equals("w-move"))
                dropItem.setCanPickupItems(false);
            String customName = itemStack.getType().name().toLowerCase() + " × " + itemStack.getAmount();
            if (DropItem.Slanguages.get(itemStack.getType().name()) == null)
                System.out.println("对不起，我们暂时还没有物品类型为：" + itemStack.getType().name() + "的中文译名");
            else if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
                customName = itemStack.getItemMeta().getDisplayName();
            else if (CraftDropItem.drop.getConfig().getString("Language", "zhs").equals("zhs"))
                customName = DropItem.Slanguages.get(itemStack.getType().name()) + " × " + itemStack.getAmount();
            else if (CraftDropItem.drop.getConfig().getString("Language", "zhs").equals("zht"))
                customName = DropItem.Tlanguages.get(itemStack.getType().name()) + " × " + itemStack.getAmount();
            dropItem.setCustomName(customName);
            CraftDropItem.droppedItems.add(dropItem);
            if (iscalled) {
                final DropItemSpawnEvent event = new DropItemSpawnEvent(dropItem);
                CraftDropItem.drop.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    CraftDropItem.droppedItems.remove(dropItem);
                    dropItem.remove();
                }
            }
            DropItemInfo.registerInfo(dropItem);
            return dropItem;
        } catch (final Exception e) {
            Debug.debug(e, "Something wrong in spawning ItemStack(Type = " + itemStack.getType().name() + ",Count = "
                    + itemStack.getAmount() + ").");
            return null;
        }
    }

    public static void uploadItems(final int anxiCode) {
        try {
            if (CraftDropItem.anxiCode == anxiCode) {
                final HashMap<Location, ItemStack> ais = CraftDropItem.drop.getCraftAIListener(anxiCode)
                        .getAIs(anxiCode);
                for (final Location location : ais.keySet()) {
                    final ItemStack itemStack = ais.get(location);
                    final Location temp = new Location(location.getWorld(), location.getBlockX(),
                            location.getBlockY() + 1, location.getBlockZ());
                    if (itemStack != null)
                        CraftDropItem.spawnItem(itemStack, temp, false);
                }
                for (final EntityDropItem dropItem : CraftDropItem.droppedItems) {
                    final File uuidFile = new File(
                            CraftDropItem.drop.getDataFolder() + "/drops/" + dropItem.getUniqueId().toString());
                    uuidFile.createNewFile();
                }
            } else {
                System.err.println("某些程序试图重载DropItem信息");
                AnxiCode.shut(CraftDropItem.class);
            }
        } catch (final Exception e) {
            Debug.debug(e, "Something wrong in upload config.");
        }
    }

}