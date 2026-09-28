# Master Learning System — AI Teacher Product Requirements

## Product intent
AI Teacher is a core learning product, not a generic chatbot. It must behave like an adaptive human-style teacher powered by AI and teach students across subjects while making difficult concepts easy to understand.

## Core teaching promise
- explain concepts step by step in simple language;
- adapt explanations to demonstrated understanding;
- switch explanation strategy when the student is confused;
- use real-life examples and familiar analogies;
- use diagrams, illustrations, animated explanations and interactive visual models when they improve understanding;
- teach through voice/audio as well as text;
- accept student voice questions and answers where browser/device support exists;
- ask understanding checks instead of only delivering information;
- provide guided practice and hints before revealing answers;
- create concise summaries and study notes;
- revisit prerequisites when foundational knowledge is missing;
- stay grounded in active lesson/course context when course material is available;
- support Gujarati and English initially, with architecture suitable for more languages.

## Subject coverage
The architecture must support a growing subject catalog, including at minimum:
- Mathematics
- Science
- Physics
- Chemistry
- Biology
- Social Science
- English
- Computer Science

The subject must be passed as structured context to the AI teaching engine so teaching strategy can vary by discipline.

## Teaching experience
The preferred lesson flow is:

`INTRO -> EXPLAIN -> VISUAL/EXAMPLE -> CHECK -> PRACTICE -> RECAP`

The engine may branch or repeat phases when the learner needs remediation. A student saying "I don't understand" must cause simplification, a new analogy, a different visual, or a prerequisite explanation rather than merely repeating the same paragraph.

## Visual teaching modes
The UI and AI response contract should support visual modes such as:
- `TEACHER_AVATAR`
- `DIAGRAM_2D`
- `ANIMATION`
- `REAL_LIFE_SCENE`
- `INTERACTIVE_MODEL`
- `DIGITAL_BOARD`
- `SUMMARY_BOARD`
- `INTERACTIVE_PROMPT`

Visual output must be meaningful to the active concept. The long-term implementation should support generated illustrations, animation sequences, interactive simulations/3D models where appropriate, and synchronized narration rather than decorative motion.

## Audio and voice
- teacher narration / text-to-speech;
- student voice input;
- language-aware speech settings;
- synchronized visual and spoken explanation where the platform supports it.

## Adaptive intelligence
The AI Teacher should eventually consume current course and lesson context, subject and topic, learner level/grade, prior responses, understanding checks, known strengths and weak areas, prerequisite knowledge, preferred language, preferred explanation style, and recent learning history.

These signals should influence the next explanation, example, visual, question difficulty and practice intervention.

## Safety and quality
The AI Teacher must not invent course-specific facts when grounded source material is available. It should distinguish explanation from uncertainty, avoid unsafe instructions, and keep student data within authorized learning context.

## Current implementation foundation
The current platform already provides phased AI teaching, OpenAI-backed responses with deterministic fallback, Gujarati/English voice interaction, quota controls, and a visual-mode response field. New work should extend this foundation rather than create a parallel AI Teacher system.

## Definition of done for the full AI Teacher vision
The AI Teacher vision is not considered complete until a student can choose a subject, start a lesson, receive clear adaptive explanations, see useful concept visuals/animations, hear the teacher, ask by voice or text, connect concepts to real life, demonstrate understanding, practise with guidance, receive a recap, and continue through an appropriate personalized next step.