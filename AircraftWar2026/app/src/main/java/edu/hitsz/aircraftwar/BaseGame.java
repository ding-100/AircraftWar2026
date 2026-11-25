package edu.hitsz.aircraftwar;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import edu.hitsz.aircraftwar.game.Difficulty;
import edu.hitsz.aircraftwar.game.GameSession;

import static edu.hitsz.aircraftwar.game.AbstractFlyingObject.FIELD_HEIGHT;
import static edu.hitsz.aircraftwar.game.AbstractFlyingObject.FIELD_WIDTH;
import static edu.hitsz.aircraftwar.game.AbstractFlyingObject.STEP_MILLIS;

final class BaseGame extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private final GameSession session;
    private final GameRenderer renderer;
    private final int borderColor;

    private volatile Viewport viewport = new Viewport(1f, 0f, 0f);
    private volatile boolean isRunning;
    private boolean isSurfaceReady;
    private boolean isScreenActive;
    private Thread gameThread;
    private volatile TouchPosition target;

    BaseGame(Context context, Difficulty difficulty) {
        super(context);
        session = new GameSession(difficulty);
        renderer = new GameRenderer(context, difficulty);
        setContentDescription(context.getString(R.string.game_controls));
        borderColor = getResources().getColor(R.color.window_background, context.getTheme());
        getHolder().addCallback(this);
    }

    void setScreenActive(boolean isActive) {
        isScreenActive = isActive;
        if (isActive) {
            startLoopIfReady();
        } else {
            stopLoop();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isSurfaceReady = true;
        startLoopIfReady();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        float scale = Math.min(
                (float) width / FIELD_WIDTH, (float) height / FIELD_HEIGHT);
        viewport = new Viewport(scale,
                (width - FIELD_WIDTH * scale) / 2f,
                (height - FIELD_HEIGHT * scale) / 2f);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceReady = false;
        stopLoop();
    }

    private void startLoopIfReady() {
        if (!isScreenActive || !isSurfaceReady || gameThread != null) {
            return;
        }
        isRunning = true;
        gameThread = new Thread(this, "AircraftWar-Game");
        gameThread.start();
    }

    private void stopLoop() {
        if (gameThread == null) {
            return;
        }
        isRunning = false;
        gameThread.interrupt();
        boolean wasInterrupted = false;
        while (gameThread.isAlive()) {
            try {
                gameThread.join();
            } catch (InterruptedException exception) {
                wasInterrupted = true;
            }
        }
        gameThread = null;
        if (wasInterrupted) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            long startedAt = SystemClock.uptimeMillis();
            TouchPosition currentTarget = target;
            if (currentTarget != null && !session.isGameOver()) {
                session.moveHero(currentTarget.x(), currentTarget.y());
            }
            session.update();
            drawFrame();
            if (session.isGameOver()) {
                return;
            }
            long delay = STEP_MILLIS - (SystemClock.uptimeMillis() - startedAt);
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException exception) {
                    return;
                }
            }
        }
    }

    private void drawFrame() {
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }
        try {
            Viewport current = viewport;
            canvas.drawColor(borderColor);
            canvas.translate(current.left(), current.top());
            canvas.scale(current.scale(), current.scale());
            canvas.clipRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
            renderer.draw(canvas, session);
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE
                || action == MotionEvent.ACTION_UP) {
            Viewport current = viewport;
            if (current.scale() > 0) {
                target = new TouchPosition(
                        Math.round((event.getX() - current.left()) / current.scale()),
                        Math.round((event.getY() - current.top()) / current.scale()));
            }
        }
        if (action == MotionEvent.ACTION_UP) {
            performClick();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private record TouchPosition(int x, int y) {
    }

    private record Viewport(float scale, float left, float top) {
    }
}
