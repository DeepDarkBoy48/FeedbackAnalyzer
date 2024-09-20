package com.chenyangxu.FeedbackAnalyzer.controller;

import cn.hutool.core.bean.BeanUtil;
import com.chenyangxu.FeedbackAnalyzer.pojo.NlpData;
import com.chenyangxu.FeedbackAnalyzer.pojo.dto.CourseFormDto;
import com.chenyangxu.FeedbackAnalyzer.pojo.dto.CourseItemsDto;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.CourseItem;
import com.chenyangxu.FeedbackAnalyzer.pojo.vo.CourseItemResultVo;
import com.chenyangxu.FeedbackAnalyzer.pojo.vo.CourseItemNameVo;
import com.chenyangxu.FeedbackAnalyzer.pojo.entity.Result;
import com.chenyangxu.FeedbackAnalyzer.pojo.vo.ScoreVo;
import com.chenyangxu.FeedbackAnalyzer.pojo.vo.SentimentVo;
import com.chenyangxu.FeedbackAnalyzer.service.AnalyzeService;
import com.chenyangxu.FeedbackAnalyzer.service.FeedbackResultService;
import com.chenyangxu.FeedbackAnalyzer.utils.ExcelUtils;
import com.chenyangxu.FeedbackAnalyzer.utils.StanfordCoreNlpUtil;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;


@RestController
@RequestMapping("/teacher/analyze")
public class AnalyzeController {
    @Autowired
    private AnalyzeService analyzeService;
    @Autowired
    private StanfordCoreNLP stanfordCoreNLP;
    @Autowired
    private FeedbackResultService feedbackResultService;

    @PostMapping("/generate")
    public Result<CourseItemNameVo> upload(@RequestPart("file") MultipartFile file, HttpServletRequest httpServletRequest) throws Exception {
        //call excel tool class
        XSSFSheet sheet = ExcelUtils.ExcelHandller(file);
        //Get the row number of the last row
        int lastRowNum = sheet.getLastRowNum();
        //Get the column number of the last column
        int lastCellNum = sheet.getRow(0).getLastCellNum();
        System.out.println("lastRowNum = " + lastRowNum);
        System.out.println("lastCellNum = " + lastCellNum);
        //Store all data of each project, map, key is the project, value is arraylist, which stores all feedback
        Map<String, ArrayList<String>> map = new HashMap<>();
        //Store all evaluation projects of the course in linkedlist
        LinkedList<String> items = new LinkedList<>();
        for (int i = 1; i <= lastCellNum; i = i + 2) {
            ArrayList<String> arrayList = new ArrayList<>();
            for (int j = 1; j < lastRowNum; j++) {
                arrayList.add(sheet.getRow(j).getCell(i).toString());
            }
            String flag = sheet.getRow(0).getCell(i - 1).toString();
            map.put(flag, arrayList);
            items.add(flag);
        }
        //
        CourseItemNameVo courseItemDto = new CourseItemNameVo();
        courseItemDto.setCourseItems(items);
        return Result.success(courseItemDto);
    }

