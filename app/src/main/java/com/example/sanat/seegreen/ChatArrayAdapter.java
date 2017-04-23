package com.example.sanat.seegreen;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sanat.seegreen.util.ChatMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

    /* savedInstanceState constants */
    private final String SAVED_CHAT_ITEM_COUNT = "SAVED_CHAT_ITEM_COUNT";
    private final String SAVED_CHAT_ITEM_ALIGN = "SAVED_CHAT_ITEM";
    private final String SAVED_CHAT_ITEM_MESSAGE = "SAVED_CHAT_ITEM";

    private TextView chatText;
    private static List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
    private static ChatMessage first = new ChatMessage(ChatMessage.CHAT_LEFT_ALIGN,
            "Hello! What would you like to sort today?");
    private Context context;

    @Override
    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public int size(){
        return chatMessageList.size();
    }

    public Iterator<ChatMessage> iterator(){
        return chatMessageList.iterator();
    }


    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public int getCount() {
        return this.chatMessageList.size() + 1;
    }

    public ChatMessage getItem(int index) {
        if(index == 0){
            return first;
        }
        return this.chatMessageList.get(index - 1);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessageObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (chatMessageObj.align == ChatMessage.CHAT_RIGHT_ALIGN) {
            row = inflater.inflate(R.layout.activity_chat_right, parent, false);
        }else{
            row = inflater.inflate(R.layout.activity_chat_left, parent, false);
        }
        chatText = (TextView) row.findViewById(R.id.msgr);
        chatText.setText(chatMessageObj.message);
        return row;
    }



}