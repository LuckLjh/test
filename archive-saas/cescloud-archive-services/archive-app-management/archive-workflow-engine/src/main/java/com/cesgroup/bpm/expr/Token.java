package com.cesgroup.bpm.expr;

/**
 * 符号
 * 
 * @author 国栋
 *
 */
public class Token {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isOper() {
        return false;
    }

    @Override
    public String toString() {
        return value;
    }
}
