package com.elacs.testStudents.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Singleton row (id=1) storing the TDLib session binlog.
 * TDLib writes td.binlog to a temp directory; we flush it here periodically
 * so the session survives restarts without relying on the filesystem.
 */
@Data
@Entity
@Table(name = "telegram_session")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelegramSession {

    @Id
    private Long id = 1L;

    /** Raw bytes of td.binlog (~10-100 KB) */
    @Column(name = "binlog", columnDefinition = "LONGBLOB")
    private byte[] binlog;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
