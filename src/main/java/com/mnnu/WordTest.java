package com.mnnu;

import com.mnnu.entities.Options;
import com.mnnu.entities.Question;
import com.mnnu.entities.QuestionTypeEnum;
import com.mnnu.utils.ImageParse;
import com.mnnu.utils.ParagraphChildOrderManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.xwpf.usermodel.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author qiaoh
 */
@SuppressWarnings({"AlibabaCommentsMustBeJavadocFormat", "AlibabaRemoveCommentedCode"})
public class WordTest {
    // 该正则用来匹配一个大题（例如：二、多选题）
    private static String regex = "([一|二|三|四|五|六|七|八|九|十]{1,3})([、.]{1})([\\u4E00-\\u9FA5\\s]+题)";


    public static void main(String[] args) throws IOException {
        ArrayList<Question> list = new ArrayList<Question>();
        XWPFDocument doc = new XWPFDocument(POIXMLDocument.openPackage("D:\\优酷\\Youku Files\\nplayerdisk\\2318928417\\FileRecv\\question.docx"));
        List<IBodyElement> paragraphs = doc.getBodyElements();
        // 2021年福建省公务员录用考试《行测》卷
        String title = ((XWPFParagraph) paragraphs.get(0)).getParagraphText();

        for (int i = 1; i < paragraphs.size(); i++) {
            String paragraphText = ((XWPFParagraph) paragraphs.get(i)).getParagraphText();
            // 匹配大题，例如： 一、常识判断，共20题。（每题0.8分）
            if (paragraphText.contains("常识判断")) {
                i = getChoiceQuestionList(i, "单选题", paragraphs, list);
            } else if (paragraphText.contains("判断推理")) {
                i = getChoiceQuestionList(i, "单选题", paragraphs, list);
            }
        }
        list.forEach(System.out::println);
    }

    public static int getChoiceQuestionList(int i, String questionType, List<IBodyElement> paragraphs, List<Question> list) throws IOException {
        // 匹配大题，例如： 一、常识判断，共20题。（每题0.8分）
        Pattern compile = Pattern.compile(regex);
        i++;
        for (i = i; i < paragraphs.size(); i++) {
            String question = getContent(paragraphs.get(i), new StringBuilder());
            // 表示一种题型的结束
            // 匹配到 二、多选 所以i-- break
            if (compile.matcher(question).find()) {
                i--;
                break;
            }
            //获取word试卷题目中的题干信息
            if (question.contains("【题文】")) {
                i++;
                for (i = i; i < paragraphs.size(); i++) {
                    String content = getContent(paragraphs.get(i), new StringBuilder());
                    if (content.contains(QuestionTypeEnum.CHOICE.getType())) {
                        break;
                    }
                    question += content;
                }
            }
            //获取题目中的选项信息
            String option = getContent(paragraphs.get(i), new StringBuilder());
            if (option.contains("【选项】")) {
                i++;
                //如果标签不是以【答案】结尾，该题目为多行一题
                while (!((XWPFParagraph) paragraphs.get(i)).getParagraphText().contains(QuestionTypeEnum.ANSWER.getType())) {
                    String content = getContent(paragraphs.get(i), new StringBuilder());

                    option += content;
                    i++;
                }
            }
            //获取题目的答案信息
            String answer = getContent(paragraphs.get(i), new StringBuilder());
            if (answer.contains(QuestionTypeEnum.ANSWER.getType())) {
                i++;
                while (!((XWPFParagraph) paragraphs.get(i)).getParagraphText().contains(QuestionTypeEnum.ANALYSIS.getType())) {
                    String content = getContent(paragraphs.get(i), new StringBuilder());
                    answer += content;
                    i++;
                }
            }
            //获取题目的解析信息
            String analysis = getContent(paragraphs.get(i), new StringBuilder());
            if (analysis.contains(QuestionTypeEnum.ANALYSIS.getType())) {
                i++;
                while (!((XWPFParagraph) paragraphs.get(i)).getParagraphText().contains(QuestionTypeEnum.FINISH.getType())) {
                    String content = getContent(paragraphs.get(i), new StringBuilder());
                    analysis += content;
                    i++;
                }
            }

            if (!StringUtils.isBlank(question) && !StringUtils.isBlank(analysis) && !StringUtils.isBlank(answer) && !StringUtils.isBlank(option)) {
                Question q = new Question();
                //第一段固定为试卷名称
                String title = ((XWPFParagraph) paragraphs.get(0)).getParagraphText();
                //第二段固定为什么类型的试卷
//                String subject = ((XWPFParagraph) paragraphs.get(1)).getParagraphText();
                q.setTitle(title);
//                q.setSubject(subject);
                q.setQuestionType(questionType);
                q.setQuestion(question);
                q.setAnalysis(analysis);
                q.setRightAnswer(answer);
                q.setOption(option);
                Options options = divideOption(option);
                q.setOptions(options);
                list.add(q);
            }
        }
        return i;
    }


