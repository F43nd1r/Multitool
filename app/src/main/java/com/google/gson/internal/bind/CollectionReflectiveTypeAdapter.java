package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created on 10.08.2016.
 *
 * @author F43nd1r
 */
public class CollectionReflectiveTypeAdapter<T extends Collection> extends TypeAdapter<T> {
    private static final String ELEMENTS_NAME = "com#faendir#collectionReflectiveTypeAdapter#items";
    private final TypeAdapter<T> arrayDelegate;
    private final Gson gson;
    private final TypeToken<T> type;
    private final ObjectConstructor<T> constructor;
    private final Map<String, ReflectiveTypeAdapterFactory.BoundField> boundFields;

    private CollectionReflectiveTypeAdapter(Gson gson, TypeToken<T> type, TypeAdapter<T> delegate, ObjectConstructor<T> constructor) {
        this.gson = gson;
        this.type = type;
        this.constructor = constructor;
        arrayDelegate = delegate;
        boundFields = getBoundFields();
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        out.beginObject();
        try {
            out.name(ELEMENTS_NAME);
            arrayDelegate.write(out, value);
            for (ReflectiveTypeAdapterFactory.BoundField boundField : boundFields.values()) {
                if (boundField.writeField(value)) {
                    out.name(boundField.name);
                    boundField.write(out, value);
                }
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
        out.endObject();
    }

    @Override
    public T read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        T instance = constructor.construct();

        try {
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if (ELEMENTS_NAME.equals(name)) {
                    //noinspection unchecked
                    instance.addAll(arrayDelegate.read(in));
                    continue;
                }
                ReflectiveTypeAdapterFactory.BoundField field = boundFields.get(name);
                if (field == null || !field.deserialized) {
                    in.skipValue();
                } else {
                    field.read(in, instance);
                }
            }
        } catch (IllegalStateException e) {
            throw new JsonSyntaxException(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
        in.endObject();
        return instance;
    }

    private Map<String, ReflectiveTypeAdapterFactory.BoundField> getBoundFields() {
        TypeToken<?> type = this.type;
        Class<?> raw = type.getRawType();
        Map<String, ReflectiveTypeAdapterFactory.BoundField> result = new LinkedHashMap<>();

        Type declaredType = type.getType();
        while (raw != ArrayList.class) {
            Field[] fields = raw.getDeclaredFields();
            for (Field field : fields) {
                boolean serialize = ReflectiveTypeAdapterFactory.excludeField(field, true, Excluder.DEFAULT);
                boolean deserialize = ReflectiveTypeAdapterFactory.excludeField(field, false, Excluder.DEFAULT);
                if (!serialize && !deserialize) {
                    continue;
                }
                field.setAccessible(true);
                Type fieldType = $Gson$Types.resolve(type.getType(), raw, field.getGenericType());
                String name = field.getName();
                ReflectiveTypeAdapterFactory.BoundField boundField = createBoundField(gson, field, name,
                        TypeToken.get(fieldType), serialize, deserialize);
                ReflectiveTypeAdapterFactory.BoundField previous = result.put(name, boundField);
                if (previous != null) {
                    throw new IllegalArgumentException(declaredType
                            + " declares multiple JSON fields named " + previous.name);
                }
            }
            type = TypeToken.get($Gson$Types.resolve(type.getType(), raw, raw.getGenericSuperclass()));
            raw = type.getRawType();
        }
        return result;
    }

    private ReflectiveTypeAdapterFactory.BoundField createBoundField(
            final Gson context, final Field field, final String name,
            final TypeToken<?> fieldType, boolean serialize, boolean deserialize) {
        final boolean isPrimitive = Primitives.isPrimitive(fieldType.getRawType());

        final TypeAdapter<?> typeAdapter = context.getAdapter(fieldType);
        return new ReflectiveTypeAdapterFactory.BoundField(name, serialize, deserialize) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            // the type adapter and field type always agree
            @Override
            void write(JsonWriter writer, Object value)
                    throws IOException, IllegalAccessException {
                Object fieldValue = field.get(value);
                TypeAdapter t = new TypeAdapterRuntimeTypeWrapper(context, typeAdapter, fieldType.getType());
                t.write(writer, fieldValue);
            }

            @Override
            void read(JsonReader reader, Object value)
                    throws IOException, IllegalAccessException {
                Object fieldValue = typeAdapter.read(reader);
                if (fieldValue != null || !isPrimitive) {
                    field.set(value, fieldValue);
                }
            }

            @Override
            public boolean writeField(Object value) throws IOException, IllegalAccessException {
                if (!serialized) return false;
                Object fieldValue = field.get(value);
                return fieldValue != value; // avoid recursion for example for Throwable.cause
            }
        };
    }

    public static class Factory<T extends Collection> implements TypeAdapterFactory {

        private final Class<? super T> clazz;

        public Factory() {
            this(Collection.class);
        }

        public Factory(Class<? super T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public <P> TypeAdapter<P> create(Gson gson, TypeToken<P> type) {
            if (clazz.isAssignableFrom(type.getRawType())) {
                try {
                    Field field = Gson.class.getField("constructorConstructor");
                    field.setAccessible(true);
                    ConstructorConstructor constructorConstructor = (ConstructorConstructor) field.get(gson);
                    //noinspection unchecked
                    return (TypeAdapter<P>) new CollectionReflectiveTypeAdapter(gson, type, gson.getDelegateAdapter(this, type), constructorConstructor.get(type));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
