package com.daily.press;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.*;

/**
 * Hello world!
 */
public class App {
    private static final String MAIN_THEME = "/Users/lixinke/Documents/workspace/android/daily/MainBuilder/app/src/main/res/values/styles.xml";
    private static final String MODULE_PATH = "/Users/lixinke/Documents/workspace/android/daily/UserProject/userCenter/src/main/res/";

    public static void main(String[] args) {
        removeIvMaskColor();
        replaceDayAttr();
        createNightFolder();

//        renameNightFile(MODULE_PATH + "mipmap-night-xxhdpi");
//        renameNightFile(MODULE_PATH + "drawable-night-xxhdpi");
    }

    private static void renameNightFile(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                String name = file.getName();
                if (name.contains("_night")) {
                    name = name.replace("_night", "");
                    file.renameTo(new File(folderPath, name));
                }
            }
        }
    }

    private static void createNightFolder() {
        parseStyleable(MODULE_PATH + "values/attrs.xml");

    }

    private static void parseStyleable(String styleablePath) {
        Map<String, String> attrs = parseStyleable(styleablePath, "SupportUiMode");

        List<String> colors = new ArrayList<>();
        List<String> drawables = new ArrayList<>();

        Set<String> keySet = attrs.keySet();
        for (String key : keySet) {
            String value = attrs.get(key);
            if (value.contains("color")) {
                colors.add(key);
            } else {
                drawables.add(key);
            }
        }

        Map<String, String> nightStyles = parseStyle(MAIN_THEME, "AppThemeNight");
        createNightColor(colors, nightStyles);

        List<String> drawableValues = new ArrayList<>();
        for (String key : drawables) {

            if (nightStyles.get(key) != null && nightStyles.get(key).split("/").length > 1) {
                drawableValues.add(nightStyles.get(key).split("/")[1]);
            }

        }

        createNightDrawable(drawableValues);
        createNightMipmap(drawableValues);
    }


    private static void createNightMipmap(List<String> drawableValues) {
        File resFolder = new File(MODULE_PATH);
        File[] mipmaps = resFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith("mipmap");
            }
        });


        if (mipmaps != null && mipmaps.length > 0) {
            for (File mipmap : mipmaps) {
                String mipmapName = mipmap.getName();
                String[] temp = mipmapName.split("-");
                String last = temp[temp.length - 1];

                File[] files = mipmap.listFiles();
                if (files != null && files.length > 0) {

                    File nightFolder = new File(MODULE_PATH + "mipmap-night-" + last);
                    if (!nightFolder.exists()) {
                        nightFolder.mkdir();
                    }

                    for (File file : files) {
                        String name = file.getName();
                        String nameTemp = name.substring(0, name.lastIndexOf("."));
                        if (!name.endsWith(".xml") && drawableValues.contains(nameTemp)) {
                            file.renameTo(new File(nightFolder, file.getName()));
                        }
                    }
                }
            }
        }
    }

    private static void createNightDrawable(List<String> drawableValues) {


        File resFolder = new File(MODULE_PATH);
        File[] drawables = resFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith("drawable");
            }
        });

        if (drawables != null && drawables.length > 0) {
            for (File dir : drawables) {
                String dirName = dir.getName();
                String[] dirNames = dirName.split("-");
                String last = "";
                if (dirNames.length > 1) {
                    last = dirNames[dirNames.length - 1];
                    last = "-" + last;
                }
                File[] files = dir.listFiles();

                if (files != null && files.length > 0) {

                    File nightFolder = new File(MODULE_PATH + "drawable-night" + last);
                    if (!nightFolder.exists()) {
                        nightFolder.mkdir();
                    }

                    for (File file : files) {
                        String name = file.getName();
                        String nameTemp = name.substring(0, name.lastIndexOf("."));
                        if (drawableValues.contains(nameTemp)) {
                            file.renameTo(new File(nightFolder, file.getName()));
                            if (name.endsWith(".xml") && drawableValues.contains(nameTemp)) {
                                List<String> temp = parseSelector(file, "android:drawable");
                                for (String key : temp) {
                                    for (File sFile : files) {
                                        String sName = sFile.getName();
                                        if (key.equals(sName.substring(0, sName.lastIndexOf(".")))) {
                                            sFile.renameTo(new File(nightFolder, sFile.getName()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static List<String> parseSelector(File inputXml, String themeName) {

        List<String> drawables = new ArrayList<>();
        SAXReader saxReader = new SAXReader();
        if (!inputXml.exists()) {
            return drawables;
        }
        try {
            Document document = saxReader.read(inputXml);
            Element rootElement = document.getRootElement();

            List<Element> elements = rootElement.elements();
            for (Element element : elements) {
                List<Attribute> attributes = element.attributes();
                for (Attribute attribute : attributes) {
                    if (attribute.getQualifiedName().equals(themeName)) {
                        drawables.add(attribute.getValue().split("/")[1]);
                    }
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }


        return drawables;
    }

    private static void createNightColor(List<String> colors, Map<String, String> nightStyles) {

        Map<String, String> colorMap = parseColor(MODULE_PATH + "values/colors.xml");


        File valueNightFolder = new File(MODULE_PATH + "values-night");
        if (!valueNightFolder.exists()) {
            valueNightFolder.mkdir();
        }

        try {
            File color_night = new File(valueNightFolder, "colors.xml");
            if (!color_night.exists()) {
                color_night.createNewFile();
            }
            Document document = DocumentHelper.createDocument();
            Element element = document.addElement("resources");
            for (String color : colors) {
                String value = nightStyles.get(color);
                if (value != null && value.length() > 0) {
                    value = value.replaceAll("\\s*", "");

                    if(value.split("/").length>1){
                        String colorTemp = colorMap.get(value.split("/")[1]);
                        if (colorTemp != null && !colorTemp.equals("")) {
                            Element colorElement = element.addElement("color");
                            colorElement.addAttribute("name", color);
                            colorElement.setText(colorTemp);
                        }
                    }


                }
            }
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(new FileOutputStream(color_night), format);
            writer.write(document);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> parseColor(String path) {
        HashMap<String, String> dayStyles = new HashMap<>();
        File inputXml = new File(path);
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(inputXml);
            Element rootElement = document.getRootElement();

            List<Element> elements = rootElement.elements();
            for (Element element : elements) {
                List<Attribute> attributes = element.attributes();
                for (Attribute attribute : attributes) {
                    dayStyles.put(attribute.getValue(), element.getStringValue());
                }
            }

        } catch (DocumentException e) {
            System.out.println(e.getMessage());
        }
        return dayStyles;
    }


    /**
     * 替换图片夜间模式
     */
    private static void removeIvMaskColor() {
        try {
            File folder = new File(MODULE_PATH + "layout");
            File[] list = folder.listFiles();
            for (File file : list) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = null;
                StringBuffer buffer = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    if (line.contains("app:iv_maskColor=\"@android:color/transparent\"")) {
                        String start = line.substring(0, line.indexOf("app:iv_maskColor="));
                        String end = line.substring(line.lastIndexOf("\"") + 1, line.length());
                        line = start + end;
                    }

                    line = line.replaceAll("app:iv_maskColor", "app:maskColor");
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
    private static void replaceDayAttr() {
        Map<String, String> dayStyles = parseStyle(MAIN_THEME, "AppTheme");
        File folder = new File(MODULE_PATH + "layout");
        File[] files = folder.listFiles();

        for (File file : files) {
            if (!file.getName().endsWith(".xml")) {
                continue;
            }
            try {
                SAXReader saxReader = new SAXReader();

                Document document = saxReader.read(file);
                Element temp = document.getRootElement();
                replaceAttr(dayStyles, file, temp);
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
                writer.write(document);
                writer.close();
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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
            Element temp = (Element) i.next();
            replaceAttr(styles, file, temp);
        }
    }

    private static Map<String, String> parseStyle(String themePath, String themeName) {
        HashMap<String, String> dayStyles = new HashMap<>();
        File inputXml = new File(themePath);
        SAXReader saxReader = new SAXReader();

        try {
            Document document = saxReader.read(inputXml);
            Element rootElement = document.getRootElement();

            List<Element> elements = rootElement.elements();
            for (Element element : elements) {
                List<Attribute> attributes = element.attributes();
                for (Attribute attribute : attributes) {
                    if (attribute.getText().equals(themeName)) {
                        List<Element> elements1 = element.elements();
                        for (Element element1 : elements1) {
                            String name = element1.attribute(0).getText();
                            name = name.replaceAll("\\s*", "");
                            String value = element1.getText();
                            value = value.replaceAll("\\s*", "");
                            dayStyles.put(name, value);
                        }
                    }
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }


        return dayStyles;
    }

    private static Map<String, String> parseStyleable(String themePath, String themeName) {
        HashMap<String, String> dayStyles = new HashMap<>();
        File inputXml = new File(themePath);
        SAXReader saxReader = new SAXReader();

        try {
            Document document = saxReader.read(inputXml);
            Element rootElement = document.getRootElement();

            List<Element> elements = rootElement.elements();
            for (Element element : elements) {
                List<Attribute> attributes = element.attributes();
                for (Attribute attribute : attributes) {
                    if (attribute.getText().equals(themeName)) {
                        List<Element> elements1 = element.elements();
                        for (Element element1 : elements1) {
                            if (element1.attributes().size() > 1) {
                                String name = element1.attribute(0).getText();
                                name = name.replaceAll("\\s*", "");
                                String value = element1.attribute(1).getText();
                                value = value.replaceAll("\\s*", "");
                                dayStyles.put(name, value);
                            }
                        }
                    }
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }


        return dayStyles;
    }
}
