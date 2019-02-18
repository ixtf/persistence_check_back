package cglib;

import lombok.Data;
import net.sf.cglib.proxy.Enhancer;

import java.io.Serializable;

/**
 * @author jzb 2019-02-14
 */
@Data
public class LoaderBean {
    private String loaderName;
    private int loaderValue;
    private PropertyBean propertyBean;

    public LoaderBean() {
        this.loaderName = "loaderNameA";
        this.loaderValue = 123;
        this.propertyBean = createPropertyBean();
    }

    protected PropertyBean createPropertyBean() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(PropertyBean.class);
        return (PropertyBean) enhancer.create(PropertyBean.class, new ConcreteClassLazyLoader());
    }

    public void test() {
        System.out.println(loaderName);
//        final String test = propertyBean.getPropertyName();
//        System.out.println(test);
    }

    @Data
    public static class DTO implements Serializable {
        private String loaderName;
        private int loaderValue;
    }

}