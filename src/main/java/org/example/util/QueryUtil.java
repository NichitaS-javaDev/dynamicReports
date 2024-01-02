package org.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class QueryUtil {
    public static String getQueryFromXML(String propFileName) throws IOException {
        Properties properties = readProperties(propFileName);
        return properties.getProperty("sql-query");
    }

    private static Properties readProperties(String xmlFileName) throws IOException {
        Properties properties = new Properties();
        InputStream stream = QueryUtil.class.getClassLoader().getResourceAsStream(xmlFileName);
        properties.loadFromXML(stream);
        return properties;
    }

}
