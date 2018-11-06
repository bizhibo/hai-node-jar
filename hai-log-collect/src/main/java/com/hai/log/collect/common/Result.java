package com.hai.log.collect.common;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.ModelAndView;

public class Result extends ModelAndView {

    public String toJson() {
        return JsonUtils.toJson(this.getModel());
    }

    public void setMessage(String message) {
        this.addObject("message", message);
    }

    public void setCode(int code) {
        this.addObject("code", code);
    }

    public void setFlag(boolean flag) {
        this.addObject("flag", flag);
    }

    public void setData(Object data) {
        this.addObject("data", data);
    }

    public void setException(Throwable exception) {
        this.addObject("exception", exception);
    }

    public static Result build(int code, boolean flag, String message, Object data) {
        Result result = new Result();
        result.setCode(code);
        result.setFlag(flag);
        if (StringUtils.isNotBlank(message)) {
            result.setMessage(message);
        }
        if (data != null) {
            result.setData(data);
        }
        return result;
    }

    public static Result buildSuccessResult(String message) {
        return build(200, true, message, null);
    }

    public static Result buildSuccessResult(String message, Object data) {
        return build(200, true, message, data);
    }

    public static Result buildFaileResult(String message) {
        return build(500, false, message, null);
    }

}