    /**
     * 新增信息表
     *
     * @param courseFormDto
     * @return
     */
    @PostMapping("/form")
    public Result<List<CourseItemResultVo>> analyzeForm(@RequestPart("form") CourseFormDto courseFormDto, @RequestPart("file") MultipartFile file) {
        //add information form
        Integer couseId = analyzeService.addAnalyzeForm(courseFormDto);
        //call excel tool class
        XSSFSheet sheet = ExcelUtils.ExcelHandller(file);
        //get the row number of the last row
        int lastRowNum = sheet.getLastRowNum();
        //get the column number of the last column
        int lastCellNum = sheet.getRow(0).getLastCellNum();
        //All course project feedback feedbackMap key is the project name value is ArrayList<String> containing all feedback
        Map<String, ArrayList<String>> feedbackMap = new LinkedHashMap<>();
        //Get the project title item.getTitle from the course project passed from the front end
        List<CourseItemsDto> courseItemsDtos = courseFormDto.getCourseItems();
        LinkedList<String> items = new LinkedList<>();
        for (int i = 1; i < lastCellNum; i = i + 2) {
            ArrayList<String> arrayList = new ArrayList<>();
            for (int j = 1; j <= lastRowNum; j++) {
                String string = sheet.getRow(j).getCell(i).toString();
                arrayList.add(sheet.getRow(j).getCell(i).toString());
            }
            String itemName = sheet.getRow(0).getCell(i - 1).toString();
            for (CourseItemsDto name : courseItemsDtos) {
                if (itemName.equals(name.getTitle())) {
                    feedbackMap.put(itemName, arrayList);
                }
            }
            items.add(itemName);
        }
        Map<String, ScoreVo> scoreRatioMap = new HashMap<>();
        //Good rate statistics of all course projects map key is the project name value is ScoreVo containing good bad normal
        for (int i = 0; i < lastCellNum; i = i + 2) {
            int good = 0;
            int bad = 0;
            int normal = 0;
            for (int j = 1; j <= lastRowNum; j++) {
                if (sheet.getRow(j).getCell(i) == null) {
                    normal++;
                    continue;
                }
                if (sheet.getRow(j).getCell(i).toString().equals("1.0")) {
                    good++;
                } else if (sheet.getRow(j).getCell(i).toString().equals("-1.0")) {
                    bad++;
                } else if (sheet.getRow(j).getCell(i).toString().equals("0.0")) {
                    normal++;
                }
            }
            ScoreVo scoreVo = new ScoreVo();
            scoreVo.setGood(good);
            scoreVo.setBad(bad);
            scoreVo.setNormal(normal);
            scoreRatioMap.put(sheet.getRow(0).getCell(i).toString(), scoreVo);
        }

        //将好评率信息存入数据库 en:insert good rate information into database
        analyzeService.insertScoreRatio(scoreRatioMap,couseId);
        //需要返回给前端的list，包含了所有Airesult，情感，好评信息 en:List that needs to be returned to the front end, containing all Airesult, sentiment, and good information
        ArrayList<CourseItemResultVo> list = new ArrayList<>();
        //从前端传来的课程项目获取项目title item.getTitle() en:Get the project title item.getTitle() from the course project passed from the front end
        List<CourseItemsDto> courseItemsDto = courseFormDto.getCourseItems();
        //前端所选课程项目的情感统计 使用工具类StanfordCoreNlpUtil sentimentVoMap key为项目名称 value为SentimentVo 包含所有情感
        Map<String, NlpData> sentimentVoMap = StanfordCoreNlpUtil.analyzeSentimentCustomize(stanfordCoreNLP, feedbackMap);
        Map<String, SentimentVo> newSentimentVoMap = new HashMap<>();
        for (Map.Entry<String, NlpData> entry : sentimentVoMap.entrySet()) {
            SentimentVo sentimentVo = new SentimentVo();
            BeanUtil.copyProperties(entry.getValue(), sentimentVo);
            newSentimentVoMap.put(entry.getKey(), sentimentVo);
        }
        //获取课程项目的所有信息
        List<CourseItem> courseItems = analyzeService.getCourseItemByCourseId(couseId);
        //将情感信息包括数量feedback分类全部存入数据库
        analyzeService.insertSentimentFeedback(sentimentVoMap,courseItems);

        for (CourseItemsDto item : courseItemsDto) {
            //new 一个CourseItemScoreVo对象 单个项目
            CourseItemResultVo courseItemScoreVo = new CourseItemResultVo();
            //设置项目名称
            courseItemScoreVo.setTitle(item.getTitle());
            //设置项目的好评
            courseItemScoreVo.setScoreVo(scoreRatioMap.get(item.getTitle()));
            //设置项目的情感
            courseItemScoreVo.setSentimentVo(newSentimentVoMap.get(item.getTitle()));
            list.add(courseItemScoreVo);
        }
        return Result.success(list);
    }
}