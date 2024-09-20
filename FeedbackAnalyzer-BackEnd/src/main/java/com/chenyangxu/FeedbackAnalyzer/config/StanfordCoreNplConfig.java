package com.chenyangxu.FeedbackAnalyzer.config;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class StanfordCoreNplConfig {
    private static StanfordCoreNLP StanfordCoreNLP;
    @Bean
    public StanfordCoreNLP stanfordCoreNLP(){
        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,sentiment");
        //singleton pattern
        if (StanfordCoreNLP == null){
            StanfordCoreNLP = new StanfordCoreNLP(properties);
        }
        return StanfordCoreNLP;
    }
}
