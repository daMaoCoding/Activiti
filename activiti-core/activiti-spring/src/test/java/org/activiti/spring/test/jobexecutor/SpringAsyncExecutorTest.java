package org.activiti.spring.test.jobexecutor;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class SpringAsyncExecutorTest {

  @Autowired
  protected ManagementService managementService;

  @Autowired
  protected RuntimeService runtimeService;

  @Autowired
  protected TaskService taskService;

  @Test
  public void testHappyJobExecutorPath() throws Exception {

        ProcessInstance instance = runtimeService.startProcessInstanceByKey("process1");
        assertThat(instance).isNotNull();
        waitForTasksToExpire();

        List<Task> activeTasks = taskService.createTaskQuery().processInstanceId(instance.getId()).list();
        assertThat(activeTasks).isEmpty();
    }

    @Test
    public void testRollbackJobExecutorPath() throws Exception {

        ProcessInstance instance = runtimeService.startProcessInstanceByKey("errorProcess1");
        assertThat(instance).isNotNull();
        waitForTasksToExpire();

        List<Task> activeTasks = taskService.createTaskQuery().processInstanceId(instance.getId()).list();
        assertThat(activeTasks).hasSize(1);
    }

    private void waitForTasksToExpire() throws Exception {
        boolean finished = false;
        int nrOfSleeps = 0;
        while (!finished) {
            long jobCount = managementService.createJobQuery().count();
            long timerCount = managementService.createTimerJobQuery().count();
            if (jobCount == 0 && timerCount == 0) {
                finished = true;
            } else if (nrOfSleeps < 20) {
                nrOfSleeps++;
                Thread.sleep(500L);
            } else {
                finished = true;
            }
        }
    }

}
