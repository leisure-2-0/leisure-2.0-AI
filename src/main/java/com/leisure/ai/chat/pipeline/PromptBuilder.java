package com.leisure.ai.chat.pipeline;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {
    public Prompt build(String rewrittenQuery, List<ScoredDocument> documents) {
        String context = documents.stream()
                .map(this::formatDocument)
                .collect(Collectors.joining("\n\n"));

        String userMessage = """
                [참고 글]
                %s

                [질문]
                %s
                """.formatted(context, rewrittenQuery);

        return new Prompt(List.of(
                new SystemMessage(PromptTemplates.SYSTEM),
                new UserMessage(userMessage)));
    }

    private String formatDocument(ScoredDocument scoredDocument) {
        var metadata = scoredDocument.document().getMetadata();
        Object title = metadata.get("title");
        Object content = metadata.get("content");
        return "제목: " + title + "\n내용: " + content;
    }
}
