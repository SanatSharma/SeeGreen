package com.example.sanat.seegreen.util;

/**
 * Created by Sanat on 4/22/2017.
 */
public class ChatMessage {
    public static final int CHAT_LEFT_ALIGN = 0;
    public static final int CHAT_RIGHT_ALIGN = 1;

    public int align;
    public String message;

    public ChatMessage(int align, String message) {
        super();
        this.align = align;
        this.message = message;
    }
}