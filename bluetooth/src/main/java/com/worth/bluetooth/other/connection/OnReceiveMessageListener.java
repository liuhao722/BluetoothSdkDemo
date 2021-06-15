package com.worth.bluetooth.other.connection;

public interface OnReceiveMessageListener extends IErrorListener, IConnectionLostListener {
    void onNewLine(String s);
}
