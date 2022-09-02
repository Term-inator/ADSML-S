package com.ecnu.adsmls.components.modal.impl;

import com.ecnu.adsmls.components.choosebutton.impl.ChooseDirectoryButton;
import com.ecnu.adsmls.components.modal.Modal;
import javafx.scene.control.Label;

public class OpenProjectModal extends Modal {
    private String directory;

    private ChooseDirectoryButton btDir;

    public OpenProjectModal() {
        super();
    }


    public String getDirectory() {
        return directory;
    }

    @Override
    protected void createWindow() {
        super.createWindow();
        this.setTitle("Open Project");

        Label lbDirName = new Label("project");
        this.btDir = new ChooseDirectoryButton(this.gridPane);

        this.slot.addRow(0, lbDirName, this.btDir.getNode());
    }

    @Override
    protected void update() {
        this.updateDirectory();
    }

    @Override
    protected void check() {
        this.checkDirectory();
    }

    @Override
    protected void then() {
    }

    private void updateDirectory() {
        try {
            this.directory = this.btDir.getFile().getAbsolutePath();
        } catch (Exception ignored) {

        }
    }

    private void checkDirectory() {
        if (this.directory == null) {
            this.valid = false;
        }
    }
}
