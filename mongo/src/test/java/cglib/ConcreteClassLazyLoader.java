package cglib;

import net.sf.cglib.proxy.LazyLoader;

/**
 * @author jzb 2019-02-14
 */
public class ConcreteClassLazyLoader implements LazyLoader {
    @Override
    public Object loadObject() throws Exception {
        System.out.println("LazyLoader loadObject() ...");
        PropertyBean bean = new PropertyBean();
        bean.setPropertyName("lazy-load object propertyName!");
        bean.setPropertyValue(11);
        return bean;
    }
}