    public static String getContent(IBodyElement body, StringBuilder content) {
        //将word中的图片存储到本地磁盘中
        ImageParse imageParse = new ImageParse("D:/demo", "D:/demo/");
        //拿到所有的段落的表格，这两个属于同级无素
        if (body.getElementType().equals(BodyElementType.PARAGRAPH)) {
            //处理段落中的文本以及公式图片
            handleParagraph(content, body, imageParse);
        }
        return content.toString();
    }

    public static void handleParagraph(StringBuilder content, IBodyElement body, ImageParse imageParser) {
        XWPFParagraph p = (XWPFParagraph) body;
        if (p.isEmpty() || p.isWordWrap() || p.isPageBreak()) {
            return;
        }
//        String tagName = "p";
//        content.append("<" + tagName + ">");
      /*XWPFParagraph 有两个方法可以分别提出XWPFRun和CTOMath，但是不知道位置
      ParagraphChildOrderManager这个类是专门解决这个问题的
      */
        ParagraphChildOrderManager runOrMaths = new ParagraphChildOrderManager(p);
        List<Object> childList = runOrMaths.getChildList();

        for (Object child : childList) {
            if (child instanceof XWPFRun) {
                //处理段落中的文本以及图片
                handleParagraphRun(content, (XWPFRun) child, imageParser);
            }
        }
//        content.append("</" + tagName + ">");
    }


    private static void handleParagraphRun(StringBuilder content, XWPFRun run, ImageParse imageParser) {
        // 有内嵌的图片
        List<XWPFPicture> pics = run.getEmbeddedPictures();
        if (pics != null && pics.size() > 0) {
            handleParagraphRunsImage(content, pics, imageParser);
        } else {
            //纯文本直接获取
            content.append(run.toString());
        }
    }


    private static void handleParagraphRunsImage(StringBuilder content, List<XWPFPicture> pics, ImageParse imageParser) {
        for (XWPFPicture pic : pics) {
            //这里已经获取好了
            String path = imageParser.parse(pic.getPictureData().getData(),
                    pic.getPictureData().getFileName());

            content.append(String.format("<img src=\"%s\" />", path));
//            CTPicture ctPicture = pic.getCTPicture();
//            Node domNode = ctPicture.getDomNode();
//
//            Node extNode = W3cNodeUtil.getChildChainNode(domNode, "pic:spPr", "a:ext");
//            NamedNodeMap attributes = extNode.getAttributes();
//            if (attributes != null && attributes.getNamedItem("cx") != null) {
//                int width = WordMyUnits.emuToPx(new Double(attributes.getNamedItem("cx").getNodeValue()));
//                int height = WordMyUnits.emuToPx(new Double(attributes.getNamedItem("cy").getNodeValue()));
//                content.append(String.format("<img src=\"%s\" width=\"%d\" height=\"%d\" />", path, width, height));
//            } else {
//                content.append(String.format("<img src=\"%s\" />", path));
//            }
        }
    }


    public static Options divideOption(String option) {
        //使用subString将答案分割
        String regex = "[A|B|C|D]、";
        Options options = new Options();
        String optionA = option.substring(option.indexOf("A"), option.indexOf("B"));
        String optionB = option.substring(option.indexOf("B"), option.indexOf("C"));
        String optionC = option.substring(option.indexOf("C"), option.indexOf("D"));
        String optionD = option.substring(option.indexOf("D"));
        options.setAnswerA(optionA);
        options.setAnswerB(optionB);
        options.setAnswerC(optionC);
        options.setAnswerD(optionD);
        return options;
    }

}
