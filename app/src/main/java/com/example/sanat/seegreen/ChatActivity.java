package com.example.sanat.seegreen;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sanat.seegreen.util.ChatBot;
import com.example.sanat.seegreen.util.ChatMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.Semaphore;


public class ChatActivity extends Activity {
    private static final String TAG = "ChatActivity";
    private final int REQ_CODE_SPEECH_INPUT = 100;

    /* savedInstanceState constants */
    private final String SAVED_CHAT_INPUT = "SAVED_CHAT_INPUT";

    /* Sychronization :) */
    private final Semaphore botMutex = new Semaphore(1);
    private final Semaphore chatMutex = new Semaphore(1);


    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private Button btnSpeak;
    private boolean side = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        buttonSend = (Button) findViewById(R.id.send);

        listView = (ListView) findViewById(R.id.msgview);

        btnSpeak = (Button) findViewById(R.id.btnSpeak);

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.activity_chat_right);
        listView.setAdapter(chatArrayAdapter);

        chatText = (EditText) findViewById(R.id.msg);
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });

        /* Restore input
        if there is a savedInstanceState */
        if(savedInstanceState != null){
            chatText.setText(savedInstanceState.getString(SAVED_CHAT_INPUT));
        }

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });
    }

    private boolean sendChatMessage() {
        final String message = chatText.getText().toString();
        chatArrayAdapter.add(new ChatMessage(ChatMessage.CHAT_RIGHT_ALIGN, message));
        chatText.setText("");

        new SendMessageTask().execute(message);

        return true;
    }

    private class SendMessageTask extends AsyncTask<String, Void, ChatMessage> {
        private boolean hasPermit;

        protected ChatMessage doInBackground(String... message) {
            if(botMutex.tryAcquire()) {
                hasPermit = true;
                return new ChatMessage(ChatMessage.CHAT_LEFT_ALIGN, ChatBot.processMessage(message[0]));
            } else {
                hasPermit = false;
                return new ChatMessage(ChatMessage.CHAT_LEFT_ALIGN, "Recieving requests too frequently");
            }
        }
        protected void onPostExecute(ChatMessage result) {
            /* Add bot's response to chat */
            while(true) {
                try {
                    chatMutex.acquire();
                    break;
                } catch (Exception ex){/* Loop Again*/}
            }
            chatArrayAdapter.add(result);
            chatMutex.release();
            if(hasPermit){
                botMutex.release();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putString(SAVED_CHAT_INPUT, chatText.getText().toString());
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    chatText.setText(result.get(0));
                }
                break;
            }
        }
    }
}