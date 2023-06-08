package ru.practicum.shareit.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StartDateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStartDate {

    String message() default "Дата начала не может быть равна дате окончания";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}