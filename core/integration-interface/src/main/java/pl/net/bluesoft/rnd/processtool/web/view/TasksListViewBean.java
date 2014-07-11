package pl.net.bluesoft.rnd.processtool.web.view;

import pl.net.bluesoft.rnd.processtool.web.domain.AbstractResultBean;

import java.util.Date;

/**
 * Created by Marcin Król on 2014-05-12.
 */
public abstract class TasksListViewBean extends AbstractResultBean {

    private String queueName;
    private Date taskAssigneDate;
    private Boolean userCanClaim = false;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Boolean getUserCanClaim() {
        return userCanClaim;
    }

    public void setUserCanClaim(Boolean userCanClaim) {
        this.userCanClaim = userCanClaim;
    }

    public Date getTaskAssigneDate() {
        return taskAssigneDate;
    }

    public void setTaskAssigneDate(Date taskAssigneDate) {
        this.taskAssigneDate = taskAssigneDate;
    }
}
