
package com.mediatek.ngin3d.demo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DemolistGlo3D extends Activity {
    private ListView mListView;
    private MyAdapter mMyAdapter;
    public List<String> mListTag = new ArrayList<String>();
    private List<String> mData = new ArrayList<String>();
    private HashMap<String, Object> mImageMap = new HashMap<String, Object>();
    private static final String packageName = "com.mediatek.ngin3d.demo";

    private String[] mGloActivity3 = {
            "Glo3DDemo", "ProjectionsDemo", "Glo3DTextureDemo", "Glo3DAntiAliasingDemo",
            "Glo3DScaleRotationDemo", "CameraPositionDemo", "EularAngleDemo", "EulerOrderDemo","OptimusPrime"
    };

    /***
     * this method to avoid loading the picture from Resource caused by OOM
     *
     * @param context
     * @param resId
     * @return Bitmap
     */
    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;

        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    private void initImageMap() {
        mImageMap.clear();
        mImageMap.put("EmptyStage", R.drawable.demo_emptystage);
        mImageMap.put("BitmapFontDemo", R.drawable.demo_bitmapfontdemo);
        mImageMap.put("HelloCaster", R.drawable.demo_hellocaster);

        mImageMap.put("HelloNgin3d", R.drawable.demo_hellongin3d);
        mImageMap.put("MipmapDemo", R.drawable.demo_mipmapdemo);
        mImageMap.put("RotationDemo", R.drawable.demo_rotationdemo);

        mImageMap.put("TextDemo", R.drawable.demo_textdemo);
        mImageMap.put("TimelineDemo", R.drawable.demo_timelinedemo);
        mImageMap.put("TransitionEffectDemo", R.drawable.demo_transitioneffectdemo);

        mImageMap.put("CuboidDemo", R.drawable.demo_cuboiddemo);
        mImageMap.put("SphereDemo", R.drawable.demo_space3ddemoapp);
        mImageMap.put("VideoTextureDemo", R.drawable.demo_videotexturedemo);

        mImageMap.put("AspectRatioDemo", R.drawable.demo_aspectratiodemo);
        mImageMap.put("AnimationDemo", R.drawable.demo_animationdemo);
        mImageMap.put("AnimationCloneDemo", R.drawable.demo_animationclonedemo);

        mImageMap.put("AnimationStress", R.drawable.demo_animationstress);
        mImageMap.put("CameraAnimationDemo", R.drawable.demo_cameraanimationdemo);
        mImageMap.put("Canvas2dDemo", R.drawable.demo_canvas2ddemo);

        mImageMap.put("ContainerRotationDemo1", R.drawable.demo_containerrotationdemo1);
        mImageMap.put("ContainerRotationDemo2", R.drawable.demo_containerrotationdemo2);
        mImageMap.put("DisplayAreaDemo", R.drawable.demo_displayareademo);

        mImageMap.put("DragAnimationDemo", R.drawable.demo_draganimationdemo);
        mImageMap.put("ImplicitAnimationDemo", R.drawable.demo_implicitanimationdemo);
        mImageMap.put("PlaneDemo", R.drawable.demo_planedemo);

        mImageMap.put("RippleDemo", R.drawable.demo_rippledemo);
        mImageMap.put("ScriptDemo", R.drawable.demo_scriptdemo);
        mImageMap.put("SheetAnimationDemo", R.drawable.demo_sheetanimationdemo);

        mImageMap.put("SpriteAnimationDemo", R.drawable.demo_spriteanimationdemo);
        mImageMap.put("SnowFall", R.drawable.demo_snowfall);
        mImageMap.put("GLTextureViewDemo", R.drawable.demo_gltextureviewdemo);

        mImageMap.put("Glo3DDemo", R.drawable.demo_glo3ddemo);
        mImageMap.put("ProjectionsDemo", R.drawable.demo_projectionsdemo);
        mImageMap.put("Glo3DTextureDemo", R.drawable.demo_glo3dtexturedemo);
        mImageMap.put("Glo3DAntiAliasingDemo", R.drawable.demo_glo3dantialiasingdemoapp);
        mImageMap.put("Glo3DScaleRotationDemo", R.drawable.demo_glo3dscalerotationdemoapp);

        mImageMap.put("ScaleRotationDemo", R.drawable.demo_scalerotationdemoapp);
        mImageMap.put("AnchorDemo", R.drawable.demo_anchordemoapp);
        mImageMap.put("CameraPositionDemo", R.drawable.demo_camerapositiondemoapp);
        mImageMap.put("EularAngleDemo", R.drawable.demo_eularabgledemoapp);
        mImageMap.put("Stereo3DDemo", R.drawable.demo_stereo3ddemoapp);
        mImageMap.put("EulerOrderDemo", R.drawable.demo_eulerorderdemoapp);
        mImageMap.put("OptimusPrime", R.drawable.demo_optimusprimedemoapp);

    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mListView = (ListView) findViewById(R.id.list);
        mMyAdapter = new MyAdapter(this,
                android.R.layout.simple_expandable_list_item_1, getData());

        mListView.setAdapter(mMyAdapter);

        initImageMap();

        mListView.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                try {
                    startActivity(new Intent(getBaseContext(), Class.forName(packageName + "."
                            + mData.get(position))));
                } catch (ClassNotFoundException e) {

                    e.printStackTrace();
                }

            }

        });

    }

    private Object getImageFromMap(Object key) {

        return mImageMap.get(key);
    }

    private List<String> getData() {

        mListTag.add("Glo");
        mData.add("Glo");
        for (int i = 0; i < mGloActivity3.length; i++) {
            mData.add(mGloActivity3[i]);
        }

        return mData;
    }

    class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(Context context, int textViewResourceId,
                List<String> objects) {
            super(context, textViewResourceId, objects);

        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {

            return !mListTag.contains(getItem(position));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (mListTag.contains(getItem(position))) {

                view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_tag, null);
            } else {

                view = LayoutInflater.from(getContext()).inflate(R.layout.list_item, null);

                if (getImageFromMap(getItem(position)) != null) {

                    Bitmap bmp = readBitMap(this.getContext(),
                            (Integer) getImageFromMap(getItem(position)));
                    // int resId = (Integer) getImageFromMap(getItem(position));
                    ImageView imageView = (ImageView) view.findViewById(R.id.group_list_item_image);
                    imageView.setImageBitmap(bmp);
                    /*
                     * if(!bmp.isRecycled() ){ bmp.recycle(); System.gc(); }
                     */
                    // imageView.setImageResource(resId);
                    // imageView.setBackgroundResource(resId);
                }

            }

            TextView textView = (TextView) view.findViewById(R.id.group_list_item_text);
            textView.setText(getItem(position));

            return view;
        }

    }

}
