package ir.sharif.math.ap2023.hw7.models.sample1;

import ir.sharif.math.ap2023.hw7.ObjectMaker;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws ReflectiveOperationException {
        ObjectMaker objectMaker = new ObjectMaker();
        Map<String, Object> values = new HashMap<>();
        values.put("a", 10);
        A a = (A) objectMaker.makeObject(values, "ir.sharif.math.ap2023.hw7.models.sample1.A");
        System.out.println(a.getA()); // 10
    }

}
