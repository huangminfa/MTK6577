package com.mediatek.media3d.weather;

import android.content.Context;
import com.mediatek.media3d.FormatUtil;
import com.mediatek.media3d.LogUtil;
import com.mediatek.media3d.R;
import com.mediatek.ngin3d.Box;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Dimension;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Text;

import java.util.Calendar;
import java.util.TimeZone;

public class DigitClock extends Container {

    static final String TAG = "Media3D.DigitClock";

    private final Image mH1;
    private final Image mH2;
    private final Image mM1;
    private final Image mM2;
    private final Image mComma;
    private final Text mHighLowTemp;
    private final Text mDate;

    /* digit: 116x163 */
    static final int DIGIT_WIDTH = 105;
    static final int DIGIT_HEIGHT = 147;
    /* comma, ":" : 49x163 , it is the last position */
    static final int COMMA_WIDTH = 45;
    static final int COMMA_HEIGHT = 147;

    private final int[] mDigitOffset;

    private LocationWeather mLocation;
    private int mHour = -1;
    private int mMinute = -1;

    public DigitClock(final String name, final Context c, final LocationWeather location) {
        mLocation = location;

        mDigitOffset = new int[11];
        int dx = 0;
        for (int i = 0; i < mDigitOffset.length; i++) {
            mDigitOffset[i] = dx;
            dx += DIGIT_WIDTH;
        }

        mH1 = Image.createFromResource(c.getResources(), R.drawable.digi_numbers);
        mH2 = Image.createFromResource(c.getResources(), R.drawable.digi_numbers);
        mM1 = Image.createFromResource(c.getResources(), R.drawable.digi_numbers);
        mM2 = Image.createFromResource(c.getResources(), R.drawable.digi_numbers);
        mComma = Image.createFromResource(c.getResources(), R.drawable.digi_numbers);
        final Image panel = Image.createFromResource(c.getResources(), R.drawable.timepanel_blank);

        mHighLowTemp = mLocation.getTempHighLowText(c);
        FormatUtil.applyTextAttributes(mHighLowTemp, 22, true);
        mDate = mLocation.getDateText();
        FormatUtil.applyTextAttributes(mDate, 22, false);

        mH1.setSize(new Dimension(DIGIT_WIDTH, DIGIT_HEIGHT));
        mH2.setSize(new Dimension(DIGIT_WIDTH, DIGIT_HEIGHT));
        mM1.setSize(new Dimension(DIGIT_WIDTH, DIGIT_HEIGHT));
        mM2.setSize(new Dimension(DIGIT_WIDTH, DIGIT_HEIGHT));
        mComma.setSize(new Dimension(COMMA_WIDTH, COMMA_HEIGHT));

        panel.setPosition(new Point(359, 105, 10));
        mH1.setPosition(new Point(204, 101, -10));
        mH2.setPosition(new Point(320, 101, -10));
        mM1.setPosition(new Point(475, 101, -10));
        mM2.setPosition(new Point(591, 101, -10));
        mComma.setPosition(new Point(402, 101, -10));
        mHighLowTemp.setPosition(new Point(90, 190, -10));
        mDate.setPosition(new Point(620, 190, -10));

        this.add(mH1, mH2, mM1, mM2, mComma, panel, mHighLowTemp, mDate);
    }

    private void setDigit(final Image digit, int n) {
        LogUtil.v(TAG, "digit :" + n);
        final int offset = (n >= 0) ? (n % 10) : (-n) % 10; /* only 0~9 */
        final int left = mDigitOffset[offset];
        digit.setSourceRect(new Box(left, 0, left + DIGIT_WIDTH, DIGIT_HEIGHT));
    }

    public void update(final LocationWeather location, final Context context) {
        mLocation = location;
        final TimeZone timeZone = TimeZone.getTimeZone(mLocation.getTimeZone());
        final Calendar now = Calendar.getInstance(timeZone);
        final int hourNow = now.get(Calendar.HOUR_OF_DAY);
        final int minuteNow = now.get(Calendar.MINUTE);
        boolean updated = false;

        LogUtil.v(TAG, "At time :" + timeZone + ", Hour :" + hourNow + ", Minute :" + minuteNow);
        if (hourNow != mHour) {
            mHour = hourNow;
            setDigit(mH1, mHour / 10);
            setDigit(mH2, mHour % 10);
            updated = true;
        }
        if (minuteNow != mMinute) {
            mMinute = minuteNow;
            setDigit(mM1, mMinute / 10);
            setDigit(mM2, mMinute % 10);
            updated = true;
        }

        if (updated) {
            mComma.setSourceRect(new Box(mDigitOffset[10], 0, mDigitOffset[10] + COMMA_WIDTH, COMMA_HEIGHT));
        }

        mHighLowTemp.setText(mLocation.getTempHighLowString(context));
        mDate.setText(mLocation.getDateString());
    }
}