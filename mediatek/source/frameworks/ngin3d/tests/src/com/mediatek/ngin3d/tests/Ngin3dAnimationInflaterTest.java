package com.mediatek.ngin3d.tests;

import android.test.ActivityInstrumentationTestCase2;
import com.mediatek.ngin3d.Color;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.Scale;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.android.Ngin3dAnimationInflater;
import com.mediatek.ngin3d.android.Ngin3dLayoutInflater;
import com.mediatek.ngin3d.animation.AnimationGroup;
import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.animation.Mode;
import com.mediatek.ngin3d.animation.PropertyAnimation;
import com.mediatek.ngin3d.animation.SpriteAnimation;

public class Ngin3dAnimationInflaterTest extends ActivityInstrumentationTestCase2<PresentationStubActivity> {

    protected Stage mTestStage;
    protected PresentationStubActivity mActivity;

    public Ngin3dAnimationInflaterTest() {
        super("com.mediatek.ngin3d.tests", PresentationStubActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTestStage = new Stage();
        mActivity = getActivity();
    }

    @Override
    protected void tearDown() throws Exception {
        mTestStage = null;
        mActivity = null;
        super.tearDown();
    }

    public void testAnimationPositionPropertyAnimation() {

        Image image = (Image) Ngin3dLayoutInflater.inflateLayout(mActivity, R.xml.test_single_actor_image_with_full_info, mTestStage);
        assertNotNull(image);

        /* TC 1: test "position" PropertyAnimation with full info in xml */
        PropertyAnimation move = (PropertyAnimation) Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_position_property_animatino_with_full_info, mTestStage);
        assertEquals(image , move.getTarget());

        /* TC 2: test "position" PropertyAnimation -- start property info in xml */
        Point startP = new Point(0, 0, 0);
        assertEquals(startP , move.getStartValue());

        /* TC 3: test "position" PropertyAnimation -- end property info in xml */
        Point endP = new Point(480, 800, 0);
        assertEquals(endP , move.getEndValue());

        /* TC 4: test "position" PropertyAnimation -- mode info in xml */
        assertEquals(Mode.EASE_IN_OUT_CUBIC, move.getMode());

        /* TC 5: test "position" PropertyAnimation -- loop info in xml */
        assertEquals(true, move.getLoop());

        /* TC 6: test "position" PropertyAnimation -- autoReverse info in xml */
        assertEquals(true, move.getAutoReverse());

        /* TC 7: test "position" PropertyAnimation -- duration info in xml */
        assertEquals(2000, move.getDuration());

        /* TC 8: test "position" PropertyAnimation with partial info in xml */
        BasicAnimation move2 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_position_property_animatino_with_partial_info, mTestStage);
        assertEquals(image , move2.getTarget());

        /* TC 9: test "position" PropertyAnimation -- mode default value */
        assertEquals(Mode.LINEAR, move2.getMode());

        /* TC 10: test "position" PropertyAnimation -- loop default value */
        assertEquals(false, move2.getLoop());

        /* TC 11: test "position" PropertyAnimation -- autoReverse default value */
        assertEquals(false, move2.getAutoReverse());

        /* TC 12: test "position" PropertyAnimation -- duration default value */
        assertEquals(2000, move2.getDuration());

