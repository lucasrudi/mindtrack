package com.mindtrack.ai.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Builds AI system prompts from user data and generates context fingerprints
 * for cache invalidation.
 */
@Service
public class ContextBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ContextBuilder.class);
    private static final int MOOD_HISTORY_DAYS = 7;
    private static final int INTERVIEW_RECENCY_DAYS = 30;

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates the context builder.
     *
     * @param jdbcTemplate the JDBC template for raw queries
     */
    public ContextBuilder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Builds a system prompt with the user's recent data context.
     *
     * @param userId the user ID
     * @return the system prompt string
     */
    public String buildSystemPrompt(Long userId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a supportive AI mental health coach for the MindTrack app. ");
        prompt.append("Be empathetic, encouraging, and evidence-based. ");
        prompt.append("Never provide medical diagnoses or medication advice. ");
        prompt.append("Always recommend consulting a professional for serious concerns.\n\n");

        appendMoodContext(prompt, userId);
        appendActivityContext(prompt, userId);
        appendGoalContext(prompt, userId);
        appendInterviewContext(prompt, userId);

        return prompt.toString();
    }

    /**
     * Generates a fingerprint hash of the user's current data state.
     * Changes in the fingerprint indicate the cached response should be invalidated.
     *
     * @param userId the user ID
     * @return SHA-256 hex string
     */
    public String generateFingerprint(Long userId) {
        StringBuilder state = new StringBuilder();

        // Latest mood from journal
        Integer latestMood = queryLatestMood(userId);
        state.append("mood:").append(latestMood).append(";");

        // Active goals count
        int activeGoals = queryActiveGoalsCount(userId);
        state.append("goals:").append(activeGoals).append(";");

        // Recent activity completion count (last 7 days)
        int recentCompletions = queryRecentCompletions(userId);
        state.append("completions:").append(recentCompletions).append(";");

        // Latest interview date
        String latestInterview = queryLatestInterviewDate(userId);
        state.append("interview:").append(latestInterview).append(";");

        return sha256(state.toString());
    }

    private void appendMoodContext(StringBuilder prompt, Long userId) {
        try {
            List<Map<String, Object>> moods = jdbcTemplate.queryForList(
                    "SELECT entry_date, mood FROM journal_entries "
                            + "WHERE user_id = ? AND mood IS NOT NULL "
                            + "AND entry_date >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY) "
                            + "ORDER BY entry_date DESC",
                    userId, MOOD_HISTORY_DAYS);

            if (!moods.isEmpty()) {
                prompt.append("RECENT MOOD DATA (last 7 days):\n");
                for (Map<String, Object> row : moods) {
                    prompt.append("- ").append(row.get("entry_date"))
                            .append(": mood ").append(row.get("mood")).append("/10\n");
                }
                prompt.append("\n");
            }
        } catch (Exception e) {
            LOG.debug("Could not load mood context for user={}: {}", userId, e.getMessage());
        }
    }

    private void appendActivityContext(StringBuilder prompt, Long userId) {
        try {
            List<Map<String, Object>> activities = jdbcTemplate.queryForList(
                    "SELECT a.name, COUNT(al.id) AS completed_count "
                            + "FROM activities a LEFT JOIN activity_logs al "
                            + "ON a.id = al.activity_id AND al.completed = TRUE "
                            + "AND al.log_date >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY) "
                            + "WHERE a.user_id = ? AND a.active = TRUE "
                            + "GROUP BY a.id, a.name",
                    MOOD_HISTORY_DAYS, userId);

            if (!activities.isEmpty()) {
                prompt.append("ACTIVE ACTIVITIES (last 7 days completions):\n");
                for (Map<String, Object> row : activities) {
                    prompt.append("- ").append(row.get("name"))
                            .append(": completed ").append(row.get("completed_count"))
                            .append(" times\n");
                }
                prompt.append("\n");
            }
        } catch (Exception e) {
            LOG.debug("Could not load activity context for user={}: {}", userId, e.getMessage());
        }
    }

    private void appendGoalContext(StringBuilder prompt, Long userId) {
        try {
            List<Map<String, Object>> goals = jdbcTemplate.queryForList(
                    "SELECT g.title, g.status, COUNT(m.id) AS total_milestones, "
                            + "SUM(CASE WHEN m.completed_at IS NOT NULL THEN 1 ELSE 0 END) "
                            + "AS completed_milestones "
                            + "FROM goals g LEFT JOIN milestones m ON g.id = m.goal_id "
                            + "WHERE g.user_id = ? AND g.status IN ('NOT_STARTED', 'IN_PROGRESS') "
                            + "GROUP BY g.id, g.title, g.status",
                    userId);

            if (!goals.isEmpty()) {
                prompt.append("ACTIVE GOALS:\n");
                for (Map<String, Object> row : goals) {
                    prompt.append("- ").append(row.get("title"))
                            .append(" (").append(row.get("status")).append(")")
                            .append(": ").append(row.get("completed_milestones"))
                            .append("/").append(row.get("total_milestones"))
                            .append(" milestones done\n");
                }
                prompt.append("\n");
            }
        } catch (Exception e) {
            LOG.debug("Could not load goal context for user={}: {}", userId, e.getMessage());
        }
    }

    private void appendInterviewContext(StringBuilder prompt, Long userId) {
        try {
            List<Map<String, Object>> interviews = jdbcTemplate.queryForList(
                    "SELECT interview_date, recommendations, mood_after "
                            + "FROM interviews WHERE user_id = ? "
                            + "AND interview_date >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY) "
                            + "ORDER BY interview_date DESC LIMIT 1",
                    userId, INTERVIEW_RECENCY_DAYS);

            if (!interviews.isEmpty()) {
                Map<String, Object> latest = interviews.get(0);
                prompt.append("LATEST INTERVIEW (").append(latest.get("interview_date")).append("):\n");
                if (latest.get("recommendations") != null) {
                    prompt.append("- Recommendations: ").append(latest.get("recommendations")).append("\n");
                }
                if (latest.get("mood_after") != null) {
                    prompt.append("- Post-session mood: ").append(latest.get("mood_after")).append("/10\n");
                }
                prompt.append("\n");
            }
        } catch (Exception e) {
            LOG.debug("Could not load interview context for user={}: {}", userId, e.getMessage());
        }
    }

    private Integer queryLatestMood(Long userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT mood FROM journal_entries WHERE user_id = ? "
                            + "AND mood IS NOT NULL ORDER BY entry_date DESC LIMIT 1",
                    Integer.class, userId);
        } catch (Exception e) {
            return null;
        }
    }

    private int queryActiveGoalsCount(Long userId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM goals WHERE user_id = ? "
                            + "AND status IN ('NOT_STARTED', 'IN_PROGRESS')",
                    Integer.class, userId);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private int queryRecentCompletions(Long userId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM activity_logs al "
                            + "JOIN activities a ON al.activity_id = a.id "
                            + "WHERE a.user_id = ? AND al.completed = TRUE "
                            + "AND al.log_date >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY)",
                    Integer.class, userId, MOOD_HISTORY_DAYS);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private String queryLatestInterviewDate(Long userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT MAX(interview_date) FROM interviews WHERE user_id = ?",
                    String.class, userId);
        } catch (Exception e) {
            return "none";
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
