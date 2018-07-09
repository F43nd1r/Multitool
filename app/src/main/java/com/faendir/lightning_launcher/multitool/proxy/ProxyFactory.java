package com.faendir.lightning_launcher.multitool.proxy;

import com.faendir.lightning_launcher.multitool.util.LightningObjectFactory.EvalFunction;
import java9.util.stream.Stream;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author lukas
 * @since 04.07.18
 */
public final class ProxyFactory {
    private ProxyFactory() {
    }

    static <T extends Proxy> T lightningProxy(Object lightningObject, Class<T> interfaceClass) {
        //noinspection unchecked
        return (T) java.lang.reflect.Proxy.newProxyInstance(ProxyFactory.class.getClassLoader(), new Class[]{interfaceClass}, new JavaProxyInvocationHandler(lightningObject));
    }

    static Lightning evalProxy(EvalFunction eval) {
        return (Lightning) java.lang.reflect.Proxy.newProxyInstance(ProxyFactory.class.getClassLoader(), new Class[]{Lightning.class}, new EvalProxyInvocationHandler(eval));
    }

    public static <T extends Proxy> T cast(Proxy proxy, Class<T> interfaceClass) {
        return lightningProxy(proxy.getReal(), interfaceClass);
    }

    private static class JavaProxyInvocationHandler extends BaseProxyInvocationHandler {
        private final Class<?> clazz;
        private final Object invokeOn;

        JavaProxyInvocationHandler(Object lightningObject) {
            super(lightningObject);
            boolean isClass = lightningObject instanceof Class;
            clazz = isClass ? (Class<?>) lightningObject : lightningObject.getClass();
            invokeOn = isClass ? null : lightningObject;
        }

        @Override
        protected Object doInvoke(String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception {
            return clazz.getMethod(methodName, parameterTypes).invoke(invokeOn, parameters);
        }
    }

    private static class EvalProxyInvocationHandler extends BaseProxyInvocationHandler {
        private final EvalFunction eval;

        private EvalProxyInvocationHandler(EvalFunction eval) {
            super(eval);
            this.eval = eval;
        }

        @Override
        protected Object doInvoke(String methodName, Class<?>[] parameterTypes, Object[] parameters) {
            return eval.eval(null, methodName, parameters);
        }
    }

    private abstract static class BaseProxyInvocationHandler implements InvocationHandler {
        private final Object object;

        protected BaseProxyInvocationHandler(Object object) {
            this.object = object;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("getReal".equals(method.getName())) {
                return object;
            }
            Object result;
            Class<?>[] parameterTypes = method.getParameterTypes();
            try {
                if (args == null) {
                    args = new Object[0];
                }
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> type = parameterTypes[i];
                    if (Proxy.class.isAssignableFrom(type)) {
                        args[i] = ((Proxy) args[i]).getReal();
                        if (Proxy.class.isAssignableFrom(type)) {
                            Class<?> t = args[i].getClass();
                            while (!t.getSimpleName().equals(type.getSimpleName())) {
                                t = t.getSuperclass();
                            }
                            parameterTypes[i] = t;
                        }
                    } else if (type.getName().startsWith("org.mozilla.javascript")) {
                        parameterTypes[i] = object.getClass().getClassLoader().loadClass(type.getName());
                    }
                }
                result = doInvoke(method.getName(), parameterTypes, args);
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodException(object.getClass().getName() + "#" + method.getName() + " " + Arrays.toString(parameterTypes));
            }
            if (result != null) {
                if (Proxy.class.isAssignableFrom(method.getReturnType())) {
                    //noinspection unchecked
                    result = lightningProxy(result, (Class<? extends Proxy>) method.getReturnType());
                } else if (method.getReturnType().isArray() && Proxy.class.isAssignableFrom(method.getReturnType().getComponentType())) {
                    //noinspection unchecked
                    Class<? extends Proxy> componentType = (Class<? extends Proxy>) method.getReturnType().getComponentType();
                    result = Stream.of((Object[]) result).map(object -> lightningProxy(object, componentType)).toArray(i -> (Object[]) Array.newInstance(componentType, i));
                }
            }
            return result;
        }

        protected abstract Object doInvoke(String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception;
    }
}
