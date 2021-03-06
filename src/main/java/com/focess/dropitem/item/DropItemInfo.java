package com.focess.dropitem.item;

import com.focess.dropitem.DropItem;
import com.focess.dropitem.event.DropItemDeathEvent.DeathCause;
import com.focess.dropitem.util.configuration.DropItemConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DropItemInfo {

    private static BukkitTask live;

    public static void clear() {
        DropItemInfo.dropItemInfos.clear();
    }

    private static final Map<UUID, DropItemInfo> dropItemInfos = new ConcurrentHashMap<>();

    public static DropItemInfo getDropItemInfo(final UUID uuid) {
        return DropItemInfo.dropItemInfos.get(uuid);
    }

    public static void reload() {
        live.cancel();
    }

    public static void loadDefault(final DropItem drop) {
        live = drop.getServer().getScheduler().runTaskTimer(drop, (Runnable) new DropItemLive(), 0, 20);
    }

    public static Map<UUID, DropItemInfo> getDropItemInfos() {
        return dropItemInfos;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final DropItemInfo that = (DropItemInfo) o;
        return Objects.equals(this.uuid, that.uuid);
    }

    protected static DropItemInfo registerInfo(final EntityDropItem dropItem) {
        return new DropItemInfo(dropItem);
    }

    public static void remove(final UUID uuid) {
        DropItemInfo.dropItemInfos.remove(uuid);
    }

    private boolean alive = true;

    private EntityDropItem dropItem;

    private int time;

    private UUID uuid;

    private DropItemInfo(final EntityDropItem dropItem) {
        this.dropItem = dropItem;
        this.uuid = dropItem.getUniqueId();
        DropItemInfo.dropItemInfos.put(dropItem.getUniqueId(), this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid);
    }

    private static class DropItemLive extends BukkitRunnable {

        @Override
        public void run() {
            for (final UUID uuid : DropItemInfo.dropItemInfos.keySet()) {
                final DropItemInfo dropItemInfo = DropItemInfo.dropItemInfos.get(uuid);
                dropItemInfo.time++;
                if (dropItemInfo.time > DropItemConfiguration.getRefreshTime()) {
                    DropItemInfo.dropItemInfos.remove(uuid);
                    if (!dropItemInfo.alive)
                        CraftAIListener.remove(dropItemInfo.getUUID());
                    CraftDropItem.remove(dropItemInfo.dropItem, DeathCause.DEATH);
                }
            }
        }

    }

    public EntityDropItem getDropItem() {
        return this.dropItem;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void setAlive(final boolean alive) {
        this.alive = alive;
    }

    protected void setDropItem(final EntityDropItem dropItem) {
        this.dropItem = dropItem;
        this.uuid = dropItem.getUniqueId();
        DropItemInfo.dropItemInfos.put(dropItem.getUniqueId(), this);
    }

}
