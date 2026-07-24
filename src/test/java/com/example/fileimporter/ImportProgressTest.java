package com.example.fileimporter;

import com.example.fileimporter.dto.ImportProgress;
import com.example.fileimporter.model.FileImport;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class ImportProgressTest {
    private final ResourceBundleMessageSource messages = messages();

    @Test
    void derivesPersistedPercentageAndTerminalZeroRowPercentage() {
        FileImport running = new FileImport("data.csv", "one.csv", 10);
        running.markRunning(1);
        running.addResults(4, 0, 0);
        assertThat(ImportProgress.from(running, messages, Locale.ENGLISH).percentage()).isEqualTo(40);

        FileImport empty = new FileImport("empty.csv", "two.csv", 0);
        empty.markCompleted();
        assertThat(ImportProgress.from(empty, messages, Locale.ENGLISH).percentage()).isEqualTo(100);
    }

    private ResourceBundleMessageSource messages() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        return source;
    }
}
