package com.demo.java.xposed.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtils {

    /**
     * 修改对象中指定的 private final 字段的值
     *
     * @param target    需要修改字段值的对象
     * @param fieldName 字段的名称
     * @param newValue  新的字段值，可以是 String 或 Integer
     * @throws NoSuchFieldException   如果指定的字段不存在
     * @throws IllegalAccessException 如果无法访问或修改指定的字段
     */
    public static void setPrivateFinalField(Object target, String fieldName, Object newValue) {
        try {
            if (target == null || fieldName == null || newValue == null) {
                LogUtils.show("setPrivateFinalField: " + target + fieldName + newValue);
                return;
            }
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);

            if (field.getType() == int.class && newValue instanceof Integer) {
                field.setInt(target, (Integer) newValue);
            } else {
                field.set(target, newValue);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LogUtils.show("setPrivateFinalField: " + fieldName + newValue + e.getMessage());
        }

    }


    /**
     * 获取所有符合指定条件的字段
     *
     * @param clazz              目标类
     * @param isPublic           字段是否为public
     * @param isFinal            字段是否为final
     * @param isPrimitive        字段是否为基础类型
     * @param classIsPublic      类是否为public
     * @param classIsFinal       类是否为final

     * @return 符合条件的字段列表
     */
    public static List<Field> getFieldsByConditions(Class<?> clazz, boolean isPublic, boolean isFinal, boolean isPrimitive, boolean classIsPublic,
                                                    boolean classIsFinal) {
        List<Field> resultFields = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {

            int modifiers = field.getModifiers();

            boolean fieldIsPublic = Modifier.isPublic(modifiers);
            boolean fieldIsFinal = Modifier.isFinal(modifiers);

            boolean fieldIsPrimitive = field.getType().isPrimitive();

            Class fieldClazz = field.getType();
            int classModifiers = fieldClazz.getModifiers();
            boolean isClassPublic = Modifier.isPublic(classModifiers);
            boolean isClassFinal = Modifier.isFinal(classModifiers);


            // 根据传入的条件进行字段过滤
            if ((isPublic && fieldIsPublic || !isPublic && !fieldIsPublic) &&
                    (isFinal && fieldIsFinal || !isFinal && !fieldIsFinal) &&
                    (isPrimitive && fieldIsPrimitive || !isPrimitive && !fieldIsPrimitive) &&((classIsPublic && isClassPublic || !classIsPublic && !isClassPublic) &&
                    (classIsFinal && isClassFinal || !classIsFinal && !isClassFinal)
                   )
            ) {
                LogUtils.show("class name="+clazz.getName()+ " filed name="+field.getName() +" fieldIsPublic= " + fieldIsPublic + " fieldIsFinal= " + fieldIsFinal + " fieldIsPrimitive= " + fieldIsPrimitive + " classIsPublic= " + isClassPublic + " classIsFinal= " + isClassFinal );

                resultFields.add(field);
            }
        }
        return resultFields;
    }

}


