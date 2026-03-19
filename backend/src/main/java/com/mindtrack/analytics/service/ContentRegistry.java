package com.mindtrack.analytics.service;

import com.mindtrack.analytics.dto.ContentItemResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Registry of curated content items for dashboard widgets, keyed by goal category.
 */
@Component
public class ContentRegistry {

    private static final Map<ContentCategory, List<ContentItemResponse>> CATEGORY_CONTENT = Map.of(
            ContentCategory.MENTAL_HEALTH, List.of(
                tip("Practice the 5-4-3-2-1 Grounding Technique",
                    "When feeling anxious, name 5 things you see, 4 you can touch, 3 you hear, 2 you smell,"
                        + " 1 you taste. This anchors you in the present moment.", ContentCategory.MENTAL_HEALTH),
                resource("Understanding CBT for Anxiety",
                    "A gentle introduction to Cognitive Behavioral Therapy techniques"
                        + " you can practice on your own.", ContentCategory.MENTAL_HEALTH,
                    "https://www.youtube.com/watch?v=9c_Bv_FBE-c", ContentSourceType.YOUTUBE,
                    ContentSourceLabel.YOUTUBE),
                therapistTip("Journaling Prompt for Today",
                    "Write about a recent moment when you felt calm or content. What was happening?"
                        + " What were you doing? Use this as a reference point.",
                    ContentCategory.MENTAL_HEALTH)
            ),
            ContentCategory.WELLNESS, List.of(
                tip("Start Your Day with Gratitude",
                    "Write down 3 specific things you are grateful for each morning."
                        + " Research shows this rewires the brain toward positive thinking over time.",
                    ContentCategory.WELLNESS),
                resource("The Wellness Wheel",
                    "A practical framework for assessing all dimensions of your well-being: physical,"
                        + " emotional, intellectual, social, spiritual, and occupational.",
                    ContentCategory.WELLNESS, null, ContentSourceType.BOOK, ContentSourceLabel.BOOK_REVIEW),
                therapistTip("Body Scan Meditation",
                    "Spend 5 minutes slowly scanning from head to toe, noticing sensations without judgment."
                        + " This builds body awareness and reduces tension.", ContentCategory.WELLNESS)
            ),
            ContentCategory.SLEEP, List.of(
                tip("The 10-3-2-1-0 Sleep Rule",
                    "No caffeine 10 hrs before bed. No alcohol 3 hrs before."
                        + " No more food or water 2 hrs before. No screens 1 hr before. Hit snooze 0 times.",
                    ContentCategory.SLEEP),
                resource("Why We Sleep by Matthew Walker",
                    "A landmark book that explains the science of sleep and its profound effects"
                        + " on mental and physical health.",
                    ContentCategory.SLEEP, null, ContentSourceType.BOOK, ContentSourceLabel.BOOK_REVIEW),
                therapistTip("Progressive Muscle Relaxation",
                    "Before sleep: tense each muscle group for 5 seconds, then release."
                        + " Start at your feet and work up to your face. Promotes deep physical relaxation.",
                    ContentCategory.SLEEP)
            ),
            ContentCategory.FITNESS, List.of(
                tip("The 2-Minute Rule for Exercise",
                    "If you do not feel like working out, commit to just 2 minutes."
                        + " Most people continue once they start. Momentum is the hardest part.",
                    ContentCategory.FITNESS),
                resource("Atomic Habits by James Clear",
                    "The definitive guide to building lasting habits through small,"
                        + " incremental changes that compound over time.",
                    ContentCategory.FITNESS, null, ContentSourceType.BOOK, ContentSourceLabel.BOOK_REVIEW),
                therapistTip("Exercise as Emotional Regulation",
                    "Physical activity reduces cortisol and releases endorphins."
                        + " Even a 20-minute walk can shift your emotional state significantly.",
                    ContentCategory.FITNESS)
            )
    );

    private static final List<ContentItemResponse> DEFAULT_CONTENT = List.of(
            tip("One Small Step Today",
                "Progress is made one small, intentional action at a time."
                    + " What is one thing you can do today that your future self will thank you for?",
                ContentCategory.GENERAL),
            resource("Feeling Good by David D. Burns",
                "A classic evidence-based book on overcoming negative thinking"
                    + " and building emotional resilience using CBT techniques.",
                ContentCategory.GENERAL, null, ContentSourceType.BOOK, ContentSourceLabel.BOOK_REVIEW),
            therapistTip("Self-Compassion Practice",
                "When you are struggling, ask: what would I say to a close friend in this situation?"
                    + " Then offer yourself the same kindness.",
                ContentCategory.GENERAL),
            wellbeingIndicator("Mood Patterns This Week",
                "Tracking your mood consistently helps you identify patterns and triggers."
                    + " Even a few data points reveal meaningful trends.",
                ContentCategory.GENERAL)
    );

