package com.tem.booksys.validation;

import com.tem.booksys.anno.State;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StateValidation implements ConstraintValidator<State,String> {
    /**
     *
     * @param value 要校验的数据
     * @param constraintValidatorContext
     * @return 校验不通过返回false，通过返回true
     *
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        //提供校验规则
        if (value == null){
            return false;
        }
        if (value.equals("已发布") || value.equals("草稿")) return true;
        return false;
    }
}
