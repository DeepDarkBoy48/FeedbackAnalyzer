package com.chenyangxu.FeedbackAnalyzer.utils;

import com.chenyangxu.FeedbackAnalyzer.pojo.NlpData;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

public class StanfordCoreNlpUtil {


    //word分割

//    public static void analyzeTokens(StanfordCoreNLP pipeline) {
//        String text = "Because when you participiate you get reappear or backlog and no time is given for exam preparation";
//        CoreDocument document = new CoreDocument(text);
//        pipeline.annotate(document);
//        String token = "";
//        List<CoreLabel> tokens = document.tokens();
//        for (CoreLabel coreLabel : tokens) {
//            token += coreLabel.originalText() + "|";
//        }
//        System.out.println(token);
////        return sentiment.trim();
//    }
//
//    //句子分割
//
//    public static void analyzeSsplit(StanfordCoreNLP pipeline) {
//        String text = "hey! I am Robin. I am a software engineering.";
//        CoreDocument document = new CoreDocument(text);
//        pipeline.annotate(document);
//        List<CoreSentence> sentences = document.sentences();
//        for (CoreSentence sentence : sentences) {
//            System.out.println(sentence.toString());
//        }
//    }
//
//    //语法分析
//
//    public static void analyzePos(StanfordCoreNLP pipeline) {
//        String text = "hey! I am Robin. I am a software engineering.";
//        CoreDocument coreDocument = new CoreDocument(text);
//        pipeline.annotate(coreDocument);
//        List<CoreLabel> tokens = coreDocument.tokens();
//        for (CoreLabel token : tokens) {
//            String s = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//            System.out.println(token.originalText() + " : " + s);
//        }
//    }
//
//    //词形还原lemmatization
//
//    public static void analyzeLemma(StanfordCoreNLP pipeline) {
//        String text = "hey! I am Robin. I am a software engineering.";
//        CoreDocument coreDocument = new CoreDocument(text);
//        pipeline.annotate(coreDocument);
//        List<CoreLabel> tokens = coreDocument.tokens();
//        for (CoreLabel token : tokens) {
//            String s = token.lemma();
//            System.out.println(token.originalText() + " : " + s);
//        }
//    }
//
//    //命名实体识别
//
//    public static void analyzeNer(StanfordCoreNLP pipeline) {
//        String text = "extra curricular are so good. university is providing many pathways to many students....apart from education";
//        CoreDocument coreDocument = new CoreDocument(text);
//        pipeline.annotate(coreDocument);
//        List<CoreLabel> tokens = coreDocument.tokens();
//        for (CoreLabel token : tokens) {
//            String s = token.ner();
//            System.out.println(token.originalText() + " : " + s);
//        }
//    }
//
//    //情感分析
//
//    public static void analyzeSentiment(StanfordCoreNLP pipeline) {
//        String text = "hey! I am Robin. I am a software engineering.";
//        CoreDocument coreDocument = new CoreDocument(text);
//        pipeline.annotate(coreDocument);
//        //句子分割
//        List<CoreSentence> sentences = coreDocument.sentences();
//        //合并多个句子
//        StringBuffer processedText = new StringBuffer();
//        for (CoreSentence sentence : sentences) {
//            //去掉句子末尾的句号
//            String string = sentence.toString().substring(0, sentence.toString().length() - 1);
//            processedText.append(string).append(" ");
//        }
//        System.out.println(processedText.toString());
//        String IntegreteText = processedText.toString();
//        //情感分析
//        CoreDocument coreDocument1 = new CoreDocument(processedText.toString());
//        pipeline.annotate(coreDocument1);
//        List<CoreSentence> sentences1 = coreDocument1.sentences();
//        System.out.println(sentences1.get(0).toString() + " : " + sentences1.get(0).sentiment());
//
//    }

    //情绪分析（自定义）

    public static Map<String, NlpData> analyzeSentimentCustomize(StanfordCoreNLP pipeline, Map<String, ArrayList<String>> feedbackMap) {
        //map key为项目名称 value为SentimentVo 包含Negative SomewhatNegative Neutral SomewhatPositive Positive
        Map<String, NlpData> sentimentMap = new HashMap<>();
        //遍历每一个项目
        for (Map.Entry<String, ArrayList<String>> entry : feedbackMap.entrySet()) {
            NlpData nlpData = new NlpData();
            //获取项目名称
            String item = entry.getKey();
            //获取项目的feedback
            ArrayList<String> feedbacks = entry.getValue();
            int Negative = 0;
            int SomewhatNegative = 0;
            int Neutral = 0;
            int SomewhatPositive = 0;
            int Positive = 0;

            //iterating over each text feedback
            for (String feedback : feedbacks) {
                //transforming text feedback to lowercase
                String feedbackLowerCase = feedback.toLowerCase();
                Annotation annotation = pipeline.process(feedbackLowerCase);
                //set the main sentiment of the text feedback
                int mainsentiment = 0;
                //iterating over each sentence in the text feedback
                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    int sentenceLenth = 0;
                    Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                    //current sentiment of the sentence
                    int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                    String partText = sentence.toString();
                    //find the longest sentence and set it as the main sentiment
                    if (partText.length() > sentenceLenth) {
                        mainsentiment = sentiment;
                        sentenceLenth = partText.length();
                    }
                }
                //classify the sentiment: 0,1 -> Negative; 2 -> Neutral; 3,4 -> Positive
                switch (mainsentiment) {
                    case 0:
                    case 1:
                        Negative++;
                        nlpData.getNegativeData().add(feedback);
                        break;
                    case 2:
                        Neutral++;
                        nlpData.getNeutralData().add(feedback);
                        break;
                    case 3:
                    case 4:
                        Positive++;
                        nlpData.getPositiveData().add(feedback);
                        break;
                }
                //将每个项目的情感分析结果存入sentimentMap
                nlpData.setNegative(Negative);
                nlpData.setSomewhatNegative(SomewhatNegative);
                nlpData.setNeutral(Neutral);
                nlpData.setSomewhatPositive(SomewhatPositive);
                nlpData.setPositive(Positive);
                sentimentMap.put(item, nlpData);
            }
            System.out.println(sentimentMap);
        }
        return sentimentMap;
    }
}
