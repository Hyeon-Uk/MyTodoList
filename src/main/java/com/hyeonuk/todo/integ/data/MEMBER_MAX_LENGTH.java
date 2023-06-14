package com.hyeonuk.todo.integ.data;

public enum MEMBER_MAX_LENGTH{
    ID(20),EMAIL(255),PASSWORD(255),NAME(50),IMG(255),DESC(500);

    public final int value;

    MEMBER_MAX_LENGTH(final int value){
        this.value=value;
    }

    public int getValue(){
        return this.value;
    }
}