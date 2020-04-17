package com.stylefeng.guns.common.exception;

import java.io.Serializable;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2020/4/17 8:41
 * @Version 1.0
 */
public class IndependenceException extends RuntimeException implements MessageException, Serializable {

    private static final long serialVersionUID = -4973033875436354887L;

    protected String messageCode;

    protected String[] arguments = new String[0];

    public IndependenceException(){}

    public IndependenceException(String code){
        this.messageCode = code;
    }

    public IndependenceException(String code, String[] arguments){
        this.messageCode = code;
        String[] newArguments = new String[arguments.length];
        System.arraycopy(arguments, 0, newArguments, 0, arguments.length);

        this.arguments = newArguments;
    }

    @Override
    public String getMessageCode() {
        return this.messageCode;
    }

    public void addArguments(String argument) {
        String[] newArguments = new String[arguments.length + 1];

        System.arraycopy(arguments, 0, newArguments, 0, arguments.length);

        newArguments[arguments.length] = argument;

        this.arguments = newArguments;
    }

    @Override
    public String[] getMessageArgs() {
        return this.arguments;
    }
}
