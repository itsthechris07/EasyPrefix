package com.christian34.easyprefix.setup;

import java.util.ArrayList;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class CustomInventory {
    private final String TITLE;
    private final int LINES;
    private final ArrayList<Button> buttons = new ArrayList<>();

    public CustomInventory(String title, int lines) {
        this.TITLE = title;
        this.LINES = lines;
    }

    public ArrayList<Button> getButtons() {
        return buttons;
    }

    public String getTitle() {
        return TITLE;
    }

    public int getLines() {
        return LINES;
    }

    public CustomInventory addItem(Button button) {
        buttons.add(button);
        return this;
    }

}