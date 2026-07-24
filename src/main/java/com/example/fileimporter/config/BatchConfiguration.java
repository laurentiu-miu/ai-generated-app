package com.example.fileimporter.config;

import com.example.fileimporter.importing.CsvFileSupport;
import com.example.fileimporter.importing.CsvRow;
import com.example.fileimporter.importing.CsvRowProcessor;
import com.example.fileimporter.importing.CsvRowReader;
import com.example.fileimporter.importing.ImportItemWriter;
import com.example.fileimporter.importing.ImportJobListener;
import com.example.fileimporter.importing.ImportSkipListener;
import com.example.fileimporter.importing.ImportStepListener;
import com.example.fileimporter.importing.RowResult;
import com.example.fileimporter.repository.ChildRepository;
import com.example.fileimporter.repository.FileImportErrorRepository;
import com.example.fileimporter.repository.FileImportRepository;
import com.example.fileimporter.repository.ParentRepository;
import com.example.fileimporter.util.JsonObjectMapper;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Path;
import java.util.UUID;

@Configuration
public class BatchConfiguration {
    @Bean("importTaskExecutor")
    TaskExecutor importTaskExecutor(BatchProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("csv-import-");
        executor.setCorePoolSize(properties.corePoolSize());
        executor.setMaxPoolSize(properties.maxPoolSize());
        executor.setQueueCapacity(properties.queueCapacity());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @Bean("importBatchJobLauncher")
    JobLauncher importBatchJobLauncher(JobRepository repository,
                                       @Qualifier("importTaskExecutor") TaskExecutor executor) throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(repository);
        launcher.setTaskExecutor(executor);
        launcher.afterPropertiesSet();
        return launcher;
    }

    @Bean
    Job fileImportJob(JobRepository repository, @Qualifier("parentImportStep") Step parentStep,
                      @Qualifier("childImportStep") Step childStep, ImportJobListener listener) {
        return new JobBuilder("fileImportJob", repository).listener(listener).start(parentStep).next(childStep).build();
    }

    @Bean("parentImportStep")
    Step parentImportStep(JobRepository repository, PlatformTransactionManager transactionManager,
                          @Qualifier("parentCsvReader") ItemReader<CsvRow> reader,
                          @Qualifier("parentCsvProcessor") ItemProcessor<CsvRow, RowResult> processor,
                          @Qualifier("parentCsvWriter") ItemWriter<RowResult> writer, BatchProperties properties,
                          ImportStepListener stepListener, ImportSkipListener skipListener) {
        return new StepBuilder("parentImportStep", repository)
                .<CsvRow, RowResult>chunk(properties.chunkSize(), transactionManager)
                .reader(reader).processor(processor).writer(writer)
                .listener(stepListener).listener(skipListener).build();
    }

    @Bean("childImportStep")
    Step childImportStep(JobRepository repository, PlatformTransactionManager transactionManager,
                         @Qualifier("childCsvReader") ItemReader<CsvRow> reader,
                         @Qualifier("childCsvProcessor") ItemProcessor<CsvRow, RowResult> processor,
                         @Qualifier("childCsvWriter") ItemWriter<RowResult> writer, BatchProperties properties,
                         ImportStepListener stepListener, ImportSkipListener skipListener) {
        return new StepBuilder("childImportStep", repository)
                .<CsvRow, RowResult>chunk(properties.chunkSize(), transactionManager)
                .reader(reader).processor(processor).writer(writer)
                .listener(stepListener).listener(skipListener).build();
    }

    @Bean("parentCsvReader") @StepScope
    CsvRowReader parentCsvReader(@Value("#{jobParameters['storedFilePath']}") String path, CsvFileSupport support) {
        return new CsvRowReader(Path.of(path), "P", support);
    }

    @Bean("childCsvReader") @StepScope
    CsvRowReader childCsvReader(@Value("#{jobParameters['storedFilePath']}") String path, CsvFileSupport support) {
        return new CsvRowReader(Path.of(path), "C", support);
    }

    @Bean("parentCsvProcessor") @StepScope
    CsvRowProcessor parentCsvProcessor(ParentRepository parents, ChildRepository children, JsonObjectMapper mapper) {
        return new CsvRowProcessor("P", parents, children, mapper);
    }

    @Bean("childCsvProcessor") @StepScope
    CsvRowProcessor childCsvProcessor(ParentRepository parents, ChildRepository children, JsonObjectMapper mapper) {
        return new CsvRowProcessor("C", parents, children, mapper);
    }

    @Bean("parentCsvWriter") @StepScope
    ImportItemWriter parentCsvWriter(@Value("#{jobParameters['importId']}") String id,
                                          FileImportRepository imports, FileImportErrorRepository errors,
                                          ParentRepository parents, ChildRepository children) {
        return new ImportItemWriter(UUID.fromString(id), imports, errors, parents, children);
    }

    @Bean("childCsvWriter") @StepScope
    ImportItemWriter childCsvWriter(@Value("#{jobParameters['importId']}") String id,
                                         FileImportRepository imports, FileImportErrorRepository errors,
                                         ParentRepository parents, ChildRepository children) {
        return new ImportItemWriter(UUID.fromString(id), imports, errors, parents, children);
    }
}
