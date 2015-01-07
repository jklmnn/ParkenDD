package de.jkliemann.parkendd;

/**
 * Created by jkliemann on 07.01.15.
 */
public class GlobalSettings {

    private static GlobalSettings mInstance = null;
    private String[] citylist;
    private String mail;

    private GlobalSettings(){
        citylist = null;
        mail = "";
    }

    public static GlobalSettings getGlobalSettings(){
        if(mInstance == null){
            mInstance = new GlobalSettings();
        }
        return mInstance;
    }

    public String[] getCitylist(){
        return citylist;
    }

    public String getMail(){
        return mail;
    }

    public void setCitylist(String[] citylist){
        this.citylist = citylist;
    }

    public void setMail(String mail){
        this.mail = mail;
    }
}
