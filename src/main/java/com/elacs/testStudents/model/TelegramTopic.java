package com.elacs.testStudents.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A chat or forum topic that should be auto-marked as read.
 * topicId = 0 means the entire chat (no forum topic filtering).
 * topicId > 0 means the message_thread_id of a forum topic.
 */
@Data
@Entity
@Table(name = "telegram_topic")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelegramTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Telegram chat ID (negative for groups/channels, e.g. -1001234567890) */
    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    /**
     * Forum topic message_thread_id (= ID of the first message in the thread).
     * Use 0 to mark the entire chat as read instead of a specific topic.
     */
    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "chat_title", length = 255)
    private String chatTitle;

    @Column(name = "topic_title", length = 255)
    private String topicTitle;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
