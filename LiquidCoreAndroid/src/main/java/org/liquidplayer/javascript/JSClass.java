package org.liquidplayer.javascript;

import java.lang.reflect.Method;

public abstract class JSClass<TInstance extends JSInstance> extends JSFunction {
    public JSClass(JSContext jsContext, final Class<? extends JSObject> klass, Class<TInstance> proto) {
        context = jsContext;
        subclass = klass;
        invokeObject = this;
        String name = ("constructor"==null) ? "__nullFunc" : "constructor";
        Method [] methods = this.invokeObject.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(name)) {
                this.method = method;
                break;
            }
        }
        context.sync(new Runnable() {
            @Override
            public void run() {
                valueRef = makeFunctionWithCallback(context.ctxRef(), "constructor");
                Method[] methods = klass.getDeclaredMethods();
                for (Method m : methods) {
                    JSFunction f = new JSFunction(context, m, JSObject.class, JSClass.this);
                    property(m.getName(), f);
                }
            }
        });

        context.persistObject(this);
        context.zombies.add(this);

        JSObject protoObject = new JSObject(jsContext);
        Method[] methodsProto = proto.getDeclaredMethods();
        for (Method m : methodsProto) {
            JSFunction f = new JSFunction(context, m, proto);
            protoObject.property(m.getName(), f);
        }
        prototype(protoObject);
    }

    public void constructor() {
        // Intentionally left blank.
    }
}