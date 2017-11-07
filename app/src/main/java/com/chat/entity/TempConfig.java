package com.chat.entity;

/**
 * Created by m on 11.10.2017.
 */

public class TempConfig {
    private String companionToken;
    private int adapterPosition;
    private int fragmentPosition;

    public TempConfig() {
    }

    public String getCompanionToken() {
        return companionToken;
    }

    public void setCompanionToken(String companionToken) {
        this.companionToken = companionToken;
    }

    public int getAdapterPosition() {
        return adapterPosition;
    }

    public void setAdapterPosition(int adapterPosition) {
        this.adapterPosition = adapterPosition;
    }

    public int getFragmentPosition() {
        return fragmentPosition;
    }

    public void setFragmentPosition(int fragmentPosition) {
        this.fragmentPosition = fragmentPosition;
    }
}
