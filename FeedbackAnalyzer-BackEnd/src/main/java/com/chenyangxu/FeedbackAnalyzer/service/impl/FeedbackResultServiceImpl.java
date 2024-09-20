package com.chenyangxu.FeedbackAnalyzer.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.chenyangxu.FeedbackAnalyzer.mapper.FeedbackResultMapper;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.*;
import com.chenyangxu.FeedbackAnalyzer.service.AnalyzeService;
import com.chenyangxu.FeedbackAnalyzer.service.FeedbackResultService;
import com.chenyangxu.FeedbackAnalyzer.utils.CommonUtils;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackResultServiceImpl implements FeedbackResultService {

    @Autowired
    private FeedbackResultMapper feedbackResultMapper;
    @Autowired
    private AnalyzeService analyzeService;
    @Value("${Elasticsearch.api.url}")
    private String ElasticSearchUrl;
    @Value("${Elasticsearch.api.key}")
    private String ElasticSearchKey;

    /**
     * course列表查询
     * @param pageNum
     * @param pageSize
     * @param keyword
     * @return
     */
    @Override
    public PageBean<Course> list(Integer pageNum, Integer pageSize, String keyword) {
        //1.创建PageBean对象
        PageBean<Course> pb = new PageBean<>();

        //2.开启分页查询 PageHelper
        PageHelper.startPage(pageNum,pageSize);

        //3.调用mapper
        Integer userId = CommonUtils.getId();
        List<Course> Course = feedbackResultMapper.list(userId,keyword);
        //Page中提供了方法,可以获取PageHelper分页查询后 得到的总记录条数和当前页数据
        Page<Course> p = (Page<Course>) Course;

        //把数据填充到PageBean对象中
        pb.setTotal(p.getTotal());
        pb.setItems(p.getResult());
        return pb;
    }

    //删除course以及courseItem，以及courseItem对应的feedback
    @Override
    @Transactional
    public void delete(Integer id) {
        //先删feedback
        List<CourseItem> courseItems = analyzeService.getCourseItemByCourseId(id);
        List<Integer> itemIds = courseItems.stream()
                .map(CourseItem::getId)
                .collect(Collectors.toList());
        // 批量删除 feedback 表中的记录
        if (!itemIds.isEmpty()) {
            feedbackResultMapper.deleteCourseItemFeedbacks(itemIds);
        }
        //再删courseitem
        feedbackResultMapper.deleteCourseItem(id);
        //最后删course
        feedbackResultMapper.deleteCourse(id);
        //删除向量库里的数据
        EmbeddingStore<TextSegment> embeddingStore = ElasticsearchEmbeddingStore.builder()
                .serverUrl(ElasticSearchUrl)
                .apiKey(ElasticSearchKey)
                .dimension(384)
                .build();
        Filter filter = new IsEqualTo("courseId", id);
        embeddingStore.removeAll(filter);
    }

    //查询course item
    @Override
    public List<CourseItem> getcourseItem(Integer courseId) {
        return analyzeService.getCourseItemByCourseId(courseId);
    }

    //查询couresItem的feedbacks
    @Override
    public List<feedback> getcourseItemFeedback(List<Integer> courseItemId) {
        return feedbackResultMapper.getcourseItemFeedback(courseItemId);
    }

    @Override
    public List<feedback> getcourseItemFeedbackBySentiment(List<Integer> courseItemId, List<String> sentiment) {
        return feedbackResultMapper.getcourseItemFeedbackBySentiment(courseItemId,sentiment);
    }
}
