package com.mangakousei.mangakousei_backend.constant;

public final class RealtimeQueues {
    private RealtimeQueues() {}

    /**
     * Chuông thông báo chung (bell icon) cho MỌI loại thông báo trong hệ thống
     */
    public static final String NOTIFICATIONS         = "/queue/notifications";

    /**
     * Tin nhắn chat 1-1 giữa 2 user (mangaka-assistant, tantou-mangaka, hoặc
     * với admin)
     */
    public static final String MESSAGES              = "/queue/messages";

    /**
     * Lời mời cộng tác Mangaka ⇄ Assistant thay đổi trạng thái:
     */
    public static final String ASSIGNMENT_UPDATES    = "/queue/assignment-updates";

    /**
     * Task thay đổi (tạo mới / đổi trạng thái) — dành riêng cho phía MANGAKA xem.
     */
    public static final String TASK_UPDATES          = "/queue/task-updates";

    /**
     * Task thay đổi (tạo mới / đổi trạng thái / được review xong) —
     * dành riêng cho phía ASSISTANT xem.
     */
    public static final String ASSISTANT_TASK_UPDATES = "/queue/assistant-task-updates";

    /**
     * Báo cho Assistant biết 1 task đã bị Mangaka XOÁ hẳn (deleteTask).
     */
    public static final String ASSISTANT_TASK_DELETED = "/queue/assistant-task-deleted";

    /**
     * Bài nộp (TaskSubmission) thay đổi trạng thái — dùng CHUNG cho cả 2 chiều
     */
    public static final String SUBMISSION_UPDATES    = "/queue/submission-updates";
}