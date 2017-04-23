package com.example.sanat.seegreen.util;

/**
 * Created by Sanat on 4/22/2017.
 */
import android.util.Log;
import android.util.Pair;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;

public class ChatBot {
    private static final String TAG = "CHATBOT";
    static final int WAITING_FOR_ITEM = 1;
    static final int WAITING_FOR_CATEGORY_INTENT = 1 << 1;
    static final int WAITING_FOR_CATEGORY = 1 << 2;
    static final int WAITING_FOR_CONFIRMATION = 1 << 3;
    static final int ERR = 1 << 4;
    static final int CONFIRMATION = 1 << 5;
    static final int NEEDS_CONFIRMATION = WAITING_FOR_CATEGORY | WAITING_FOR_ITEM;
    static int state = WAITING_FOR_ITEM;
    static LinkedList<String> scratch = new LinkedList<String>();
    static HashMap<String, String> cache = new HashMap<String, String>();

    public static String processMessage(String input) {
        scratch.push(input.toLowerCase());
        String result;
        Log.d(TAG, "Bot responding. state: " + state );
        if((state & WAITING_FOR_CONFIRMATION) > 0){
            Log.d(TAG, "confirmation called");
            if(!confirmation()){
                Log.d(TAG, "confirmation returned false");
                return "I'm sorry, I don't quite understand." +
                        "  Try saying \"yes\" or \"no\".";
            }
            Log.d(TAG, "confirmation returned true");
        }


        Log.d(TAG, "Bot responding. state: " + state );

        switch (state & ~CONFIRMATION & ~WAITING_FOR_CONFIRMATION) {
            case WAITING_FOR_ITEM:
                Log.d(TAG, "Calling findItem");
                result = findItem();
                break;
            case WAITING_FOR_CATEGORY_INTENT:
                Log.d(TAG, "Calling categoryIntent");
                result = categoryIntent();
                break;
            case WAITING_FOR_CATEGORY:
                Log.d(TAG, "Calling categorize");
                result = categorize();
                break;
            default:
                result = "Sorry, there was an internal error";
                break;
        }

        /* Usually lets user respond to previous prompt
         * when there is an error */
        if((state & ERR) > 0) {
            state ^= ERR;
        }
        /* Generally flips waiting for confirmation bit high
           if the bot requires a response, and the low after
           it gets it.
           Stays high when bot asks consecutive
           yes, no questions
        */
        else if((state & NEEDS_CONFIRMATION) > 0) {
            state ^= WAITING_FOR_CONFIRMATION;
            Log.d(TAG, "Flipping confirmation bit");
        }

        return result;
    }

    /* Used when asking user binary questions
    * Returns true on valid input, false for invalid*/
    private static boolean confirmation(){
        String message = scratch.pop();
        if(message.equals("yes") || message.equals("y")){
            state |= CONFIRMATION;
            return true;
        } else if (message.equals("no") || message.equals("n")){
            state &= ~CONFIRMATION;
            return true;
        } else {
            return false;
        }
    }

    /* Used when the user is asking for where to dispose of an item
       and handles their response to the answer
     */
    private static String findItem() {
        String result = "There was a network error. Please try again";


        if ((state & WAITING_FOR_CONFIRMATION) == 0) {
            String item = scratch.pop();
            String value = "";
            /* TODO: query server for item */
            if(cache.containsKey(item)){
                value = cache.get(item);
                StringBuilder sb = new StringBuilder();
                scratch.push(item);
                scratch.push(value);
                sb.append(item.charAt(0) - 0x20);
                sb.append(item.substring(1));
                sb.append(" huh?  That item is ");
                sb.append(value);
                sb.append(".   Do you believe this to be correct?");
                result = sb.toString();
            } else {

                Pair<Integer, String> response = null;
                try {
                    response = new Pair<Integer, String>(200, "");
                    response = httpGet("http://63a2c555.ngrok.io/api/analyze?query=\"" + item + "\"");

                } catch (Exception ex) {
                    state |= ERR;
                }
                if (response != null && response.first == 200) {

                    try {
                        JSONObject object = new JSONObject(response.second);
                        item = object.getString("name");
                        value = object.getString("value");
                        Log.v("CHAT RESPONSE!", value);

                    } catch (Exception ex) {
                        state |= ERR;
                    }
                }
            }
            if ((state & ERR) == 0){

                StringBuilder sb = new StringBuilder();
                scratch.push(item);
                scratch.push(value);
                sb.append(item);
                sb.append(" huh?  That item is ");
                sb.append(value);
                sb.append(".   Do you believe this to be correct?");
                result = sb.toString();
            }
        } else {
        /* When asking user if they are satisfied with the response
         * from previous call */
            if ((state & CONFIRMATION) == 0) {
            /* User unsastified. */
            /* preserving NEED_CONFIRMATION bit */
                state ^= WAITING_FOR_CATEGORY_INTENT | WAITING_FOR_ITEM;
                result = "Okay, Would you like to submit a correction?";
            } else {
            /* User satisfied */
            /* Making sure WAITING_FOR_ITEM but still high */
                assert ((state & WAITING_FOR_ITEM) > 0);
                String value = scratch.pop();
                String item = scratch.pop();
                cache.put(item, value);
                result = "Great! Give me another item!";
            }

        }

        return result;
    }


