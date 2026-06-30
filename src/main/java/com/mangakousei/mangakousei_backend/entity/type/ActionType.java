package com.mangakousei.mangakousei_backend.entity.type;

public enum ActionType {

    // ── review ──────────────────────────────────────────────────────────────
    REVIEW_APPROVED("review"),
    REVIEW_REVISION("review"),
    ADMIN_REVIEW_APPROVED("review"),
    ADMIN_REVIEW_REVISION("review"),

    // ── submission ──────────────────────────────────────────────────────────
    SUBMIT_PAGES("submission"),
    RESUBMIT_PAGES("submission"),
    SUBMIT_CHAPTER_TO_ADMIN("submission"),
    SUBMIT_TASK("submission"),
    RESUBMIT_TASK("submission"),

    // ── progress ────────────────────────────────────────────────────────────
    CREATE_PAGE_DEADLINE("progress"),
    UPDATE_PAGE_DEADLINE("progress"),
    CREATE_CHAPTER("progress"),

    // ── proposal ────────────────────────────────────────────────────────────
    CREATE_PROPOSAL("proposal"),
    REVIEW_PROPOSAL("proposal"),
    APPROVE_SERIES("proposal"),

    // ── account ─────────────────────────────────────────────────────────────
    LOGIN("account"),
    UPDATE_PROFILE("account"),
    CHANGE_PASSWORD("account"),
    CREATE_USER("account"),
    ASSIGN_TANTOU("account"),
    REMOVE_ASSIGNMENT("account"),
    CREATE_PAYMENT("account"),

    // ── system ──────────────────────────────────────────────────────────────
    UPDATE_SETTINGS("system");

    private final String category;

    ActionType(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
}