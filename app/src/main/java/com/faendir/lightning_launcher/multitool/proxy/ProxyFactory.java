package com.faendir.lightning_launcher.multitool.proxy;

import java9.util.stream.Stream;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author lukas
 * @since 04.07.18
 */
public final class ProxyFactory {
    private ProxyFactory() {
    }

    public static <T extends Proxy> T lightningProxy(Object lightningObject, Class<T> interfaceClass) {
        //noinspection unchecked
        return (T) java.lang.reflect.Proxy.newProxyInstance(ProxyFactory.class.getClassLoader(), new Class[]{interfaceClass}, new LightningProxyInvocationHandler(lightningObject));
    }

    public static <T extends Proxy> T cast(Proxy proxy, Class<T> interfaceClass) {
        return lightningProxy(proxy.getReal(), interfaceClass);
    }

    private static class LightningProxyInvocationHandler implements InvocationHandler {
        private final Object lightningObject;

        LightningProxyInvocationHandler(Object lightningObject) {
            this.lightningObject = lightningObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("getReal".equals(method.getName())) {
                return lightningObject;
            }
            Object result;
            if (args == null) {
                result = lightningObject.getClass().getMethod(method.getName()).invoke(lightningObject);
            } else {
                Class<?>[] parameterTypes = method.getParameterTypes();
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> type = parameterTypes[i];
                    if (Proxy.class.isAssignableFrom(type)) {
                        args[i] = ((Proxy)args[i]).getReal();
                        Class<?> t = args[i].getClass();
                        while (!t.getSimpleName().equals(type.getSimpleName())){
                            t = t.getSuperclass();
                        }
                        parameterTypes[i] = t;
                    }
                }
                result = lightningObject.getClass().getMethod(method.getName(), parameterTypes).invoke(lightningObject, args);
            }
            if (result != null) {
                if (Proxy.class.isAssignableFrom(method.getReturnType())) {
                    //noinspection unchecked
                    result = ProxyFactory.lightningProxy(result, (Class<? extends Proxy>) method.getReturnType());
                } else if (method.getReturnType().isArray() && Proxy.class.isAssignableFrom(method.getReturnType().getComponentType())) {
                    //noinspection unchecked
                    Class<? extends Proxy> componentType = (Class<? extends Proxy>) method.getReturnType().getComponentType();
                    result = Stream.of((Object[]) result)
                            .map(object -> ProxyFactory.lightningProxy(object, componentType))
                            .toArray(i -> (Object[]) Array.newInstance(componentType, i));
                }
            }
            return result;
        }
    }
}
