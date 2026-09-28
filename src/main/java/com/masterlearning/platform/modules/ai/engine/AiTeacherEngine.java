package com.masterlearning.platform.modules.ai.engine;

import com.masterlearning.platform.modules.ai.dto.request.AiTeacherTurnRequest;
import com.masterlearning.platform.modules.ai.dto.response.AiTeacherTurnResponse;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AiTeacherEngine {

    private final AiTeacherLlmClient llmClient;

    public AiTeacherEngine(AiTeacherLlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public AiTeacherTurnResponse teach(AiTeacherTurnRequest request) {
        var llmResponse = llmClient.teach(request);
        if (llmResponse.isPresent()) return llmResponse.get();
        String topic = safe(request == null ? null : request.topic(), "today's topic");
        String subject = safe(request == null ? null : request.subject(), "GENERAL");
        String language = safe(request == null ? null : request.language(), "EN").toUpperCase(Locale.ROOT);
        topic = subject + ": " + topic;
        String phase = safe(request == null ? null : request.phase(), "INTRO").toUpperCase(Locale.ROOT);

        return switch (phase) {
            case "EXPLAIN" -> new AiTeacherTurnResponse(
                    "EXPLAIN",
                    text(language,
                            "ચાલો હવે " + topic + " ને step-by-step સમજીએ. પહેલા તેનો core idea સમજીએ, પછી example અને છેલ્લે quick check કરીશું.",
                            "Now let's understand " + topic + " step by step. First the core idea, then a real-life example, and finally a quick check."),
                    "CONCEPT_FIRST", "DIAGRAM_2D", "EXAMPLE", true,
                    text(language, "આ conceptને તમારી પોતાની ભાષામાં એક lineમાં સમજાવી શકો?", "Can you explain this concept in one sentence in your own words?"),
                    false);
            case "EXAMPLE" -> new AiTeacherTurnResponse(
                    "EXAMPLE",
                    text(language,
                            "હવે " + topic + " ને real-life example સાથે જોઈએ. આસપાસની કોઈ situation સાથે જોડીએ તો concept વધુ સરળ બને છે.",
                            "Let's connect " + topic + " to a real-life situation. A familiar example makes the concept easier to remember."),
                    "REAL_LIFE", "REAL_LIFE_SCENE", "CHECK", true,
                    text(language, "આ exampleમાં સૌથી important idea કયું છે?", "What is the most important idea in this example?"),
                    false);
            case "CHECK" -> new AiTeacherTurnResponse(
                    "CHECK",
                    text(language,
                            "સરસ. હવે એક નાનો understanding check કરીએ. જવાબ આપ્યા પછી હું તમારી સમજ પ્રમાણે આગળનું explanation બદલીશ.",
                            "Good. Let's do a quick understanding check. Your answer will decide how we continue the lesson."),
                    "SOCRATIC", "INTERACTIVE_PROMPT", "PRACTICE", true,
                    text(language, "શું તમે તૈયાર છો? તમારો જવાબ લખો અથવા બોલીને આપો.", "Are you ready? Type your answer or say it aloud."),
                    false);
            case "PRACTICE" -> new AiTeacherTurnResponse(
                    "PRACTICE",
                    text(language,
                            "હવે guided practice કરીએ. પહેલા તમે approach બતાવો; હું તરત final answer નહીં આપું. જરૂર પડશે તો hint આપીશ.",
                            "Now let's practise. Show me your approach first. I will guide you with hints instead of immediately giving the final answer."),
                    "GUIDED_PRACTICE", "DIGITAL_BOARD", "RECAP", true,
                    text(language, "ચાલો, પહેલું step તમે કરો.", "Show me the first step."),
                    false);
            case "RECAP" -> new AiTeacherTurnResponse(
                    "RECAP",
                    text(language,
                            "આજના lessonના ત્રણ key points યાદ રાખો: concept સમજો, તેને real-life situation સાથે જોડો અને પછી practiceથી mastery ચેક કરો.",
                            "Remember three things from today's lesson: understand the concept, connect it to real life, and verify mastery through practice."),
                    "RECAP", "SUMMARY_BOARD", "COMPLETE", false, "", true);
            default -> new AiTeacherTurnResponse(
                    "INTRO",
                    text(language,
                            "નમસ્તે! આજે આપણે " + topic + " શીખીશું. હું પહેલા તમારું prior understanding check કરીશ, પછી concept, real-life example અને practice સાથે આગળ જઈશ.",
                            "Hello! Today we are going to learn " + topic + ". I will first check what you already know, then teach the concept with a real-life example and guided practice."),
                    "WARM_INTRO", "TEACHER_AVATAR", "EXPLAIN", true,
                    text(language, "આ topic વિશે તમને પહેલેથી શું ખબર છે?", "What do you already know about this topic?"),
                    false);
        };
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String text(String language, String gu, String en) {
        return language.startsWith("GU") ? gu : en;
    }
}
