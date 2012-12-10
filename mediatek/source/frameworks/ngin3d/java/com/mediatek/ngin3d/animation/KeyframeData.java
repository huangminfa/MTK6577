package com.mediatek.ngin3d.animation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * The keyframe data loaded from json or cached object
 * @hide
 */
public class KeyframeData implements Serializable {

    protected transient int mDuration;
    protected transient int mDelay;      // animation delay if combine with other object
    private transient Samples mSamples;
    private static final long serialVersionUID = 1L;
    protected transient boolean mNormalized;

    public KeyframeData(int duration, int delay, Samples samples, boolean normalized) {
        mDuration = duration;
        mDelay = delay;
        mSamples = samples;
        mNormalized = normalized;
    }

    public KeyframeData(int duration, int delay, Samples samples) {
        this(duration, delay, samples, false);
    }

    public int getDuration() {
        return mDuration;
    }

    public int getDelay() {
        return mDelay;
    }

    public Samples getSamples() {
        return mSamples;
    }

    public boolean isNormalized() {
        return mNormalized;
    }

    private void dumpSamples(ObjectOutputStream s, float[]... sampleArrays) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(sampleArrays[0].length * Float.SIZE);
        FloatBuffer fb = bb.asFloatBuffer();
        final int length = bb.array().length;

