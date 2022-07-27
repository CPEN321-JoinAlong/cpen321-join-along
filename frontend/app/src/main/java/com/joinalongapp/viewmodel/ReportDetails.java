package com.joinalongapp.viewmodel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class ReportDetails implements Serializable, IDetailsModel {
    private String id;
    private String reporterName;
    private String reportedName;
    private String reason;
    private String description;
    private Boolean blockStatus;
    private Boolean isEvent;
    private Event reportingEvent;
    private String reporterId;
    private String reportedId;


    public ReportDetails() {
    }

    public String getId() {
        return id;
    }

    public String getReporterName() {
        return reporterName;
    }

    public Boolean getEvent() {
        return isEvent;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public void setReportedName(String reportedName) {
        this.reportedName = reportedName;
    }

    public void setEvent(Boolean event) {
        isEvent = event;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReportedName() {
        return reportedName;
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
        this.reportedName = reportingName;
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

        json.put("reporterName", getReporterName());
        json.put("reportedName", getReportedName());
        json.put("reason", getReason());
        json.put("description", getDescription());
        json.put("isBlocked", getBlockStatus());
        json.put("isEvent", getIsEvent());
        json.put("reporterID", getReporterId());
        json.put("reportedID", getReportedId());
        return json;
    }

    public String toJsonString() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("reporterName", getReporterName());
        json.put("reportedName", getReportedName());
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
        setReporterName(jsonBody.getString("reporterName"));
        setReportedName(jsonBody.getString("reportedName"));
        setReporterId(jsonBody.getString("reporterID"));
        setReportedId(jsonBody.getString("reportedID"));
        setReason(jsonBody.getString("reason"));
        setDescription(jsonBody.getString("description"));
        setIsEvent(jsonBody.getBoolean("isEvent"));
        return this;
    }
}
