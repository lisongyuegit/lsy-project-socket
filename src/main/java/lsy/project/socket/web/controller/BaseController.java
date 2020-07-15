package lsy.project.socket.web.controller;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.lsy.base.date.DateHelper;
import com.lsy.base.string.StringHelper;
import com.lsy.base.utils.ConvertHelper;
import com.lsy.mybatisplus.plugins.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class BaseController {


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // String类型转换，将所有传递进来的String进行HTML编码，防止XSS攻击
        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {

            @Override
            public void setAsText(String text) {
                //setValue(text == null ? null : StringEscapeUtils.escapeHtml4(text.trim()));
                setValue(text == null ? null : text.trim());
            }

            @Override
            public String getAsText() {
                Object value = getValue();
                return value != null ? value.toString() : "";
            }
        });

        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {

            @Override
            public void setAsText(String text) {
                setValue(DateHelper.parseString(text));
            }
        });

        // Timestamp 类型转换
        binder.registerCustomEditor(Timestamp.class, new PropertyEditorSupport() {

            @Override
            public void setAsText(String text) {
                Date date = DateHelper.parseString(text);
                setValue(date == null ? null : new Timestamp(date.getTime()));
            }
        });
    }


    public int getIntParam(String field, int defaultValue) {
        return StringHelper.strTrim(field, defaultValue);
    }

    public int getIntParam(Map<String, Object> map, String field) {
        if (null == map || map.isEmpty()) {
            return 0;
        }
        return ConvertHelper.strToInt(map.get(field).toString());
    }

    public String getStrParam(Map<String, Object> map, String field) {
        if (null == map || map.isEmpty()) {
            return "";
        }
        return map.get(field).toString();
    }

    public String getStrParam(Map<String, Object> map, String field, String defaultValue) {
        if (null == map || map.isEmpty()) {
            return "";
        }
        if (StringHelper.isBlank(map.get(field).toString())) {
            map.put(field, defaultValue);
        }
        return map.get(field).toString();
    }

    public int getIntParam(Map<String, Object> map, String field, int defaultValue) {
        if (null == map || map.isEmpty()) {
            return 0;
        }
        if (StringHelper.isBlank(map.get(field).toString())) {
            map.put(field, defaultValue);
        }
        return ConvertHelper.strToInt(map.get(field).toString());
    }

    /**
     * 构造分页返回VO对象
     *
     * @param page
     * @param rClass 返回对象的class类
     * @param <T>    构造前的数据对象
     * @param <R>    构造后的返回对象
     * @return
     */
    protected <T, R> Page<R> cstructPageResultVO(Page<T> page, Class<R> rClass) {
        Page<R> resultPage = new Page<>();
        BeanUtils.copyProperties(page, resultPage);
        List<T> tList = page.getRecords();
        if (CollectionUtils.isNotEmpty(tList)) {
            List<R> rList = new ArrayList<>();
            for (T t : tList) {
                R r = null;
                try {
                    r = rClass.newInstance();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
                BeanUtils.copyProperties(t, r);
                rList.add(r);
            }
            resultPage.setRecords(rList);
        }
        return resultPage;
    }

}
