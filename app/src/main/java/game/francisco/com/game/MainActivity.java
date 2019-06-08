package game.francisco.com.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity {

    private Gameview gameview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameview = new Gameview(this);
        setContentView(gameview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameview.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameview.pause();
    }

    class Gameview extends SurfaceView implements Runnable {

        private Thread gameThread;
        private SurfaceHolder ourHolder;
        private volatile boolean playing;
        private Canvas canvas;
        private Bitmap bitmapRunningMan;
        private boolean isMoving;
        private float runSpeedPerSecond = 500;
        private float manXPos = 10, manYPos = 10;
        private int frameWidth = 550, frameHeight = 350;
        private int frameCount = 4;
        private int currentFrame = 0;
        private long fps;
        private long timeThisFrame;
        private long lastFrameChangeTime = 0;
        private int frameLengthInMillisecond = 50;

        private Rect frameToDraw = new Rect(0, 0, frameWidth, frameHeight);
        private RectF whereToDraw = new RectF(manXPos, manYPos, manXPos + frameWidth, frameHeight);

        public Gameview(Context context) {

            super(context);
            ourHolder = getHolder();
            bitmapRunningMan = BitmapFactory.decodeResource(getResources(), R.drawable.wolf_man);
            bitmapRunningMan = Bitmap.createScaledBitmap(bitmapRunningMan, frameWidth * frameCount, frameHeight, false);
        }

        @Override
        public void run() {
            while (playing) {
                long startFrameTime = System.currentTimeMillis();
                update();
                draw();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void update() {
            if (isMoving) {
                manXPos = manXPos + runSpeedPerSecond / fps;
                if (manXPos > getWidth()) {
                    manYPos += (int) frameHeight;
                    manXPos = 10;
                }
                if (manYPos + frameHeight > getHeight()) {
                    manYPos = 10;
                }
            }
        }

        public void manageCurrentFrame() {
            long time = System.currentTimeMillis();
            if (isMoving) {
                if (time > lastFrameChangeTime + frameLengthInMillisecond) {
                    lastFrameChangeTime = time;
                    currentFrame++;
                    if (currentFrame >= frameCount) {
                        currentFrame = 0;
                    }
                }
            }

            frameToDraw.left = currentFrame * frameWidth;
            frameToDraw.right = frameToDraw.left + frameWidth;
        }

        public void draw() {
            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.WHITE);
                whereToDraw.set((int) manXPos, (int) manYPos, (int) manXPos
                        + frameWidth, (int) manYPos + frameHeight);
                manageCurrentFrame();
                canvas.drawBitmap(bitmapRunningMan, frameToDraw, whereToDraw, null);
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch(InterruptedException e) {
                Log.e("ERR", "Joining Thread");
            }
        }

        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN :
                    isMoving = !isMoving;
                    break;
            }
            return true;
        }
    }
}
