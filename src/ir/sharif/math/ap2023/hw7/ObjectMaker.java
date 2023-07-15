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

    private List<Node<Container>> trees = new ArrayList<>();


    public ObjectMaker(URL... urls) {
        classLoader =  new URLClassLoader(urls);
    }



    public Object makeObject(Map<String, Object> values, String className) throws ReflectiveOperationException {
        if (layer == 0) {
            treeHead = new Node<>(new Container(null, null));
            current = treeHead;
            trees.add(current);
        }

        layer++;

        // Class loading
        Class clazz = Class.forName(className, true, classLoader);

        // Object creation
        Object out = generateObject(values, clazz);
        clazz = out.getClass();

        // Getting all the fields
        Set<Field> fields = getAllFields(clazz);


        for (Field field : fields) {


            field.setAccessible(true);


            // Skipping final-statics
            if (Modifier.isStatic(field.getModifiers()))
                continue;

            // New node
            Node<Container> node = new Node<>(new Container(field, out));
            trees.get(trees.size() - 1).addChild(node);

            // Skipping the value-set fields
            SetValue annotation = field.getAnnotation(SetValue.class);
            if (annotation != null) {
                queue.addToQueue(node);
                continue;
            }

            // Name annotation
            Name name = field.getAnnotation(Name.class);
            String nameToResolve = (name != null) ? name.name() : field.getName();


            if (!values.containsKey(nameToResolve))
                continue;


            // Object field classification
            if (primitiveClasses.contains(field.getType()))
                field.set(out, values.get(nameToResolve));
            else if (!List.class.isAssignableFrom(values.get(nameToResolve).getClass())) {
                Node tmp = trees.get(trees.size() - 1);
                trees.set(trees.size() - 1, node);
                field.set(out, makeObject((Map) values.get(nameToResolve), field.getType().getName()));
                trees.set(trees.size() - 1, tmp);
            } else
                field.set(out, resolveList((List) values.get(nameToResolve), field.getType().getComponentType()));
        }


        layer--;

        // Finishing the value-set fields
        if (layer == 0)
            finish();


        return out;
    }

    private Set<Field> getAllFields(Class<?> clazz) {
        Set<Field> fields = new HashSet<>(Arrays.asList(clazz.getDeclaredFields()));
        Class superClazz = clazz.getSuperclass();
        while (superClazz != null) {
            fields.addAll(Arrays.asList(superClazz.getDeclaredFields()));
            superClazz = superClazz.getSuperclass();
        }
        return fields;
    }

    private Set<Method> getAllMethods(Class<?> clazz) {
        Set<Method> methods = new HashSet<>(Arrays.asList(clazz.getDeclaredMethods()));
        Class superClazz = clazz.getSuperclass();
        while (superClazz != null) {
            methods.addAll(Arrays.asList(superClazz.getDeclaredMethods()));
            superClazz = superClazz.getSuperclass();
        }
        return methods;
    }

    private Object resolveList(List list, Class componentType) throws ReflectiveOperationException {
        int size = list.size();
        Object array = Array.newInstance(componentType, size);
        for (int i = 0; i < size; i++) {
            trees.add(new Node(new Container(null, null)));
            if (primitiveClasses.contains(componentType))
                Array.set(array, i, list.get(i));
            else if (!List.class.isAssignableFrom(list.get(i).getClass()))
                Array.set(array, i, makeObject((Map) list.get(i), componentType.getName()));
            else
                Array.set(array, i, resolveList((List) list.get(i), componentType.getComponentType()));
            trees.remove(trees.size() - 1);
        }
        return array;
    }

    private Object generateObject(Map values, Class clazz) throws ReflectiveOperationException{
        Set<Method> methods = getAllMethods(clazz);
        boolean hasAlternateConstructor = false;
        Method alternateConstructor = null;
        String[] constructorParams = new String[0];
        for (Method method : methods) {
            UseAsConstructor annotation = method.getAnnotation(UseAsConstructor.class);
            if (annotation != null && Modifier.isStatic(method.getModifiers())) {
                method.setAccessible(true);
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

    private void finish() throws ReflectiveOperationException{
        outer:
        for (Node<Container> node : queue.queue) {
            Node<Container> tmp = node;
            Container container = node.getData();
            SetValue setValue = container.field.getAnnotation(SetValue.class);
            String path = setValue.path();
            Object current = container.declaring;
            if (path.equals(""))
                node.getData().field.set(node.getData().declaring, node.getData().declaring);
            else {
                while (!path.equals("")) {
                    if (path.equals("..")) {
                        tmp.getData().field.set(tmp.getData().declaring, node.getParent().getData().declaring);
                        continue outer;
                    }
                    else if (path.charAt(0) == '.') {
                        path = path.substring(3); // ../ -> 3 chars
                        // move node and current to parent
                        node = node.getParent();
                        current = node.getData().declaring;
                    }
                    else if (path.charAt(0) == '/'){ // /x/ or /x or /..
                        int i = 1;
                        for (; i < path.length(); i++)
                            if (path.charAt(i) == '/' || path.charAt(i) == '.')
                                break;


                        i = (i == path.length() - 1) ? i + 1 : i;
                        // the last character cant be a / or a . and it
                        // must be part of a name and I need an empty String after that


                        String nameToResolve = path.substring(1, i);
                        path = path.substring(i);
                        // move node and current to new-found child
                        for (Node<Container> child : node.getChildren()) {
                            Name nameAnnotation = child.getData().field.getAnnotation(Name.class);
                            if ((nameAnnotation != null && nameAnnotation.name().equals(nameToResolve))
                                    || child.getData().field.getName().equals(nameToResolve)) {
                                node = child;
                                current = child.getData().declaring;
                                break;
                            }
                        }
                    }
                    else {
                        int i = 1;
                        for (; i < path.length(); i++)
                            if (path.charAt(i) == '/' || path.charAt(i) == '.')
                                break;

                        i = (i == path.length() - 1) ? i + 1 : i;
                        // the last character cant be a / or a . and it
                        // must be part of a name and I need an empty String after that


                        String nameToResolve = path.substring(0, i);
                        path = path.substring(i);
                        // move node and current to same-level sister


                        // first : move the node pointer to parent node
                        // second : find the child like before
                        // third reassign the node
                        node = node.getParent();
                        for (Node<Container> child : node.getChildren()) {
                            Name nameAnnotation = child.getData().field.getAnnotation(Name.class);
                            if ((nameAnnotation != null && nameAnnotation.name().equals(nameToResolve))
                                    || child.getData().field.getName().equals(nameToResolve)) {
                                node = child;
                                current = child.getData().declaring;
                                break;
                            }
                        }
                    }
                }
                tmp.getData().field.set(tmp.getData().declaring, node.getData().field.get(node.getData().declaring));
            }
        }
    }
}
