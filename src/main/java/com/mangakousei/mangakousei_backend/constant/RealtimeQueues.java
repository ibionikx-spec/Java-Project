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


    public static final String PAGE_DEADLINE_UPDATES = "/queue/page-deadline-updates";

    /**
     * Chapter được TẠO MỚI (Mangaka) hoặc có deadline MỚI (Tantou).
     */
    public static final String CHAPTER_UPDATES = "/queue/chapter-updates";

    /**
     * Chapter chuyển sang trạng thái pending_publish (Tantou gửi lên Admin)
     * hoặc Admin vừa duyệt/yêu cầu sửa xong.
     */
    public static final String ADMIN_CHAPTER_UPDATES = "/queue/admin-chapter-updates";

    /**
     * Báo rằng ảnh của 1 hoặc nhiều trang thuộc 1 PageDeadline vừa thay đổi
     */
    public static final String DEADLINE_PAGES_CHANGED = "/queue/deadline-pages-changed";

    /**
     * Proposal (bản Name) được tạo mới / đổi trạng thái ở bất kỳ bước nào
     * (Mangaka tạo, Tantou duyệt, Admin duyệt, reopen, cancel-approve...).
     */
    public static final String PROPOSAL_UPDATES = "/queue/proposal-updates";
}