package org.streaming.revenuemanagement.domain.member.exception;

public class MemberNotFoundException extends RuntimeException{

    public MemberNotFoundException(String msg) {
        super(msg);
    }
}
