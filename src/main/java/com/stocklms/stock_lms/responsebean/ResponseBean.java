package com.stocklms.stock_lms.responsebean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Standard API response wrapper matching the teacher's sample structure.
 * Every response contains errorCode, errorDesc, and payload data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseBean {

    private String errorCode;
    private String errorDesc;
    private Object data;

    public static ResponseBean success(String errorCode, String errorDesc, Object data) {
        return new ResponseBean(errorCode, errorDesc, data);
    }

    public static ResponseBean error(String errorCode, String errorDesc, Object data) {
        return new ResponseBean(errorCode, errorDesc, data);
    }
}
