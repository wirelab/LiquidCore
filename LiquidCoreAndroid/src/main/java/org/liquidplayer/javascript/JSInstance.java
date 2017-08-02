package org.liquidplayer.javascript;

/**
 * Created by cwessels on 01/08/2017.
 */

public class JSInstance extends JSObject {
    public JSInstance(JSObject value) {
        context = value.context;
        valueRef = value.valueRef;

        context.persistObject(this);
        context.zombies.add(this);
    }
}
