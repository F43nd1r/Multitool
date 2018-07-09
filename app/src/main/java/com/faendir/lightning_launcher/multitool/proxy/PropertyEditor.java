package com.faendir.lightning_launcher.multitool.proxy;

/**
 * @author lukas
 * @since 04.07.18
 */
public interface PropertyEditor extends Proxy {
    PropertyEditor setBoolean(@PropertySet.BooleanProperty String name, boolean value);

    void commit();

    Box getBox(@PropertySet.BoxProperty String name);

    PropertyEditor setEventHandler(@PropertySet.EventProperty String name, int action, String data);

    PropertyEditor setString(@PropertySet.StringProperty String name, String value);

    PropertyEditor setInteger(@PropertySet.IntProperty String name, long value);

    PropertyEditor setEventHandler(@PropertySet.EventProperty String name, EventHandler eventHandler);
}
