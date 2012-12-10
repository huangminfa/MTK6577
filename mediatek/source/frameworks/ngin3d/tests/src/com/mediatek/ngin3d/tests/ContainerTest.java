package com.mediatek.ngin3d.tests;

import android.test.suitebuilder.annotation.SmallTest;
import com.mediatek.ngin3d.Actor;
import com.mediatek.ngin3d.Container;
import com.mediatek.ngin3d.Empty;
import com.mediatek.ngin3d.Group;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.Plane;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Text;
import com.mediatek.ngin3d.presentation.Presentation;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ContainerTest extends Ngin3dTest {
    Container mContainer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mContainer = new Container();
        mStage.add(mContainer);
    }

    @SmallTest
    public void testBasics() {
        Container<Presentation> container = new Container<Presentation>();
        Text text = new Text();
        Actor empty = new Empty();
        container.add(text);
        container.add(empty);
        assertEquals(2, container.getChildrenCount());

        assertSame(text, container.<Actor>getChild(0));
        container.<Text>getChild(0).setText("First");
        assertEquals("First", text.getText());

        assertSame(empty, container.<Actor>getChild(1));
        empty.setPosition(new Point(1, 1, 1));
        assertEquals(new Point(1, 1, 1), container.<Empty>getChild(1).getPosition());

        container.realize(mPresentationEngine);
        assertFalse("Container should not be dirty after realized", container.isDirty());
        assertFalse("Text should not be dirty after its container is realized", text.isDirty());
        assertFalse("Actor should not be dirty after its container is realized", empty.isDirty());

        Container<Presentation> container2 = new Container<Presentation>();
        container.add(container2);
        container.realize(mPresentationEngine);
        assertFalse("Container should not be dirty after its container is realized", container2.isDirty());
    }

    @SmallTest
    public void testAddRemove() {
        Actor actor = new Image();
        mContainer.add(actor);
        assertEquals(mContainer.getChildren().size(), 1);

        mContainer.remove(actor);
        assertEquals(mContainer.getChildren().size(), 0);

        // Can add duplicate actor?
        // Can add null actor?
        // Will throw exception when actor to remove does not exist?
    }

    @SmallTest
    public void testAddNull() {
        Empty empty = new Empty();
        mContainer.add(empty, null);
        assertEquals(1, mContainer.getChildren().size());
    }

    @SmallTest 
    public void testAddAgain() {
        Actor actor = new Image();
        mContainer.add(actor);
        mContainer.remove(actor);
        mContainer.add(actor);
    }

    @SmallTest
    public void testFind() {
        Container childContainer1 = new Container();
        Container childContainer2 = new Container();
        Container childContainer3 = new Container();

        Actor grandchild1 = new Image();
        Actor grandchild2 = new Image();
        Actor grandchild3 = new Image();
        mContainer.add(childContainer1);
        mContainer.add(childContainer2);
        mContainer.add(childContainer3);
        childContainer1.add(grandchild1);
        childContainer2.add(grandchild2);
        childContainer3.add(grandchild3);

        childContainer1.setName("childContainer1");
        childContainer2.setName("childContainer2");
        childContainer3.setName("childContainer3");
        grandchild1.setName("granchild1");
        grandchild2.setName("granchild2");
        grandchild3.setName("granchild3");

        Actor foundChild = mContainer.findChildByName(childContainer1.getName());
        assertEquals(childContainer1, foundChild);
        foundChild = mContainer.findChildByName(childContainer1.getName(), Group.BREADTH_FIRST_SEARCH);
        assertEquals(childContainer1, foundChild);

        Actor foundGrandchild = mContainer.findChildByName(grandchild1.getName());
        assertEquals(null, foundGrandchild);
        foundGrandchild = mContainer.findChildByName(grandchild1.getName(), Group.BREADTH_FIRST_SEARCH);
        assertEquals(grandchild1, foundGrandchild);

        childContainer1.setTag(100);
        childContainer2.setTag(200);
        childContainer3.setTag(300);
        grandchild1.setTag(110);
        grandchild2.setTag(220);
        grandchild3.setTag(330);
        foundChild = mContainer.findChildByTag(100);
        assertEquals(childContainer1, foundChild);
        foundChild = mContainer.findChildByTag(100, Group.BREADTH_FIRST_SEARCH);
        assertEquals(childContainer1, foundChild);
        foundChild = mContainer.findChildByTag(100, Group.DEPTH_FIRST_SEARCH);
        assertEquals(childContainer1, foundChild);

        foundChild = mContainer.findChildByTag(110);
        assertEquals(null, foundChild);
        foundChild = mContainer.findChildByTag(110, Group.BREADTH_FIRST_SEARCH);
        assertEquals(grandchild1, foundChild);
        foundChild = mContainer.findChildByTag(110, Group.DEPTH_FIRST_SEARCH);
        assertEquals(grandchild1, foundChild);

        foundChild = mContainer.findChildByTag(220);
        assertEquals(null, foundChild);
        foundChild = mContainer.findChildByTag(220, Group.BREADTH_FIRST_SEARCH);
        assertEquals(grandchild2, foundChild);
        foundChild = mContainer.findChildByTag(220, Group.DEPTH_FIRST_SEARCH);
        assertEquals(grandchild2, foundChild);

        foundChild = mContainer.findChildByTag(330);
        assertEquals(null, foundChild);
        foundChild = mContainer.findChildByTag(330, Group.BREADTH_FIRST_SEARCH);
        assertEquals(grandchild3, foundChild);
        foundChild = mContainer.findChildByTag(330, Group.DEPTH_FIRST_SEARCH);
        assertEquals(grandchild3, foundChild);

        grandchild3.setTag(200);
        foundChild = mContainer.findChildByTag(200);
        assertEquals(childContainer2, foundChild);
        foundChild = mContainer.findChildByTag(200, Group.BREADTH_FIRST_SEARCH);
        assertEquals(childContainer2, foundChild);
        foundChild = mContainer.findChildByTag(200, Group.DEPTH_FIRST_SEARCH);
        assertEquals(grandchild3, foundChild);

        assertEquals(3, mContainer.getChildrenCount());
        assertEquals(6, mContainer.getDescendantCount());
    }

    @SmallTest
    public void testRaise() {
        Actor actor1 = new Image();
        mContainer.add(actor1);

        Actor actor2 = new Image();
        mContainer.add(actor2);

        assertEquals(mContainer.getChildren().get(0), actor1);
        assertEquals(mContainer.getChildren().get(1), actor2);

        mContainer.raise(actor1, actor2);

        assertEquals(mContainer.getChildren().get(0), actor2);
        assertEquals(mContainer.getChildren().get(1), actor1);

        mContainer.remove(actor1);
        try {
            mContainer.raise(actor2, actor1);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            mContainer.raise(actor1, actor2);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @SmallTest
    public void testLower() {
        Actor actor1 = new Image();
        mContainer.add(actor1);

        Actor actor2 = new Image();
        mContainer.add(actor2);

        Actor actor3 = new Image();
        mContainer.add(actor3);

        mContainer.lower(actor3, actor2);

        assertEquals(mContainer.getChildren().get(0), actor1);
        assertEquals(mContainer.getChildren().get(1), actor3);
        assertEquals(mContainer.getChildren().get(2), actor2);

        mContainer.remove(actor3);

        try {
            mContainer.lower(actor2, actor3);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            mContainer.lower(actor3, actor2);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @SmallTest
    public void testGetChild() {

        Actor actor1 = new Image();
        mContainer.add(actor1);

        Actor actor2 = new Image();
        mContainer.add(actor2);

        assertEquals(mContainer.getChild(0), actor1);
        assertEquals(mContainer.getChild(1), actor2);
    }

    @SmallTest
    public void testRealize() {
        mContainer.realize(mPresentationEngine);
        assertTrue("Container should be realized successfully", mContainer.isRealized());
    }

    @SmallTest
    public void testPosition() {
        mContainer.setPosition(new Point(100.f, 100.f));
        mContainer.realize(mPresentationEngine);
        Point pos = mContainer.getPresentation().getPosition(false);
        assertEquals(100f, pos.x);
        assertEquals(100f, pos.y);
    }

    @SmallTest
    public void testGetChildrenCount() {

        Actor actor1 = new Image();
        mContainer.add(actor1);
        assertEquals(mContainer.getChildrenCount(), 1);

        Actor actor2 = new Image();
        mContainer.add(actor2);
        assertEquals(mContainer.getChildrenCount(), 2);
    }

    @SmallTest
    public void testRemoveAllChildren() {

        Actor actor1 = new Image();
        mContainer.add(actor1);

        Actor actor2 = new Image();
        mContainer.add(actor2);

        Actor actor3 = new Image();
        mContainer.add(actor3);

        assertEquals(mContainer.getChildren().get(0), actor1);
        assertEquals(mContainer.getChildren().get(1), actor2);
        assertEquals(mContainer.getChildren().get(2), actor3);

        mContainer.removeAll();

        assertEquals(mContainer.getChildrenCount(), 0);
    }

    /**
     * The same test is done in PresentationTest while the presentation tree is realized.
     */
    @SmallTest
    public void testMultiThreadAccess() {
        final Random rnd = new Random(System.currentTimeMillis());
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; ++i) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    for (int j = 0; j < 200; ++j) {
                        try {
                            Thread.sleep(rnd.nextInt(10));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                        Actor a = new Empty();
                        mContainer.add(a);
                        mContainer.getChildrenCount();
                        mContainer.getChild(0);
                        mContainer.remove(a);
                    }
                }
            });
            threads[i].start();
        }

        for (int i = 0; i < threads.length; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertEquals(0, mContainer.getChildrenCount());
    }

    public void testOpacity() {
        Plane a = new Plane();
        a.setOpacity(100);
        mContainer.add(a);
        mContainer.setOpacity(150);
        assertThat(a.getOpacity(), is(150));
    }

    public void testFindChildByTag() {
        Actor a = new Image();
        a.setTag(10);
        mContainer.add(a);
        assertEquals(a, mContainer.findChildByTag(10));
    }

    public void testFindChildById() {
        Actor a = new Image();
        a.setTag(1001);
        mContainer.add(a);
        
        int id = a.getId();
        assertEquals(a.getTag(), mContainer.findChildById(id).getTag());
    }

}
