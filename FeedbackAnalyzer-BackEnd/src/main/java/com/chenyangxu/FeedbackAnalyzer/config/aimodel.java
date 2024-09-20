package com.chenyangxu.FeedbackAnalyzer.config;


import com.chenyangxu.FeedbackAnalyzer.pojo.AiMessageFormat;
import com.chenyangxu.FeedbackAnalyzer.service.impl.FunctionCalling2;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.time.Duration.ofSeconds;

@Configuration
public class aimodel {
    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Autowired
    private ApplicationContext applicationContext;

    public interface AiAssistant {
        @SystemMessage("If you receive any message contains HTML tags,like " +
                "<table border=\"1\">\n" +
                "    <tr>\n" +
                "        <th>Feedback</th>\n" +
                "        <th>cos similarity</th>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "        <td><strong>\"rough handling\"</strong></td>\n" +
                "        <td>0.62600267</td>\n" +
                "    </tr>\n" +
                "</table>" +
                "do not try change anything about the message, just return to the original message with html tags.")
        String chat(@MemoryId Object userId, @UserMessage("should not reformat the message") AiMessageFormat aiMessageFormat);
    }

    public interface AiAssistant2 {
        @SystemMessage("you are a summarizer assistant to help people summarize the student's feedback from the course")
        String chat(@MemoryId Object userId, @UserMessage String message);
    }

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(openaiApiKey)
                .modelName("gpt-4o-mini")
                .build();
    }
    @Bean
    public AiAssistant aiAssistant(ChatMemoryProvider chatMemoryProvider) {
        FunctionCalling2 functionCalling2 = applicationContext.getBean(FunctionCalling2.class);
        return AiServices.builder(AiAssistant.class)
                .chatLanguageModel(chatLanguageModel())
                .tools(functionCalling2)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .build();
    }

    @Bean
    public AiAssistant2 aiAssistant2(ChatMemoryProvider chatMemoryProvider) {
        ChatLanguageModel openAiChatModel = OpenAiChatModel.builder()
                .apiKey(openaiApiKey)
                .maxTokens(500)
                .modelName("gpt-4o-mini")
                .build();
        return AiServices.builder(AiAssistant2.class)
                .chatLanguageModel(openAiChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }




}
