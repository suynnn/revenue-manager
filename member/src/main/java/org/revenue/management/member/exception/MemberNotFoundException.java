package org.revenue.management.member.exception;

public class MemberNotFoundException extends RuntimeException{

    public MemberNotFoundException(String msg) {
        super(msg);
    }
}
