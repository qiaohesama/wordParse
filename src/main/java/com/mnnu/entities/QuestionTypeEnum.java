package com.mnnu.entities;

/**
 * @author qiaoh
 */

@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum QuestionTypeEnum {
    SINGLE_CHOICE_QUESTION(1, "单选题"),
    MULTIPLE_CHOICE(2, "多选题"),
    GAP_FILLING(3, "填空题"),
    RESPONSE_QUESTION(4, "解答题"),
    SUBJECTIVE_ITEM(5, "主观题"),
    TOPIC(6, "【题文】"),
    CHOICE(7, "【选项】"),
    ANSWER(8, "【答案】"),
    ANALYSIS(9, "【解析】"),
    FINISH(10, "【结束】"),
    NOT_CHOICE_QUESTION(11, "非选择题"),
    CHOICE_QUESTION(12, "选择题");


    private Integer code;
    private String type;

    QuestionTypeEnum(Integer code, String type) {
        this.code = code;
        this.type = type;
    }

    public Integer getCode() {
        return code;
    }

    public String getType() {
        return type;
    }
}
