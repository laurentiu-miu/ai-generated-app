package com.example.fileimporter.service;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.UUID;

@Component
public class SpringBatchImportJobLauncher implements ImportJobLauncher {
    private final JobLauncher launcher;
    private final Job job;

    public SpringBatchImportJobLauncher(@Qualifier("importBatchJobLauncher") JobLauncher launcher,
                                        @Qualifier("fileImportJob") Job job) {
        this.launcher = launcher;
        this.job = job;
    }

    @Override
    public void launch(UUID importId, Path filePath) throws Exception {
        launcher.run(job, new JobParametersBuilder()
                .addString("importId", importId.toString())
                .addString("storedFilePath", filePath.toAbsolutePath().normalize().toString())
                .addLong("unique", System.nanoTime())
                .toJobParameters());
    }
}
