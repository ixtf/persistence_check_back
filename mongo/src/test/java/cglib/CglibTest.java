package cglib;

import net.sf.cglib.proxy.Enhancer;

/**
 * @author jzb 2019-02-14
 */
public class CglibTest {
    public static void main(String[] args) {
        LoaderBean loader = new LoaderBean();
        loader.setPropertyBean(createPropertyBean());


//        final LoaderBean.DTO dto = MAPPER.convertValue(loader, LoaderBean.DTO.class);
//        System.out.println(dto);

//        loader.test();
//        loader.setPropertyBean(new PropertyBean());

        System.out.println(loader.getLoaderName());
        System.out.println(loader.getLoaderValue());
//        PropertyBean propertyBean = loader.getPropertyBean();
//
//        System.out.println(propertyBean.getPropertyName());
//        System.out.println(propertyBean.getPropertyValue());
//        System.out.println("after...");
//        System.out.println(propertyBean.getPropertyName());
    }

    static PropertyBean createPropertyBean() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(PropertyBean.class);
        return (PropertyBean) enhancer.create(PropertyBean.class, new ConcreteClassLazyLoader());
    }

    static <T> T read(Class<T> clazz, Object value) {
        if (Integer.class.isInstance(value)) {
            return (T) value;
        }

        if (Number.class.isInstance(value)) {
            return (T) Integer.valueOf(Number.class.cast(value).intValue());
        }
        return (T) Integer.valueOf(value.toString());
    }
}
