package com.mindtrack.analytics.service;

import com.mindtrack.analytics.dto.ContentItemResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Registry of curated content items for dashboard widgets, keyed by goal category.
 */
@Component
public class ContentRegistry {

    private static final Map<String, List<ContentItemResponse>> CATEGORY_CONTENT = Map.of(
            "Mental Health", List.of(
                new ContentItemResponse("TIP", "Practice the 5-4-3-2-1 Grounding Technique",
                    "When feeling anxious, name 5 things you see, 4 you can touch, 3 you hear, 2 you smell,"
                        + " 1 you taste. This anchors you in the present moment.",
                    "Mental Health", null, null, null),
                new ContentItemResponse("RESOURCE", "Understanding CBT for Anxiety",
                    "A gentle introduction to Cognitive Behavioral Therapy techniques"
                        + " you can practice on your own.",
                    "Mental Health", "https://www.youtube.com/watch?v=9c_Bv_FBE-c", "YOUTUBE", "YouTube"),
                new ContentItemResponse("THERAPIST_TIP", "Journaling Prompt for Today",
                    "Write about a recent moment when you felt calm or content. What was happening?"
                        + " What were you doing? Use this as a reference point.",
                    "Mental Health", null, null, null)
            ),
            "Wellness", List.of(
                new ContentItemResponse("TIP", "Start Your Day with Gratitude",
                    "Write down 3 specific things you are grateful for each morning."
                        + " Research shows this rewires the brain toward positive thinking over time.",
                    "Wellness", null, null, null),
                new ContentItemResponse("RESOURCE", "The Wellness Wheel",
                    "A practical framework for assessing all dimensions of your well-being: physical,"
                        + " emotional, intellectual, social, spiritual, and occupational.",
                    "Wellness", null, "BOOK", "Book Review"),
                new ContentItemResponse("THERAPIST_TIP", "Body Scan Meditation",
                    "Spend 5 minutes slowly scanning from head to toe, noticing sensations without judgment."
                        + " This builds body awareness and reduces tension.",
                    "Wellness", null, null, null)
            ),
            "Sleep", List.of(
                new ContentItemResponse("TIP", "The 10-3-2-1-0 Sleep Rule",
                    "No caffeine 10 hrs before bed. No alcohol 3 hrs before."
                        + " No more food or water 2 hrs before. No screens 1 hr before. Hit snooze 0 times.",
                    "Sleep", null, null, null),
                new ContentItemResponse("RESOURCE", "Why We Sleep by Matthew Walker",
                    "A landmark book that explains the science of sleep and its profound effects"
                        + " on mental and physical health.",
                    "Sleep", null, "BOOK", "Book Review"),
                new ContentItemResponse("THERAPIST_TIP", "Progressive Muscle Relaxation",
                    "Before sleep: tense each muscle group for 5 seconds, then release."
                        + " Start at your feet and work up to your face. Promotes deep physical relaxation.",
                    "Sleep", null, null, null)
            ),
            "Fitness", List.of(
                new ContentItemResponse("TIP", "The 2-Minute Rule for Exercise",
                    "If you do not feel like working out, commit to just 2 minutes."
                        + " Most people continue once they start. Momentum is the hardest part.",
                    "Fitness", null, null, null),
                new ContentItemResponse("RESOURCE", "Atomic Habits by James Clear",
                    "The definitive guide to building lasting habits through small,"
                        + " incremental changes that compound over time.",
                    "Fitness", null, "BOOK", "Book Review"),
                new ContentItemResponse("THERAPIST_TIP", "Exercise as Emotional Regulation",
                    "Physical activity reduces cortisol and releases endorphins."
                        + " Even a 20-minute walk can shift your emotional state significantly.",
                    "Fitness", null, null, null)
            )
    );

    private static final List<ContentItemResponse> DEFAULT_CONTENT = List.of(
        new ContentItemResponse("TIP", "One Small Step Today",
            "Progress is made one small, intentional action at a time."
                + " What is one thing you can do today that your future self will thank you for?",
            "General", null, null, null),
        new ContentItemResponse("RESOURCE", "Feeling Good by David D. Burns",
            "A classic evidence-based book on overcoming negative thinking"
                + " and building emotional resilience using CBT techniques.",
            "General", null, "BOOK", "Book Review"),
        new ContentItemResponse("THERAPIST_TIP", "Self-Compassion Practice",
            "When you are struggling, ask: what would I say to a close friend in this situation?"
                + " Then offer yourself the same kindness.",
            "General", null, null, null),
        new ContentItemResponse("WELLBEING_INDICATOR", "Mood Patterns This Week",
            "Tracking your mood consistently helps you identify patterns and triggers."
                + " Even a few data points reveal meaningful trends.",
            "General", null, null, null)
    );

    private static final List<ContentItemResponse> ALL_TIPS;

    static {
        ALL_TIPS = new ArrayList<>();
        CATEGORY_CONTENT.values().forEach(items ->
                items.stream()
                        .filter(i -> "TIP".equals(i.getType()))
                        .forEach(ALL_TIPS::add)
        );
        DEFAULT_CONTENT.stream()
                .filter(i -> "TIP".equals(i.getType()))
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
            List<ContentItemResponse> items = CATEGORY_CONTENT.get(category);
            if (items != null) {
                items.stream()
                        .filter(i -> !"TIP".equals(i.getType()))
                        .forEach(result::add);
            }
        }

        // Fill with defaults if needed
        if (result.size() < 2) {
            DEFAULT_CONTENT.forEach(result::add);
        }

        return result.stream().distinct().limit(maxItems).toList();
    }
}
