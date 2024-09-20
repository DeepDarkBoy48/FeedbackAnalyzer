package com.chenyangxu.FeedbackAnalyzer.service.impl;

import cn.hutool.json.JSONUtil;
import com.chenyangxu.FeedbackAnalyzer.mapper.AnalyzeMapper;
import com.chenyangxu.FeedbackAnalyzer.pojo.AiMessageForSummary;
import com.chenyangxu.FeedbackAnalyzer.pojo.AiMessageFormat;
import com.chenyangxu.FeedbackAnalyzer.pojo.FunctionCallingEntity.FunctionCallingFormat;
import com.chenyangxu.FeedbackAnalyzer.pojo.sentiment;
import com.chenyangxu.FeedbackAnalyzer.websocket.WebSocketServer;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;
import static java.time.Duration.ofSeconds;

@Service
public class FunctionCalling2 {
    @Value("${openai.api.url}")
    static private String openaiApiUrl;
    @Value("${openai.api.key}")
    private String openaiApiKey;
    @Autowired
    private ChatLanguageModel model;
    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private AnalyzeMapper analyzeMapper;
    @Value("${Elasticsearch.api.url}")
    private String ElasticSearchUrl;
    @Value("${Elasticsearch.api.key}")
    private String ElasticSearchKey;

    @Tool("The tool property of aiMessageFormat must be a sentiment to call this method")
    String sentimentTool(@ToolMemoryId String userid, @P("AiMessageFormat") AiMessageFormat aiMessageFormat) {
        System.out.println(aiMessageFormat.toString());
        String message = aiMessageFormat.getMessage();
        if (message.startsWith("@sentiment")) {
            message = message.substring("@sentiment".length()).trim();
        }
        Integer courseId = Integer.valueOf(aiMessageFormat.getCourseid());
        System.out.println(message);
        enum Sentiment {
            POSITIVE, NEUTRAL, NEGATIVE
        }
        interface SentimentAnalyzer {
            @UserMessage("Analyze sentiment of {{it}},and give the reasons for judgement.For completely negative feedback, identify it as negative. Sometimes, however, people appear to be saying positive things when they are actually saying negative things, and such sentences need to be recognized and identified as negative. But don't be overly sensitive and don't judge it as negative unless you can sense the mockery. For example, yes should be judged as neutral, it is good as positive, and Nice going, Einstein. should be seen as mocking. Interpreted as used when someone does something stupid, sarcastically comparing them to famous physicists.")
            sentiment analyzeSentimentOf(String sentence);
        }
        SentimentAnalyzer sentimentAnalyzer = AiServices.create(SentimentAnalyzer.class, model);
        sentiment sentimentClass = sentimentAnalyzer.analyzeSentimentOf(message);
        String result = sentimentClass.getSentimentType()+"\n"+sentimentClass.getReasonForSentiment();
        HashMap<String, String> map = new HashMap<>();
        map.put(message, result);
        FunctionCallingFormat Rag = new FunctionCallingFormat("Sentiment", map);
        // 使用 Hutool 将 RagEntity 对象转换为 JSON 字符串
        String jsonString = JSONUtil.toJsonStr(Rag);
        interface Converter {
            @SystemMessage("Convert this json {{json}},Example of JSON received：\n" +
                    "{\n" +
                    "\"ToolName\": \"Sentiment\",\n" +
                    "\"data\": {\n" +
                    "\"I am happy\": positive->explain: I am happy to express positive emotions,\n" +
                    "}\n" +
                    "}\n" +
                    "\n" +
                    "Example of output format：\n" +
                    "Rag result\n" +
                    "<table border=\"1\">\n" +
                    "    <tr>\n" +
                    "        <th>Feedback</th>\n" +
                    "        <th>Result</th>\n" +
                    "    </tr>\n" +
                    "    <tr>\n" +
                    "        <td><strong>\"I am happy\"</strong></td>\n" +
                    "        <td><strong>positive</strong><br>explain: I am happy to express positive emotions</td>\n" +
                    "    </tr>\n" +
                    "</table>")
            String convert(@UserMessage @V("json") String json);
        }
        Converter converter = AiServices.create(Converter.class, model);
        String htmlOutput = converter.convert(jsonString);
        System.out.println(htmlOutput);
        return htmlOutput;
    }


