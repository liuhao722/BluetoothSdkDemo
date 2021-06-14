package com.worth.bluetooth.other;

public class MessageItem {
    enum TYPE {
        STRING,
        CHAR
    }

    String text;
    char[] data;

    TYPE mTYPE;

    public MessageItem(String text) {
        this.text = text;
        mTYPE = TYPE.STRING;
    }

    public MessageItem(char[] data) {
        this.data = data;
        mTYPE = TYPE.CHAR;
    }
}
