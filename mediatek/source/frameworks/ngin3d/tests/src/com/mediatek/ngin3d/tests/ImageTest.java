package com.mediatek.ngin3d.tests;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.test.suitebuilder.annotation.SmallTest;

import com.mediatek.a3m.AssetPool;
import com.mediatek.ngin3d.a3m.A3mPresentationEngine;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Ngin3d;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.presentation.BitmapGenerator;
import com.mediatek.ngin3d.presentation.ImageDisplay;
import com.mediatek.ngin3d.presentation.ImageSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.test.MoreAsserts.assertNotEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class ImageTest extends Ngin3dTest {
    Image mImage;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mImage = new Image();
    }

    @SmallTest
    public void testEmptyImage() {
        try {
            mStage.add(mImage);
            mStage.realize(mPresentationEngine);
            fail("should throw exception when realize empty image.");
        } catch (Exception e) {
            // expected
        }
    }

    @SmallTest
    public void testAnchorPoint() {
        mImage.setAnchorPoint(new Point(0.5f, 1.0f));
        assertEquals(mImage.getAnchorPoint().x, 0.5f);
        assertEquals(mImage.getAnchorPoint().y, 1.0f);
        assertEquals(mImage.getAnchorPoint().z, 0.0f);
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    // \todo Add support for getTotalCImageBytes and getTotalTextureBytes in
    // A3M. Put back inner code for testing by removing !Ngin3d.usingA3m()
    // conditional statements. Then, run instrumentation test to test A3M's
    // support for getTotalCImageBytes and getTotalTextureBytes.
    @SmallTest
    public void testImageFromResource() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        for (int i = 0 ; i < 10 ; i ++) {
            Image bmpImage = Image.createFromResource(getInstrumentation().getContext().getResources(), R.drawable.earth);
            Image pngImage = Image.createFromResource(getInstrumentation().getContext().getResources(), R.drawable.building_london);
            Image jpgImage = Image.createFromResource(getInstrumentation().getContext().getResources(), R.drawable.photo_01);

            bmpImage.realize(mPresentationEngine);
            BitmapFactory.decodeResource(getInstrumentation().getContext().getResources(), R.drawable.earth, options);
            assertEquals(options.outHeight, (int) bmpImage.getSize().height);
            assertEquals(options.outWidth, (int)bmpImage.getSize().width);

            pngImage.realize(mPresentationEngine);
            BitmapFactory.decodeResource(getInstrumentation().getContext().getResources(), R.drawable.building_london, options);
            assertEquals(options.outHeight, (int)pngImage.getSize().height);
            assertEquals(options.outWidth, (int)pngImage.getSize().width);

            jpgImage.realize(mPresentationEngine);
            BitmapFactory.decodeResource(getInstrumentation().getContext().getResources(), R.drawable.photo_01, options);
            assertEquals(options.outHeight, (int)jpgImage.getSize().height);
            assertEquals(options.outWidth, (int)jpgImage.getSize().width);

            bmpImage.unrealize();
            pngImage.unrealize();
            jpgImage.unrealize();

        }
    }

    // \todo Add support for getTotalCImageBytes and getTotalTextureBytes in
    // A3M. Put back inner code for testing by removing !Ngin3d.usingA3m()
    // conditional statements. Then, run instrumentation test to test A3M's
    // support for getTotalCImageBytes and getTotalTextureBytes.
    @SmallTest
    public void testImageFromBitmap() {
        int totalImageBytes = 0;
        int totalTextureBytes = 0;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        for (int i = 0 ; i < 10 ; i ++) {
            Bitmap bmpBitmap = BitmapFactory.decodeResource(getInstrumentation().getContext().getResources(), R.drawable.earth, options);
            Image bmpImage = Image.createFromBitmap(bmpBitmap);

            bmpImage.realize(mPresentationEngine);

            assertEquals(options.outHeight, (int) bmpImage.getSize().height);
            assertEquals(options.outWidth, (int) bmpImage.getSize().width);

            Bitmap pngBitmap = BitmapFactory.decodeResource(getInstrumentation().getContext().getResources(), R.drawable.building_london, options);
            Image pngImage = Image.createFromBitmap(pngBitmap);
            pngImage.realize(mPresentationEngine);
            assertEquals(options.outHeight, (int)pngImage.getSize().height);
            assertEquals(options.outWidth, (int)pngImage.getSize().width);

            Bitmap jpgBitmap = BitmapFactory.decodeResource(getInstrumentation().getContext().getResources(), R.drawable.photo_01, options);
            Image jpgImage = Image.createFromBitmap(jpgBitmap);
            jpgImage.realize(mPresentationEngine);
            assertEquals(options.outHeight, (int)jpgImage.getSize().height);
            assertEquals(options.outWidth, (int)jpgImage.getSize().width);

            bmpImage.unrealize();
            pngImage.unrealize();
            jpgImage.unrealize();

        }
    }

    // \todo Add support for getTotalCImageBytes and getTotalTextureBytes in
    // A3M. Put back inner code for testing by removing !Ngin3d.usingA3m()
    // conditional statements. Then, run instrumentation test to test A3M's
    // support for getTotalCImageBytes and getTotalTextureBytes.
    @SmallTest
    public void testImageFromBitmapGenerator() {
        final int size = 32;
        BitmapGenerator generator = new BitmapGenerator() {
            public Bitmap generate() {
                Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                return bitmap;
            }
        };

        for (int i = 0; i < 10; i++) {
            Image bmpImage = Image.createFromBitmapGenerator(generator);
            generator.cacheBitmap();
            bmpImage.realize(mPresentationEngine);
            assertEquals(size, (int)bmpImage.getSize().height);
            assertEquals(size, (int)bmpImage.getSize().width);

            bmpImage.unrealize();
        }

        generator.cacheBitmap();
        assertNotNull(generator.getBitmap());
        generator.free();
        assertNull(generator.getBitmap());
    }

    // \todo Add support for getTotalCImageBytes and getTotalTextureBytes in
    // A3M. Put back inner code for testing by removing !Ngin3d.usingA3m()
    // conditional statements. Then, run instrumentation test to test A3M's
    // support for getTotalCImageBytes and getTotalTextureBytes.
    @SmallTest
    public void testImageFromFile() {
        int totalImageBytes = 0;
        int totalTextureBytes = 0;

        String imagePath = "/sdcard/a3d/";
        // For A3M, to load an image file it is required to register a file
        // path with the asset pool. A3M native engine then uses it search the
        // image file from it.

        AssetPool pool =
            ((A3mPresentationEngine) mPresentationEngine).getAssetPool();
        pool.registerSource(imagePath);


        String testFile1 = "photo_01_big.jpg";
        String testFile2 = "photo_02_big.jpg";
        String testFile3 = "photo_03_big.jpg";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            for (int i = 0 ; i < 10 ; i ++) {
                Image image1;
                Image image2;
                Image image3;
                image1 = Image.createFromFile(testFile1);
                image2 = Image.createFromFile(testFile2);
                image3 = Image.createFromFile(testFile3);

                image1.realize(mPresentationEngine);
                BitmapFactory.decodeFile(imagePath + testFile1, options);
                assertEquals(options.outHeight, (int)image1.getSize().height);
                assertEquals(options.outWidth, (int)image1.getSize().width);

                image2.realize(mPresentationEngine);
                BitmapFactory.decodeFile(imagePath + testFile2, options);
                assertEquals(options.outHeight, (int)image2.getSize().height);
                assertEquals(options.outWidth, (int)image2.getSize().width);

                image3.realize(mPresentationEngine);
                BitmapFactory.decodeFile(imagePath+ testFile3, options);
                assertEquals(options.outHeight, (int)image3.getSize().height);
                assertEquals(options.outWidth, (int)image3.getSize().width);

                image1.unrealize();
                image2.unrealize();
                image3.unrealize();
            }
        }
    }

    @SmallTest
    public void testImageDecoding() throws Exception {

        AssetManager assetManager = getInstrumentation().getContext().getAssets();

        try {
            InputStream is = assetManager.open("photo_01.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();
            Image image = Image.createFromBitmap(bitmap);
            image.realize(mPresentationEngine);
        } catch (IOException e) {
            throw e;
        }

        try {
            Image image = Image.createFromResource(getInstrumentation().getContext().getResources(), R.drawable.sydney);
            image.realize(mPresentationEngine);
        } catch (Exception e) {
            throw e;
        }

        // test null case
        try {
            Image.createFromFile(null);
            fail("NullPointerException expected.");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            Image.createFromBitmap(null);
            fail("NullPointerException expected.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @SmallTest
    public void testOpacitySet
            () {
        mImage.setOpacity(0);
        assertEquals(mImage.getOpacity(), 0);

        mImage.setOpacity(255);
        assertEquals(mImage.getOpacity(), 255);

        try {
            mImage.setOpacity(300);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(mImage.getOpacity(), 255);

        try {
            mImage.setOpacity(-50);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(mImage.getOpacity(), 255);
    }

    public void testImageSource() {
        ImageDisplay.Resource res = new ImageDisplay.Resource(getInstrumentation().getContext().getResources(), R.drawable.android);
        ImageSource imageSource = new ImageSource(ImageSource.RES_ID, res);
        ImageSource imageSource2 = new ImageSource(ImageSource.RES_ID, res);
        assertEquals(imageSource2.toString(), imageSource.toString());

        ImageDisplay.Resource res1 = new ImageDisplay.Resource(getInstrumentation().getContext().getResources(), R.drawable.android);
        ImageDisplay.Resource res2 = new ImageDisplay.Resource(getInstrumentation().getContext().getResources(), R.drawable.earth);
        assertEquals(res, res1);
        assertNotEqual(res, res2);
        assertEquals(res.hashCode(), res1.hashCode());
    }

    public void testOtherMethod() {
        Image image = Image.createFromResource(getInstrumentation().getContext().getResources(), R.drawable.earth);
        image.setFilterQuality(1);
        assertThat(image.getFilterQuality(), is(1));

        image.setRepeat(2, 2);
        assertThat(image.getRepeatX(), is(2));
        assertThat(image.getRepeatY(), is(2));

        image.setKeepAspectRatio(true);
        assertTrue(image.isKeepAspectRatio());
    }
}
