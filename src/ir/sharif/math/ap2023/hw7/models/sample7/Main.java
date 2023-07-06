package ir.sharif.math.ap2023.hw7.models.sample7;

import ir.sharif.math.ap2023.hw7.ObjectMaker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws ReflectiveOperationException {
        ObjectMaker objectMaker = new ObjectMaker();
        Map<String, Object> values = new HashMap<>();
        Map<String, Object> b = new HashMap<>();
        Map<String, Object> c = new HashMap<>();
        List<List<Map<String, Object>>> cDoubleArr = Arrays.asList(Arrays.asList(c, c, c), Arrays.asList(c, c, c), Arrays.asList(c, c, c));
        List<Map<String, Object>> bList = Arrays.asList(b, b, b, b, b);
        b.put(
                "cDoubleArr", cDoubleArr
        );
        values.put(
                "b", b
        );
        values.put(
                "c", c
        );
        values.put(
                "bArr", bList
        );
        A output = (A) objectMaker.makeObject(values, "ir.sharif.math.ap2023.hw7.models.sample7.A");
        System.out.println(output);
    }
}