    @Tool("The tool property of aiMessageFormat must be a keyword to call this method")
    String KeywordTool(@ToolMemoryId String userid, @P("AiMessageFormat") AiMessageFormat aiMessageFormat) {
        System.out.println(aiMessageFormat.toString());
        String message = aiMessageFormat.getMessage();
        if (message.startsWith("@keyword")) {
            message = message.substring("@keyword".length()).trim();
        }
        Integer courseId = Integer.valueOf(aiMessageFormat.getCourseid());
        EmbeddingStore<TextSegment> embeddingStore = ElasticsearchEmbeddingStore.builder()
                .serverUrl(ElasticSearchUrl)
                .apiKey(ElasticSearchKey)
                .dimension(384)
                .build();
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        Embedding queryEmbedding = embeddingModel.embed(message).content();
        //set the search request
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest
                .builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(10)
                .minScore(0.5)
                .filter(metadataKey("courseId").isEqualTo(courseId))
                .build();
        EmbeddingSearchResult<TextSegment> search = embeddingStore.search(embeddingSearchRequest);
        List<EmbeddingMatch<TextSegment>> matches = search.matches();
        HashMap<String, String> RagMap = new LinkedHashMap<>();
        for (EmbeddingMatch<TextSegment> match : matches) {
            RagMap.put(match.embedded().text(), String.valueOf(match.score()));
        }
        FunctionCallingFormat Rag = new FunctionCallingFormat("Rag", RagMap);
        String jsonString = JSONUtil.toJsonStr(Rag);

        interface Converter {
            @SystemMessage("Convert this json {{json}},Example of JSON received：\n" +
                    "{\n" +
                    "\"ToolName\": \"Rag\",\n" +
                    "\"data\": {\n" +
                    "\"rough handling\": 0.62600267,\n" +
                    "}\n" +
                    "}\n" +
                    "\n" +
                    "Example of output format：\n" +
                    "Rag result\n" +
                    "<table border=\"1\">\n" +
                    "    <tr>\n" +
                    "        <th>Feedback</th>\n" +
                    "        <th>cos similarity</th>\n" +
                    "    </tr>\n" +
                    "    <tr>\n" +
                    "        <td><strong>\"rough handling\"</strong></td>\n" +
                    "        <td>0.62600267</td>\n" +
                    "    </tr>\n" +
                    "</table>")
            String convert(@UserMessage @V("json") String json);
        }
        Converter converter = AiServices.create(Converter.class, model);
        String htmlOutput = converter.convert(jsonString);
        return htmlOutput;
    }


    @Tool("The tool property of aiMessageFormat must be a summary to call this method")
    String summaryTool(@P("The user will enter information like \"experiment, exercise, entire\". " +
            "These three should be seperated and stored as a string array in the private String[] summaryItem;") AiMessageForSummary aiMessageForSummary) {
        Boolean flag = false;
        String[] summaryItem = aiMessageForSummary.getSummaryItem();
        String courseid = aiMessageForSummary.getCourseid();
        ArrayList<String> courseItmeNames = new ArrayList<>();
        for (String item : summaryItem) {
            if ("entire".equals(item)) {
                flag = true;
                continue;
            }
            courseItmeNames.add(item);
        }
        FunctionCallingFormat functionCallingFormat = null;
        if (flag) {
            String courseAiResult = analyzeMapper.getAiResultFromCourseId(courseid);
            courseAiResult = "{airesult=" + courseAiResult + "}";
            Map<String, String> courseitemAiResult = analyzeMapper.getAiResultFromCourseItemid(courseid, courseItmeNames);
            courseitemAiResult.put("entire", courseAiResult);
            functionCallingFormat = new FunctionCallingFormat("Summary", courseitemAiResult);
        } else if (courseItmeNames.size() == 0) {
            String courseAiResult = analyzeMapper.getAiResultFromCourseId(courseid);
            courseAiResult = "{airesult=" + courseAiResult + "}";
            HashMap<String, String> map = new HashMap<>();
            map.put("entire", courseAiResult);
            functionCallingFormat = new FunctionCallingFormat("Summary", map);

        } else {
            Map<String, String> courseitemAiResult = analyzeMapper.getAiResultFromCourseItemid(courseid, courseItmeNames);
            functionCallingFormat = new FunctionCallingFormat("Summary", courseitemAiResult);
        }
        String jsonString = JSONUtil.toJsonStr(functionCallingFormat);
        interface Converter {
            @SystemMessage("Convert this json {{json}},Example of JSON received：\n" +
                    "{\n" +
                    "\"ToolName\": \"Summary\",\n" +
                    "\"data\": {\n" +
                    "\"teaching\": First paragraph...Fifth paragraph,\n" +
                    "\"Entire\": First paragraph...Fifth paragraph,\n" +
                    "}\n" +
                    "}\n" +
                    "\n" +
                    "Example of output format：\n" +
                    "<div>\n" +
                    "    <h3>teaching</h3>\n" +
                    "    <strong>First paragraph's header</strong>\n" +
                    "    <p>First paragraph</p>\n" +
                    "    <strong>Second paragraph's header</strong>\n" +
                    "    <p>Second paragraph</p>\n" +
                    "    <strong>Third paragraph's header</strong>\n" +
                    "    <p>Third paragraph</p>\n" +
                    "    <strong>Fourth paragraph's header</strong>\n" +
                    "    <p>Fourth paragraph</p>\n" +
                    "    <strong>Fifth paragraph's header</strong>\n" +
                    "    <p>Fifth paragraph</p>\n" +
                    "    <h3>Entire</h3>\n" +
                    "    <strong>First paragraph's header</strong>\n" +
                    "    <p>First paragraph</p>\n" +
                    "    <strong>Second paragraph's header</strong>\n" +
                    "    <p>Second paragraph</p>\n" +
                    "    <strong>Third paragraph's header</strong>\n" +
                    "    <p>Third paragraph</p>\n" +
                    "    <strong>Fourth paragraph's header</strong>\n" +
                    "    <p>Fourth paragraph</p>\n" +
                    "    <strong>Fifth paragraph's header</strong>\n" +
                    "    <p>Fifth paragraph</p>\n" +
                    "</div>")
            String convert(@UserMessage @V("json") String json);
        }

        Converter converter = AiServices.create(Converter.class, model);

        String htmlOutput = converter.convert(jsonString);
        return htmlOutput;
    }
}

