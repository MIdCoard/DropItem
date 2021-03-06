package com.focess.dropitem.item;

import com.focess.dropitem.util.NMSManager;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class EntityDropItem1_8_p extends EntityDropItem {

    EntityDropItem1_8_p(final LivingEntity armorStand) {
        super(armorStand);
        this.check(armorStand);
        this.setItemStack(((ArmorStand) armorStand).getItemInHand());
    }

    EntityDropItem1_8_p(final LivingEntity armorStand, final ItemStack itemStack) {
        super(armorStand, itemStack);
        this.check(armorStand);
        this.getEntity().setItemInHand(itemStack);
    }

    private void check(final LivingEntity armorStand) {
        if (!(armorStand instanceof ArmorStand))
            throw new ClassCastException();
    }

    @Override
    public boolean isVisible() {
        return this.getEntity().isVisible();
    }

    @Override
    public void setRightArmPose(final EulerAngle eulerAngle) {
        this.getEntity().setRightArmPose(eulerAngle);
    }

    @Override
    public ArmorStand getEntity() {
        return (ArmorStand) super.getEntity();
    }

    @Override
    protected void setUp() {
        this.getEntity().setVisible(false);
        this.getEntity().setRemoveWhenFarAway(false);
        this.getEntity().setCustomNameVisible(false);
        this.getEntity().setCanPickupItems(false);
        NMSManager.setGravity(this.getEntity(),false);
    }


}