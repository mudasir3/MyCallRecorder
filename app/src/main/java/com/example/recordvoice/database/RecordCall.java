package com.example.recordvoice.database;

/**
 * Created by vieta on 8/9/2016.
 */
public class RecordCall {
    private int id;
    private String phoneNumber;
    private String date;
    private String lengthRecord;
    private String fileName;
    private int typeCall;

    public RecordCall(String phoneNumber, String date, String fileName, int typeCall) {
        this.phoneNumber = phoneNumber;
        this.date = date;
        this.fileName = fileName;
        this.typeCall = typeCall;
    }

    public RecordCall(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLengthRecord() {
        return lengthRecord;
    }

    public void setLengthRecord(String lengthRecord) {
        this.lengthRecord = lengthRecord;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String filePath) {
        this.fileName = fileName;
    }

    public int getTypeCall() {
        return typeCall;
    }

    public void setTypeCall(int typeCall) {
        this.typeCall = typeCall;
    }
}