    private static String categoryIntent(){
        /* Makes sure the user was asked a binary question */
        assert ((state & WAITING_FOR_CONFIRMATION) > 0);
        String result;

        if((state & CONFIRMATION) > 0) {
            /* Keeps WAITING_FOR_CONFIRMATION bit high, which will be flipped later
            while setting WAITING_FOR_CATEGORY_INTENT low.
            */
            state ^= WAITING_FOR_CATEGORY | WAITING_FOR_CATEGORY_INTENT;
            scratch.pop();
            result = "Where do you think this item should sort this item? " +
                    "Recyclable, Compostable, or Trash";
        } else {
            state = WAITING_FOR_ITEM | WAITING_FOR_CATEGORY_INTENT;
            result = "Okay, maybe some other time. Give me another item to evaluate";
            scratch.pop();
            scratch.pop();
        }

        return result;
    }

    public static Pair<Integer, String> httpGet(String urlStr) throws IOException {
        Log.d("MAIN", "httpstart");
        Log.d(TAG, "URL: " + urlStr);
        URL url = new URL(urlStr);
        URLConnection con = url.openConnection();
        HttpURLConnection conn = (HttpURLConnection) con;

        //Error
        int responseCode = conn.getResponseCode();
        Log.d(TAG, "responseCode = " + responseCode);
        if (responseCode != 200) {
            return new Pair<Integer, String>(responseCode , conn.getResponseMessage());
        }

        // Buffer the result into a string
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            Log.d(TAG, "line append");
            sb.append(line);
        }
        rd.close();
        Log.d(TAG, "finish append");
        conn.disconnect();
        return new Pair<Integer, String>(responseCode, sb.toString());
    }

    private static String categorize(){
        /* first call */
        String result = "I'm sorry, I don't quite understand." +
                "  Try saying \"compostable\", \"trash\" or \"recyclable\"";
        if((state & WAITING_FOR_CONFIRMATION) == 0){
            String message = scratch.pop();
            if(message.equals("compostable") || editDistance(message, "compostable") < 5){
                scratch.push("compostable");
                result = "Do you want to mark this as compostable?";

            } else if(message.equals("trash") || editDistance(message, "trash") < 5){
                scratch.push("trash");
                result = "Do you want to mark this as trash?";

            } else if(message.equals("recyclable") || editDistance(message, "recyclable") < 5){
                scratch.push("recyclable");
                result = "Do you want to mark this as recylable?";

            } else {
                state ^= ERR;
            }
        } else {
        /* second call */
            assert((state & NEEDS_CONFIRMATION) > 0);
            if((state & CONFIRMATION) > 0){
                /* TODO: post to server */
                String value = scratch.pop();
                String name = scratch.pop();
                StringBuilder sb = new StringBuilder();
                sb.append("Thank you!  ");
                sb.append(name);
                sb.append(" is now updated to be ");
                sb.append(value);
                sb.append("!  Give me another item to sort!");
                cache.put(name, value);
                state ^= WAITING_FOR_CATEGORY | WAITING_FOR_ITEM;
                result = sb.toString();
            } else {
                scratch.pop();
                result = "Where do you think we should sort this item? " +
                        "Recyclable, Compostable, or Trash";
            }

        }

        return  result;
    }

    private static int editDistance(String a, String b) {
        int[][] distance = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            distance[i][0] = i;
        }
        for (int i = 0; i <= b.length(); i++) {
            distance[0][i] = i;
        }

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                distance[i][j] =
                        Math.min(Math.min(distance[i - 1][j - 1] + cost ,
                                distance[1 - 1][j] + 1), distance[i][j - 1] + 1);
            }
        }

        Log.d(TAG, "Edit difference between " + a + " and " + b + " is " + distance[a.length()][b.length()]);

        return distance[a.length()][b.length()];
    }
}