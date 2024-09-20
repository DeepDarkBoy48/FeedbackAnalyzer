package com.chenyangxu.FeedbackAnalyzer.mapper;


import com.chenyangxu.FeedbackAnalyzer.pojo.entity.Course;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.feedback;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FeedbackResultMapper {



    //列表
    List<Course> list(Integer userId, String keyword);

    //删除course
    @Delete("delete from course where id = #{id};")
    void deleteCourse(Integer id);

    @Delete("delete from courseitem where course_id = #{id};")
    void deleteCourseItem(Integer id);



    void deleteCourseItemFeedbacks(List<Integer> itemIds);


    List<feedback> getcourseItemFeedback(List<Integer> courseItemId);

    List<feedback> getcourseItemFeedbackBySentiment(List<Integer> courseItemId, List<String> sentiment);
}
