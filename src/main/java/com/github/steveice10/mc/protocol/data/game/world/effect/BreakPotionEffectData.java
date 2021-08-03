package com.github.steveice10.mc.protocol.data.game.world.effect;

public class BreakPotionEffectData implements WorldEffectData {
    private final int potionId;

    public BreakPotionEffectData(int potionId) {
        this.potionId = potionId;
    }

    public int getPotionId() {
        return this.potionId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BreakPotionEffectData && this.potionId == ((BreakPotionEffectData) o).potionId;
    }

    @Override
    public int hashCode() {
        return this.potionId;
    }
}
