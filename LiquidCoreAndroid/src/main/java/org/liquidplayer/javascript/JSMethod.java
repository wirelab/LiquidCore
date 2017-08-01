package org.liquidplayer.javascript;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by cwessels on 01/08/2017.
 */

@SuppressWarnings("JniMissingFunction")
public class JSMethod extends JSObject {
    private final Method method;

    public JSMethod(JSContext jsContext, final Method method) {
        context = jsContext;
        this.method = method;
        context.sync(new Runnable() {
            @Override
            public void run() {
                valueRef = makeFunctionWithCallback(context.ctxRef(), method.getName());
            }
        });

        context.persistObject(this);
        context.zombies.add(this);
    }

    @SuppressWarnings("unused") // This is called directly from native code
    private long functionCallback(long thisObjectRef,
                                  long argumentsValueRef[], long exceptionRefRef) {
        try {
            JSValue [] args = new JSValue[argumentsValueRef.length];
            for (int i=0; i<argumentsValueRef.length; i++) {
                JSObject obj = context.getObjectFromRef(argumentsValueRef[i],false);
                if (obj!=null) args[i] = obj;
                else args[i] = new JSValue(argumentsValueRef[i],context);
            }
            JSObject thiz = context.getObjectFromRef(thisObjectRef);
            JSValue value = function(thiz,args);
            setException(0L, exceptionRefRef);
            return value==null ? 0L : value.valueRef();
        } catch (JSException e) {
            e.printStackTrace();
            setException(e.getError().valueRef(), exceptionRefRef);
            return 0L;
        }
    }

    protected JSValue function(JSObject thiz, JSValue [] args) {
        Class<?>[] pType  = method.getParameterTypes();
        Object [] passArgs = new Object[pType.length];
        for (int i=0; i<passArgs.length; i++) {
            if (i<args.length) {
                if (args[i]==null) passArgs[i] = null;
                else passArgs[i] = args[i].toJavaObject(pType[i]);
            } else {
                passArgs[i] = null;
            }
        }
        JSValue returnValue;
        try {
            Object ret = method.invoke(thiz, passArgs);
            if (ret == null) {
                returnValue = null;
            } else if (ret instanceof JSValue) {
                returnValue = (JSValue) ret;
            } else {
                returnValue = new JSValue(context, ret);
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            context.throwJSException(new JSException(context, e.toString()));
            returnValue = null;
        } catch (IllegalAccessException e) {
            context.throwJSException(new JSException(context, e.toString()));
            returnValue = null;
        } finally {
//            invokeObject.setThis(stack);
        }
        return returnValue;
    }

    protected native long makeFunctionWithCallback(long ctx, String name);
}
