package org.aperteworkflow.service;

import org.aperteworkflow.bpm.graph.GraphElement;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.util.eventbus.EventBusManager;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface AperteWorkflowProcessService {

    ProcessInstance createProcessInstance(ProcessDefinitionConfig config,
                                          String externalKey,
                                          UserData user,
                                          String description,
                                          String keyword,
                                          String source);
    ProcessInstance getProcessData(String internalId);
    boolean isProcessRunning(String internalId);
    void saveProcessInstance(ProcessInstance processInstance);
    Collection<ProcessQueue> getUserAvailableQueues(UserData user);
    boolean isProcessOwnedByUser(ProcessInstance processInstance, UserData user);
    BpmTask assignTaskFromQueue(ProcessQueue q, UserData user);
    BpmTask assignTaskFromQueue(ProcessQueue q, BpmTask task, UserData user);
    void assignTaskToUser(String taskId, UserData user);
    BpmTask getTaskData(String taskExecutionId, String taskName);
    BpmTask getTaskData(String taskId);
    List<BpmTask> findUserTasks(ProcessInstance processInstance, UserData user);
    List<BpmTask> findUserTasks(Integer offset, Integer limit, UserData user);
    List<BpmTask> findProcessTasks(ProcessInstance pi, UserData user);
    List<BpmTask> findProcessTasks(ProcessInstance pi, UserData user, Set<String> taskNames);
    Integer getRecentTasksCount(Calendar minDate, UserData user);
    Collection<BpmTask> getAllTasks(UserData user);
    BpmTask performAction(ProcessStateAction action, BpmTask bpmTask, UserData user);
    List<String> getOutgoingTransitionNames(String executionId);
    UserData getSubstitutingUser(UserData user);
    List<String> getOutgoingTransitionDestinationNames(String executionId);
    void adminCancelProcessInstance(ProcessInstance pi);
    void adminReassignProcessTask(ProcessInstance pi, BpmTask bpmTask, UserData user);
    void adminCompleteTask(ProcessInstance pi, BpmTask bpmTask, ProcessStateAction action);
    List<GraphElement> getProcessHistory(ProcessInstance pi);

    void deployProcessDefinition(
            ProcessDefinitionConfig cfg,
            ProcessQueueConfig[] queues,
            byte[] processMapDefinition,
            byte[] processMapImageStream,
            byte[] logo);

    void deployProcessDefinition(byte[] cfgXmlFile, byte[] queueXmlFile,
                                 byte[] processMapDefinition, byte[] processMapImageStream, byte[] logo);
}
