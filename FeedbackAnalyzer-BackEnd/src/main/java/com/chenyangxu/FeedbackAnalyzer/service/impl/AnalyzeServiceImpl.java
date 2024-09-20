package com.chenyangxu.FeedbackAnalyzer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.chenyangxu.FeedbackAnalyzer.config.aimodel;
import com.chenyangxu.FeedbackAnalyzer.mapper.AnalyzeMapper;
import com.chenyangxu.FeedbackAnalyzer.pojo.NlpData;
import com.chenyangxu.FeedbackAnalyzer.pojo.dto.CourseFormDto;
import com.chenyangxu.FeedbackAnalyzer.pojo.dto.CourseItemsDto;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.Course;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.CourseItem;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.feedback;
import com.chenyangxu.FeedbackAnalyzer.pojo.vo.ScoreVo;
import com.chenyangxu.FeedbackAnalyzer.service.AnalyzeService;
import com.chenyangxu.FeedbackAnalyzer.service.PublicService;
import com.chenyangxu.FeedbackAnalyzer.utils.CommonUtils;
import com.chenyangxu.FeedbackAnalyzer.utils.ThreadLocalUtil;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AnalyzeServiceImpl implements AnalyzeService {
    @Autowired
    private AnalyzeMapper analyzeMapper;
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;
    @Autowired
    private aimodel.AiAssistant2 aiAssistant2;
    @Autowired
    private PublicService publicService;
    @Value("${Elasticsearch.api.url}")
    private String ElasticSearchUrl;
    @Value("${Elasticsearch.api.key}")
    private String ElasticSearchKey;
    @Value("${openai.api.key}")
    private String openaiApiKey;


    /**
     * add new analyze form
     *
     * @param courseFormDto
     */
    @Transactional
    @Override
    public Integer addAnalyzeForm(CourseFormDto courseFormDto) {
        //add new course
        Course course = new Course();
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        course.setUserId(userId);
        BeanUtil.copyProperties(courseFormDto, course);
        course.setCreateTime(LocalDateTime.now());
        analyzeMapper.insertCourse(course);
        Integer courseId = course.getId();

        //add new module
        List<CourseItemsDto> courseItemsDto = courseFormDto.getCourseItems();
        ArrayList<CourseItem> courseItems = new ArrayList<>();
        for (CourseItemsDto item : courseItemsDto) {
            CourseItem courseItem = new CourseItem();
            BeanUtil.copyProperties(item, courseItem);
            courseItem.setCourseId(courseId);
            courseItems.add(courseItem);
        }
        //store course items
        analyzeMapper.insertCourseItems(courseItems);

        return courseId;
    }

    /**
     * get course item by course id
     *
     * @param couseId
     * @return
     */
    @Override
    public List<CourseItem> getCourseItemByCourseId(Integer couseId) {
        List<CourseItem> courseItems = analyzeMapper.getCourseItemByCourseId(couseId);
        return courseItems;
    }

    /**
     * 插入情感反馈到mysql 并且向量化
     *
     * @param sentimentVoMap
     * @param courseItems
     */
    @Override
    public void insertSentimentFeedback(Map<String, NlpData> sentimentVoMap, List<CourseItem> courseItems) {

        interface Assistant {
            String chat(@MemoryId int memoryId, @dev.langchain4j.service.UserMessage String userMessage);
        }
        // Setup LangChain4J embedding store
        EmbeddingStore<TextSegment> embeddingStore = ElasticsearchEmbeddingStore.builder()
                .serverUrl(ElasticSearchUrl)
                .apiKey(ElasticSearchKey)
                .dimension(384)
                .build();
        // Embedding model
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");


        List<feedback> feedbacks = new ArrayList<>();
        for (Map.Entry<String, NlpData> entry : sentimentVoMap.entrySet()) {
            int courseItemId = 0;
            int courseId = courseItems.get(0).getCourseId();
            for (CourseItem item : courseItems) {
                if (item.getTitle().equals(entry.getKey())) {
                    courseItemId = item.getId();
                    //存入数量
                    NlpData nlpData = entry.getValue();
                    item.setPositive(nlpData.getPositive());
                    item.setSomewhatpositive(nlpData.getSomewhatPositive());
                    item.setNeutral(nlpData.getNeutral());
                    item.setSomewhatnegative(nlpData.getSomewhatNegative());
                    item.setNegative(nlpData.getNegative());
                    analyzeMapper.updateCoursxeItem(item);
                    break;
                }
            }
            NlpData value = entry.getValue();
            ArrayList<String> negativeData = value.getNegativeData();
            for (String item : negativeData) {
                //insert metadata
                Metadata metadata = new Metadata();
                metadata.put("userId", userId);
                metadata.put("courseItemId", courseItemId);
                metadata.put("courseId", courseId);
                metadata.put("feedbackType", "negative");
                TextSegment segment = TextSegment.from(item, metadata);
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
                feedback feedback = new feedback();
                feedback.setFeedbackType("negative");
                feedback.setCourseitemid(courseItemId);
                feedback.setFeedback(item);
                feedbacks.add(feedback);
            }
            ArrayList<String> somewhatnegativeData = value.getSomewhatnegativeData();
            for (String item : somewhatnegativeData) {
                //insert metadata
                Metadata metadata = new Metadata();
                metadata.put("userId", userId);
                metadata.put("courseItemId", courseItemId);
                metadata.put("courseId", courseId);
                metadata.put("feedbackType", "somewhatnegative");
                TextSegment segment = TextSegment.from(item, metadata);
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
                feedback feedback = new feedback();
                feedback.setFeedbackType("somewhatnegative");
                feedback.setCourseitemid(courseItemId);
                feedback.setFeedback(item);
                feedbacks.add(feedback);
            }
            ArrayList<String> neutralData = value.getNeutralData();
            for (String item : neutralData) {
                //insert metadata
                Metadata metadata = new Metadata();
                metadata.put("userId", userId);
                metadata.put("courseItemId", courseItemId);
                metadata.put("courseId", courseId);
                metadata.put("feedbackType", "neutral");
                TextSegment segment = TextSegment.from(item, metadata);
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
                feedback feedback = new feedback();
                feedback.setFeedbackType("neutral");
                feedback.setCourseitemid(courseItemId);
                feedback.setFeedback(item);
                feedbacks.add(feedback);
            }
            ArrayList<String> somewhatpositiveData = value.getSomewhatpositiveData();
            for (String item : somewhatpositiveData) {
                //insert metadata
                Metadata metadata = new Metadata();
                metadata.put("userId", userId);
                metadata.put("courseItemId", courseItemId);
                metadata.put("courseId", courseId);
                metadata.put("feedbackType", "somewhatpositive");
                TextSegment segment = TextSegment.from(item, metadata);
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
                feedback feedback = new feedback();
                feedback.setFeedbackType("somewhatpositive");
                feedback.setCourseitemid(courseItemId);
                feedback.setFeedback(item);
                feedbacks.add(feedback);
            }
            ArrayList<String> positiveData = value.getPositiveData();
            for (String item : positiveData) {
                //insert metadata
                Metadata metadata = new Metadata();
                metadata.put("userId", userId);
                metadata.put("courseItemId", courseItemId);
                metadata.put("courseId", courseId);
                metadata.put("feedbackType", "positive");
                TextSegment segment = TextSegment.from(item, metadata);
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
                feedback feedback = new feedback();
                feedback.setFeedbackType("positive");
                feedback.setCourseitemid(courseItemId);
                feedback.setFeedback(item);
                feedbacks.add(feedback);
            }
        }
        //存入数据到feedback
        analyzeMapper.insertFeedback(feedbacks);


        //ai summary
        Integer userid = CommonUtils.getId();
        int courseId = courseItems.get(0).getCourseId();
        List<CourseItem> courseItemsList = this.getCourseItemByCourseId(courseId);

        Integer totalPositive = 0;
        Integer totalNeutral = 0;
        Integer totalNegative = 0;
        //对应课程的每一个item
        for (CourseItem item : courseItemsList) {

            //calculate the proportion of each sentiment
            Integer positive = item.getPositive();
            totalPositive += positive;
            Integer neutral = item.getNeutral();
            totalNeutral += neutral;
            Integer negative = item.getNegative();
            totalNegative += negative;
            Integer total = positive + neutral + negative;
            String positiveProportion = String.format("%.2f%%", (positive / (double) total) * 100);
            String neutralProportion = String.format("%.2f%%", (neutral / (double) total) * 100);
            String negativeProportion = String.format("%.2f%%", (negative / (double) total) * 100);
            String sentimentScore = "positive Proportion: " + positiveProportion + " neutral Proportion: " + neutralProportion + " negative Proportion: " + negativeProportion;

            //itemid add all text feedback to a StringBuffer
            Integer itemId = item.getId();
            List<Integer> itemIds = new ArrayList<>();
            itemIds.add(itemId);
            List<feedback> feedbacks1 = publicService.getcourseItemFeedback(itemIds);
            StringBuffer summary = new StringBuffer();
            for (feedback feedback : feedbacks1) {
                String feedback1 = feedback.getFeedback();
                summary.append(feedback1 + "\n");
            }
            summary.append(sentimentScore+"\n"+"\n");
            String title = item.getTitle();


            summary.append("Based on the feedback data and sentiment percentage above. Help me analyze the"+ title +"module. " +
                    "First paragraph: What didn't go well? " +
                    "Second paragraph: What went well? " +
                    "Third paragraph: How can it be improved? " +
                    "Fourth paragraph: Keyword (Identify at least the 5 most critical keywords in the above feedback)." +
                    "Fifth paragraph: Sentiment ratio (show the sentiment ratio.)" +
                    "Each paragraph should have a heading,and Shouldn't be more than five paragraphs. " +
                    "Only output the required content as indicated in the 5 paragraphs, do not output additional summary content");
            String finalSummary = summary.toString();
            String aiResultItem = aiAssistant2.chat(userid, finalSummary);
            System.out.println(aiResultItem);
            System.out.println("---------------------------------------------------");
            item.setAiresult(aiResultItem);
            analyzeMapper.updateCoursxeItem(item);
        }
        Integer total2 = totalPositive + totalNeutral + totalNegative;
        String positiveProportion = String.format("%.2f%%", (totalPositive / (double) total2) * 100);
        String neutralProportion = String.format("%.2f%%", (totalNeutral / (double) total2) * 100);
        String negativeProportion = String.format("%.2f%%", (totalNegative / (double) total2) * 100);
        String sentimentScore = "positive Proportion: " + positiveProportion + " neutral Proportion: " + neutralProportion + " negative Proportion: " + negativeProportion;

        String summary = "Please make a summary as the ultimate feedback based on the feedback from each course module in the historical data. " +
                "First paragraph: what didn't go well? " +
                "Second paragraph: what went well? " +
                "Third paragraph: How can it be improved? " +
                "Fourth paragraph: Identify 5-10 most critical keywords from the above feedback. " +
                "Fifth paragraph: shows the total sentiment percentage, the data:"+sentimentScore+
                "Each paragraph should have a heading,and Shouldn't be more than five paragraphs.Only output the required content as indicated in the 5 paragraphs, do not output additional summary content";
        String aiResultCourse = aiAssistant2.chat(userid, summary);
        System.out.println(aiResultCourse);
        Course course = analyzeMapper.getCourse(courseId);
        course.setAiresult(aiResultCourse);
        analyzeMapper.updateCourse(course);

    }

    /**
     * 插入好评率
     *
     * @param scoreRatioMap
     * @param couseId
     */
    @Override
    public void insertScoreRatio(Map<String, ScoreVo> scoreRatioMap, Integer couseId) {
        //存入数量到courseitem
        List<CourseItem> courseItems = this.getCourseItemByCourseId(couseId);
        for (CourseItem item : courseItems) {
            if (scoreRatioMap.containsKey(item.getTitle())) {
                ScoreVo scoreVo = scoreRatioMap.get(item.getTitle());
                item.setGood(scoreVo.getGood());
                item.setBad(scoreVo.getBad());
                item.setNormal(scoreVo.getNormal());
                analyzeMapper.updateCoursxeItem(item);
            }
        }
    }
}
