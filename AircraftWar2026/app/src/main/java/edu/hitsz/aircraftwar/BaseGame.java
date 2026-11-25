package edu.hitsz.aircraftwar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

final class BaseGame extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final long FRAME_MILLIS = 40;

    private final Bitmap background;
    private final Bitmap hero;
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final int borderColor;

    private volatile Viewport viewport = new Viewport(1f, 0f, 0f);
    private volatile boolean isRunning;
    private boolean isSurfaceReady;
    private boolean isScreenActive;
    private Thread gameThread;
    private int backgroundOffset;

    BaseGame(Context context) {
        super(context);
        background = GameImages.decode(getResources(), R.drawable.background_easy);
        hero = GameImages.decode(getResources(), R.drawable.hero);
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
                (float) width / GameImages.FIELD_WIDTH, (float) height / GameImages.FIELD_HEIGHT);
        viewport = new Viewport(scale,
                (width - GameImages.FIELD_WIDTH * scale) / 2f,
                (height - GameImages.FIELD_HEIGHT * scale) / 2f);
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
            drawFrame();
            backgroundOffset = (backgroundOffset + 1) % GameImages.FIELD_HEIGHT;
            long delay = FRAME_MILLIS - (SystemClock.uptimeMillis() - startedAt);
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
            canvas.clipRect(0, 0, GameImages.FIELD_WIDTH, GameImages.FIELD_HEIGHT);
            canvas.drawBitmap(background, 0, backgroundOffset, paint);
            canvas.drawBitmap(background, 0, backgroundOffset - GameImages.FIELD_HEIGHT, paint);
            canvas.drawBitmap(hero, (GameImages.FIELD_WIDTH - hero.getWidth()) / 2f,
                    GameImages.FIELD_HEIGHT - 1.5f * hero.getHeight(), paint);
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private record Viewport(float scale, float left, float top) {
    }
}
