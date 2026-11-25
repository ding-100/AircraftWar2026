package edu.hitsz.aircraftwar.game;

final class Supply extends AbstractFlyingObject {
    private static final Sprite[] SUPPLY_SPRITES = {
            Sprite.HEALTH, Sprite.FIRE, Sprite.SUPER_FIRE, Sprite.FREEZE, Sprite.BOMB
    };

    Supply(int x, int y, int speedY, int kind) {
        super(x, y, 0, speedY, SUPPLY_SPRITES[kind]);
    }

    @Override
    public void forward() {
        super.forward();
        if (locationY - getSprite().height / 2 >= FIELD_HEIGHT) {
            vanish();
        }
    }
}
