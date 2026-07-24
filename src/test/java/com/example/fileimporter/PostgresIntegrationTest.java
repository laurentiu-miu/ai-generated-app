package com.example.fileimporter;

import com.example.fileimporter.model.Child;
import com.example.fileimporter.model.Parent;
import com.example.fileimporter.model.FileImport;
import com.example.fileimporter.repository.ChildRepository;
import com.example.fileimporter.repository.FileImportErrorRepository;
import com.example.fileimporter.repository.FileImportRepository;
import com.example.fileimporter.repository.ParentRepository;
import com.example.fileimporter.service.ImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.time.Duration;

import static org.awaitility.Awaitility.await;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(properties = "app.upload.directory=target/test-uploads")
class PostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired ParentRepository parentRepository;
    @Autowired ChildRepository childRepository;
    @Autowired FileImportRepository fileImportRepository;
    @Autowired FileImportErrorRepository fileImportErrorRepository;
    @Autowired ImportService importService;

    @Test
    void liquibaseOwnsApplicationAndBatchSchema() {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*) from information_schema.tables
                where table_schema='public' and lower(table_name) in
                ('parent','child','file_import','file_import_error','batch_job_instance','batch_step_execution')
                """, Integer.class);
        assertThat(count).isEqualTo(6);
        assertThat(jdbcTemplate.queryForObject("select count(*) from pg_indexes where indexname='idx_child_parent_id'", Integer.class))
                .isEqualTo(1);
    }

    @Test
    @Transactional
    void persistsUuidSevenJsonbAndNestedRelationship() {
        Parent parent = parentRepository.saveAndFlush(new Parent("  Alpha  ", Map.of("active", true)));
        Child child = childRepository.saveAndFlush(new Child(parent, " Beta ", Map.of("score", 10)));

        assertThat(parent.getId().version()).isEqualTo(7);
        assertThat(child.getId().version()).isEqualTo(7);
        assertThat(parent.getDisplayName()).isEqualTo("Alpha");
        assertThat(childRepository.findByIdAndParentId(child.getId(), parent.getId())).isPresent();
        assertThat(parentRepository.findById(parent.getId()).orElseThrow().getDynamicProperties()).containsEntry("active", true);
    }

    @Test
    void importsParentsBeforeChildrenAndKeepsValidRows() {
        String csv = """
                recordType,parentExternalKey,parentDisplayName,childExternalKey,childDisplayName,properties
                C,P-1,,C-1,Child one,"{""score"":10}"
                P,P-1,Parent one,,,"{""active"":true}"
                P,P-1,Duplicate parent,,,{ }
                C,MISSING,,C-2,Orphan,{ }
                """;
        FileImport submitted = importService.submit(new MockMultipartFile(
                "file", "folder\\mixed.csv", "text/csv", csv.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(fileImportRepository.findById(submitted.getId()).orElseThrow().getStatus().isFinished()).isTrue());

        FileImport finished = fileImportRepository.findById(submitted.getId()).orElseThrow();
        assertThat(finished.getStatus()).isEqualTo(FileImport.Status.COMPLETED_WITH_ERRORS);
        assertThat(finished.getTotalRows()).isEqualTo(4);
        assertThat(finished.getSuccessfulRows()).isEqualTo(2);
        assertThat(finished.getFailedRows()).isEqualTo(1);
        assertThat(finished.getSkippedRows()).isEqualTo(1);
        assertThat(finished.getProcessedRows()).isEqualTo(4);
        assertThat(parentRepository.findByExternalKey("P-1")).isPresent();
        assertThat(childRepository.findByExternalKey("C-1")).isPresent();
        assertThat(fileImportErrorRepository.findByFileImportIdOrderByLineNumberAscIdAsc(finished.getId()))
                .extracting(error -> error.getErrorCode().name())
                .containsExactlyInAnyOrder("DUPLICATE_EXTERNAL_KEY", "PARENT_NOT_FOUND");
        assertThat(finished.getOriginalFilename()).isEqualTo("mixed.csv");
    }
}
