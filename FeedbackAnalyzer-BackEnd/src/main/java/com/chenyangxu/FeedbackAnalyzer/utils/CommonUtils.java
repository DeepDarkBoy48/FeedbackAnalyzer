package com.chenyangxu.FeedbackAnalyzer.utils;

import java.util.Map;

public class CommonUtils {
//    从ThreadLocal获取id
    public static Integer getId(){
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        return id;
    }
}
