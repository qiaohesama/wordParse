package com.mnnu.entities;

import lombok.Data;

import java.io.Serializable;

/**
 * @author qiaoh
 */
@Data
public class Options implements Serializable {
    //A选项
    private String answerA;
    //B选项
    private String answerB;
    //C选项
    private String answerC;
    //D选项
    private String answerD;
}
