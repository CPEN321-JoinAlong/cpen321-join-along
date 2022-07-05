package com.joinalongapp.viewmodel;

import org.json.JSONException;
import org.json.JSONObject;

public class ReportDetails {

    private String reportingName;
    private String reason;
    private String description;
    private Boolean blockStatus;

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

    public String toJsonString() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("user", getReportingName());
        json.put("reason", getReason());
        json.put("description", getDescription());
        json.put("block", getBlockStatus());

        return json.toString();
    }
}
