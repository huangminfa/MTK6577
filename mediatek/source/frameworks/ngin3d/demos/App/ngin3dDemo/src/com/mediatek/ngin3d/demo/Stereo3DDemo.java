
package com.mediatek.ngin3d.demo;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Sphere;
import com.mediatek.ngin3d.android.StageActivity;

/**
 * A demo for usage of Object3D.
 */
public class Stereo3DDemo extends StageActivity {

    Container mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContainer = new Container();
        final Sphere smallEarth = Sphere.createFromResource(getResources(), R.drawable.earth);

        // smallEarth.setPosition(new Point(0, 260, 0));
        // smallEarth.setScale(new Scale(20, -20, 20));
        mContainer.setPosition(new Point(240, 440, 0));
        mContainer.setScale(new Scale(60, -60, 60));

        mContainer.add(smallEarth);
        mStage.add(mContainer);

        mStage.setStereo3D(true, 150);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stereo3d_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();

        switch (item_id) {
            case R.id.stereo3d_turnon:
                mStage.setStereo3D(true, 150);
                break;

            case R.id.stereo3d_turnoff:
                mStage.setStereo3D(false, 0);
                break;

            default:
                return false;
        }
        return true;
    }
}
