package com.example.fileimporter;

import com.example.fileimporter.dto.ImportProgress;
import com.example.fileimporter.model.FileImport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImportProgressTest {
    @Test
    void derivesPersistedPercentageAndTerminalZeroRowPercentage() {
        FileImport running = new FileImport("data.csv", "one.csv", 10);
        running.markRunning(1);
        running.addResults(4, 0, 0);
        assertThat(ImportProgress.from(running).percentage()).isEqualTo(40);

        FileImport empty = new FileImport("empty.csv", "two.csv", 0);
        empty.markCompleted();
        assertThat(ImportProgress.from(empty).percentage()).isEqualTo(100);
    }
}
