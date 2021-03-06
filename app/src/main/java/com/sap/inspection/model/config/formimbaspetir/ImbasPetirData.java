package com.sap.inspection.model.config.formimbaspetir;

import java.util.ArrayList;

public class ImbasPetirData
{
    private String scheduleid;
    private ArrayList<Warga> warga;
    private int countaddwarga = 0;
    private boolean isApproved;

    public ArrayList<Warga> getWarga ()
    {
        return warga;
    }

    public void setWarga (ArrayList<Warga> warga)
    {
        this.warga = warga;
    }

    public String getScheduleid ()
    {
        return scheduleid;
    }

    public void setScheduleid (String scheduleid)
    {
        this.scheduleid = scheduleid;
    }

    public void setCountaddwarga(int countaddwarga) {
        this.countaddwarga = countaddwarga;
    }

    public int getCountaddwarga() {
        return countaddwarga;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public boolean isApproved() {
        return isApproved;
    }
}
