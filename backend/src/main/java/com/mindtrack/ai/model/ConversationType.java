package com.mindtrack.ai.model;

/**
 * Defines the type of AI conversation, which determines the max_tokens budget.
 */
public enum ConversationType {

    /**
     * Short check-in via Telegram/WhatsApp. Budget: ~200 tokens.
     */
    QUICK_CHECKIN,

    /**
     * Interactive coaching conversation via web chat. Budget: ~500 tokens.
     */
    COACHING,

    /**
     * Comprehensive post-interview summary or weekly report. Budget: ~1500 tokens.
     */
    SESSION_SUMMARY
}
