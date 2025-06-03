package alg.coyote001.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require specific subscription plan for accessing methods or classes
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireSubscription {
    
    /**
     * Required subscription plan (FREE, PREMIUM)
     */
    String plan() default "FREE";
    
    /**
     * Resource type for fine-grained access control
     */
    String resourceType() default "";
    
    /**
     * Error message when access is denied
     */
    String message() default "Subscription required";
} 