        /* TC 13: test "position" PropertyAnimation -- without target info */
        BasicAnimation move3 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_position_property_animatino_without_target_info, mTestStage);
        assertNull(move3.getTarget());

        /* TC 14: test "position" PropertyAnimation -- without property info, invalid case*/
        try {
            BasicAnimation move4 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_position_property_animatino_without_property_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }

    }

    public void testAnimationRotationPropertyAnimation() {

        Image image = (Image)Ngin3dLayoutInflater.inflateLayout(mActivity, R.xml.test_single_actor_image_with_full_info, mTestStage);
        assertNotNull(image);

        /* TC 1: test "rotation" PropertyAnimation with full info in xml */
        PropertyAnimation rotation = (PropertyAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_rotation_property_animatino_with_full_info, mTestStage);
        assertEquals(image , rotation.getTarget());

        /* TC 2: test "rotation" PropertyAnimation -- start property info in xml */
        Rotation startR = new Rotation(0, 0, 0);
        assertEquals(startR , rotation.getStartValue());

        /* TC 3: test "rotation" PropertyAnimation -- end property info in xml */
        Rotation endR = new Rotation(0, 0, 360);
        assertEquals(endR, rotation.getEndValue());

        /* TC 4: test "rotation" PropertyAnimation -- mode info in xml */
        assertEquals(Mode.EASE_IN_SINE, rotation.getMode());

        /* TC 5: test "rotation" PropertyAnimation -- loop info in xml */
        assertEquals(true, rotation.getLoop());

        /* TC 6: test "rotation" PropertyAnimation -- autoReverse info in xml */
        assertEquals(true, rotation.getAutoReverse());

        /* TC 7: test "rotation" PropertyAnimation -- duration info in xml */
        assertEquals(2000, rotation.getDuration());

        /* TC 8: test "rotation" PropertyAnimation with partial info in xml */
        BasicAnimation rotation2 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_rotation_property_animatino_with_partial_info, mTestStage);
        assertEquals(image , rotation2.getTarget());

        /* TC 9: test "rotation" PropertyAnimation -- mode default value */
        assertEquals(Mode.LINEAR, rotation2.getMode());

        /* TC 10: test "rotation" PropertyAnimation -- loop default value */
        assertEquals(false, rotation2.getLoop());

        /* TC 11: test "rotation" PropertyAnimation -- autoReverse default value */
        assertEquals(false, rotation2.getAutoReverse());

        /* TC 12: test "rotation" PropertyAnimation -- duration default value */
        assertEquals(2000, rotation2.getDuration());

        /* TC 13: test "rotation" PropertyAnimation -- without target info */
        BasicAnimation rotation3 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_rotation_property_animatino_without_target_info, mTestStage);
        assertNull(rotation3.getTarget());

        /* TC 14: test "rotation" PropertyAnimation -- without property info, invalid case*/
        try {
            BasicAnimation rotation4 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_rotation_property_animatino_without_property_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    public void testAnimationScalePropertyAnimation() {

        Image image = (Image)Ngin3dLayoutInflater.inflateLayout(mActivity, R.xml.test_single_actor_image_with_full_info, mTestStage);
        assertNotNull(image);

        /* TC 1: test "scale" PropertyAnimation with full info in xml */
        PropertyAnimation scale = (PropertyAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_scale_property_animatino_with_full_info, mTestStage);
        assertEquals(image , scale.getTarget());

        /* TC 2: test "scale" PropertyAnimation -- start property info in xml */
        Scale startS = new Scale(1, 1, 1);
        assertEquals(startS , scale.getStartValue());

        /* TC 3: test "scale" PropertyAnimation -- end property info in xml */
        Scale endS = new Scale(2, 2, 2);
        assertEquals(endS, scale.getEndValue());

        /* TC 4: test "scale" PropertyAnimation -- mode info in xml */
        assertEquals(Mode.EASE_IN_OUT_QUAD, scale.getMode());

        /* TC 5: test "scale" PropertyAnimation -- loop info in xml */
        assertEquals(true, scale.getLoop());

        /* TC 6: test "scale" PropertyAnimation -- autoReverse info in xml */
        assertEquals(true, scale.getAutoReverse());

        /* TC 7: test "scale" PropertyAnimation -- duration info in xml */
        assertEquals(2000, scale.getDuration());

        /* TC 8: test "scale" PropertyAnimation with partial info in xml */
        BasicAnimation scale2 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_scale_property_animatino_with_partial_info, mTestStage);
        assertEquals(image , scale2.getTarget());

        /* TC 9: test "scale" PropertyAnimation -- mode default value */
        assertEquals(Mode.LINEAR, scale2.getMode());

        /* TC 10: test "scale" PropertyAnimation -- loop default value */
        assertEquals(false, scale2.getLoop());

        /* TC 11: test "scale" PropertyAnimation -- autoReverse default value */
        assertEquals(false, scale2.getAutoReverse());

        /* TC 12: test "scale" PropertyAnimation -- duration default value */
        assertEquals(2000, scale2.getDuration());

        /* TC 13: test "scale" PropertyAnimation -- without target info */
        BasicAnimation scale3 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_scale_property_animatino_without_target_info, mTestStage);
        assertNull(scale3.getTarget());

        /* TC 14: test "scale" PropertyAnimation -- without property info, invalid case*/
        try {
            BasicAnimation scale4 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_scale_property_animatino_without_property_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    public void testAnimationColorPropertyAnimation() {

        Image image = (Image)Ngin3dLayoutInflater.inflateLayout(mActivity, R.xml.test_single_actor_image_with_full_info, mTestStage);
        assertNotNull(image);

        /* TC 1: test "color" PropertyAnimation with full info in xml */
        PropertyAnimation color = (PropertyAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_color_property_animatino_with_full_info, mTestStage);
        assertEquals(image , color.getTarget());

        /* TC 2: test "color" PropertyAnimation -- start property info in xml */
        Color startC = new Color(Color.WHITE.getRgb());
        assertEquals(startC , color.getStartValue());

        /* TC 3: test "color" PropertyAnimation -- end property info in xml */
        Color endC = new Color(Color.BLACK.getRgb());
        assertEquals(endC, color.getEndValue());

        /* TC 4: test "color" PropertyAnimation -- mode info in xml */
        assertEquals(Mode.EASE_IN_OUT_QUAD, color.getMode());

        /* TC 5: test "color" PropertyAnimation -- loop info in xml */
        assertEquals(true, color.getLoop());

        /* TC 6: test "color" PropertyAnimation -- autoReverse info in xml */
        assertEquals(true, color.getAutoReverse());

        /* TC 7: test "color" PropertyAnimation -- duration info in xml */
        assertEquals(2000, color.getDuration());

        /* TC 8: test "color" PropertyAnimation with partial info in xml */
        BasicAnimation color2 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_color_property_animatino_with_partial_info, mTestStage);
        assertEquals(image , color2.getTarget());

        /* TC 9: test "color" PropertyAnimation -- mode default value */
        assertEquals(Mode.LINEAR, color2.getMode());

        /* TC 10: test "color" PropertyAnimation -- loop default value */
        assertEquals(false, color2.getLoop());

        /* TC 11: test "color" PropertyAnimation -- autoReverse default value */
        assertEquals(false, color2.getAutoReverse());

        /* TC 12: test "color" PropertyAnimation -- duration default value */
        assertEquals(2000, color2.getDuration());

        /* TC 13: test "color" PropertyAnimation -- without target info */
        BasicAnimation color3 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_color_property_animatino_without_target_info, mTestStage);
        assertNull(color3.getTarget());

        /* TC 14: test "color" PropertyAnimation -- without property info, invalid case*/
        try {
            BasicAnimation color4 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_color_property_animatino_without_property_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    public void testAnimationOpacityPropertyAnimation() {

        Image image = (Image)Ngin3dLayoutInflater.inflateLayout(mActivity, R.xml.test_single_actor_image_with_full_info, mTestStage);
        assertNotNull(image);

        /* TC 1: test "opacity" PropertyAnimation with full info in xml */
        PropertyAnimation opacity = (PropertyAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_opacity_property_animatino_with_full_info, mTestStage);
        assertEquals(image , opacity.getTarget());

        /* TC 2: test "opacity" PropertyAnimation -- start property info in xml */
        Integer startO = 0;
        assertEquals(startO , opacity.getStartValue());

        /* TC 3: test "opacity" PropertyAnimation -- end property info in xml */
        Integer endO = 255;
        assertEquals(endO, opacity.getEndValue());

        /* TC 4: test "opacity" PropertyAnimation -- mode info in xml */
        assertEquals(Mode.EASE_IN_OUT_QUAD, opacity.getMode());

        /* TC 5: test "opacity" PropertyAnimation -- loop info in xml */
        assertEquals(true, opacity.getLoop());

        /* TC 6: test "opacity" PropertyAnimation -- autoReverse info in xml */
        assertEquals(true, opacity.getAutoReverse());

        /* TC 7: test "opacity" PropertyAnimation -- duration info in xml */
        assertEquals(2000, opacity.getDuration());

        /* TC 8: test "opacity" PropertyAnimation with partial info in xml */
        BasicAnimation opacity2 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_opacity_property_animatino_with_partial_info, mTestStage);
        assertEquals(image , opacity2.getTarget());

        /* TC 9: test "opacity" PropertyAnimation -- mode default value */
        assertEquals(Mode.LINEAR, opacity2.getMode());

        /* TC 10: test "opacity" PropertyAnimation -- loop default value */
        assertEquals(false, opacity2.getLoop());

        /* TC 11: test "opacity" PropertyAnimation -- autoReverse default value */
        assertEquals(false, opacity2.getAutoReverse());

        /* TC 12: test "opacity" PropertyAnimation -- duration default value */
        assertEquals(2000, opacity2.getDuration());

        /* TC 13: test "opacity" PropertyAnimation -- without target info */
        BasicAnimation opacity3 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_opacity_property_animatino_without_target_info, mTestStage);
        assertNull(opacity3.getTarget());

        /* TC 14: test "opacity" PropertyAnimation -- without property info, invalid case*/
        try {
            BasicAnimation opacity4 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_opacity_property_animatino_without_property_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    public void testAnimationKeyframeAnimation() {

        Image image = (Image)Ngin3dLayoutInflater.inflateLayout(mActivity, R.xml.test_single_actor_image_with_full_info, mTestStage);
        assertNotNull(image);

        /* TC 1: test KeyframeAnimation with full info in xml */
        BasicAnimation keyframeAnimation = (BasicAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_keyframe_animation_with_full_info, mTestStage);
        assertEquals(image , keyframeAnimation.getTarget());

        /* TC 2: test KeyframeAnimation -- loop info in xml */
        assertEquals(true, keyframeAnimation.getLoop());

        /* TC 3: test KeyframeAnimation -- autoReverse info in xml */
        assertEquals(true, keyframeAnimation.getAutoReverse());

        /* TC 4: test KeyframeAnimation with partial info in xml */
        BasicAnimation keyframeAnimation2 = (BasicAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_keyframe_animation_with_partial_info, mTestStage);
        assertEquals(image , keyframeAnimation2.getTarget());

        /* TC 5: test KeyframeAnimation -- loop default valuel */
        assertEquals(true, keyframeAnimation2.getLoop());

        /* TC 6: test KeyframeAnimation -- autoReverse default value */
        assertEquals(true, keyframeAnimation2.getAutoReverse());

        /* TC 7: test KeyframeAnimation without target info in xml */
        BasicAnimation keyframeAnimation3 = (BasicAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_keyframe_animation_without_target_info, mTestStage);
        assertNull(keyframeAnimation3.getTarget());

        /* TC 8: test KeyframeAnimation without keyframe info in xml */
        try {
            BasicAnimation keyframeAnimation4 = Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_keyframe_animation_without_keyframe_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    public void testAnimationMultiSpriteAnimation() {

        Image image = (Image)Ngin3dLayoutInflater.inflateLayout(mActivity, R.xml.test_single_actor_multi_sprite_animation_target, mTestStage);
        assertNotNull(image);

        /* TC 1: test multi SpriteAnimation with full info in xml */
        SpriteAnimation multiSpriteAni = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_multi_sprite_animation_with_full_info, mTestStage);
        assertEquals(image , multiSpriteAni.getTarget());

        /* TC 2: test multi SpriteAnimation -- get frame count */
        assertEquals(4 , multiSpriteAni.getSpriteFrameCount());

        /* TC 3: test multi SpriteAnimation -- loop info in xml */
        assertEquals(true, multiSpriteAni.getLoop());

        /* TC 4: test multi SpriteAnimation -- autoReverse info in xml */
        assertEquals(true, multiSpriteAni.getAutoReverse());

        /* TC 5: test multi SpriteAnimation -- duration info in xml */
        assertEquals(200, multiSpriteAni.getDuration());

        /* TC 6: test multi SpriteAnimation with partial info in xml */
        SpriteAnimation multiSpriteAni2 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_multi_sprite_animation_with_partial_info, mTestStage);
        assertEquals(image , multiSpriteAni2.getTarget());

        /* TC 7: test multi SpriteAnimation -- get frame count */
        assertEquals(0 , multiSpriteAni2.getSpriteFrameCount());

        /* TC 8: test multi SpriteAnimation -- loop info in xml */
        assertEquals(false, multiSpriteAni2.getLoop());

        /* TC 9: test multi SpriteAnimation -- autoReverse info in xml */
        assertEquals(false, multiSpriteAni2.getAutoReverse());

        /* TC 10: test multi SpriteAnimation -- duration info in xml */
        assertEquals(200, multiSpriteAni2.getDuration());

        /* TC 11: test multi SpriteAnimation without target info in xml */
        SpriteAnimation multiSpriteAni3 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_multi_sprite_animation_without_target_info, mTestStage);
        assertNull(multiSpriteAni3.getTarget());

        /* TC 12: test multi SpriteAnimation without duration info in xml */
        try {
            SpriteAnimation multiSpriteAni4 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_multi_sprite_animation_without_duration_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }

        /* TC 13: test multi SpriteAnimation without type info in xml */
        try {
            SpriteAnimation multiSpriteAni5 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_multi_sprite_animation_without_type_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    public void testAnimationSingleSpriteAnimation() {

        /* TC 1: test single SpriteAnimation with full info in xml */
        Image image2 = (Image)Ngin3dLayoutInflater.inflateLayout(mActivity, R.xml.test_single_actor_single_sprite_animation_target, mTestStage);
        assertNotNull(image2);

        SpriteAnimation singleSpriteAni = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_single_sprite_animation_with_full_info, mTestStage);
        assertEquals(image2 , singleSpriteAni.getTarget());

        /* TC 2: test single SpriteAnimation -- get frame count */
        assertEquals(16 , singleSpriteAni.getSpriteFrameCount());

        /* TC 3: test single SpriteAnimation -- loop info in xml */
        assertEquals(true, singleSpriteAni.getLoop());

        /* TC 4: test single SpriteAnimation -- autoReverse info in xml */
        assertEquals(true, singleSpriteAni.getAutoReverse());

        /* TC 5: test single SpriteAnimation -- duration info in xml */
        assertEquals(5000, singleSpriteAni.getDuration());

        /* TC 6: test single SpriteAnimation with partial info in xml */
        SpriteAnimation singleSpriteAni2 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_single_sprite_animation_with_partial_info, mTestStage);
        assertEquals(image2 , singleSpriteAni2.getTarget());

        /* TC 7: test single SpriteAnimation -- get frame count */
        assertEquals(16 , singleSpriteAni2.getSpriteFrameCount());

        /* TC 8: test single SpriteAnimation -- loop info in xml */
        assertEquals(false, singleSpriteAni2.getLoop());

        /* TC 9: test single SpriteAnimation -- autoReverse info in xml */
        assertEquals(false, singleSpriteAni2.getAutoReverse());

        /* TC 10: test single SpriteAnimation -- duration info in xml */
        assertEquals(5000, singleSpriteAni2.getDuration());

        /* TC 11: test single SpriteAnimation without target info in xml */
        SpriteAnimation singleSpriteAni3 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_single_sprite_animation_without_target_info, mTestStage);
        assertNull(singleSpriteAni3.getTarget());

        /* TC 12: test single SpriteAnimation without type info in xml */
        try {
            SpriteAnimation singleSpriteAni4 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_single_sprite_animation_without_type_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }

        /* TC 13: test single SpriteAnimation without duration info in xml */
        try {
            SpriteAnimation singleSpriteAni5 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_single_sprite_animation_without_duration_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }

        /* TC 14: test single SpriteAnimation without width/height info in xml */
        try {
            SpriteAnimation singleSpriteAni6 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_single_sprite_animation_without_width_height_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }

        /* TC 15: test single SpriteAnimation without image src info in xml */
        try {
            SpriteAnimation singleSpriteAni7 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_single_sprite_animation_without_image_src_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    public void testAnimationSheetSpriteAnimation() {
		
        /* TC 1: test sheet SpriteAnimation with full info in xml */
        Image image3 = (Image)Ngin3dLayoutInflater.inflateLayout(mActivity, R.xml.test_single_actor_sheet_sprite_animation_target, mTestStage);
        assertNotNull(image3);

        SpriteAnimation sheetSpriteAni = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_sheet_sprite_animation_with_full_info, mTestStage);
        assertEquals(image3 , sheetSpriteAni.getTarget());

        /* TC 2: test sheet SpriteAnimation -- loop info in xml */
        assertEquals(true, sheetSpriteAni.getLoop());

        /* TC 3: test sheet SpriteAnimation -- autoReverse info in xml */
        assertEquals(true, sheetSpriteAni.getAutoReverse());

        /* TC 4: test sheet SpriteAnimation -- duration info in xml */
        assertEquals(1500, sheetSpriteAni.getDuration());

        /* TC 5: test sheet SpriteAnimation with partial info in xml */
        SpriteAnimation sheetSpriteAni2 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_sheet_sprite_animation_with_partial_info, mTestStage);
        assertEquals(image3 , sheetSpriteAni2.getTarget());

        /* TC 6: test sheet SpriteAnimation -- loop info in xml */
        assertEquals(false, sheetSpriteAni2.getLoop());

        /* TC 7: test sheet SpriteAnimation -- autoReverse info in xml */
        assertEquals(false, sheetSpriteAni2.getAutoReverse());

        /* TC 8: test sheet SpriteAnimation -- duration info in xml */
        assertEquals(1500, sheetSpriteAni.getDuration());

        /* TC 9: test sheet SpriteAnimation without target info in xml */
        SpriteAnimation sheetSpriteAni3 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_sheet_sprite_animation_without_target_info, mTestStage);
        assertNull(sheetSpriteAni3.getTarget());

        /* TC 10: test sheet SpriteAnimation without type info in xml */
        try {
            SpriteAnimation sheetSpriteAni4 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_sheet_sprite_animation_without_type_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }

        /* TC 11: test sheet SpriteAnimation without duration info in xml */
        try {
            SpriteAnimation sheetSpriteAni5 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_sheet_sprite_animation_without_duration_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }

        /* TC 12: test sheet SpriteAnimation without image src info in xml */
        try {
            SpriteAnimation sheetSpriteAni6 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_sheet_sprite_animation_without_image_src_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }

        /* TC 13: test sheet SpriteAnimation without image descritpion info in xml */
        try {
            SpriteAnimation sheetSpriteAni7 = (SpriteAnimation)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_sheet_sprite_animation_without_image_description_info, mTestStage);
            fail("Should throw exception.");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }


    public void testAnimationAnimationGroup() {

        Image image = (Image) Ngin3dLayoutInflater.inflateLayout(mActivity, R.xml.test_single_actor_image_with_full_info, mTestStage);
        assertNotNull(image);

        /* TC 1: test AnimationGroup with full info in xml */
        AnimationGroup animationGroup = (AnimationGroup)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_single_animation_group_with_full_info, mTestStage);
        assertEquals(image , animationGroup.getTarget());

        /* TC 2: test AnimationGroup -- loop info in xml */
        assertEquals(true, animationGroup.getLoop());

        /* TC 3: test AnimationGroup -- autoReverse info in xml */
        assertEquals(true, animationGroup.getAutoReverse());

        /* TC 4: test AnimationGroup -- animation count in xml */
        assertEquals(4 , animationGroup.getAnimationCount());

        /* TC 5: test AnimationGroup with partial info in xml */
        AnimationGroup animationGroup2 = (AnimationGroup)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_single_animation_group_with_partial_info, mTestStage);
        assertEquals(image , animationGroup2.getTarget());

        /* TC 6: test AnimationGroup -- loop info in xml */
        assertEquals(false, animationGroup2.getLoop());

        /* TC 7: test AnimationGroup -- autoReverse info in xml */
        assertEquals(false, animationGroup2.getAutoReverse());

        /* TC 8: test AnimationGroup -- animation count in xml */
        assertEquals(2 , animationGroup2.getAnimationCount());

        /* TC 9: test AnimationGroup without target info in xml */
        AnimationGroup animationGroup3 = (AnimationGroup)Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_single_animation_group_without_target_info, mTestStage);
        assertNull(animationGroup3.getTarget());

        /* TC 10: test nested AnimationGroup */
        AnimationGroup animationGroup4 = (AnimationGroup) Ngin3dAnimationInflater.inflateAnimation(mActivity, R.xml.test_nested_animation_group, mTestStage);
        assertEquals(image , animationGroup2.getTarget());

        /* TC 11: test AnimationGroup -- animation count in xml */
        /* failed case, need ngin3d's support 
        assertEquals(5 , animationGroup4.getAnimationCount());  */

        /* TC 12: test AnimationGroup -- check one of the child's info in xml */
        /* failed case, need ngin3d's support 
        BasicAnimation childAnimation = (BasicAnimation)animationGroup4.getAnimationByTag(R.id.keyframe_animation3);
        assertEquals(false , childAnimation.getLoop());
        assertEquals(false , childAnimation.getAutoReverse()); */
    }

}
