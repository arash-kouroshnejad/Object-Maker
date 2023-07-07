package ir.sharif.math.ap2023.hw7.models.sample9;

import ir.sharif.math.ap2023.hw7.ObjectMaker;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws ReflectiveOperationException {
        ObjectMaker maker = new ObjectMaker();
        Map<String, Object> values = new HashMap<>();
        A out = (A) maker.makeObject(values, "ir.sharif.math.ap2023.hw7.models.sample9.A");
        System.out.println(out);
    }
}
