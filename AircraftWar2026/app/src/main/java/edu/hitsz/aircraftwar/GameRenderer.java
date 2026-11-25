package edu.hitsz.aircraftwar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import edu.hitsz.aircraftwar.game.AbstractFlyingObject;
import edu.hitsz.aircraftwar.game.Difficulty;
import edu.hitsz.aircraftwar.game.GameSession;
import edu.hitsz.aircraftwar.game.Sprite;

import java.util.List;

import static edu.hitsz.aircraftwar.game.AbstractFlyingObject.FIELD_HEIGHT;
import static edu.hitsz.aircraftwar.game.AbstractFlyingObject.FIELD_WIDTH;

final class GameRenderer {
    private final GameImages images;
    private final Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint uiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int textColor;
    private final int panelColor;
    private final String scoreLabel;
    private final String lifeLabel;
    private final String gameOverLabel;

    GameRenderer(Context context, Difficulty difficulty) {
        images = new GameImages(context.getResources(), difficulty);
        textColor = context.getColor(R.color.game_text);
        panelColor = context.getColor(R.color.game_panel);
        scoreLabel = context.getString(R.string.game_score);
        lifeLabel = context.getString(R.string.game_life);
        gameOverLabel = context.getString(R.string.game_over);
        uiPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    }

    void draw(Canvas canvas, GameSession session) {
        int offset = session.getBackgroundOffset();
        canvas.drawBitmap(images.background, 0, offset, bitmapPaint);
        canvas.drawBitmap(images.background, 0, offset - FIELD_HEIGHT, bitmapPaint);

        drawObjects(canvas, session.getSupplies());
        drawObjects(canvas, session.getEnemies());
        drawObjects(canvas, session.getHeroBullets());
        drawObjects(canvas, session.getEnemyBullets());
        drawObject(canvas, session.getHero());
        if (session.isShieldActive()) {
            AbstractFlyingObject hero = session.getHero();
            drawSprite(canvas, Sprite.SHIELD, hero.getLocationX(), hero.getLocationY() + 20);
        }
        drawHud(canvas, session);
    }

    private void drawObjects(Canvas canvas, List<? extends AbstractFlyingObject> objects) {
        for (AbstractFlyingObject object : objects) {
            drawObject(canvas, object);
        }
    }

    private void drawObject(Canvas canvas, AbstractFlyingObject object) {
        drawSprite(canvas, object.getSprite(), object.getLocationX(), object.getLocationY());
    }

    private void drawSprite(Canvas canvas, Sprite sprite, int x, int y) {
        Bitmap bitmap = images.get(sprite);
        canvas.drawBitmap(bitmap, x - sprite.width / 2f, y - sprite.height / 2f, bitmapPaint);
    }

    private void drawHud(Canvas canvas, GameSession session) {
        uiPaint.setColor(panelColor);
        canvas.drawRoundRect(12, 12, FIELD_WIDTH - 12, 64, 12, 12, uiPaint);
        uiPaint.setColor(textColor);
        uiPaint.setTextSize(23);
        uiPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(scoreLabel + session.getScore(), 26, 46, uiPaint);
        uiPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(lifeLabel + session.getHero().getHp(), FIELD_WIDTH - 26, 46, uiPaint);
        if (session.isGameOver()) {
            uiPaint.setColor(panelColor);
            canvas.drawRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT, uiPaint);
            uiPaint.setColor(textColor);
            uiPaint.setTextAlign(Paint.Align.CENTER);
            uiPaint.setTextSize(36);
            canvas.drawText(gameOverLabel, FIELD_WIDTH / 2f, FIELD_HEIGHT / 2f, uiPaint);
        }
    }
}
