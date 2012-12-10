
package com.mediatek.widgetdemos.folders3d;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.mediatek.ngin3d.Glo3D;
import com.mediatek.ngin3d.Point;

/**
 * Activity for Folders3D application. Also adds a lot of tweak functions useful
 * for tweaking parameters when in application mode.
 */
public class FoldersActivity extends Activity {

    private Map<String, String> mTweakNames;
    private Map<String, Tweak> mTweaks;

    private String mCurrentTweakId;

    /*
     * (non-Javadoc)
     * @see
     * com.mediatek.widgetdemos.demowidget3d.DemoWidgetActivity#onCreate(android
     * .os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The activity embeds the widget layout within a special activity
        // layout containing the development user interface
        setContentView(R.layout.demo_widget_activity_layout);
        View container = this.findViewById(R.id.widget_container);
        this.getLayoutInflater().inflate(R.layout.folders_layout, (ViewGroup) container, true);

        // Tweaks are stored in maps for easy lookup
        mTweakNames = new HashMap<String, String>();
        mTweaks = new HashMap<String, Tweak>();

        // When the tweak button is pressed, we need to launch the tweak
        // menu activity, so that the user can select a value to tweak
        Button tweakButton = (Button) this.findViewById(R.id.tweak_button);
        tweakButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(FoldersActivity.this, TweakListActivity.class);

                // Bundle up the tweaks to pass to the activity
                Bundle tweaks = new Bundle();
                tweaks.putStringArray("ids", mTweakNames.keySet().toArray(new String[0]));
                tweaks.putStringArray("names", mTweakNames.values().toArray(new String[0]));
                intent.putExtra("tweaks", tweaks);

                startActivityForResult(intent, 0);
            }
        });

        // When the user interacts with the "tweaker" view, we call the
        // callbacks defined by the user in order to effect a change
        FloatTweaker floatTweaker = (FloatTweaker) this.findViewById(R.id.float_tweaker);
        floatTweaker.setOnValueChangeCallback(new FloatTweaker.OnValueChangeCallback() {
            public float onValueChange(float value) {
                Tweak tweak = mTweaks.get(mCurrentTweakId);

                if (tweak != null && tweak.getType() == Tweak.TWEAK_TYPE_FLOAT) {
                    FloatTweak floatTweak = (FloatTweak) tweak;
                    floatTweak.set(value);
                    value = floatTweak.get();
                }

                return value;
            }
        });


        final FoldersView widgetView = (FoldersView) findViewById(R.id.folders_view);
        
        addTweak("camera_fov", "Camera FOV", new FloatTweak() {
            public float get() {
                return widgetView.getCameraFov();
            }

            public void set(float value) {
                widgetView.setCameraFov(value);
            }
        });

        addTweak("camera_angle_x", "Camera Angle X", new FloatTweak() {
            public float get() {
                return widgetView.getCameraAngleX();
            }

            public void set(float value) {
                widgetView.setCameraAngleX(value);
            }
        });

        addTweak("camera_angle_y", "Camera Angle Y", new FloatTweak() {
            public float get() {
                return widgetView.getCameraAngleY();
            }

            public void set(float value) {
                widgetView.setCameraAngleY(value);
            }
        });

        addTweak("camera_distance", "Camera Distance", new FloatTweak() {
            public float get() {
                return widgetView.getCameraDistance();
            }

            public void set(float value) {
                widgetView.setCameraDistance(value);
            }
        });

        addTweak("camera_lookat_y", "Camera LookAt Y", new FloatTweak() {
            public float get() {
                return widgetView.getCameraLookAt().y;
            }

            public void set(float value) {
                Point lookAt = widgetView.getCameraLookAt();
                lookAt.y = value;
                widgetView.setCameraLookAt(lookAt);
            }
        });

        // The "distance" of the billboard plane from the camera
        addTweak("screen_distance", "Screen Distance", new FloatTweak() {
            public float get() {
                return widgetView.getScreenDistance();
            }

            public void set(float value) {
                widgetView.setScreenDistance(value);
            }
        });

        addTweak("animation_speed", "Animation Speed", new FloatTweak() {
            public float get() {
                Log.v("Animation speed get",
                        Float.toString(widgetView._getAnimationSpeed() * 100f));
                return widgetView._getAnimationSpeed() * 100f;
            }

            public void set(float value) {
                Log.v("Animation speed set", Float.toString(value * 0.01f));
                widgetView.setAnimationSpeed(value * 0.01f);
            }
        });

        addTweak("folders_pos_x", "Folders Pos X", new FloatTweak() {
            public float get() {
                Glo3D folders = widgetView._getFolders();
                Point position = folders.getPosition();
                return position.x;
            }

            public void set(float value) {
                Glo3D folders = widgetView._getFolders();
                Point position = folders.getPosition();
                position.x = value;
                folders.setPosition(position);
            }
        });

        addTweak("folders_pos_y", "Folders Pos y", new FloatTweak() {
            public float get() {
                Glo3D folders = widgetView._getFolders();
                Point position = folders.getPosition();
                return position.y;
            }

            public void set(float value) {
                Glo3D folders = widgetView._getFolders();
                Point position = folders.getPosition();
                position.y = value;
                folders.setPosition(position);
            }
        });

        addTweak("folders_pos_z", "Folders Pos z", new FloatTweak() {
            public float get() {
                Glo3D folders = widgetView._getFolders();
                Point position = folders.getPosition();
                return position.z;
            }

            public void set(float value) {
                Glo3D folders = widgetView._getFolders();
                Point position = folders.getPosition();
                position.z = value;
                folders.setPosition(position);
            }
        });

        addTweak("light_position_x", "Light Position X", new FloatTweak() {
            public float get() {
                Glo3D light = widgetView._getLight();
                Point position = light.getPosition();
                return position.x;
            }

            public void set(float value) {
                Glo3D light = widgetView._getLight();
                Point position = light.getPosition();
                position.x = value;
                light.setPosition(position);
            }
        });

        addTweak("light_position_y", "Light Position Y", new FloatTweak() {
            public float get() {
                Glo3D light = widgetView._getLight();
                Point position = light.getPosition();
                return position.y;
            }

            public void set(float value) {
                Glo3D light = widgetView._getLight();
                Point position = light.getPosition();
                position.y = value;
                light.setPosition(position);
            }
        });

        addTweak("light_position_z", "Light Position Z", new FloatTweak() {
            public float get() {
                Glo3D light = widgetView._getLight();
                Point position = light.getPosition();
                return position.z;
            }

            public void set(float value) {
                Glo3D light = widgetView._getLight();
                Point position = light.getPosition();
                position.z = value;
                light.setPosition(position);
            }
        });
    }
    
    // Handle the user selection from the tweak menu
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String id = data.getStringExtra("id");
            String name = data.getStringExtra("name");

            mCurrentTweakId = id;

            Tweak tweak = mTweaks.get(mCurrentTweakId);

            // Currently only floating point tweakers are supported
            if (tweak.getType() == Tweak.TWEAK_TYPE_FLOAT) {
                FloatTweak floatTweak = (FloatTweak) mTweaks.get(mCurrentTweakId);
                FloatTweaker floatTweaker = (FloatTweaker) this.findViewById(R.id.float_tweaker);
                floatTweaker.setName(name);
                floatTweaker.setValue(floatTweak.get());
            }
            else {
                name = "Unknown Type";
            }
        }
    }
    
    // Base class for user-defined tweaks
    public abstract class Tweak {
        public static final int TWEAK_TYPE_FLOAT = 0;

        private int mType;

        public int getType() {
            return mType;
        }
    }

    // The user should implement the set and get callbacks to effect tweaking
    public abstract class FloatTweak extends Tweak {
        public abstract void set(float value);

        public abstract float get();
    }

    // Users should call this function to add their own custom tweaks
    // Tweaks are values that can be adjusted in realtime for debugging and
    // development purposes. The user just has to provide a unique ID string,
    // a human-readable name, and a tweak object in which are defined set and
    // get callbacks which modify and retrieve the actual values which the
    // tweaks are controlling within the program. The user can do anything they
    // wish inside the set and get function. It may be desirable to put
    // constraints on the value within the set function.
    public void addTweak(String id, String name, Tweak tweak) {
        mTweakNames.put(id, name);
        mTweaks.put(id, tweak);
    }
    
    @Override
    public void onPause() {
        super.onPause();
//        FoldersView widgetView = (FoldersView) findViewById(R.id.folders_view);
//        widgetView.freeze(0);
//        widgetView.getStageView().onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
//        FoldersView widgetView = (FoldersView) findViewById(R.id.folders_view);
//        widgetView.freeze(0);
//        widgetView.getStageView().onResume();
    }
}
