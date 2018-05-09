package com.wzc.httpServer.common;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * 一个简单的通用json解析
 *
 * @author wangzhencheng
 */
public class CustomJsonMapper extends ObjectMapper {

    private static CustomJsonMapper customJsonMapper = new CustomJsonMapper();
    private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public CustomJsonMapper() {
        this(null, null);
    }

    public CustomJsonMapper(String[] ignoredNames) {
        this(ignoredNames, null);
    }

    public CustomJsonMapper(String[] ignoredNames, String[] containedNames) {
        super();
        init();
    }

    private void init() {
        setSerializationInclusion(Include.NON_NULL);
        // 允许单引号、允许不带引号的字段名称
        configure(Feature.ALLOW_SINGLE_QUOTES, true);
        configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // // 进行HTML解码。
        // registerModule(new SimpleModule().addSerializer(String.class,
        // new JsonSerializer<String>() {
        // @Override
        // public void serialize(String value, JsonGenerator jgen,
        // SerializerProvider provider) throws IOException,
        // JsonProcessingException {
        // jgen.writeString(StringEscapeUtils.unescapeHtml(value));
        // }
        // }));
        // 设置时区
        setTimeZone(TimeZone.getDefault());// getTimeZone("GMT+8:00")
        // 日期格式化
        setDateFormat(sdf);
    }


    public static CustomJsonMapper getDefault() {
        return customJsonMapper;
    }

    /**
     * 先将fromValue转字符串，然后再转obj。
     *
     * @param fromValue
     * @param toValueType
     * @return
     */
    public <T> T convertToJsonThenObj(Object fromValue, Class<T> toValueType) {
        try {
            String str = writeValueAsString(fromValue);
            return readValue(str, toValueType);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public <T> T convertToJsonThenObj(Object fromValue, JavaType toValueType) {
        try {
            String str = writeValueAsString(fromValue);
            return readValue(str, toValueType);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public JavaType getJavaTypeNested(Class... classes) {
        if (null == classes || classes.length <= 1) {
            return null;
        }
        JavaType lastJavaType = null;
        lastJavaType = getTypeFactory().constructParametricType(classes[classes.length - 2], classes[classes.length - 1]);
        for (int i = classes.length - 3; i >= 0; i--) {
            lastJavaType = getTypeFactory().constructParametricType(classes[i], lastJavaType);
        }

        return lastJavaType;
    }

    public JavaType getJavaType(Class<?> parametrized,
                                Class... parameterClasses) {
        return getTypeFactory().constructParametricType(parametrized,
                parameterClasses);
    }

    public String prettyJson(Object obj) {
        if (null == obj) {
            return null;
        }
        try {
            if (obj instanceof String) {
                return prettyJson(readValue(String.valueOf(obj), HashMap.class));
            }
            return writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public String writeValueAsString(Object arg0) {
        // TODO Auto-generated method stub
        if (arg0 == null) return null;
        if (arg0 instanceof String) return String.valueOf(arg0);

        try {
            return super.writeValueAsString(arg0);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> T readValue(String src, Class<T> valueType) {
        // TODO Auto-generated method stub
        if (src == null) return null;
        try {
            return super.readValue(src, valueType);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public <T> T readValue(String src, JavaType valueType) {
        // TODO Auto-generated method stub
        if (src == null) return null;
        try {
            return super.readValue(src, valueType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T readValue(String src, Class arg0, Class... arg1) {
        // TODO Auto-generated method stub
        if (src == null) return null;
        try {
            return super.readValue(src, getTypeFactory()
                    .constructParametricType(arg0, arg1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
