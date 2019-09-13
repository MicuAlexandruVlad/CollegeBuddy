package com.example.micua.licenseapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MessageService extends FirebaseMessagingService {
    public static final String TAG = "MessageService";

    private Repository repository;
    private Gson gson;
    private DBLinks dbLinks;
    private boolean displayNotification;
    private Constants constants;
    private User currentUser;
    private Message lastImageMessage, lastFileMessage;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: service created");
        repository = new Repository(this);
        gson = new Gson();
        constants = new Constants();
        dbLinks = new DBLinks();
        this.currentUser = new User();
        this.lastImageMessage = new Message();
        this.lastImageMessage.setTimestamp("");
        this.lastImageMessage.setSentBy("");
        this.lastImageMessage.setAlphaNumeric("");
        this.lastFileMessage = new Message();
        this.lastFileMessage.setTimestamp("");
        this.lastFileMessage.setSentBy("");
        this.lastFileMessage.setAlphaNumeric("");


        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                displayNotification = intent.getBooleanExtra("notify", true);
                String firedBy = intent.getStringExtra("by");
                Log.d(TAG, "onReceive - " + firedBy +
                        ": allow notification -> " + displayNotification);
            }
        }, new IntentFilter("display-notification"));

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: image message received & inserted");
                Message message = (Message) intent.getSerializableExtra("message");
                lastImageMessage.setAlphaNumeric(message.getAlphaNumeric());
                lastImageMessage.setSentBy(message.getSentBy());
                lastImageMessage.setTimestamp(message.getTimestamp());
                repository.insertMessage(message);
            }
        }, new IntentFilter("picture-taken"));

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: file message received & inserted");
                Message message = (Message) intent.getSerializableExtra("file_message");
                lastFileMessage.setAlphaNumeric(message.getAlphaNumeric());
                lastFileMessage.setSentBy(message.getSentBy());
                lastFileMessage.setTimestamp(message.getTimestamp());
                repository.insertMessage(message);
            }
        }, new IntentFilter("file-message-sent"));

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "onMessageReceived: message received");

            String data = remoteMessage.getData().get("data");
            StringBuilder messageJson = new StringBuilder();
            for (int i = 1; i < data.toCharArray().length - 1; i++) {
                messageJson.append(data.toCharArray()[i]);
            }
            Message receivedMessage = gson.fromJson(messageJson.toString(), Message.class);
            if (receivedMessage.getType() == constants.MESSAGE_TEXT) {
                repository.insertMessage(receivedMessage);
                broadcastMessage(receivedMessage);
                Log.d(TAG, "onMessageReceived: message json -> " + gson.toJson(receivedMessage));
            }

            if (receivedMessage.getType() == constants.MESSAGE_PHOTO) {
                if (lastImageMessage.getAlphaNumeric().equals("") && lastImageMessage.getSentBy().equals("")) {
                    long id = repository.insertMessage(receivedMessage);
                    Log.d(TAG, "onMessageReceived: id -> " + id);
                    broadcastMessage(receivedMessage);
                    Log.d(TAG, "onMessageReceived: image inserted");
                }
                else if (!receivedMessage.getAlphaNumeric().equals(lastImageMessage.getAlphaNumeric())
                        && !receivedMessage.getTimestamp().equals(lastImageMessage.getTimestamp())
                        && !receivedMessage.getSentBy().equals(lastImageMessage.getSentBy())) {
                    long id = repository.insertMessage(receivedMessage);
                    Log.d(TAG, "onMessageReceived: id -> " + id);
                    broadcastMessage(receivedMessage);
                    Log.d(TAG, "onMessageReceived: image inserted");
                }

            }

            if (receivedMessage.getType() == constants.MESSAGE_FILE) {
                Log.d(TAG, "onMessageReceived: file message -> " + new Gson().toJson(receivedMessage));
                if (lastFileMessage.getAlphaNumeric().equals("") && lastFileMessage.getSentBy().equals("")) {
                    long id = repository.insertMessage(receivedMessage);
                    LocalFile localFile = gson.fromJson(receivedMessage.getLocalFileData(), LocalFile.class);
                    String mime = localFile.getFileName().split("\\.")[1];
                    Log.d(TAG, "onMessageReceived: file extension -> " + mime);
                    Log.d(TAG, "onMessageReceived: file mime -> " + localFile.getMimeType());
                    Log.d(TAG, "onMessageReceived: file name -> " + localFile.getFileName());
                    Log.d(TAG, "onMessageReceived: id -> " + id);
                    broadcastMessage(receivedMessage);
                    Log.d(TAG, "onMessageReceived: file inserted");
                }
                else if (!receivedMessage.getAlphaNumeric().equals(lastFileMessage.getAlphaNumeric())
                        && !receivedMessage.getTimestamp().equals(lastFileMessage.getTimestamp())
                        && !receivedMessage.getSentBy().equals(lastFileMessage.getSentBy())) {
                    long id = repository.insertMessage(receivedMessage);
                    Log.d(TAG, "onMessageReceived: id -> " + id);
                    broadcastMessage(receivedMessage);
                    Log.d(TAG, "onMessageReceived: file inserted");
                }
            }

            if (receivedMessage.getType() == constants.MESSAGE_QUIZ) {
                Log.d(TAG, "onMessageReceived: received quiz");
                Log.d(TAG, "onMessageReceived: quiz data -> " + receivedMessage.getQuizData());

                handleQuizMessage(receivedMessage, dbLinks);
            }

            if (displayNotification) {
                if (receivedMessage.getType() == constants.MESSAGE_TEXT)
                    sendNotification(receivedMessage.getMessage(), receivedMessage.getSentBy());
                if (receivedMessage.getType() == constants.MESSAGE_PHOTO) {
                    sendNotification("Sent a picture", receivedMessage.getSentBy());
                }
                if (receivedMessage.getType() == constants.MESSAGE_QUIZ)
                    sendNotification("Shared a quiz. Give it a try.", receivedMessage.getSentBy());
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }


    }

    private void broadcastMessage(Message message) {
        Intent intent = new Intent("new-message");
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
    }

    private void handleQuizMessage(Message quizMessage, DBLinks dbLinks) {
        String url = dbLinks.getBaseLink() + "get-quizzes-data?referencedGroupId=" +
                quizMessage.getReferencedGroupId() + "&alphaNumeric=" + quizMessage.getAlphaNumeric();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                Log.d(TAG, "onResponse: server response -> " + json);

                try {
                    JSONObject object = new JSONObject(json);
                    JSONArray res = object.getJSONArray("result");

                    Message message;
                    Gson gson = new Gson();
                    message = gson.fromJson(res.getJSONObject(0).toString(), Message.class);
                    repository.insertMessage(message);
                    broadcastMessage(message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendNotification(String messageBody, String sender) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(sender)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
