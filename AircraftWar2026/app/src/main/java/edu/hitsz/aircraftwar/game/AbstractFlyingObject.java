package edu.hitsz.aircraftwar.game;

import edu.hitsz.aircraftwar.game.aircraft.AbstractAircraft;

/** Positions refer to the center of each sprite. */
public abstract class AbstractFlyingObject {
    public static final int FIELD_WIDTH = 512;
    public static final int FIELD_HEIGHT = 768;
    public static final int STEP_MILLIS = 40;

    protected int locationX;
    protected int locationY;
    protected int speedX;
    protected int speedY;
    private final Sprite sprite;
    private boolean isValid = true;
    private int slowTicks;
    private int speedDivisor = 1;

    protected AbstractFlyingObject(int x, int y, int speedX, int speedY, Sprite sprite) {
        locationX = x;
        locationY = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.sprite = sprite;
    }

    public void forward() {
        locationX += getSpeedX();
        locationY += getSpeedY();
        if (slowTicks > 0 && --slowTicks == 0) {
            speedDivisor = 1;
        }
    }

    public boolean crash(AbstractFlyingObject other) {
        // Aircraft use the inner half of their sprite height, as on desktop.
        int ownHeight = this instanceof AbstractAircraft ? sprite.height / 2 : sprite.height;
        int otherHeight = other instanceof AbstractAircraft
                ? other.sprite.height / 2 : other.sprite.height;
        return Math.abs(other.locationX - locationX) < (other.sprite.width + sprite.width) / 2
                && Math.abs(other.locationY - locationY) < (otherHeight + ownHeight) / 2;
    }

    protected void slow(int divisor, int durationMillis) {
        speedDivisor = divisor;
        slowTicks = durationMillis / STEP_MILLIS;
    }

    protected void freeze(int durationMillis) {
        slow(Integer.MAX_VALUE, durationMillis);
    }

    public int getLocationX() {
        return locationX;
    }

    public int getLocationY() {
        return locationY;
    }

    public int getSpeedX() {
        return speedX / speedDivisor;
    }

    public int getSpeedY() {
        return speedY / speedDivisor;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void vanish() {
        isValid = false;
    }

    public boolean notValid() {
        return !isValid;
    }
}
