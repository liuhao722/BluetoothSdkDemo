package com.worth.bluetooth.other;

public interface OnReceiveMessageListener extends IErrorListener, IConnectionLostListener {
    void onNewLine(String s);
}
