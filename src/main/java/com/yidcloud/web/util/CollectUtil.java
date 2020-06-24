package com.yidcloud.web.util;


import com.lsy.base.utils.ClazzConverHelper;

import java.util.ArrayList;
import java.util.List;

public class CollectUtil {
    /**
     * 根据方法名称获取类名称
     * @return
     * @param
     * @since  2017/11/13  17:11
     */
    public static String getClazzPath(String javaMethod){
        return javaMethod.substring(0,javaMethod.lastIndexOf("."));
    }

    /**
     * 根据方法名称获取类名称
     * @return
     * @param
     * @since  2017/11/13  17:11
     */
    public static String getMethodPath(String javaMethod){
        return javaMethod.substring(javaMethod.lastIndexOf(".")+1,javaMethod.length());
    }
    /**
     * 获取入参的类型
     * @return
     * @param
     * @since  2017/11/13  21:06
     */
    public static Class [] getMethodTypes(String typeString){
        List<Class> returnClass = new ArrayList<>();
        String [] types = typeString.split(",");
        for (int i=0;i<types.length;i++){
            returnClass.add(ClazzConverHelper.getClazz(types[i]));
        }
        return returnClass.toArray(new Class[types.length]);
    }
}
