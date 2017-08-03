package org.liquidplayer.javascript;

import android.util.Log;

import junit.framework.Assert;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by cwessels on 01/08/2017.
 */

public class JSClassTest {
    public class TestObj extends JSInstance {
        public final String _param;

        public boolean called = false;

        public TestObj(JSObject value, String param) {
            super(value);
            _param = param;
        }

        public void doThings() {
            called = true;
        }
    }

    public class TestObjClass extends JSClass<TestObj> {
        public TestObjClass(JSContext jsContext) {
            super(jsContext, TestObjClass.class, TestObj.class);
        }

        public TestObj create() {
            return new TestObj(newInstance(), "Test!");
        }
    }

    public class OtherObj extends JSObject {
        public OtherObj(JSContext jsContext) {
            super(jsContext, OtherObj.class);
        }

        public void doThings() {
            Log.e("OtherObj", "Dothings");
        }
    }

    @org.junit.Test
    public void testJSClass() {
        JSContext jsContext = new JSContext();
        TestObjClass klass = new TestObjClass(jsContext);
        jsContext.property("TestObj", klass);
        jsContext.property("otherObj", new OtherObj(jsContext));

        jsContext.evaluateScript("var testObj = TestObj.create();");
        TestObj testObj = (TestObj) jsContext.evaluateScript("testObj").toObject();

        assertFalse(testObj.called);
        jsContext.evaluateScript("testObj.doThings()");
        assertTrue(testObj.called);
    }

    @org.junit.Test
    public void testJSMethodInvokeCrash() {
        JSContext jsContext = new JSContext();
        TestObjClass klass = new TestObjClass(jsContext);
        jsContext.property("TestObj", klass);
        jsContext.property("otherObj", new OtherObj(jsContext));

        jsContext.evaluateScript("var testObj = TestObj.create();");

        try {
            while (true) {
                jsContext.evaluateScript("testObj.doThings()");

                Thread.sleep(33);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
