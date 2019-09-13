package com.example.micua.licenseapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.koushikdutta.ion.builder.BitmapBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;
import com.squareup.picasso.Picasso;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.NormalFilePickActivity;
import com.vincent.filepicker.filter.entity.NormalFile;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private static final int LIGHT_COLOR_FACTOR = 50;
    private static final int RESULT_LOAD_IMAGE = 167;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 1115;
    private static final int SETTINGS_REQ_CODE = 551;
    private static final int REQ_FILES = 878;
    private static final int PHOTO_CAPTURE = 1511;
    private EditText messageField;
    private ImageView sendMessage, camera;
    private RecyclerView messagesList;
    private ImageView backgroundImage;
    private RelativeLayout actionsHolder;

    private Intent parentIntent;
    private User currentUser;
    private Group currentGroup;
    private Socket socket;
    private DBLinks dbLinks;
    private LinkedList<Message> messagesAll, messages;
    private List<Message> incompleteImageMessages, incompleteFileMessages;
    private List<Group> groups;
    private MessageAdapter adapter;
    private LinearLayoutManager layoutManager;
    private Constants constants;
    private Repository repository;
    private String currentPhotoPath;
    private SharedPreferences sharedPreferences;
    private ColorUtils colorUtils;
    private Window window;
    private boolean isDynamicTheme = false;

    public static final String TAG = "ChatActivity";
    public static final int CAMERA_REQ_CODE = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Log.d(TAG, "onCreate: create");

        dbLinks = new DBLinks();
        colorUtils = new ColorUtils();
        constants = new Constants();
        messages = new LinkedList<>();
        messagesAll = new LinkedList<>();
        groups = new ArrayList<>();
        incompleteImageMessages = new ArrayList<>();
        incompleteFileMessages = new ArrayList<>();
        window = getWindow();

        repository = new Repository(this.getApplicationContext());

        parentIntent = getIntent();
        currentGroup = (Group) parentIntent.getSerializableExtra("currentGroup");
        currentUser = (User) parentIntent.getSerializableExtra("currentUser");
        groups = (List<Group>) parentIntent.getSerializableExtra("group_list");
        currentGroup.setId(parentIntent.getStringExtra("current_group_id"));

        for (int i = 0; i < groups.size(); i++) {
            String id = parentIntent.getStringExtra("id_" + i);
            groups.get(i).setId(id);
        }

        bindViews();

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String filePath = sharedPreferences.getString("bg", "");
        isDynamicTheme = sharedPreferences.getBoolean("dynamic_theme", false);
        Log.d(TAG, "onCreate: saved background image path -> " + filePath);
        if (!filePath.equals("")) {
            if (isDynamicTheme) {
                File image = new File(filePath);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);

                Palette palette = colorUtils.getColorPaletteForImage(bitmap);
                adapter = new MessageAdapter(messages, currentUser, palette);

                int rgb = -1;
                Palette.Swatch dark = getRgbDark(palette);

                if (dark != null) {
                    int availableColor = getTopSwatch(palette);
                    if (availableColor != -1) {
                        getSupportActionBar().setBackgroundDrawable(new
                                ColorDrawable(availableColor));
                        window.setStatusBarColor(availableColor);
                        Log.d(TAG, "onActivityResult: available swatch");
                        actionsHolder.getBackground().setColorFilter(availableColor, PorterDuff.Mode.SRC_IN);
                    }
                    Log.d(TAG, "onCreate: rgb -> " + rgb);
                    if (isColorDark(dark.getRgb()))
                        messageField.setTextColor(ContextCompat.getColor(this, R.color.md_white));
                    else
                        messageField.setTextColor(ContextCompat.getColor(this, R.color.black));
                    messageField.setHintTextColor(getComplimentColor(dark.getRgb()));
                    camera.getDrawable().setColorFilter(getComplimentColor(dark.getRgb())
                            , PorterDuff.Mode.SRC_IN);
                    sendMessage.getDrawable().setColorFilter(getComplimentColor(dark.getRgb())
                            , PorterDuff.Mode.SRC_IN);
                }
            }

            Glide.with(this).load(new File(filePath)).into(backgroundImage);
            // adapter = new MessageAdapter(messages, currentUser, palette.getDominantSwatch());
        }
        else
            adapter = new MessageAdapter(messages, currentUser, null);

        if (!isDynamicTheme)
            adapter = new MessageAdapter(messages, currentUser, null);

        layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);

        messagesList.setLayoutManager(layoutManager);
        messagesList.setAdapter(adapter);
        messagesList.setNestedScrollingEnabled(false);

        reqMessages();

        Objects.requireNonNull(getSupportActionBar()).setTitle(currentGroup.getGroupName());

        try {
            socket = IO.socket(dbLinks.getBaseLink());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent("display-notification");
        intent.putExtra("notify", false);
        intent.putExtra("by", "onCreate");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Message message = (Message) intent.getSerializableExtra("message");
                if (!message.getReferencedUserEmail().equals(currentUser.getEmail())) {
                    messages.add(message);
                    int index = messages.size() - 1;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyItemInserted(messages.size() - 1);
                            layoutManager.scrollToPositionWithOffset(adapter.getItemCount() - 1, 0);
                        }
                    });

                    if (message.getType() == constants.MESSAGE_PHOTO) {
                        getImageData(message, index);
                    }

                    if (message.getType() == constants.MESSAGE_FILE) {
                        getFileData(message, index);
                    }
                }
                Log.d(TAG, "onReceive: new message -> " + message.getMessage());
            }
        }, new IntentFilter("new-message"));

        socket.connect();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                layoutManager.scrollToPosition(0);
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCameraHardware(getApplicationContext())) {
                    if (checkSelfPermission(Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                CAMERA_REQ_CODE);
                    }
                    else {
                        dispatchTakePictureIntent();
                    }
                }
                else
                    Toast.makeText(ChatActivity.this, "This " +
                            "device has no camera", Toast.LENGTH_SHORT).show();
            }
        });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String messageVal = messageField.getText().toString();
                if (messageVal.equalsIgnoreCase(""))
                    Toast.makeText(ChatActivity.this, "Message field is empty",
                            Toast.LENGTH_SHORT).show();
                else {
                    Message message = new Message();
                    message.setMessage(messageVal);
                    message.setReferencedGroupId(currentGroup.getId());
                    message.setSentBy(currentUser.getFirstName() + " " + currentUser.getLastName());
                    message.setReferencedUserEmail(currentUser.getEmail());
                    message.setTimestamp(getCurrentTime());
                    message.setType(constants.MESSAGE_TEXT);
                    message.setImagePath("");
                    Log.d(TAG, "onClick: referencedGroupId -> " + message.getReferencedGroupId());
                    Log.d(TAG, "onClick: groupId -> " + currentGroup.getId());

                    JSONObject object = new JSONObject();
                    try {
                        object.put("message", messageVal);
                        object.put("sentBy", message.getSentBy());
                        object.put("referencedGroupId", currentGroup.getId());
                        object.put("referencedUserEmail", message.getReferencedUserEmail());
                        object.put("timestamp", message.getTimestamp());
                        object.put("type", message.getType());
                        object.put("imagePath", "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    messageField.setText("");

                    socket.emit("chat", object);

                    //repository.insertMessage(message);
                    messages.add(message);
                    adapter.notifyItemInserted(messages.size() - 1);
                    //messagesList.scheduleLayoutAnimation();
                    layoutManager.scrollToPositionWithOffset(adapter.getItemCount() - 1, 0);
                    //messagesList.smoothScrollToPosition(adapter.getItemCount() - 1);
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Group group = (Group) intent.getSerializableExtra("group");
                Message quizMessage = (Message) intent.getSerializableExtra("message");
                Log.d(TAG, "onReceive: received new quiz message");
                Log.d(TAG, "onReceive: group to share quiz -> " + group.getGroupName());

                if (currentGroup.getAccessToken().equals(group.getAccessToken())) {
                    messages.add(quizMessage);
                    adapter.notifyItemInserted(messages.size() - 1);
                    layoutManager.scrollToPositionWithOffset(adapter.getItemCount() - 1, 0);
                }
            }
        }, new IntentFilter("new-quiz-message"));

    }

    private String saveFileToLocalStorage(String encodedData, LocalFile localFile) {
        byte[] bytes = Base64.decode(encodedData, Base64.DEFAULT);

        File file = new File(getFilesDir(), localFile.getFileName());

        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(bytes);
            bos.flush();
            bos.close();

            Log.d(TAG, "saveFileToLocalStorage: file written to path -> " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private void getFileData(final Message message, final int index) {
        String url = dbLinks.getBaseLink() + "get-file-data?groupId=" + currentGroup.getId()
                + "&data=" + message.getAlphaNumeric();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        final Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();

                try {
                    JSONObject res = new JSONObject(json);
                    JSONArray array = res.getJSONArray("result");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        String data = object.getString("fileData");
                        String filePath = saveFileToLocalStorage(data,
                                new Gson().fromJson(message.getLocalFileData(), LocalFile.class));

                        if (filePath.equals("")) {
                            Log.d(TAG, "onResponse: problem writing to local storage") ;
                        }
                        else {
                            LocalFile localFile =
                                    new Gson().fromJson(message.getLocalFileData(), LocalFile.class);
                            localFile.setFilePath(filePath);
                            message.setLocalFileData(new Gson().toJson(localFile));
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    repository.updateLocalFileData(message.getLocalFileData(), message.getId());
                                }
                            }).start();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyItemChanged(index);
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getImageData(final Message message, final int index) {
        String url = dbLinks.getBaseLink() + "get-images-for-group?groupId=" + currentGroup.getId()
                 + "&data=" + message.getAlphaNumeric();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        final Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();

                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray array = jsonObject.getJSONArray("result");

                    String data = array.getJSONObject(0).getString("image");

                    byte[] decodedString = Base64.decode(data, Base64.DEFAULT);
                    final Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    saveImage(decodedByte);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messages.get(index).setImagePath(currentPhotoPath);
                            Log.d(TAG, "run: photo path -> " + currentPhotoPath);
                            adapter.notifyItemChanged(index);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "run: message updated id -> " + messages.get(index).getId());
                                    repository.updateImagePath(messages.get(index).getImagePath(),
                                            messages.get(index).getId());
                                }
                            }).start();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, PHOTO_CAPTURE);
            }
        }
    }

    private String getCurrentTime() {
        return Calendar.getInstance().getTime().toString();
    }

    private void bindViews() {
        messageField = findViewById(R.id.et_text_message);
        sendMessage = findViewById(R.id.iv_send_message);
        camera = findViewById(R.id.iv_camera);
        messagesList = findViewById(R.id.rv_messages);
        backgroundImage = findViewById(R.id.iv_background);
        actionsHolder = findViewById(R.id.rl_1);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy: destroy");
        socket.disconnect();
        socket.off("new-text-message");

        Intent intent = new Intent("display-notification");
        intent.putExtra("notify", true);
        intent.putExtra("by", "onDestroy");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @SuppressLint("StaticFieldLeak")
    private void reqMessages() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final List<Integer> indexes = new ArrayList<>();
                final List<Integer> fileIndexes = new ArrayList<>();
                List<Message> m = repository.getMessages();
                Log.d(TAG, "doInBackground: messages -> " + m.size());
                if (!m.isEmpty()) {
                    for (int i = 0; i < m.size(); i++) {
                        if (currentGroup.getId().equals(m.get(i).getReferencedGroupId())) {
                            messages.add(m.get(i));
                            Log.d(TAG, "doInBackground: message id -> " + m.get(i).getId());

                            if (m.get(i).getSentBy().equalsIgnoreCase(currentUser.getEmail())) {
                                if (m.get(i).getType() == constants.MESSAGE_TEXT)
                                    Log.d(TAG, "doInBackground: text message sent by current user");
                                if (m.get(i).getType() == constants.MESSAGE_PHOTO)
                                    Log.d(TAG, "doInBackground: photo sent by current user");
                            }
                            else {
                                if (m.get(i).getType() == constants.MESSAGE_TEXT)
                                    Log.d(TAG, "doInBackground: text message sent by: " + m.get(i).getSentBy());
                                if (m.get(i).getType() == constants.MESSAGE_PHOTO)
                                    Log.d(TAG, "doInBackground: photo sent by: " + m.get(i).getSentBy());
                            }
                        }
                    }

                    for (int i = 0; i < messages.size(); i++) {
                        if (messages.get(i).getType() == constants.MESSAGE_PHOTO
                                && messages.get(i).getImagePath().equals("")) {
                            incompleteImageMessages.add(m.get(i));
                            indexes.add(i);
                        }
                        if (messages.get(i).getType() == constants.MESSAGE_FILE) {
                            Gson gson = new Gson();
                            LocalFile localFile = gson.fromJson(messages.get(i).getLocalFileData(),
                                    LocalFile.class);
                            if (localFile.getFilePath().equals("")) {
                                incompleteFileMessages.add(m.get(i));
                                fileIndexes.add(i);
                            }
                        }
                    }

                    if (!incompleteImageMessages.isEmpty())
                        getMissingData(currentGroup.getId(), indexes);

                    if (!incompleteFileMessages.isEmpty())
                        getMissingFileData(currentGroup.getId(), fileIndexes);

                }

                Log.d(TAG, "reqMessages: incomplete image messages -> " + incompleteImageMessages.size());
                Log.d(TAG, "reqMessages: incomplete file messages -> " + incompleteFileMessages.size());


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        layoutManager.scrollToPositionWithOffset(adapter.getItemCount() - 1, 0);
                    }
                });

                return null;
            }
        }.execute();


    }

    private void getMissingFileData(String id, final List<Integer> indexes) {
        StringBuilder data = new StringBuilder();

        for (int i = 0; i < incompleteFileMessages.size(); i++) {
            data.append(incompleteFileMessages.get(i).getAlphaNumeric());
            if (i != incompleteFileMessages.size() - 1)
                data.append("!");
        }

        String url = dbLinks.getBaseLink() + "get-file-data?referencedGroupId=" + id
                + "&data=" + data.toString();

        Log.d(TAG, "getMissingData: url -> " + url);
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

                Log.d(TAG, "onResponse: server responded");
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray array = jsonObject.getJSONArray("result");

                    for (int i = 0; i < array.length(); i++) {
                        final Message message = messages.get(indexes.get(i));
                        JSONObject object = array.getJSONObject(i);
                        String data = object.getString("fileData");
                        String filePath = saveFileToLocalStorage(data,
                                new Gson().fromJson(message.getLocalFileData(), LocalFile.class));

                        if (filePath.equals("")) {
                            Log.d(TAG, "onResponse: problem writing to local storage") ;
                        }
                        else {
                            final LocalFile localFile =
                                    new Gson().fromJson(message.getLocalFileData(), LocalFile.class);
                            localFile.setFilePath(filePath);
                            message.setLocalFileData(new Gson().toJson(localFile));
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    repository.updateLocalFileData(message.getLocalFileData(), message.getId());
                                }
                            }).start();

                            final int finalI = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messages.get(indexes.get(finalI))
                                            .setLocalFileData(new Gson().toJson(localFile));
                                    adapter.notifyItemChanged(indexes.get(finalI));
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void getMissingData(String groupId, final List<Integer> indexes) {
        StringBuilder data = new StringBuilder();

        for (int i = 0; i < incompleteImageMessages.size(); i++) {
            data.append(incompleteImageMessages.get(i).getAlphaNumeric());
            if (i != incompleteImageMessages.size() - 1)
                data.append("!");
        }

        String url = dbLinks.getBaseLink() + "get-images-for-group?referencedGroupId=" + groupId
                 + "&data=" + data.toString();
        Log.d(TAG, "getMissingData: url -> " + url);
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
                Log.d(TAG, "onResponse: server responded");

                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray array = jsonObject.getJSONArray("result");

                    for (int i = 0; i < array.length(); i++) {
                        String data = array.getJSONObject(0).getString("image");

                        byte[] decodedString = Base64.decode(data, Base64.DEFAULT);
                        final Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                        saveImage(decodedByte);

                        final int finalI = i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                messages.get(indexes.get(finalI)).setImagePath(currentPhotoPath);
                                adapter.notifyItemChanged(indexes.get(finalI));
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "run: update message with id -> "
                                                + messages.get(indexes.get(finalI)).getId());
                                        repository.updateImagePath(messages.get(indexes.get(finalI)).getImagePath(),
                                                messages.get(indexes.get(finalI)).getId());
                                    }
                                }).start();
                            }
                        });


                    }



                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void add50(List<Integer> indexes) {
        messages.addAll(messagesAll);

    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "createImageFile: photo path -> " + currentPhotoPath);
        return image;
    }

    private void saveImage(Bitmap finalBitmap) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            FileOutputStream out = new FileOutputStream(image);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            currentPhotoPath = image.getAbsolutePath();
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PHOTO_CAPTURE && resultCode == RESULT_OK) {
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            testImage.setImageBitmap(imageBitmap);*/

            Message m = new Message();
            m.setType(constants.MESSAGE_PHOTO);
            m.setImagePath(currentPhotoPath);
            m.setSentBy(currentUser.getFirstName() + " " + currentUser.getLastName());
            m.setReferencedUserEmail(currentUser.getEmail());
            m.setReferencedGroupId(currentGroup.getId());
            m.setMessage("");
            m.setTimestamp(getCurrentTime());
            m.setAlphaNumeric(getAlphaNumeric(16));
            messages.add(m);

            Log.d(TAG, "onActivityResult: picture taken");

            Intent intent = new Intent("picture-taken");
            intent.putExtra("message", m);
            LocalBroadcastManager.getInstance(ChatActivity.this).sendBroadcast(intent);

            adapter.notifyItemInserted(messages.size() - 1);
            layoutManager.scrollToPositionWithOffset(adapter.getItemCount() - 1, 0);

            emitImage(m);

            Log.d(TAG, "onActivityResult: photo message sent by -> " + m.getSentBy());
        }
        else if (data == null) {
            Log.d(TAG, "onActivityResult: camera data is null");
        }

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            Log.d(TAG, "onActivityResult: wallpaper path -> " + picturePath);

            cursor.close();

            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("bg", picturePath);
            editor.apply();

            File image = new File(picturePath);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);

            if (isDynamicTheme) {
                Palette palette = colorUtils.getColorPaletteForImage(bitmap);
                adapter.setPalette(palette);
                adapter.notifyDataSetChanged();
                Palette.Swatch dark = getRgbDark(palette);

                if (dark != null) {
                    int availableColor = getTopSwatch(palette);
                    if (availableColor != -1) {
                        getSupportActionBar().setBackgroundDrawable(new
                                ColorDrawable(availableColor));
                        window.setStatusBarColor(availableColor);
                        actionsHolder.getBackground().setColorFilter(availableColor, PorterDuff.Mode.SRC_IN);
                        Log.d(TAG, "onActivityResult: available swatch");
                    }

                    if (isColorDark(dark.getRgb()))
                        messageField.setTextColor(ContextCompat.getColor(this, R.color.md_white));
                    else
                        messageField.setTextColor(ContextCompat.getColor(this, R.color.black));
                    camera.getDrawable().setColorFilter(getComplimentColor(dark.getRgb())
                            , PorterDuff.Mode.SRC_IN);
                    sendMessage.getDrawable().setColorFilter(getComplimentColor(dark.getRgb())
                            , PorterDuff.Mode.SRC_IN);
                    messageField.setHintTextColor(getComplimentColor(dark.getRgb()));
                }
            }

            Glide.with(ChatActivity.this).load(new File(picturePath)).into(backgroundImage);
        }

        if (requestCode == SETTINGS_REQ_CODE && resultCode == RESULT_OK && data != null) {
            isDynamicTheme = data.getBooleanExtra("dynamic_theme", false);
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("dynamic_theme", isDynamicTheme);
            editor.apply();
        }

        if (requestCode == Constant.REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK && data != null) {
            ArrayList<NormalFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE);
            if (list.get(0) != null) {
                Log.d(TAG, "onActivityResult: picked file path -> " + list.get(0).getPath());
                final File file = new File(list.get(0).getPath());
                Log.d(TAG, "onActivityResult: picked file size -> " + list.get(0).getSize());
                Log.d(TAG, "onActivityResult: picked file length -> " + file.length());
                final LocalFile localFile = new LocalFile();
                String path = list.get(0).getPath();
                String fileName = path.split("/")[path.split("/").length - 1];
                localFile.setMimeType(list.get(0).getMimeType());
                localFile.setFilePath(list.get(0).getPath());
                localFile.setFileName(fileName);
                localFile.setFileData("");

                Log.d(TAG, "onActivityResult: file data -> " + localFile.getFileData());

                // file message
                final Message message = new Message();
                message.setAlphaNumeric(getAlphaNumeric(16));
                message.setType(constants.MESSAGE_FILE);
                message.setLocalFileData(new Gson().toJson(localFile));
                message.setSentBy(currentUser.getFirstName() + " " + currentUser.getLastName());
                message.setMessage("");
                message.setReferencedGroupId(currentGroup.getId());
                message.setReferencedUserEmail(currentUser.getEmail());
                message.setTimestamp(getCurrentTime());
                message.setImagePath("");
                message.setQuizData("");
                message.setQuizId(-1);
                message.setImageData("");
                message.setFileExtension(localFile.getMimeType());

                Intent intent = new Intent("file-message-sent");
                intent.putExtra("file_message", message);
                LocalBroadcastManager.getInstance(ChatActivity.this).sendBroadcast(intent);

                messages.add(message);
                adapter.notifyItemInserted(messages.size() - 1);
                layoutManager.scrollToPositionWithOffset(adapter.getItemCount() - 1, 0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String fileData = Base64.encodeToString(
                                    FileUtils.readFileToByteArray(file), Base64.DEFAULT);

                            localFile.setFilePath("");

                            JSONObject object = new JSONObject();
                            try {
                                object.put("message", "");
                                object.put("sentBy", message.getSentBy());
                                object.put("referencedGroupId", message.getReferencedGroupId());
                                object.put("referencedUserEmail", message.getReferencedUserEmail());
                                object.put("timestamp", message.getTimestamp());
                                object.put("type", message.getType());
                                object.put("imagePath", "");
                                object.put("localFileData", new Gson().toJson(localFile));
                                object.put("alphaNumeric", message.getAlphaNumeric());
                                object.put("quizData", "");
                                object.put("quizId", -1);
                                object.put("imageData", "");
                                object.put("fileExtension", message.getFileExtension());

                                // this needs to be downloaded
                                object.put("fileData", fileData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            socket.emit("chat", object);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    public boolean checkPermissionForReadExternalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForReadExternalStorage() throws Exception {
        try {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQ_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == READ_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                launchImagePicker();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_quiz:
                createOptionsDialog();
                return true;
            case R.id.change_wallpaper:
                if (checkPermissionForReadExternalStorage())
                    launchImagePicker();
                else {
                    try {
                        requestPermissionForReadExternalStorage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            case R.id.settings:
                Intent intent = new Intent(ChatActivity.this, SettingsActivity.class);
                intent.putExtra("dynamic_theme", isDynamicTheme);
                startActivityForResult(intent, SETTINGS_REQ_CODE);
                return true;
            case R.id.share_file:
                Intent filePickIntent = new Intent(ChatActivity.this, NormalFilePickActivity.class);
                filePickIntent.putExtra(Constant.MAX_NUMBER, 1);
                filePickIntent.putExtra(NormalFilePickActivity.SUFFIX, new String[] {"xlsx", "xls", "doc", "docx", "ppt", "pptx", "pdf", "txt"});
                startActivityForResult(filePickIntent, Constant.REQUEST_CODE_PICK_FILE);
                return true;
            case R.id.leave_group:
                showLeaveGroupDialog();
                return true;
            case R.id.group_access_token:
                if (currentGroup.getGroupType().equals("private")) {
                    if (currentUser.getEmail().equals(currentGroup.getGroupAdministrator())) {
                        showAccessTokenDialog();
                    }
                    else {
                        showNotAdminDialog();
                    }
                }
                else {
                    showGroupNotPrivateDialog();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAccessTokenDialog() {
        copyAccessTokenToClipboard();
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Access Token")
                .setMessage("This group's access token has been copied to your clipboard. " +
                        "Send it to whoever you want to join this group")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create();
        builder.show();
    }

    private void copyAccessTokenToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("token", currentGroup.getAccessToken());
        clipboard.setPrimaryClip(clip);
    }

    private void showNotAdminDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Administrator Rights")
                .setMessage("Only the group administrator has access to this feature.")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create();
        builder.show();
    }

    private void showGroupNotPrivateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Public Group")
                .setMessage("This feature is only available for private groups")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create();
        builder.show();
    }

    private void showLeaveGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Leave group")
                .setMessage("You are about to leave this group. Are you sure you want to continue ?")
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String[] s = currentUser.getJoinedGroupsAccessTokens().split("!@!");
                        List<String> tokens = new ArrayList<>(Arrays.asList(s));
                        Log.d(TAG, "onClick: tokens size -> " + tokens.size());
                        String currentToken = currentGroup.getAccessToken();
                        for (int j = 0; j < tokens.size(); j++) {
                            if (tokens.get(j).equals(currentToken)) {
                                tokens.remove(j);
                                String result = arrayToString(tokens);
                                currentUser.setJoinedGroupsAccessTokens(result);
                                updateUserInDb(currentToken);
                                break;
                            }
                        }
                    }
                });
        builder.create();
        builder.show();
    }

    private void updateUserInDb(final String removedToken) {
        String url = dbLinks.getBaseLink() + "update-user-group-tokens?id="
                + currentUser.getId() + "&tokens=" + currentUser.getJoinedGroupsAccessTokens()
                + "&groupId=" + currentGroup.getId();

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

                try {
                    JSONObject res = new JSONObject(json);
                    boolean userUpdated = res.getBoolean("userUpdated");

                    if (userUpdated) {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(currentGroup.getId());
                        Log.d(TAG, "getGroups: unsubscribed from -> " + currentGroup.getId());
                        Intent intent = new Intent("user-left-group");
                        Log.d(TAG, "onResponse: removed token ->" + removedToken);
                        intent.putExtra("removed_token", removedToken);
                        intent.putExtra("user", currentUser);
                        LocalBroadcastManager.getInstance(ChatActivity.this).sendBroadcast(intent);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChatActivity.this, "User data updated", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String arrayToString(List<String> tokens) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            builder.append(tokens.get(i));
            builder.append("!@!");
        }

        return builder.toString();
    }

    private void launchImagePicker() {
        Intent i = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    private void createOptionsDialog() {
        String[] items = new String[] {"Create new quiz", "Share a quiz"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Pick an option")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            Intent intent = new Intent(ChatActivity.this, QuizWizardActivity.class);
                            intent.putExtra("currentUser", currentUser);
                            intent.putExtra("isEdit", false);
                            intent.putExtra("currentGroup", (Serializable) currentGroup);
                            startActivity(intent);
                        }
                        else {
                            Intent intent = new Intent(ChatActivity.this, UserQuizzesActivity.class);
                            intent.putExtra("currentUser", currentUser);
                            intent.putExtra("group_list", (Serializable) groups);
                            startActivity(intent);
                        }
                    }
                });
        builder.create();
        builder.show();
    }

    private long getTimeInMillis() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private void emitImage(final Message message) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bm = BitmapFactory.decodeFile(message.getImagePath());
                Log.d(TAG, "run: bm size -> " + bm.getByteCount());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
                byte[] b = byteArrayOutputStream.toByteArray();

                Log.d(TAG, "run: post encoded size -> " + b.length);

                String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

                Log.d(TAG, "onCreate: encoded string size -> " + encodedImage.getBytes().length);
                Log.d(TAG, "run: encoded image -> " + encodedImage);

                JSONObject object = new JSONObject();
                try {
                    object.put("message", "");
                    object.put("sentBy", message.getSentBy());
                    object.put("referencedGroupId", message.getReferencedGroupId());
                    object.put("referencedUserEmail", message.getReferencedUserEmail());
                    object.put("timestamp", message.getTimestamp());
                    object.put("type", message.getType());
                    object.put("imagePath", "");
                    object.put("image", encodedImage);
                    object.put("alphaNumeric", message.getAlphaNumeric());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                socket.emit("chat", object);
                Log.d(TAG, "run: image sent");
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (!messages.isEmpty())
            parentIntent.putExtra("last_message", messages.getLast());
        setResult(RESULT_OK, parentIntent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: pause");
        Intent intent = new Intent("display-notification");
        intent.putExtra("by", "onPause");
        intent.putExtra("notify", true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: resume");
        Intent intent = new Intent("display-notification");
        intent.putExtra("notify", false);
        intent.putExtra("by", "onResume");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public String getAlphaNumeric(int len) {

        char[] ch = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

        char[] c = new char[len];
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < len; i++) {
            c[i] = ch[random.nextInt(ch.length)];
        }

        return new String(c);
    }

    private Palette.Swatch getRgbDark(Palette palette) {
        if (palette.getDarkMutedSwatch() != null)
            return palette.getDarkMutedSwatch();
        if (palette.getDarkVibrantSwatch() != null)
            return palette.getDarkVibrantSwatch();
        return null;
    }

    public boolean isColorDark(int color){
        double darkness = 1-(0.299 * Color.red(color) + 0.587 * Color.green(color)
                + 0.114 * Color.blue(color))/255;
        Log.d(TAG, "isColorDark: darkness -> " + darkness);
        if(darkness < 0.5){
            return false; // It's a light color
        }else{
            return true; // It's a dark color
        }
    }

    public double getColorDarkness(int color){
        double darkness = 1-(0.299 * Color.red(color) + 0.587 * Color.green(color)
                + 0.114 * Color.blue(color))/255;
        return darkness;
    }

    public static int getComplimentColor(int color) {
        // get existing colors
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);

        // find compliments
        red = (~red) & 0xff;
        blue = (~blue) & 0xff;
        green = (~green) & 0xff;

        return Color.argb(alpha, red, green, blue);
    }

    private int getTopSwatch(Palette palette) {
        if (palette.getDominantSwatch() != null
                && getColorDarkness(palette.getDominantSwatch().getRgb()) > 0.4)
            return palette.getDominantSwatch().getRgb();
        if (palette.getDarkVibrantSwatch() != null
                && getColorDarkness(palette.getDarkVibrantSwatch().getRgb()) > 0.4)
            return palette.getDarkVibrantSwatch().getRgb();
        if (palette.getLightMutedSwatch() != null
                && getColorDarkness(palette.getLightMutedSwatch().getRgb()) > 0.4)
            return palette.getLightMutedSwatch().getRgb();
        if (palette.getLightVibrantSwatch() != null
                && getColorDarkness(palette.getLightVibrantSwatch().getRgb()) > 0.4)
            return palette.getLightVibrantSwatch().getRgb();
        if (palette.getVibrantSwatch() != null
                && getColorDarkness(palette.getVibrantSwatch().getRgb()) > 0.4)
            return palette.getVibrantSwatch().getRgb();
        if (palette.getDarkMutedSwatch() != null
                && getColorDarkness(palette.getDarkMutedSwatch().getRgb()) > 0.4)
            return palette.getDarkMutedSwatch().getRgb();
        return -1;
    }
}
