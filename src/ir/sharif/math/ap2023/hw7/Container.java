package ir.sharif.math.ap2023.hw7;

import java.lang.reflect.Field;

@SuppressWarnings("all")
public class Container {
    Field field;
    Object declaring;

    public Container(Field field, Object declaring) {
        this.field = field;
        this.declaring = declaring;
    }
}
