package com.chenyangxu.FeedbackAnalyzer.mapper;


import com.chenyangxu.FeedbackAnalyzer.pojo.entity.Course;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.CourseItem;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.feedback;
import org.apache.ibatis.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface AnalyzeMapper {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into course (title, prompt, user_id, create_time) values (#{title}, #{prompt}, #{userId}, #{createTime})")
    void insertCourse(Course course);


    void insertCourseItems(ArrayList<CourseItem> courseItems);


    @Select("select * from courseitem where course_id = #{courseId}")
    List<CourseItem> getCourseItemByCourseId(Integer couseId);


    void insertFeedback(List<feedback> feedbacks);


    void updateCoursxeItem(CourseItem item);

    @Select("select * from course where id = #{courseId};")
    Course getCourse(int courseId);

    void updateCourse(Course course);


    @Select("select airesult from course where id = #{courseid}")
    String getAiResultFromCourseId(String courseid);


    @MapKey("title")
    Map<String, String> getAiResultFromCourseItemid(String courseid, ArrayList<String> courseItmeNames);
}
