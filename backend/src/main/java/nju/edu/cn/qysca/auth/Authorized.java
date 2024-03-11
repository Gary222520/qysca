package nju.edu.cn.qysca.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Authorized {
    String[]  roles() default {"Bu Rep", "App Leader", "App Member", "Bu PO", "Admin"};
    String message() default "";
}
