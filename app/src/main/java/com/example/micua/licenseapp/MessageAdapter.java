package com.example.micua.licenseapp;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.example.micua.licenseapp.ChatActivity.TAG;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int LIGHT_COLOR_FACTOR = 50;

    private LinkedList<Message> messages;
    private User currentUser;
    private Context context;
    private Map<String, String> monthMap;
    private Constants constants;
    private TimeUtils timeUtils;
    private Palette palette;
    private Gson gson;
    private List<Palette.Swatch> swatches;



    public static class MessageTextHolder extends RecyclerView.ViewHolder {

        private TextView message, time, sentBy;
        private RelativeLayout messageHolder, parent;

        public MessageTextHolder(View view) {
            super(view);
            message = view.findViewById(R.id.tv_message);
            time = view.findViewById(R.id.tv_timestamp);
            parent = view.findViewById(R.id.message_parent);
            messageHolder = view.findViewById(R.id.rl_message_holder);
            sentBy = view.findViewById(R.id.tv_sent_by);
        }
    }

    public static class QuizMessageHolder extends RecyclerView.ViewHolder {

        private TextView sharedBy, tv1, sep1, sep2;
        private Button start;
        private RelativeLayout body;

        public QuizMessageHolder(View view) {
            super(view);
            sharedBy = view.findViewById(R.id.tv_shared_by);
            start = view.findViewById(R.id.btn_start_quiz);
            body = view.findViewById(R.id.rl_body);
            tv1 = view.findViewById(R.id.tv_1);
            sep1 = view.findViewById(R.id.tv_sep_1);
            sep2 = view.findViewById(R.id.tv_sep_2);
        }
    }

    public static class ImageReceivedMessageHolder extends RecyclerView.ViewHolder {

        private CardView imageHolder, background;
        private ImageView imageMessage;
        private ProgressBar loading;
        private TextView sender, time;

        public ImageReceivedMessageHolder(View view) {
            super(view);
            imageMessage = view.findViewById(R.id.iv_image_message_received);
            imageHolder = view.findViewById(R.id.cv_image_message_received_holder);
            loading = view.findViewById(R.id.pb_loading);
            background = view.findViewById(R.id.cv_background);
            sender = view.findViewById(R.id.tv_sender);
            time = view.findViewById(R.id.tv_timestamp);
        }
    }

    public static class ImageSentMessageHolder extends RecyclerView.ViewHolder {

        private CardView imageHolder;
        private ImageView imageMessage;
        private ProgressBar loading;
        private TextView time;

        public ImageSentMessageHolder(View view) {
            super(view);
            imageMessage = view.findViewById(R.id.iv_image_message_sent);
            imageHolder = view.findViewById(R.id.cv_image_message_sent_holder);
            loading = view.findViewById(R.id.pb_loading);
            time = view.findViewById(R.id.tv_timestamp);
        }
    }

    public static class FileSentMessageHolder extends RecyclerView.ViewHolder {

        private RelativeLayout body;
        private ImageView fileTypeImage;
        private TextView timeSent, fileName;

        public FileSentMessageHolder(View view) {
            super(view);
            fileTypeImage = view.findViewById(R.id.iv_file_mime);
            body = view.findViewById(R.id.rl_body);
            timeSent = view.findViewById(R.id.tv_timestamp);
            fileName = view.findViewById(R.id.tv_file_name);
        }
    }

    public static class FileReceivedMessageHolder extends RecyclerView.ViewHolder {

        private RelativeLayout body;
        private ImageView fileTypeImage;
        private TextView timeSent, fileName, sentBy;

        public FileReceivedMessageHolder(View view) {
            super(view);
            fileTypeImage = view.findViewById(R.id.iv_file_mime);
            body = view.findViewById(R.id.rl_body);
            timeSent = view.findViewById(R.id.tv_timestamp);
            fileName = view.findViewById(R.id.tv_file_name);
            sentBy = view.findViewById(R.id.tv_sent_by);
        }
    }

    public MessageAdapter(LinkedList<Message> messages, User user, Palette palette) {
        this.messages = messages;
        this.currentUser = user;
        this.swatches = new ArrayList<>();
        this.constants = new Constants();
        timeUtils = new TimeUtils();
        this.gson = new Gson();
        this.palette = palette;
        this.monthMap = new HashMap<>();
        monthMap.put("Jan", "01");
        monthMap.put("Feb", "02");
        monthMap.put("Mar", "03");
        monthMap.put("Apr", "04");
        monthMap.put("May", "05");
        monthMap.put("Jun", "06");
        monthMap.put("Jul", "07");
        monthMap.put("Aug", "08");
        monthMap.put("Sep", "08");
        monthMap.put("Oct", "10");
        monthMap.put("Nov", "11");
        monthMap.put("Dec", "12");
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getType() == constants.MESSAGE_TEXT)
            return 1;
        else
            if (messages.get(position).getType() == constants.MESSAGE_PHOTO) {
                if (messages.get(position).getReferencedUserEmail().equalsIgnoreCase(currentUser.getEmail()))
                    return 3;
                else
                    return 2;
            }
            else if (messages.get(position).getType() == constants.MESSAGE_QUIZ)
                return 4;
            else if (messages.get(position).getType() == constants.MESSAGE_FILE) {
                if (messages.get(position).getReferencedUserEmail().equalsIgnoreCase(currentUser.getEmail()))
                    return 5;
                else
                    return 6;
            }

        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View textMessageView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_item, viewGroup, false);

        View imageReceivedMessageView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_received_image_item, viewGroup, false);

        View imageSentMessageView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_sent_image_item, viewGroup, false);

        View quizMessageView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_quiz_list_item, viewGroup, false);

        View fileSentMessageView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_sent_file, viewGroup, false);

        View fileReceivedMessageView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_received_file, viewGroup, false);

        context = viewGroup.getContext();

        if (i == 1)
            return new MessageTextHolder(textMessageView);
        else if (i == 2)
            return new ImageReceivedMessageHolder(imageReceivedMessageView);
        else if (i == 4)
            return new QuizMessageHolder(quizMessageView);
        else if (i == 3)
            return new ImageSentMessageHolder(imageSentMessageView);
        else if (i == 5)
            return new FileSentMessageHolder(fileSentMessageView);
        else if (i == 6)
            return new FileReceivedMessageHolder(fileReceivedMessageView);

        return null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int i) {
        final Message message = messages.get(i);

        int viewType = holder.getItemViewType();

        if (viewType == 1) {
            // text message
            MessageTextHolder viewHolder = (MessageTextHolder) holder;
            viewHolder.message.setText(message.getMessage());

            try {
                viewHolder.time.setText(timeUtils.computeMessageDisplayTime(message.getTimestamp()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (message.getReferencedUserEmail().equalsIgnoreCase(currentUser.getEmail())) {
                // message is sent by current user
                viewHolder.messageHolder.setBackgroundResource(R.drawable.message_send);
                if (palette != null) {
                    int dark = getRgbDark(), dominant = getRgbDarkText(), vibrant = getRgbVibrant(),
                            light = getRgbLight();
                    if (dark != -1)
                        viewHolder.messageHolder.getBackground().setColorFilter(dark, PorterDuff.Mode.SRC_IN);
                    if (dominant!= -1) {
                        if (!isColorDark(manipulateColor(dominant, LIGHT_COLOR_FACTOR)))
                            viewHolder.message.setTextColor(manipulateColor(dominant, LIGHT_COLOR_FACTOR));
                        else
                            viewHolder.message.setTextColor(ContextCompat.getColor(context, R.color.md_white));
                    }
                    if (light != -1)
                        viewHolder.time.setTextColor(light);
                }
                viewHolder.sentBy.setVisibility(View.GONE);
                //viewHolder.parent.setForegroundGravity(Gravity.END);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.messageHolder.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
                params.removeRule(RelativeLayout.ALIGN_PARENT_START);
                viewHolder.messageHolder.setLayoutParams(params);
            }
            else {
                // message is received
                viewHolder.sentBy.setText(message.getSentBy());
                viewHolder.sentBy.setVisibility(View.VISIBLE);
                viewHolder.messageHolder.setBackgroundResource(R.drawable.message_receive);

                if (palette != null) {
                    int dark = getRgbDark(), dominant = getRgbDarkText(), vibrant = getRgbVibrant(),
                            light = getRgbLight();
                    if (dark != -1)
                        viewHolder.messageHolder.getBackground().setColorFilter(dark, PorterDuff.Mode.SRC_IN);
                    if (dominant != -1) {
                        if (!isColorDark(manipulateColor(dominant, LIGHT_COLOR_FACTOR)))
                            viewHolder.message.setTextColor(manipulateColor(dominant, LIGHT_COLOR_FACTOR));
                        else
                            viewHolder.message.setTextColor(ContextCompat.getColor(context, R.color.md_white));
                    }

                    if (light != -1) {
                        viewHolder.time.setTextColor(light);
                        viewHolder.sentBy.setTextColor(light);
                    }
                }
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.messageHolder.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_START);
                params.removeRule(RelativeLayout.ALIGN_PARENT_END);
                viewHolder.messageHolder.setLayoutParams(params);
            }
        }

        if (viewType == 2) {
            // image message received
            ImageReceivedMessageHolder viewHolder = (ImageReceivedMessageHolder) holder;

            try {
                viewHolder.time.setText(timeUtils.computeMessageDisplayTime(message.getTimestamp()));
                viewHolder.sender.setText(message.getSentBy());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (palette != null) {
                int dark = getRgbDark(), dominant = getRgbDarkText(), vibrant = getRgbVibrant(),
                        light = getRgbLight();
                if (dark != -1)
                    viewHolder.background.getBackground().setColorFilter(dark, PorterDuff.Mode.SRC_IN);
                if (light != -1) {
                    viewHolder.sender.setTextColor(light);
                }
                if (light != -1)
                    viewHolder.time.setTextColor(light);
            }

            if (message.getImagePath().equals("")) {
                viewHolder.loading.setVisibility(View.VISIBLE);
                viewHolder.imageMessage.setVisibility(View.INVISIBLE);
            }
            else {
                viewHolder.loading.setVisibility(View.GONE);
                viewHolder.imageMessage.setVisibility(View.VISIBLE);
                Glide.with(context).load(new File(message.getImagePath())).into(viewHolder.imageMessage);
            }

            viewHolder.imageMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, PinchToZoomActivity.class)
                            .putExtra("image_path", message.getImagePath()));
                }
            });
        }

        if (viewType == 3) {
            // image message sent
            ImageSentMessageHolder viewHolder = (ImageSentMessageHolder) holder;

            if (message.getImagePath().equals("")) {
                viewHolder.loading.setVisibility(View.VISIBLE);
                viewHolder.imageMessage.setVisibility(View.INVISIBLE);
            }
            else {
                viewHolder.loading.setVisibility(View.GONE);
                viewHolder.imageMessage.setVisibility(View.VISIBLE);
                try {
                    viewHolder.time.setText(timeUtils.computeMessageDisplayTime(message.getTimestamp()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Glide.with(context).load(new File(message.getImagePath())).into(viewHolder.imageMessage);
            }

            if (palette != null) {
                int dark = getRgbDark(), dominant = getRgbDarkText(), vibrant = getRgbVibrant(),
                        light = getRgbLight();
                if (dark != -1)
                    viewHolder.imageHolder.getBackground().setColorFilter(dark, PorterDuff.Mode.SRC_IN);
                if (light != -1) {
                    viewHolder.time.setTextColor(light);
                }
            }

            viewHolder.imageMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    context.startActivity(new Intent(context, PinchToZoomActivity.class)
                            .putExtra("image_path", message.getImagePath()));
                }
            });
        }

        if (viewType == 4) {
            // quiz message
            QuizMessageHolder viewHolder = (QuizMessageHolder) holder;
            viewHolder.sharedBy.setText(message.getSentBy() + " shared a quiz.");

            if (palette != null) {
                int dark = getRgbDark(), dominant = getRgbDominant(), vibrant = getRgbVibrant(),
                        light = getRgbLight();
                if (dark != -1)
                    viewHolder.body.getBackground().setColorFilter(dark, PorterDuff.Mode.SRC_IN);
                if (light != -1) {
                    viewHolder.tv1.setTextColor(light);
                    viewHolder.sharedBy.setTextColor(light);
                }
                int availableColor = getTopColor(palette);

                if (availableColor != -1) {
                    viewHolder.start.getBackground().setColorFilter(availableColor,
                            PorterDuff.Mode.SRC_IN);
                    viewHolder.sep1.setBackgroundColor(ChatActivity.getComplimentColor(availableColor));
                    viewHolder.sep2.setBackgroundColor(ChatActivity.getComplimentColor(availableColor));
                    viewHolder.start.setTextColor(ChatActivity.getComplimentColor(availableColor));
                    /*if (isColorDark(availableColor)) {
                        viewHolder.start.setTextColor(ContextCompat.getColor(context, R.color.md_white));
                    }
                    else {
                        viewHolder.start.setTextColor(ContextCompat.getColor(context, R.color.black));
                    }*/
                }
            }

            viewHolder.start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Gson gson = new Gson();
                    Intent intent = new Intent(context, RunQuizActivity.class);
                    intent.putExtra("currentUser", currentUser);
                    intent.putExtra("quiz", gson.fromJson(message.getQuizData(), Quiz.class));
                    context.startActivity(intent);
                }
            });
        }

        if (viewType == 5) {
            // message file sent
            final LocalFile localFile = gson.fromJson(message.getLocalFileData(), LocalFile.class);
            FileSentMessageHolder viewHolder = (FileSentMessageHolder) holder;
            try {
                viewHolder.timeSent.setText(timeUtils.computeMessageDisplayTime(message.getTimestamp()));
                viewHolder.fileName.setText(localFile.getFileName());
                String extension = localFile.getFileName().split("\\.")
                        [localFile.getFileName().split("\\.").length - 1];
                if (extension.equals("pdf"))
                    Glide.with(context).load(R.drawable.pdf).into(viewHolder.fileTypeImage);
                if (extension.equals("xlsx") || extension.equals("xls"))
                    Glide.with(context).load(R.drawable.xls).into(viewHolder.fileTypeImage);
                if (extension.equals("docx") || extension.equals("doc"))
                    Glide.with(context).load(R.drawable.doc).into(viewHolder.fileTypeImage);
                if (extension.equals("pptx") || extension.equals("ppt"))
                    Glide.with(context).load(R.drawable.ppt).into(viewHolder.fileTypeImage);
                if (extension.equals("txt"))
                    Glide.with(context).load(R.drawable.txt).into(viewHolder.fileTypeImage);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (palette != null) {
                int dark = getRgbDark(), dominant = getRgbDarkText(), vibrant = getRgbVibrant(),
                        light = getRgbLight();
                if (dark != -1)
                    viewHolder.body.getBackground().setColorFilter(dark, PorterDuff.Mode.SRC_IN);
                if (dominant != -1) {
                    if (!isColorDark(manipulateColor(dominant, LIGHT_COLOR_FACTOR)))
                        viewHolder.fileName.setTextColor(manipulateColor(dominant, LIGHT_COLOR_FACTOR));
                    else
                        viewHolder.fileName.setTextColor(ContextCompat.getColor(context, R.color.md_white));
                }
                if (light != -1)
                    viewHolder.timeSent.setTextColor(light);
            }

            viewHolder.body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String extension = localFile.getFileName().split("\\.")
                            [localFile.getFileName().split("\\.").length - 1];
                    File file = new File(localFile.getFilePath());
                    Log.d(TAG, "onClick: file path -> " + file.getAbsolutePath());
                    Uri pathUri = FileProvider.getUriForFile(context,
                            "com.example.android.localfileprovider", file);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    if (extension.equals("docx"))
                        intent.setDataAndType(pathUri, "application/msword");
                    else if (extension.equals("xlsx"))
                        intent.setDataAndType(pathUri, "application/vnd.ms-excel");
                    else if (extension.equals("pptx"))
                        intent.setDataAndType(pathUri, "application/vnd.ms-powerpoint");
                    else
                        intent.setDataAndType(pathUri, localFile.getMimeType());
                    context.startActivity(intent);
                }
            });
        }

        if (viewType == 6) {
            final LocalFile localFile;
            localFile = gson.fromJson(message.getLocalFileData(), LocalFile.class);
            FileReceivedMessageHolder viewHolder = (FileReceivedMessageHolder) holder;
            try {
                viewHolder.timeSent.setText(timeUtils.computeMessageDisplayTime(message.getTimestamp()));
                viewHolder.sentBy.setText(message.getSentBy());
                viewHolder.fileName.setText(localFile.getFileName());
                String extension = localFile.getFileName().split("\\.")
                        [localFile.getFileName().split("\\.").length - 1];
                if (extension.equals("pdf"))
                    Glide.with(context).load(R.drawable.pdf).into(viewHolder.fileTypeImage);
                if (extension.equals("xlsx") || extension.equals("xls"))
                    Glide.with(context).load(R.drawable.xls).into(viewHolder.fileTypeImage);
                if (extension.equals("docx") || extension.equals("doc"))
                    Glide.with(context).load(R.drawable.doc).into(viewHolder.fileTypeImage);
                if (extension.equals("pptx") || extension.equals("ppt"))
                    Glide.with(context).load(R.drawable.ppt).into(viewHolder.fileTypeImage);
                if (extension.equals("txt"))
                    Glide.with(context).load(R.drawable.txt).into(viewHolder.fileTypeImage);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (palette != null) {
                int dark = getRgbDark(), dominant = getRgbDarkText(), vibrant = getRgbVibrant(),
                        light = getRgbLight();
                if (dark != -1)
                    viewHolder.body.getBackground().setColorFilter(dark, PorterDuff.Mode.SRC_IN);
                if (dominant != -1) {
                    if (!isColorDark(manipulateColor(dominant, LIGHT_COLOR_FACTOR)))
                        viewHolder.fileName.setTextColor(manipulateColor(dominant, LIGHT_COLOR_FACTOR));
                    else
                        viewHolder.fileName.setTextColor(ContextCompat.getColor(context, R.color.md_white));
                }

                if (light != -1) {
                    viewHolder.timeSent.setTextColor(light);
                    viewHolder.sentBy.setTextColor(light);
                }

                viewHolder.body.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String extension = localFile.getFileName().split("\\.")
                                [localFile.getFileName().split("\\.").length - 1];
                        Log.d(TAG, "onClick: path -> " + localFile.getFilePath());
                        File file = new File(localFile.getFilePath());
                        Log.d(TAG, "onClick: file path -> " + file.getAbsolutePath());
                        Uri pathUri = FileProvider.getUriForFile(context,
                                "com.example.android.localfileprovider", file);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        if (extension.equals("docx"))
                            intent.setDataAndType(pathUri, "application/msword");
                        else if (extension.equals("xlsx"))
                            intent.setDataAndType(pathUri, "application/vnd.ms-excel");
                        else
                            intent.setDataAndType(pathUri, localFile.getMimeType());
                        context.startActivity(intent);
                    }
                });
            }
        }
    }



    @Override
    public int getItemCount() {
        return messages.size();
    }

    private String formatTime(String time) {
        String[] s1 = time.split(" ");
        String weekDay = s1[0];
        String month = s1[1];
        String monthDay = s1[2];
        String hour = s1[3].split(":")[0];
        String min = s1[3].split(":")[1];
        String timezone = s1[4];
        String year = s1[5].split("")[3] + s1[5].split("")[4];

        String currentDate = Calendar.getInstance().getTime().toString();
        if (!currentDate.split(" ")[2].equalsIgnoreCase(monthDay)
                || !currentDate.split(" ")[1].equalsIgnoreCase(month)
                || !currentDate.split(" ")[5].equalsIgnoreCase(s1[5]))
            return monthMap.get(month) + "/" + monthDay + "/" + year;
        else if (currentDate.split(" ")[4].equalsIgnoreCase(timezone))
            return monthMap.get(month) + "/" + monthDay + "/" + year;
        return convertToTimezone(hour + ":" + min, timezone);
    }

    private String convertToTimezone(String time, String timezone) {
        return time;
    }

    public void setPalette(Palette palette) {
        this.palette = palette;
    }

    private List<Palette.Swatch> generateSwatches() {
        List<Palette.Swatch> swatches = new ArrayList<>();
        for (int i = 0; i < palette.getSwatches().size(); i++) {
            if (palette.getSwatches().get(i) != null) {
                swatches.add(palette.getSwatches().get(i));
            }
        }
        return swatches;
    }

    private int getRgbDark() {
        if (palette.getDarkMutedSwatch() != null)
            return palette.getDarkMutedSwatch().getRgb();
        if (palette.getDarkVibrantSwatch() != null)
            return palette.getDarkVibrantSwatch().getRgb();
        return -1;
    }
    private int getRgbDarkText() {
        if (palette.getDarkMutedSwatch() != null)
            return palette.getDarkMutedSwatch().getTitleTextColor();
        if (palette.getDarkVibrantSwatch() != null)
            return palette.getDarkVibrantSwatch().getTitleTextColor();
        return -1;
    }

    private int getRgbDominant() {
        if (palette.getDominantSwatch() != null)
            return palette.getDominantSwatch().getBodyTextColor();
        return  -1;
    }

    private int getRgbLight() {
        if (palette.getLightVibrantSwatch() != null)
            return palette.getLightVibrantSwatch().getRgb();
        if (palette.getLightMutedSwatch() != null)
            return palette.getLightMutedSwatch().getRgb();
        return -1;
    }

    private int getRgbVibrant() {
        if (palette.getVibrantSwatch() != null)
            return palette.getVibrantSwatch().getRgb();
        return -1;
    }

    private int manipulateColor(int color, float factor) {
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.rgb(Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));
    }

    public boolean isColorDark(int color){
        double darkness = 1-(0.299 * Color.red(color) + 0.587 * Color.green(color)
                + 0.114 * Color.blue(color))/255;
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

    private int getTopColor(Palette palette) {
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
