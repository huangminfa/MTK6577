package com.mediatek.ngin3d.tests;


import com.mediatek.ngin3d.utils.Ngin3dException;

public class Ngin3dExceptionTest extends Ngin3dTest {
    public void testEmptyException() {
        Ngin3dException n = new Ngin3dException();
        assertTrue(n instanceof RuntimeException);
    }

    public void testStringException() {
        Ngin3dException n = new Ngin3dException("Just test");
        assertTrue(n instanceof RuntimeException);
        assertEquals("Just test", n.getMessage());
    }

    public void testThrowableStringException() {
        Throwable t = new Throwable("test");
        Ngin3dException n = new Ngin3dException("Just test", t);
        assertTrue(n instanceof RuntimeException);
        assertEquals("test", t.getMessage());
        assertEquals("Just test", n.getMessage());
    }

    public void testThrowableException() {
        Throwable t = new Throwable("test");
        Ngin3dException n = new Ngin3dException(t);
        assertTrue(n instanceof RuntimeException);
        assertEquals("test", t.getMessage());
    }
}
