package com.joinalongapp.viewmodel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class ReportDetails implements Serializable, IDetailsModel {

    private String reportingName;
    private String reason;
    private String description;
    private Boolean blockStatus;
    private Boolean reportPerson;
    private Event reportingEvent;

    public ReportDetails() {
    }

    public ReportDetails(String reportingName, String reason, String description, Boolean blockStatus) {
        this.reportingName = reportingName;
        this.reason = reason;
        this.description = description;
        this.blockStatus = blockStatus;
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

    public Boolean getReportPerson() {
        return reportPerson;
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

    public void setReportPerson(Boolean reportPerson) {
        this.reportPerson = reportPerson;
    }

    public void setReportingEvent(Event reportingEvent) {
        this.reportingEvent = reportingEvent;
    }

    public String toJsonString() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("user", getReportingName());
        json.put("reason", getReason());
        json.put("description", getDescription());
        json.put("block", getBlockStatus());

        return json.toString();
    }
}
