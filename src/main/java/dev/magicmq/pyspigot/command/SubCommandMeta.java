package dev.magicmq.pyspigot.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SubCommandMeta {

    String command();

    String[] aliases() default {};

    String permission() default "";

    boolean playerOnly() default false;

    String usage() default "";

    String description() default "No description provided.";

}