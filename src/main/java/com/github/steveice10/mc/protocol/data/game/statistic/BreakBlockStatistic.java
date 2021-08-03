package com.github.steveice10.mc.protocol.data.game.statistic;

public class BreakBlockStatistic implements Statistic {

    private final int id;

    public BreakBlockStatistic(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

}
