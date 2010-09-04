package com.jpexs.asdec.action.swf4;

public class RegisterNumber {
    public int number;

    public RegisterNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "register" + number;
    }
}
