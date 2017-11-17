package me.adhithyan.snakegame

import android.graphics.Point
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    var snakeView: SnakeView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        snakeView = SnakeView(this, size)
        setContentView(snakeView)

    }

    override fun onResume() {
        super.onResume()
        snakeView?.resume()
    }

    override fun onPause() {
        super.onPause()
        snakeView?.pause()
    }
}
