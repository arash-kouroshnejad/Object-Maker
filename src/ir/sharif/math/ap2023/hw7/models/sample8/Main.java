package ir.sharif.math.ap2023.hw7.models.sample8;

import ir.sharif.math.ap2023.hw7.ObjectMaker;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws ReflectiveOperationException {
        Map<String, Object> a = new HashMap<>();
        Map<String, Object> b = new HashMap<>();
        Map<String, Object> c = new HashMap<>();
        Map<String, Object> d = new HashMap<>();
        Map<String, Object> e = new HashMap<>();
        ObjectMaker maker = new ObjectMaker();


        a.put(
                "b", b
        );
        a.put(
                "gholi", c
        );


        b.put(
                "c", c
        );


        c.put(
                "e", e
        );


        A output = (A) maker.makeObject(a, "ir.sharif.math.ap2023.hw7.models.sample8.A");


    }
}
