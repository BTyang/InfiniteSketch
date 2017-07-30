package com.btyang.infinitesketch;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private InfiniteCanvas mCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCanvas = (InfiniteCanvas) findViewById(R.id.activity_main);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onDragClick(MenuItem item) {
        mCanvas.setMode(InfiniteCanvas.POINT_MODE.DRAG);
    }

    public void onDrawClick(MenuItem item) {
        mCanvas.setMode(InfiniteCanvas.POINT_MODE.DRAW);
    }

//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_drag:
//                mCanvas.setMode(InfiniteCanvas.POINT_MODE.DRAG);
//                return true;
//            case R.id.menu_draw:
//                mCanvas.setMode(InfiniteCanvas.POINT_MODE.DRAW);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//
//        }
//
//
//    }
}
