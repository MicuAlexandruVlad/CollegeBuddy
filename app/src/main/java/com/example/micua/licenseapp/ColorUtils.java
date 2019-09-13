package com.example.micua.licenseapp;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ColorUtils {
    private List<String> colors;

    public ColorUtils() {
        initColors();
    }

    private void initColors() {
        colors = new ArrayList<>();
        colors.add("#40798C");
        colors.add("#0B2027");
        colors.add("#70A9A1");
        colors.add("#BE3E82");
        colors.add("#3C0919");
        colors.add("#48BEFF");
        colors.add("#944654");
        colors.add("#1B9AAA");
        colors.add("#FFC43D");
        colors.add("#210124");
        colors.add("#247BA0");
        colors.add("#941B0C");
        colors.add("#58504A");
        colors.add("#8DB1AB");
        colors.add("#21295C");
        colors.add("#1B3B6F");
        colors.add("#7261A3");
        colors.add("#06BA63");
        colors.add("#D68FD6");
        colors.add("#46237A");
        colors.add("#256EFF");
        colors.add("#666370");
        colors.add("#E75A7C");
        colors.add("#D52941");
        colors.add("#33A1FD");
        colors.add("#2176FF");
        colors.add("#BE6E46");
        colors.add("#7286A0");
        colors.add("#978897");
        colors.add("#5AB1BB");
        colors.add("#783F8E");
        colors.add("#4F1271");

        colors.add("#FABC3C");
        colors.add("#01BAEF");
        colors.add("#9B2915");
        colors.add("#34252F");
        colors.add("#FF220C");
        colors.add("#540D6E");
        colors.add("#EE4266");
        colors.add("#FFF275");
        colors.add("#0496FF");
        colors.add("#8F2D56");
        colors.add("#9893DA");
        colors.add("#7AC74F");
        colors.add("#E87461");
        colors.add("#FF784F");
        colors.add("#3A3042");
        colors.add("#9BBEC7");
        colors.add("#253C78");
        colors.add("#2B59C3");
        colors.add("#D36582");
        colors.add("#3B7080");
        colors.add("#83BCFF");
        colors.add("#80FFE8");
        colors.add("#97D2FB");
        colors.add("#978897");
        colors.add("#706C61");
        colors.add("#918EF4");
        colors.add("#141B41");
    }

    public String getRandomColor() {
        return colors.get(randomIndex());
    }

    private int randomIndex(){
        Random random = new Random();
        return random.nextInt(colors.size());
    }

    public Palette getColorPaletteForImage(Bitmap bitmap) {
        return Palette.from(bitmap).generate();
    }
}
