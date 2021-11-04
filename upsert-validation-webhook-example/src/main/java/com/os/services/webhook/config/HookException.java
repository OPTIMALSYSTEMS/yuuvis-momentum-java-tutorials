package com.os.services.webhook.config;

public class HookException extends Exception
{
    private static final long serialVersionUID = 1L;

    public HookException(String message)
    {
        super(message);
    }

    public HookException(String string, Throwable exception)
    {
        super(string,exception);
    }
}

