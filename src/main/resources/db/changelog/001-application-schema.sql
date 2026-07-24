--liquibase formatted sql

--changeset file-importer:001
CREATE TABLE parent (
    id UUID NOT NULL,
    external_key VARCHAR(255),
    display_name VARCHAR(255) NOT NULL,
    dynamic_properties JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_parent PRIMARY KEY (id),
    CONSTRAINT uk_parent_external_key UNIQUE (external_key),
    CONSTRAINT ck_parent_display_name_not_blank CHECK (btrim(display_name) <> ''),
    CONSTRAINT ck_parent_dynamic_properties_object CHECK (jsonb_typeof(dynamic_properties) = 'object')
);

CREATE INDEX idx_parent_display_name ON parent (lower(display_name), id);

CREATE TABLE child (
    id UUID NOT NULL,
    parent_id UUID NOT NULL,
    external_key VARCHAR(255),
    display_name VARCHAR(255) NOT NULL,
    dynamic_properties JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_child PRIMARY KEY (id),
    CONSTRAINT fk_child_parent FOREIGN KEY (parent_id) REFERENCES parent (id) ON DELETE RESTRICT,
    CONSTRAINT uk_child_external_key UNIQUE (external_key),
    CONSTRAINT ck_child_display_name_not_blank CHECK (btrim(display_name) <> ''),
    CONSTRAINT ck_child_dynamic_properties_object CHECK (jsonb_typeof(dynamic_properties) = 'object')
);

CREATE INDEX idx_child_parent_id ON child (parent_id);
CREATE INDEX idx_child_parent_display_name ON child (parent_id, lower(display_name), id);

CREATE TABLE file_import (
    id UUID NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_rows BIGINT NOT NULL DEFAULT 0,
    processed_rows BIGINT NOT NULL DEFAULT 0,
    successful_rows BIGINT NOT NULL DEFAULT 0,
    failed_rows BIGINT NOT NULL DEFAULT 0,
    skipped_rows BIGINT NOT NULL DEFAULT 0,
    error_message TEXT,
    batch_job_execution_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_file_import PRIMARY KEY (id),
    CONSTRAINT uk_file_import_stored_filename UNIQUE (stored_filename),
    CONSTRAINT ck_file_import_status CHECK (status IN ('UPLOADED', 'QUEUED', 'RUNNING', 'COMPLETED', 'COMPLETED_WITH_ERRORS', 'FAILED')),
    CONSTRAINT ck_file_import_counters_nonnegative CHECK (total_rows >= 0 AND processed_rows >= 0 AND successful_rows >= 0 AND failed_rows >= 0 AND skipped_rows >= 0),
    CONSTRAINT ck_file_import_processed_total CHECK (processed_rows <= total_rows),
    CONSTRAINT ck_file_import_counter_sum CHECK (processed_rows = successful_rows + failed_rows + skipped_rows)
);

CREATE INDEX idx_file_import_created_at ON file_import (created_at DESC, id DESC);

CREATE TABLE file_import_error (
    id UUID NOT NULL,
    import_id UUID NOT NULL,
    line_number BIGINT NOT NULL,
    error_code VARCHAR(64) NOT NULL,
    message TEXT NOT NULL,
    CONSTRAINT pk_file_import_error PRIMARY KEY (id),
    CONSTRAINT fk_file_import_error_import FOREIGN KEY (import_id) REFERENCES file_import (id) ON DELETE CASCADE,
    CONSTRAINT ck_file_import_error_line CHECK (line_number > 0)
);

CREATE INDEX idx_file_import_error_import_line ON file_import_error (import_id, line_number, id);
