package com.mpower.clientcollection.utilities;

/**
 * Created by ratna on 10/8/16.
 */

public class FormButtonInfo {
    private String formId;
    private int progressBarId;
    private int progressTextId;

    public FormButtonInfo(String formId, int progressBarId, int progressTextId) {
        this.formId = formId;
        this.progressBarId = progressBarId;
        this.progressTextId = progressTextId;
    }

    public int getProgressBarId() {
        return progressBarId;
    }

    public void setProgressBarId(int progressBarId) {
        this.progressBarId = progressBarId;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }
    public int getProgressTextId() {
        return progressTextId;
    }

    public void setProgressTextId(int progressTextId) {
        this.progressTextId = progressTextId;
    }


}
