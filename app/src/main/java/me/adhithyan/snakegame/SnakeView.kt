package me.adhithyan.snakegame

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceView
import android.media.AudioManager
import android.media.SoundPool
import android.view.MotionEvent
import android.view.SurfaceHolder
import java.io.IOError
import java.io.IOException
import java.util.*





/**
 * Created by Adhithyan V on 17-11-2017.
 */

class SnakeView(context: Context?, size: Point?) : SurfaceView(context), Runnable{

    var playing = false
    var canvas: Canvas? = null
    var mHolder: SurfaceHolder? = null
    var mPaint: Paint? = null

    var mSnakeLength = 0
    var soundPool: SoundPool? = null
    var mouseSound = -1
    var deadSound = -1


    var mContext: Context? = null
    var mThread: Thread? = null
    var mHeight = 0
    var mWidth = 0
    var direction: Direction = Direction.RIGHT

    var nextFrameTime = 0L
    var score = 0
    var mSnakeX = IntArray(200, {0})
    var mSnakeY = IntArray(200, {0})

    var mouseX = 0
    var mouseY = 0

    var mBlockSize = 0
    var mNumBlocksHigh = 0

    init {
        mContext = context
        mHeight = size!!.y
        mWidth = size!!.x

        mBlockSize = mWidth!! / NUM_BLOCKS_WIDE
        mNumBlocksHigh = mHeight!! / mBlockSize

        loadSound()

        mHolder = holder
        mPaint = Paint()

        startGame()
    }

    override fun run() {
        while (playing) {
            if(checkForUpdate()) {
                updateGame()
                drawGame()
            }
        }
    }

    fun loadSound() {
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        try {
            val assetManager = context.assets
            val descriptor = assetManager.openFd("get_mouse_sound.ogg")
            mouseSound = soundPool!!.load(descriptor, 0)

            val descriptor_2 = assetManager.openFd("death_sound.ogg")
            deadSound = soundPool!!.load(descriptor_2, 0)

        } catch (ex: IOException) {

        }
    }

    fun startGame() {
        mSnakeLength = 1
        mSnakeX[0] = NUM_BLOCKS_WIDE/2
        mSnakeY[0] = mNumBlocksHigh/2

        spawnMouse()
        score = 0

        nextFrameTime = System.currentTimeMillis()
    }

    fun updateGame() {
        if(mSnakeX[0] == mouseX && mSnakeY[0] == mouseY) {
            eatMouse()
        }

        moveSnake()

        if(detectDeath()) {
            soundPool!!.play(deadSound, 1f, 1f, 0, 0, 1F)
            startGame()
        }

    }

    fun drawGame() {
        if(holder.surface.isValid) {
            canvas = holder.lockCanvas()

            canvas?.drawColor(Color.argb(255, 120, 197, 87))
            mPaint?.color = Color.argb(255, 255, 255, 255)
            mPaint?.textSize = 30F
            canvas?.drawText("Score: $score", 10F, 30F, mPaint!!)

            for(i in 0..mSnakeLength-1) {
                canvas?.drawRect((mSnakeX[i]*mBlockSize).toFloat(),
                        (mSnakeY[i] * mBlockSize).toFloat(),
                        ((mSnakeX[i] * mBlockSize) + mBlockSize).toFloat(),
                        ((mSnakeY[i] * mBlockSize) + mBlockSize).toFloat(),
                        mPaint
                        )
            }

            canvas?.drawRect((mouseX*mBlockSize).toFloat(),
                    (mouseY*mBlockSize).toFloat(),
                    ((mouseX*mBlockSize)+mBlockSize).toFloat(),
                    ((mouseY*mBlockSize)+mBlockSize).toFloat(),
                    mPaint
                    )

            holder.unlockCanvasAndPost(canvas)
        }
    }

    fun checkForUpdate(): Boolean {
        if(nextFrameTime <= System.currentTimeMillis()){
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            nextFrameTime =System.currentTimeMillis() + MILLIS_IN_A_SECOND / FPS;

            // Return true so that the update and draw
            // functions are executed
            return true;
        }

        return false;
    }

    fun spawnMouse() {
        val random = Random()
        mouseX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1
        mouseY = random.nextInt(mNumBlocksHigh - 1) + 1
    }

    fun eatMouse() {
        mSnakeLength++
        spawnMouse()
        score++
        soundPool?.play(mouseSound, 1F, 1F, 0,0,1F)
    }

    fun moveSnake() {
        for(i in mSnakeLength downTo 1) {
            mSnakeX[i] = mSnakeX[i - 1]
            mSnakeY[i] = mSnakeY[i - 1]
        }

        when (direction) {
            Direction.UP -> { mSnakeY[0] = mSnakeY[0] - 1}
            Direction.RIGHT -> { mSnakeX[0] = mSnakeX[0] + 1}
            Direction.DOWN -> { mSnakeY[0] = mSnakeY[0] + 1}
            Direction.LEFT -> { mSnakeX[0] = mSnakeX[0] - 1}
        }
    }

    fun detectDeath(): Boolean {
        var dead = false
        if (mSnakeX[0] == -1) dead = true;
        if (mSnakeX[0] >= NUM_BLOCKS_WIDE) dead = true;
        if (mSnakeY[0] == -1) dead = true;
        if (mSnakeY[0] == mNumBlocksHigh) dead = true;

        for (i in mSnakeLength - 1 downTo 1) {
            if (i > 4 && mSnakeX[0] === mSnakeX[i] && mSnakeY[0] === mSnakeY[i]) {
                dead = true
            }
        }
        return dead
    }

    fun pause() {
        playing = false

        try {
            mThread?.join()
        } catch(e: InterruptedException) {

        }
    }

    fun resume() {
        playing = true
        mThread = Thread(this)
        mThread?.start()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.getAction()!! and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_UP -> if (event?.getX() >= mWidth!! / 2) {
                when (direction) {
                    Direction.UP -> direction = Direction.RIGHT
                    Direction.RIGHT -> direction = Direction.DOWN
                    Direction.DOWN -> direction = Direction.LEFT
                    Direction.LEFT -> direction = Direction.UP
                }
            } else {
                when (direction) {
                    Direction.UP -> direction = Direction.LEFT
                    Direction.LEFT -> direction = Direction.DOWN
                    Direction.DOWN -> direction = Direction.RIGHT
                    Direction.RIGHT -> direction = Direction.UP
                }
            }
        }
        return true
    }
    companion object {
        val NUM_BLOCKS_WIDE = 40
        val FPS = 10L
        val MILLIS_IN_A_SECOND = 1000L
    }

    enum class Direction {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }
}