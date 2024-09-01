package com.nextgen.indoorplanting;

import java.util.Date;

public class UserFeedbackModel {
    private String feedbackId;
    private String userName;
    private String userEmail;
    private String userFeedback;
    private Date timestamp;

    public UserFeedbackModel(String feedbackId, String userName, String userEmail, String userFeedback, Date timestamp) {
        this.feedbackId = feedbackId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userFeedback = userFeedback;
        this.timestamp = timestamp;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserFeedback() {
        return userFeedback;
    }

    public void setUserFeedback(String userFeedback) {
        this.userFeedback = userFeedback;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
