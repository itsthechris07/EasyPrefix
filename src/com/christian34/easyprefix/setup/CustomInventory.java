package com.christian34.easyprefix.setup;

import java.util.ArrayList;

public class CustomInventory {
    private final String TITLE;
    private final int LINES;
    private ArrayList<Button> buttons = new ArrayList<>();

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