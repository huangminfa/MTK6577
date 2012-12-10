package com.mediatek.ngin3d.tests;

import android.test.ActivityInstrumentationTestCase2;
import com.mediatek.ngin3d.BitmapFont;
import com.mediatek.ngin3d.BitmapText;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Text;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BitmapTextTest extends ActivityInstrumentationTestCase2<PresentationStubActivity> {
    public BitmapTextTest() {
        super("com.mediatek.ngin3d.tests", PresentationStubActivity.class);
    }

    private PresentationStubActivity mActivity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }

    public void testBmText() {
        Point p1 = new Point(1f, 0f);

        BitmapFont font = new BitmapFont(mActivity.getResources(), R.raw.bmfont, R.drawable.bmfont);
        BitmapText.setDefaultFont(font);
        assertEquals(font, BitmapText.getDefaultFont());
        Text text = new Text();
        text.setAnchorPoint(p1);
        assertEquals(p1, text.getAnchorPoint());
        text.setText("Test");
        assertThat(text.getText(), is("Test"));

        BitmapText text2 = new BitmapText("Test2");
        assertThat(text2.getText(), is("Test2"));
        BitmapFont font2 = new BitmapFont(mActivity.getResources(), R.raw.bmfont, R.drawable.bmfont);
        text2.setFont(font2);
        assertEquals(font2, text2.getFont());
        assertThat(text2.getText(), is("Test2"));
        text2.setAnchorPoint(p1);
        assertEquals(p1, text2.getAnchorPoint());

        BitmapFont font3 = new BitmapFont(mActivity.getResources(), R.raw.bmfont, R.drawable.bmfont);
        BitmapText text3 = new BitmapText("Test3", font3);
        assertEquals(font3, text3.getFont());
        assertThat(text3.getText(), is("Test3"));

        Text text4 = new Text("string");
        text4.setText("string2");
        assertThat(text4.getText(), is("string2"));

        Color c = new Color(0, 0, 0);
        text4.setBackgroundColor(c);
        assertEquals(c, text4.getBackgroundColor());
        text4.setTextColor(c);
        assertEquals(c, text4.getTextColor());
        text4.setTextSize(32f);
        assertThat(text4.getTextSize(), is(32f));
        text4.setTypeface(null);
        assertNull(text4.getTypeface());
        text4.setShadowLayer(2f, 1f, 1f, 100);
        assertThat(text4.getShadowLayer().radius, is(2f));
        assertThat(text4.getShadowLayer().color, is(100));
        assertThat(text4.getShadowLayer().dx, is(1f));
        assertThat(text4.getShadowLayer().dy, is(1f));
    }
}
