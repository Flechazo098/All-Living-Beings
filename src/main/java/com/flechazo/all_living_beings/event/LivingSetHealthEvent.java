package com.flechazo.all_living_beings.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class LivingSetHealthEvent extends Event {
    private final LivingEntity entity;
    private float newHealth;
    private boolean canceled = false;

    public LivingSetHealthEvent(LivingEntity entity, float newHealth) {
        this.entity = entity;
        this.newHealth = newHealth;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public float getNewHealth() {
        return newHealth;
    }

    public void setNewHealth(float newHealth) {
        this.newHealth = newHealth;
    }

    public void cancel() {
        this.canceled = true;
    }

    public boolean isCanceled() {
        return canceled;
    }
}