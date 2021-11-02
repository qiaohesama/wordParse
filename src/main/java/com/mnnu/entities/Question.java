package com.mnnu.entities;

import lombok.Data;

/**
 * @author qiaoh
 */
@Data
public class Question {
    /**
     * 试卷标题
     */
    private String title;

    /**
     * 科目
     */
    private String subject;

    /**
     * 题号
     */
    private Integer number;
    /**
     * 问题
     */
    private String question;

    /**
     * 图片路径
     */
    private String imagePath;
    /**
     * 试题类型
     */
    private String questionType;
    /**
     * 正确答案
     */
    private String rightAnswer;

    /**
     * 解析
     */
    private String analysis;


    /**
     * 分数
     */
    private String score;

    /**
     * ABCDE选项
     */
    private String option;

    /**
     * 选项
     */
    private Options options;
}
