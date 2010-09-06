package com.jpexs.asdec.action.swf4;

public class RegisterNumber {
    public int number;
    public String name=null;

    public RegisterNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        if(name==null) return "register" + number;
        return name;
    }
}
