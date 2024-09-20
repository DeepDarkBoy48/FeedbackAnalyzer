package com.chenyangxu.FeedbackAnalyzer.pojo;

import lombok.Data;

import java.util.ArrayList;

@Data
public class NlpData {
    private Integer Negative;
    private Integer SomewhatNegative;
    private Integer Neutral;
    private Integer SomewhatPositive;
    private Integer Positive;
    private ArrayList<String> positiveData = new ArrayList<>();
    private ArrayList<String> negativeData = new ArrayList<>();
    private ArrayList<String> neutralData = new ArrayList<>();
    private ArrayList<String> somewhatpositiveData= new ArrayList<>();
    private ArrayList<String> somewhatnegativeData = new ArrayList<>();
}
