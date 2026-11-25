package edu.hitsz.aircraftwar;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

final class GameImages {
    static final int FIELD_WIDTH = 512;
    static final int FIELD_HEIGHT = 768;

    private GameImages() {
    }

    static Bitmap decode(Resources resources, int drawableId) {
        return BitmapFactory.decodeResource(resources, drawableId);
    }
}
