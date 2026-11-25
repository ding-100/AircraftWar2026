package edu.hitsz.aircraftwar.game;

final class ShieldSkill {
    private static final int ACTIVE_TICKS = 125;
    private static final int PERIOD_TICKS = ACTIVE_TICKS + 200;

    private int elapsedTicks;

    void update() {
        elapsedTicks = (elapsedTicks + 1) % PERIOD_TICKS;
    }

    boolean isActive() {
        return elapsedTicks < ACTIVE_TICKS;
    }
}
