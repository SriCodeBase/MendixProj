package com.org.mendix.gitHubPOC.service;

import java.io.Serializable;
import java.util.Date;

public class gitHubVar implements Serializable {

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDateOfCommit() {
        return dateOfCommit;
    }

    public void setDateOfCommit(Date dateOfCommit) {
        this.dateOfCommit = dateOfCommit;
    }

    public String getCommitMsg() {
        return commitMsg;
    }

    public void setCommitMsg(String commitMsg) {
        this.commitMsg = commitMsg;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    private String author;
    private String email;
    private Date dateOfCommit;
    private String commitMsg;
    private String commitId;

}
