package org.changppo.account.batch;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

import static org.changppo.account.batch.job.JobConfig.AUTOMATIC_PAYMENT_JOB;

@Slf4j
@Component
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class AutomaticPaymentExecutionJobRunner extends QuartzJobBean {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier(AUTOMATIC_PAYMENT_JOB)
    private Job AutomaticPaymentExecutionJob;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            Map<String, Object> jobDataMap = context.getMergedJobDataMap();
            LocalDateTime schedulerStartTime = (LocalDateTime) jobDataMap.get("SchedulerStartTime");
            log.info("Scheduler start time: {}", schedulerStartTime);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLocalDateTime("JobStartTime", LocalDateTime.now())
                    .toJobParameters();
            jobLauncher.run(AutomaticPaymentExecutionJob, jobParameters);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
