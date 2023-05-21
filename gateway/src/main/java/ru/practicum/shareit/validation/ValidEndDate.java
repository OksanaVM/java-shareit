package ru.practicum.shareit.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EndDateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEndDate {

    String message() default "Дата окончания не может быть раньше даты начала";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
