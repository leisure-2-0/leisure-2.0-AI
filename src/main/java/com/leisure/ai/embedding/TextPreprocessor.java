package com.leisure.ai.embedding;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TextPreprocessor {

    // 로직 문서 A-1 5단계 - 제목 + 본문 + 태그명을 하나의 임베딩 텍스트로 조립
    public String assemblePostText(String title, String content, List<String> tags) {
        String cleanedContent = stripHtml(content);
        String tagsText = (tags == null || tags.isEmpty()) ? null : String.join(" ", tags);

        StringBuilder sb = new StringBuilder();
        appendIfNotBlank(sb, title);
        appendIfNotBlank(sb, cleanedContent);
        appendIfNotBlank(sb, tagsText);
        return sb.toString();
    }

    // 로직 문서 A-5 5단계 - 축제는 별도 콘텐츠(본문/태그) 없이 TourAPI에서 온 필드들만 조립
    public String assembleFestivalText(String title, String description, String region, String address) {
        StringBuilder sb = new StringBuilder();
        appendIfNotBlank(sb, title);
        appendIfNotBlank(sb, description);
        appendIfNotBlank(sb, region);
        appendIfNotBlank(sb, address);
        return sb.toString();
    }

    private void appendIfNotBlank(StringBuilder sb, String text) {
        if (text != null && !text.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.append(text);
        }
    }

    // 에디터가 HTML로 저장한 본문에서 태그를 걷어내고 순수 텍스트만 추출
    private String stripHtml(String html) {
        if (html == null) {
            return "";
        }
        return Jsoup.parse(html).text();
    }
}