        for (float[] f : sampleArrays) {
            fb.clear();
            fb.put(f);
            s.writeInt(length);
            s.write(bb.array());
        }
    }

    private void dumpSamples(ObjectOutputStream oos, int[]... sampleArrays) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(sampleArrays[0].length * Integer.SIZE);
        IntBuffer ib = bb.asIntBuffer();
        final int length = bb.array().length;

        for (int[] i : sampleArrays) {
            ib.clear();
            ib.put(i);
            oos.writeInt(length);
            oos.write(bb.array());
        }
    }

    private void dumpSamples(ObjectOutputStream oos, String[] sampleArrays) throws IOException {
        oos.writeInt(sampleArrays.length);
        for (int i = 0; i < sampleArrays.length; i++) {
            oos.writeObject(sampleArrays[i]);
        }
    }

    private void dumpStaticValues(ObjectOutputStream s) throws IOException {
        int sampleType = mSamples.getType();

        // TODO: see if we have to dump them seperately.

        if (sampleType == Samples.TRANSLATE || sampleType == Samples.ROTATE || sampleType == Samples.SCALE
                || sampleType == Samples.X_ROTATE || sampleType == Samples.Y_ROTATE || sampleType == Samples.Z_ROTATE
                || sampleType == Samples.ANCHOR_POINT) {
            s.writeFloat((mSamples.get(Samples.X_AXIS))[0]);
            s.writeFloat((mSamples.get(Samples.Y_AXIS))[0]);
            s.writeFloat((mSamples.get(Samples.Z_AXIS))[0]);
        }  else if (sampleType == Samples.ALPHA) {
            s.writeFloat((mSamples.get(Samples.VALUE))[0]);
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        int sampleType = mSamples.getType();
        s.writeInt(sampleType);
        s.writeInt(mDuration);
        s.writeInt(mDelay);
        s.writeBoolean(mNormalized);

        if (mDuration == 0) {
            dumpStaticValues(s);
        } else if (sampleType == Samples.TRANSLATE || sampleType == Samples.ROTATE || sampleType == Samples.SCALE
                || sampleType == Samples.X_ROTATE || sampleType == Samples.Y_ROTATE || sampleType == Samples.Z_ROTATE
                || sampleType == Samples.ANCHOR_POINT) {
            // dump int separately.
            dumpSamples(s, mSamples.getInt(Samples.CURVE_TYPE));

            dumpSamples(s,
                        mSamples.get(Samples.KEYFRAME_TIME),
                        mSamples.get(Samples.X_AXIS),
                        mSamples.get(Samples.Y_AXIS),
                        mSamples.get(Samples.Z_AXIS),
                        mSamples.get(Samples.IN_TANX),
                        mSamples.get(Samples.IN_TANY),
                        mSamples.get(Samples.IN_TANZ),
                        mSamples.get(Samples.OUT_TANX),
                        mSamples.get(Samples.OUT_TANY),
                        mSamples.get(Samples.OUT_TANZ));

            float[] time = mSamples.get(Samples.KEYFRAME_TIME);
            mDuration = (int) (time[time.length - 1] * 1000);
        } else if (sampleType == Samples.ALPHA) {
            // dump int separately.
            dumpSamples(s, mSamples.getInt(Samples.CURVE_TYPE));
            dumpSamples(s,
                        mSamples.get(Samples.KEYFRAME_TIME),
                        mSamples.get(Samples.VALUE),
                        mSamples.get(Samples.IN_TANVAL),
                        mSamples.get(Samples.OUT_TANVAL));
        } else if (sampleType == Samples.MARKER) {
            dumpSamples(s,
                        mSamples.get(Samples.KEYFRAME_TIME));
            dumpSamples(s,
                        mSamples.getString(Samples.ACTION));
        }
    }

    private void loadSamples(ObjectInputStream ois, String... sampleNames) throws IOException {
        byte[] bytes = null;
        float[] floats;

        for (String name : sampleNames) {
            final int len = ois.readInt();
            if (bytes == null) {
                bytes = new byte[len];
            }

            floats = new float[len / Float.SIZE];
            ois.readFully(bytes, 0, len);
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            bb.asFloatBuffer().get(floats, 0, floats.length);
            mSamples.add(name, floats);
        }
    }

    private void loadSamplesInt(ObjectInputStream ois, String... sampleNames) throws IOException {
        byte[] bytes = null;
        int[] ints;

        for (String name : sampleNames) {
            final int len = ois.readInt();
            if (bytes == null) {
                bytes = new byte[len];
            }

            ints = new int[len / Integer.SIZE];
            ois.readFully(bytes, 0, len);
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            bb.asIntBuffer().get(ints, 0, ints.length);
            mSamples.add(name, ints);
        }
    }

    private void loadSamplesString(ObjectInputStream ois, String sampleNames) throws IOException {
        byte[] bytes = null;
        String[] strings;

        final int len = ois.readInt();

        strings = new String[len];
        for (int i = 0; i < len; i++) {
            try {
                strings[i] = (String) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        mSamples.add(sampleNames, strings);

    }

    private void loadStaticValues(ObjectInputStream ois, Samples samples) throws IOException {
        int sampleType = samples.getType();

        if (sampleType == Samples.TRANSLATE || sampleType == Samples.ROTATE || sampleType == Samples.SCALE
                || sampleType == Samples.X_ROTATE || sampleType == Samples.Y_ROTATE || sampleType == Samples.Z_ROTATE
                || sampleType == Samples.ANCHOR_POINT) {
            float [] tmp = new float[1];
            tmp[0] = ois.readFloat();
            samples.add(Samples.X_AXIS, tmp);
            tmp = new float[1];
            tmp[0] = ois.readFloat();
            samples.add(Samples.Y_AXIS, tmp);
            tmp = new float[1];
            tmp[0] = ois.readFloat();
            samples.add(Samples.Z_AXIS, tmp);
        }  else if (sampleType == Samples.ALPHA) {
            float [] tmp = new float[1];
            tmp[0] = ois.readFloat();
            samples.add(Samples.VALUE, tmp);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int sampleType = s.readInt();
        int duration = s.readInt();
        if (duration >= 0) {
            mDuration = duration;
        }
        int delay = s.readInt();
        if (delay > 0) {
            mDelay = delay;
        }
        mNormalized = s.readBoolean();

        // read from file and construct Sample.
        mSamples = new Samples(sampleType);
        if (mDuration == 0) {
            loadStaticValues(s, mSamples);
        } else if (sampleType == Samples.TRANSLATE || sampleType == Samples.ROTATE || sampleType == Samples.SCALE
                || sampleType == Samples.X_ROTATE || sampleType == Samples.Y_ROTATE || sampleType == Samples.Z_ROTATE
                || sampleType == Samples.ANCHOR_POINT) {
            loadSamplesInt(s, Samples.CURVE_TYPE);
            loadSamples(s,
                        Samples.KEYFRAME_TIME,
                        Samples.X_AXIS,
                        Samples.Y_AXIS,
                        Samples.Z_AXIS,
                        Samples.IN_TANX,
                        Samples.IN_TANY,
                        Samples.IN_TANZ,
                        Samples.OUT_TANX,
                        Samples.OUT_TANY,
                        Samples.OUT_TANZ);

        } else if (sampleType == Samples.ALPHA) {
            loadSamplesInt(s, Samples.CURVE_TYPE);
            loadSamples(s,
                        Samples.KEYFRAME_TIME,
                        Samples.VALUE,
                        Samples.IN_TANVAL,
                        Samples.OUT_TANVAL);
        } else if (sampleType == Samples.MARKER) {
            loadSamples(s,
                        Samples.MARKER_TIME);
            loadSamplesString(s,
                        Samples.ACTION);
        }

    }
}
