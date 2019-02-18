package reflection;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author jzb 2019-02-14
 */
public class ReflectionTest {
    public static void main(String[] args) {
        getFields(C.class).map(Field::getName).forEach(System.out::println);
    }

    private static Stream<Field> getFields(Class<?> clazz) {
        final Stream<Field> selfStream = Arrays.stream(clazz.getDeclaredFields());
        final Class<?> superclass = clazz.getSuperclass();
        if (superclass == Object.class) {
            return selfStream;
        }
        final Stream<Field> superStream = getFields(superclass);
        return Stream.concat(superStream, selfStream);
    }
}
