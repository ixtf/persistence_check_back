package reflection;

import lombok.Data;

import java.io.Serializable;

/**
 * @author jzb 2019-02-14
 */
@Data
public class A implements Serializable {
    protected String aString2;
    private String aString1;
}
