package com.joinalongapp.viewmodel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class ReportDetails implements Serializable, IDetailsModel {
    private String id;
    private String reportingName;
    private String reason;
    private String description;
    private Boolean blockStatus;
    private Boolean isEvent;
    private Event reportingEvent;
    private String reporterId;
    private String reportedId;


    public ReportDetails() {
    }

    public ReportDetails(String reportingName, String reason, String description, Boolean blockStatus) {
        this.reportingName = reportingName;
        this.reason = reason;
        this.description = description;
        this.blockStatus = blockStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReportingName() {
        return reportingName;
    }

    public String getReason() {
        return reason;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getBlockStatus() {
        return blockStatus;
    }

    public Boolean getIsEvent() {
        return isEvent;
    }

    public Event getReportingEvent() {
        return reportingEvent;
    }

    public void setReportingName(String reportingName) {
        this.reportingName = reportingName;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBlockStatus(Boolean blockStatus) {
        this.blockStatus = blockStatus;
    }

    public void setIsEvent(Boolean isEvent) {
        this.isEvent = isEvent;
    }

    public void setReportingEvent(Event reportingEvent) {
        this.reportingEvent = reportingEvent;
    }

    public JSONObject toJson() throws JSONException{
        JSONObject json = new JSONObject();

        json.put("user", getReportingName());
        json.put("reason", getReason());
        json.put("description", getDescription());
        json.put("block", getBlockStatus());

        return json;
    }

    public String toJsonString() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("name", getReportingName());
        json.put("reason", getReason());
        json.put("description", getDescription());
        json.put("isBlocked", getBlockStatus());
        json.put("isEvent", getIsEvent());
        json.put("reporterID", getReporterId());
        json.put("reportedID", getReportedId());


        return json.toString();
    }

    public String getReporterId() {
        return reporterId;
    }

    public String getReportedId() {
        return reportedId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public void setReportedId(String reportedId) {
        this.reportedId = reportedId;
    }

    public String getReportType(){
        if(this.getIsEvent()){
            return "Event";
        }
        else{
            return "User";
        }
    }
    @Override
    public IDetailsModel populateDetailsFromJson(String jsonString) throws JSONException {
        JSONObject jsonBody = new JSONObject(jsonString);
        setId(jsonBody.getString("_id"));
        setReportingName(jsonBody.getString("name"));
        setReporterId(jsonBody.getString("reporterID"));
        setReportedId(jsonBody.getString("reportedID"));
        setReason(jsonBody.getString("reason"));
        setDescription(jsonBody.getString("description"));
        setIsEvent(jsonBody.getBoolean("isEvent"));
        return this;
    }
}