    private static final List<ContentItemResponse> ALL_TIPS;

    static {
        ALL_TIPS = new ArrayList<>();
        CATEGORY_CONTENT.values().forEach(items ->
                items.stream()
                        .filter(ContentRegistry::isTip)
                        .forEach(ALL_TIPS::add)
        );
        DEFAULT_CONTENT.stream()
                .filter(ContentRegistry::isTip)
                .forEach(ALL_TIPS::add);
    }

    /**
     * Returns a personalised list of content items based on the user's goal categories.
     *
     * @param categories active goal categories for the current user
     * @param maxItems   maximum number of items to return
     * @return curated content list, at most {@code maxItems} long
     */
    public List<ContentItemResponse> getContent(Set<String> categories, int maxItems) {
        List<ContentItemResponse> result = new ArrayList<>();

        // Add daily tip (rotated by day of year)
        if (!ALL_TIPS.isEmpty()) {
            int dayIndex = LocalDate.now().getDayOfYear() % ALL_TIPS.size();
            result.add(ALL_TIPS.get(dayIndex));
        }

        // Add category-specific content
        for (String category : categories) {
            List<ContentItemResponse> items = CATEGORY_CONTENT.get(ContentCategory.fromLabel(category));
            if (items != null) {
                items.stream()
                        .filter(ContentRegistry::isNotTip)
                        .forEach(result::add);
            }
        }

        // Fill with defaults if needed
        if (result.size() < 2) {
            DEFAULT_CONTENT.forEach(result::add);
        }

        return result.stream().distinct().limit(maxItems).toList();
    }

    private static ContentItemResponse tip(String title, String body, ContentCategory category) {
        return item(ContentType.TIP, title, body, category, null, null, null);
    }

    private static ContentItemResponse resource(String title, String body, ContentCategory category,
                                                String url, ContentSourceType sourceType,
                                                ContentSourceLabel sourceLabel) {
        return item(ContentType.RESOURCE, title, body, category, url, sourceType, sourceLabel);
    }

    private static ContentItemResponse therapistTip(String title, String body, ContentCategory category) {
        return item(ContentType.THERAPIST_TIP, title, body, category, null, null, null);
    }

    private static ContentItemResponse wellbeingIndicator(String title, String body, ContentCategory category) {
        return item(ContentType.WELLBEING_INDICATOR, title, body, category, null, null, null);
    }

    private static ContentItemResponse item(ContentType type, String title, String body, ContentCategory category,
                                            String url, ContentSourceType sourceType,
                                            ContentSourceLabel sourceLabel) {
        return new ContentItemResponse(
                type.value,
                title,
                body,
                category.label,
                url,
                sourceType != null ? sourceType.value : null,
                sourceLabel != null ? sourceLabel.value : null
        );
    }

    private static boolean isTip(ContentItemResponse item) {
        return ContentType.TIP.value.equals(item.getType());
    }

    private static boolean isNotTip(ContentItemResponse item) {
        return !isTip(item);
    }

    private enum ContentType {
        TIP("TIP"),
        RESOURCE("RESOURCE"),
        THERAPIST_TIP("THERAPIST_TIP"),
        WELLBEING_INDICATOR("WELLBEING_INDICATOR");

        private final String value;

        ContentType(String value) {
            this.value = value;
        }
    }

    private enum ContentCategory {
        MENTAL_HEALTH("Mental Health"),
        WELLNESS("Wellness"),
        SLEEP("Sleep"),
        FITNESS("Fitness"),
        GENERAL("General");

        private static final Map<String, ContentCategory> BY_LABEL = Arrays.stream(values())
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        category -> category.label,
                        category -> category));

        private final String label;

        ContentCategory(String label) {
            this.label = label;
        }

        private static ContentCategory fromLabel(String label) {
            return BY_LABEL.get(label);
        }
    }

    private enum ContentSourceType {
        BOOK("BOOK"),
        YOUTUBE("YOUTUBE");

        private final String value;

        ContentSourceType(String value) {
            this.value = value;
        }
    }

    private enum ContentSourceLabel {
        BOOK_REVIEW("Book Review"),
        YOUTUBE("YouTube");

        private final String value;

        ContentSourceLabel(String value) {
            this.value = value;
        }
    }
}
