package de.grimlock.nitromc.job;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Job {
    long interval();
    TimeUnit unit() default TimeUnit.MINUTES;
    boolean async() default true;
    String name() default "";
}
