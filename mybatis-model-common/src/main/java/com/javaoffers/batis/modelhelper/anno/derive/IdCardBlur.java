package com.javaoffers.batis.modelhelper.anno.derive;

import com.javaoffers.batis.modelhelper.constants.ModelHelpperConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * id card number desensitization.
 * Blur content one third. this same as {@code StringBlur}
 * @author mingJie
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface IdCardBlur {

    /**
     * Blur percentage. Default is 0.3.
     */
    double percent() default ModelHelpperConstants.BLUR_PERCENT;

    /**
     * Blur strategy, the default is intermediate blur.
     */
    Blur blur() default Blur.MIDDLE_BLUR;

    /**
     * default fuzzy symbol
     */
    String blurTag() default  ModelHelpperConstants.BLUR_TAG;
}
