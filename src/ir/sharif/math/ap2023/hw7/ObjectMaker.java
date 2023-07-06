package ir.sharif.math.ap2023.hw7;


import java.lang.reflect.*;
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

    private static final WaitQueue queue = new WaitQueue();

    private static int layer = 0;

    private static Node<Container> treeHead; // only fields of the composite array elements are added

    private static Node<Container> current;


    public ObjectMaker(URL... urls) {
        classLoader =  new URLClassLoader(urls);
    }



    public Object makeObject(Map<String, Object> values, String className) throws ReflectiveOperationException {


        if (layer == 0) {
            treeHead = new Node<>(new Container(null, null));
            current = treeHead;
        }

        layer++;

        // Class loading
        Class clazz = Class.forName(className, true, classLoader);

        // Object creation
        Object out = generateObject(values, clazz);
        clazz = out.getClass();

        // Getting all the fields
        Set<Field> fields = new HashSet<>(Arrays.asList(clazz.getDeclaredFields()));
        fields.addAll(getSuperFields(clazz));


        for (Field field : fields) {



            field.setAccessible(true);


            // Skipping final-statics
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
                continue;

            // New node
            Node<Container> node = new Node<>(new Container(field, out));
            current.addChild(node);


            // Skipping the value-set fields
            SetValue annotation = field.getAnnotation(SetValue.class);
            if (annotation != null) {
                queue.addToQueue(node);
                continue;
            }

            // Name annotation
            Name name = field.getAnnotation(Name.class);
            String nameToResolve = (name != null) ? name.name() : field.getName();


            // Object field classification
            if (primitiveClasses.contains(field.getType()))
                field.set(out, values.get(nameToResolve));
            else if (!List.class.isAssignableFrom(values.get(nameToResolve).getClass())) {
                Node tmp = current;
                current = node;
                field.set(out, makeObject((Map) values.get(nameToResolve), field.getType().getName()));
                current = tmp;
            }
            else
                field.set(out, resolveList((List) values.get(nameToResolve), field.getType().getComponentType()));
        }


        layer--;

        // Finishing the value-set fields
        if (layer == 0) {
            // TODO : FINALIZE
            finish();
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
                Array.set(array, i, list.get(i));
            else if (!List.class.isAssignableFrom(list.get(i).getClass()))
                Array.set(array, i, makeObject((Map) list.get(i), componentType.getName()));
            else
                Array.set(array, i, resolveList((List) list.get(i), componentType.getComponentType()));
        }
        return array;
    }

    private Object generateObject(Map values, Class clazz) throws ReflectiveOperationException{
        Method[] methods = clazz.getDeclaredMethods();
        boolean hasAlternateConstructor = false;
        Method alternateConstructor = null;
        String[] constructorParams = new String[0];
        for (Method method : methods) {
            method.setAccessible(true);
            UseAsConstructor annotation = method.getAnnotation(UseAsConstructor.class);
            if (annotation != null && Modifier.isStatic(method.getModifiers())) {
                hasAlternateConstructor = true;
                alternateConstructor = method;
                constructorParams = annotation.args();
            }
        }
        Object out;
        if (hasAlternateConstructor) {
            Object[] params = new Object[constructorParams.length];
            for (int i = 0; i < params.length; i++) {
                params[i] = values.get(constructorParams[i]);
            }
            out = alternateConstructor.invoke(null, params);
        }
        else {
            Constructor constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            out = constructor.newInstance();
        }
        return out;
    }

    private void finish() {

    }
}
