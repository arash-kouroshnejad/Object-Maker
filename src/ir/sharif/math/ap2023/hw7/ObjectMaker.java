package ir.sharif.math.ap2023.hw7;


import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

@SuppressWarnings("all")
public class ObjectMaker {
    private static final List<Class<?>> primitiveClasses = Arrays.asList(
            boolean.class,
            short.class,
            byte.class,
            int.class,
            long.class,
            float.class,
            double.class,
            String.class
    );

    private URLClassLoader classLoader;

    private static final List<Class<?>> addedClasses = new ArrayList<>();

    public ObjectMaker(URL... urls) {
        // TODO
        classLoader =  new URLClassLoader(urls);
    }

    public Object makeObject(Map<String, Object> values, String className) throws ReflectiveOperationException {
        // TODO
        Class clazz = Class.forName(className, true, classLoader);
        Constructor constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object out = constructor.newInstance();
        Set<Field> fields = new HashSet<>(Arrays.asList(clazz.getDeclaredFields()));
        fields.addAll(getSuperFields(clazz));
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
                break;
            field.setAccessible(true);
            Name name = field.getAnnotation(Name.class);
            String nameToResolve = (name != null) ? name.name() : field.getName();
            if (primitiveClasses.contains(field.getType()))
                field.set(out, values.get(nameToResolve));
            else if (!List.class.isAssignableFrom(values.get(nameToResolve).getClass()))
                field.set(out, makeObject((Map) values.get(nameToResolve), field.getType().getName()));
            else
                field.set(out, resolveList((List) values.get(nameToResolve), field.getType().getComponentType()));
        }
        return out;
    }

    private Set<Field> getSuperFields(Class<?> clazz) {
        Set<Field> fields = new HashSet<Field>();
        Class superClazz = clazz.getSuperclass();
        while (superClazz != null) {
            fields.addAll(Arrays.asList(superClazz.getDeclaredFields()));
            superClazz = superClazz.getSuperclass();
        }
        return fields;
    }

    private Object resolveList(List list, Class componentType) throws ReflectiveOperationException {
        int size = list.size();
        Object array = Array.newInstance(componentType, size);
        for (int i = 0; i < size; i++) {
            if (primitiveClasses.contains(componentType))
                Array.set(array, i , list.get(i));
            else if (!List.class.isAssignableFrom(list.get(i).getClass()))
                Array.set(array, i, makeObject((Map) list.get(i), componentType.getName()));
            else
                Array.set(array, i, resolveList((List) list.get(i), componentType.getComponentType()));
        }
        return array;
    }

}
