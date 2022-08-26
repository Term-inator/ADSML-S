package com.ecnu.adsmls.components.modal;

import com.ecnu.adsmls.utils.FileSystem;

public class NewModelModal extends NewFileModal {
    public NewModelModal() {
        super("filename", FileSystem.Suffix.MODEL.value);
    }

    @Override
    protected void createWindow() {
        super.createWindow();
        this.setTitle("New Model");
    }

    @Override
    protected void check() {
        super.check();
        this.checkFilename();
    }

    @Override
    protected void update() {
        super.update();
    }

    @Override
    protected void then() {
        super.then();
    }

    private void checkFilename() {

    }
}
