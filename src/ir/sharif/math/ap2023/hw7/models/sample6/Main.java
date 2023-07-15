package ir.sharif.math.ap2023.hw7.models.sample6;

import ir.sharif.math.ap2023.hw7.ObjectMaker;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws ReflectiveOperationException {
        ObjectMaker objectMaker = new ObjectMaker();
        Map<String, Object> values = new HashMap<>();
        Map<String, Object> valuesB = new HashMap<>();
        Map<String, Object> valuesC = new HashMap<>();
        values.put("s", 6);
        values.put("b", valuesB);
        values.put("c", valuesC);
        valuesC.put("test", 13);
        A a = (A) objectMaker.makeObject(values, "ir.sharif.math.ap2023.hw7.models.sample6.A");
        System.out.println(a.i2 + "," + a.b.t); // 6,6
        System.out.println(a.c.inMap + "," + a.b.test);
    }
}
