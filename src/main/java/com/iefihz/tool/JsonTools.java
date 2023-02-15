package com.iefihz.tool;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 封装json工具类（jackson）
 *
 * @author He Zhifei
 * @date 2020/7/18 0:28
 */
public class JsonTools {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Java对象转字Json字符
     * @param o
     * @return
     */
    public static String toString(Object o) {
        if (o instanceof String) {
            return o.toString();
        }
        try {
            return MAPPER.writeValueAsString(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 把InputStream的内容转为JavaBean
     * @param inputStream
     * @param clz
     * @param <T>
     * @return
     */
    public static <T> T toBean(InputStream inputStream, Class<T> clz) {
        try {
            return MAPPER.readValue(inputStream, clz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Json字符串转JavaBean
     * @param json
     * @param clz
     * @param <T>
     * @return
     */
    public static <T> T toBean(String json, Class<T> clz) {
        try {
            return MAPPER.readValue(json, clz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Json字符串转List
     * @param json
     * @param clz
     * @param <T>
     * @return
     */
    public static  <T> List<T> toList(String json, Class<T> clz) {
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory().constructCollectionType(List.class, clz));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Json字符串转Map
     * @param json
     * @param kClz
     * @param vClz
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> toMap(String json, Class<K> kClz, Class<V> vClz) {
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory().constructMapType(Map.class, kClz, vClz));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
