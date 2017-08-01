package org.liquidplayer.javascript;

import java.lang.reflect.Method;
import java.util.ArrayList;

@SuppressWarnings("JniMissingFunction")
public abstract class JSClass<TInstance extends JSInstance> extends JSObject {

    private abstract class JNIReturnClass implements Runnable {
        JNIReturnObject jni;
    }

    public JSClass(final JSContext jsContext, final Class<? extends JSObject> klass, final Class<TInstance> proto) {
        context = jsContext;

        context.sync(new Runnable() {
            @Override
            public void run() {
                valueRef = makeFunctionWithCallback(context.ctxRef(), "constructor");
                Method[] methods = klass.getDeclaredMethods();
                for (Method m : methods) {
                    JSFunction f = new JSFunction(context, m, JSObject.class, JSClass.this);
                    property(m.getName(), f);
                }

                JSObject protoObject = new JSObject(context);
                Method[] protoMethods = proto.getDeclaredMethods();
                for (Method m : protoMethods) {
                    JSMethod f = new JSMethod(context, m);
                    protoObject.property(m.getName(), f);
                }
                property("prototype", protoObject);
            }
        });

        context.persistObject(this);
        context.zombies.add(this);
    }

    public void constructor() {
        // Intentionally left blank.
    }

    public JSObject newInstance() {
        JNIReturnClass runnable = new JNIReturnClass() {
            @Override
            public void run() {
                jni = callAsConstructor(context.ctxRef(), valueRef, new long[0]);
            }
        };
        context.sync(runnable);
        return context.getObjectFromRef(testException(runnable.jni));
    }

    private long testException(JNIReturnObject jni) {
        if (jni.exception != 0) {
            context.throwJSException(new JSException(new JSValue(jni.exception, context)));
            return (make(context.ctxRef()));
        } else {
            return jni.reference;
        }
    }

    private long[] argsToValueRefs(final Object[] args) {
        ArrayList<JSValue> largs = new ArrayList<>();
        if (args != null) {
            for (Object o : args) {
                JSValue v;
                if (o == null) break;
                if (o.getClass() == Void.class)
                    v = new JSValue(context);
                else if (o instanceof JSValue)
                    v = (JSValue) o;
                else if (o instanceof Object[])
                    v = new JSArray<>(context, (Object[]) o, Object.class);
                else
                    v = new JSValue(context, o);
                largs.add(v);
            }
        }
        long[] valueRefs = new long[largs.size()];
        for (int i = 0; i < largs.size(); i++) {
            valueRefs[i] = largs.get(i).valueRef();
        }
        return valueRefs;
    }

    protected native long makeFunctionWithCallback(long ctx, String name);
}