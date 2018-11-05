package com.daily.press;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 */
public class App {
    private static final String MAIN_THEME="/Users/lixinke/Documents/workspace/android/monitor/common-core-project/core/src/main/res/values/styles.xml";
    private static final String REPLACE_LAYOUT_PATH="/Users/lixinke/Documents/workspace/android/monitor/common-core-project/core/src/main/res/layout";
    public static void main(String[] args) {
//        replaceIvMask();
        replaceAttr();
    }

    /**
     * 替换图片夜间模式
     */
    private static void replaceIvMask() {
        try {
            File folder = new File("/Users/lixinke/Documents/workspace/android/monitor/common-core-project/core/src/main/res/layout");
            File[] list = folder.listFiles();
            for (File file : list) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = null;
                StringBuffer buffer = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    if (line.contains("app:iv_maskColor=")) {
                        String start = line.substring(0, line.indexOf("app:iv_maskColor="));
                        String end = line.substring(line.lastIndexOf("\"") + 1, line.length());
                        line = start + end;
                    }
                    buffer.append(line);
                    buffer.append("\n");
                }
                reader.close();
                FileWriter writer = new FileWriter(file);
                writer.write(buffer.toString());
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 替换布局中的属性夜间模式
     */
    private static void replaceAttr() {
        Map<String, String> styles = parseStyle(MAIN_THEME);
        File folder = new File(REPLACE_LAYOUT_PATH);
        File[] files = folder.listFiles();

        for (File file : files) {
            if (!file.getName().endsWith(".xml")) {
                continue;
            }
            SAXReader saxReader = new SAXReader();
            try {
                Document document = saxReader.read(file);
                Element employees = document.getRootElement();
                replaceAttr(styles,file,employees);
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter writer = new XMLWriter(new FileOutputStream(file),format);
                writer.write(document);
                writer.close();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }

    }

    private static void replaceAttr(Map<String, String> styles, File file, Element element) {
        List<Attribute> attributes = element.attributes();
        for (Attribute attribute : attributes) {
            String text = attribute.getValue();
            if (text.startsWith("?attr")) {
                String key = text.substring(text.indexOf("?attr/") + "?attr/".length(), text.length());
                String value = styles.get(key);
                if (value != null) {
                    attribute.setValue(styles.get(key));
                } else {
                    System.out.println(text);
                    System.out.println(file.getName());
                }
            }
        }

        for (Iterator i = element.elementIterator(); i.hasNext(); ) {
            Element employee = (Element) i.next();
            replaceAttr(styles,file,employee);
        }
    }

    private static Map<String, String> parseStyle(String themePath) {
        HashMap<String, String> styles = new HashMap<>();
        File inputXml = new File(themePath);
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(inputXml);
            Element rootElement = document.getRootElement();

            List<Element> elements = rootElement.elements();
            for (Element element : elements) {
                List<Attribute> attributes = element.attributes();
                for (Attribute attribute : attributes) {
                    if (attribute.getText().equals("CoreTheme")) {
                        List<Element> elements1 = element.elements();
                        for (Element element1 : elements1) {
                            styles.put(element1.attribute(0).getText(), element1.getText());
                        }
                    }
                }
            }

        } catch (DocumentException e) {
            System.out.println(e.getMessage());
        }
        return styles;
    }
}
