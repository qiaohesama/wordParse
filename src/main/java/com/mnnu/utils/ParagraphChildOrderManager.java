package com.mnnu.utils;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTOMath;
import org.openxmlformats.schemas.officeDocument.x2006.math.CTOMathPara;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author qiaoh
 */
public class ParagraphChildOrderManager {

    public static int TYPE_RUN = 1;
    public static int TYPE_OMATH = 2;

    List<Integer> typeList = new ArrayList<>();
    XWPFParagraph p;

    public ParagraphChildOrderManager(XWPFParagraph paragraph) {
        this.p = paragraph;
        //using a cursor to go through the paragraph from top to down
        XmlCursor xmlcursor = paragraph.getCTP().newCursor();
        while (xmlcursor.hasNextToken()) {
            XmlCursor.TokenType tokenType = xmlcursor.toNextToken();
            if (tokenType.isStart()) {
                if (xmlcursor.getName().getPrefix().equalsIgnoreCase("w") && xmlcursor.getName().getLocalPart().equalsIgnoreCase("r")) {
                    typeList.add(TYPE_RUN);
                } else if (xmlcursor.getName().getLocalPart().equalsIgnoreCase("oMath")) {
                    typeList.add(TYPE_OMATH);
                }
            } else if (tokenType.isEnd()) {
                xmlcursor.push();
                xmlcursor.toParent();
                if (xmlcursor.getName().getLocalPart().equalsIgnoreCase("p")) {
                    break;
                }
                xmlcursor.pop();
            }
        }
    }

    public List<Object> getChildList() {
        List<Object> runsOrMathList = new ArrayList<Object>();
        List<XWPFRun> runs = p.getRuns();
        List<CTOMath> oMathList = p.getCTP().getOMathList();

//        Queue<Object> mathQueue = null;
        if (oMathList != null && oMathList.size() > 0) {
            Queue<XWPFRun> runsQueue = new LinkedList<XWPFRun>(runs);
            Queue<CTOMath> mathQueue = new LinkedList<CTOMath>(oMathList);

            for (int i = 0; i < typeList.size(); i++) {
                Integer type = typeList.get(i);
                if (type.equals(TYPE_RUN) && runs.size() > 0) {

                    runsOrMathList.add(runsQueue.poll());
                } else if (type.equals(TYPE_OMATH) && mathQueue.size() > 0) {
                    runsOrMathList.add(mathQueue.poll());
                }
            }
            return runsOrMathList;
        } else {
            List<CTOMathPara> oMathParaList = p.getCTP().getOMathParaList();
            Queue<XWPFRun> runsQueue = new LinkedList<XWPFRun>(runs);
            Queue<CTOMathPara> mathQueue = new LinkedList<CTOMathPara>(oMathParaList);
            for (int i = 0; i < typeList.size(); i++) {
                Integer type = typeList.get(i);
                if (type.equals(TYPE_RUN) && runs.size() > 0) {
                    runsOrMathList.add(runsQueue.poll());
                } else if (type.equals(TYPE_OMATH) && mathQueue.size() > 0) {
                    runsOrMathList.add(mathQueue.poll());
                }
            }
            return runsOrMathList;
        }
    }

    /**
     * 将word文档中公式格式为omML转换为mathMl
     *
     * @param omML
     * @return
     */
    public static String getMathMLFromNode(String omML) {
        //通过word中的xsl转成器解析omml公式格式
        StreamSource xslSource = new StreamSource(new File("src/main/resources/OMML2MML.XSL"));
        StringWriter writer = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer(xslSource);
            Source sources = new StreamSource(new StringReader(omML));
            StreamResult result = new StreamResult(writer);
            t.transform(sources, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        String mathML = writer.getBuffer().toString();
        mathML = mathML.replaceAll("xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\"", "");
        mathML = mathML.replaceAll("xmlns:mml", "xmlns");
        mathML = mathML.replaceAll("mml:", "");
        return mathML;
    }
}
