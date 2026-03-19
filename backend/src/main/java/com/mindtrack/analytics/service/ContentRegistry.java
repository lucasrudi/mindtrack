package com.mindtrack.analytics.service;

import com.mindtrack.analytics.dto.ContentItemResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Registry of curated content items for dashboard widgets, keyed by goal category.
 */
@Component
public class ContentRegistry {

    private static final List<ContentCategory> CATEGORY_ORDER = List.of(
            ContentCategory.MENTAL_HEALTH,
            ContentCategory.WELLNESS,
            ContentCategory.SLEEP,
            ContentCategory.FITNESS
    );

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
                    ContentCategory.MENTAL_HEALTH),
                video("Why We All Need to Practice Emotional First Aid",
                    "Guy Winch makes a compelling case for practising emotional hygiene - tending to"
                        + " psychological wounds the way we treat physical ones.",
                    ContentCategory.MENTAL_HEALTH, "F2hc2FLOdhI"),
                video("Understanding Anxiety",
                    "Therapy in a Nutshell explains what anxiety is, why it happens,"
                        + " and practical strategies to manage it day-to-day.",
                    ContentCategory.MENTAL_HEALTH, "d9avpnRRXU4"),
                video("How to Meditate",
                    "Headspace guides you through a simple, accessible meditation practice"
                        + " suitable for complete beginners.",
                    ContentCategory.MENTAL_HEALTH, "wuh5B3_y8nY"),
                video("How to Stop Overthinking",
                    "Dr Daniel Amen shares evidence-based strategies for quieting an overactive mind"
                        + " and breaking the overthinking cycle.",
                    ContentCategory.MENTAL_HEALTH, "H9UI5ON-kbM"),
                video("How to Make Stress Your Friend",
                    "Kelly McGonigal reframes stress as something that can strengthen resilience"
                        + " and connection instead of something to fear.",
                    ContentCategory.MENTAL_HEALTH, "RcGyVTAoXEU"),
                video("The Power of Introverts",
                    "Susan Cain explains why quieter minds matter and how solitude can support"
                        + " creativity, energy, and wellbeing.",
                    ContentCategory.MENTAL_HEALTH, "c0KYU2j0TM4")
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
                        + " This builds body awareness and reduces tension.", ContentCategory.WELLNESS),
                video("How to Build a Morning Routine",
                    "A practical guide to designing a morning routine that boosts energy,"
                        + " focus, and overall well-being.",
                    ContentCategory.WELLNESS, "kceUBEEcgn4"),
                video("The Science of Gratitude",
                    "Explores the research behind gratitude practice and how making it a daily habit"
                        + " can rewire the brain for greater positivity.",
                    ContentCategory.WELLNESS, "IanFcCFctBk"),
                video("The Power of Vulnerability",
                    "Brene Brown's iconic TED Talk on embracing vulnerability as the birthplace"
                        + " of connection, creativity, and belonging.",
                    ContentCategory.WELLNESS, "lDGpFEo1oyg"),
                video("The Happy Secret to Better Work",
                    "Shawn Achor shows how happiness can improve productivity, resilience,"
                        + " and performance at work and beyond.",
                    ContentCategory.WELLNESS, "fLJsdqxnZb0"),
                video("The Surprising Science of Happiness",
                    "Dan Gilbert explores why our brains adapt so quickly and how we can build"
                        + " more lasting wellbeing.",
                    ContentCategory.WELLNESS, "4q1dgn_C0AU"),
                video("Your Body Language May Shape Who You Are",
                    "Amy Cuddy shares how posture and body language can influence confidence"
                        + " and stress in everyday life.",
                    ContentCategory.WELLNESS, "Ks-_Mh1QhMc")
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
                    ContentCategory.SLEEP),
                video("Why Do We Sleep?",
                    "TED-Ed explores the science of sleep - what happens in your brain and body"
                        + " each night and why it matters so much for health.",
                    ContentCategory.SLEEP, "nm1TxQj9IsQ"),
                video("How to Fall Asleep Faster",
                    "Practical, evidence-backed tips for winding down quickly and improving"
                        + " your overall sleep quality.",
                    ContentCategory.SLEEP, "t0kACis_dJE"),
                video("Sleep Is Your Superpower",
                    "Matthew Walker explains the life-saving importance of sleep in this"
                        + " powerful TEDx Talk packed with research insights.",
                    ContentCategory.SLEEP, "5MuIMqhT8pM"),
                video("All It Takes Is 10 Mindful Minutes",
                    "Andy Puddicombe shows how a short daily mindfulness practice can improve"
                        + " calm, clarity, and focus.",
                    ContentCategory.SLEEP, "qzR62JJCMBQ"),
                video("Meditation 101: A Beginner's Guide",
                    "A gentle introduction to mindfulness meditation that makes the practice"
                        + " feel approachable for anyone.",
                    ContentCategory.SLEEP, "rqoxYKtEWEc"),
                video("MindUP Program: Mindfulness Improves Academic Achievement",
                    "A classroom mindfulness example showing how regular practice can support"
                        + " attention, emotional regulation, and compassion.",
                    ContentCategory.SLEEP, "10dBXGHwNCK")
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
                    ContentCategory.FITNESS),
                video("Exercise and Mental Health",
                    "NHS-backed guidance on how regular physical activity reduces anxiety,"
                        + " depression, and stress while boosting mood.",
                    ContentCategory.FITNESS, "I-MH7bVGNzM"),
                video("The Mental Health Benefits of Exercise",
                    "A clear breakdown of the science behind exercise as medicine for"
                        + " the mind, covering depression, anxiety, and cognitive function.",
                    ContentCategory.FITNESS, "BHY0FxzoKZE"),
                video("10-Minute Morning Workout for Mental Health",
                    "A quick, accessible morning workout designed to lift mood and set"
                        + " a positive tone for the rest of your day.",
                    ContentCategory.FITNESS, "YQLQoW6BUCA"),
                video("Self-Compassion in Practice",
                    "Kristin Neff's self-compassion message encourages treating yourself"
                        + " with the same kindness you would offer a friend.",
                    ContentCategory.FITNESS, "11U0h0DPu7k"),
                video("Meditation in Public Schools",
                    "A short documentary-style look at how school mindfulness programs can"
                        + " improve mood, behavior, and attention.",
                    ContentCategory.FITNESS, "8G-UnambdUMa")
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
                ContentCategory.GENERAL),
            video("Mental Health Basics",
                "Mayo Clinic provides a clear, accessible overview of mental health - what it means,"
                    + " common conditions, and when to seek professional support.",
                ContentCategory.GENERAL, "hd-PCMKR0pY"),
            video("Self-Compassion Meditation",
                "A guided meditation from the Mindful Awareness Research Center to cultivate"
                    + " kindness and compassion toward yourself.",
                ContentCategory.GENERAL, "4d9SjbbfjS0"),
            video("Mindfulness for Beginners",
                "A beginner-friendly guided mindfulness session to help you slow down,"
                    + " notice the present moment, and reduce everyday stress.",
                ContentCategory.GENERAL, "RVA2XlmMQOs")
    );

    private static final List<ContentItemResponse> ALL_TIPS;
    private static final List<ContentItemResponse> ALL_VIDEOS;

    static {
        ALL_TIPS = new ArrayList<>();
        ALL_VIDEOS = new ArrayList<>();
        CATEGORY_ORDER.forEach(category -> {
            List<ContentItemResponse> items = CATEGORY_CONTENT.get(category);
            items.stream()
                    .filter(ContentRegistry::isTip)
                    .forEach(ALL_TIPS::add);
            items.stream()
                    .filter(ContentRegistry::isVideo)
                    .forEach(ALL_VIDEOS::add);
        });
        DEFAULT_CONTENT.stream()
                .filter(ContentRegistry::isTip)
                .forEach(ALL_TIPS::add);
        DEFAULT_CONTENT.stream()
                .filter(ContentRegistry::isVideo)
                .forEach(ALL_VIDEOS::add);
    }

    private final VideoAvailabilityChecker videoAvailabilityChecker;

    public ContentRegistry() {
        this(new OEmbedVideoAvailabilityChecker());
    }

    ContentRegistry(VideoAvailabilityChecker videoAvailabilityChecker) {
        this.videoAvailabilityChecker = videoAvailabilityChecker;
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

        // Add category-specific content in a fixed order for deterministic results.
        for (ContentCategory category : CATEGORY_ORDER) {
            if (categories.contains(category.label)) {
                CATEGORY_CONTENT.get(category).stream()
                        .filter(ContentRegistry::isNotTip)
                        .forEach(result::add);
            }
        }

        // Fill with defaults if needed.
        if (result.size() < 2) {
            DEFAULT_CONTENT.forEach(result::add);
        }

        List<ContentItemResponse> uniqueItems = result.stream()
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        replaceUnavailableVideos(uniqueItems);

        return uniqueItems.stream().limit(maxItems).toList();
    }

    private void replaceUnavailableVideos(List<ContentItemResponse> items) {
        Set<String> selectedVideoIds = items.stream()
                .filter(ContentRegistry::isVideo)
                .map(ContentItemResponse::getUrl)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (int index = 0; index < items.size(); index++) {
            ContentItemResponse item = items.get(index);
            if (!isVideo(item) || isPlayable(item.getUrl())) {
                continue;
            }

            ContentItemResponse replacement = findPlayableReplacement(
                    ContentCategory.fromLabel(item.getCategory()), selectedVideoIds);
            if (replacement != null) {
                items.set(index, replacement);
                selectedVideoIds.add(replacement.getUrl());
            }
        }
    }

    private ContentItemResponse findPlayableReplacement(ContentCategory preferredCategory,
                                                        Set<String> selectedVideoIds) {
        for (ContentItemResponse candidate : fallbackCandidates(preferredCategory)) {
            if (selectedVideoIds.contains(candidate.getUrl())) {
                continue;
            }
            if (isPlayable(candidate.getUrl())) {
                return candidate;
            }
        }
        return null;
    }

    private List<ContentItemResponse> fallbackCandidates(ContentCategory preferredCategory) {
        List<ContentItemResponse> candidates = new ArrayList<>();
        if (preferredCategory != null) {
            candidates.addAll(videosForCategory(preferredCategory));
        }
        DEFAULT_CONTENT.stream()
                .filter(ContentRegistry::isVideo)
                .forEach(candidates::add);
        ALL_VIDEOS.stream()
                .filter(candidate -> !ContentCategory.GENERAL.label.equals(candidate.getCategory()))
                .filter(candidate -> preferredCategory == null
                        || !preferredCategory.label.equals(candidate.getCategory()))
                .forEach(candidates::add);
        return candidates;
    }

    private List<ContentItemResponse> videosForCategory(ContentCategory category) {
        List<ContentItemResponse> items = CATEGORY_CONTENT.get(category);
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .filter(ContentRegistry::isVideo)
                .toList();
    }

    private boolean isPlayable(String videoId) {
        return videoId != null && videoAvailabilityChecker.isPlayable(videoId);
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

    private static ContentItemResponse video(String title, String body, ContentCategory category, String videoId) {
        return item(ContentType.VIDEO, title, body, category, videoId, ContentSourceType.YOUTUBE,
                ContentSourceLabel.YOUTUBE);
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

    private static boolean isVideo(ContentItemResponse item) {
        return ContentType.VIDEO.value.equals(item.getType());
    }

    private static boolean isNotTip(ContentItemResponse item) {
        return !isTip(item);
    }

    @FunctionalInterface
    interface VideoAvailabilityChecker {
        boolean isPlayable(String videoId);
    }

    private static final class OEmbedVideoAvailabilityChecker implements VideoAvailabilityChecker {

        private static final String OEMBED_BASE_URL =
                "https://www.youtube.com/oembed?url=%s&format=json";

        private final HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        @Override
        public boolean isPlayable(String videoId) {
            if (videoId == null || videoId.isBlank()) {
                return false;
            }

            String videoUrl = "https://www.youtube.com/watch?v=" + videoId;
            String oembedUrl = String.format(OEMBED_BASE_URL,
                    URLEncoder.encode(videoUrl, StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder(URI.create(oembedUrl))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            try {
                HttpResponse<Void> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.discarding());
                return response.statusCode() == 200;
            } catch (IOException ex) {
                return false;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    private enum ContentType {
        TIP("TIP"),
        RESOURCE("RESOURCE"),
        THERAPIST_TIP("THERAPIST_TIP"),
        WELLBEING_INDICATOR("WELLBEING_INDICATOR"),
        VIDEO("VIDEO");

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
                .collect(Collectors.toUnmodifiableMap(
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
