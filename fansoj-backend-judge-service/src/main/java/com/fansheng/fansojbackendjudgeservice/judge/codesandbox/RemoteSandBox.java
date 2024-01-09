package com.fansheng.fansojbackendjudgeservice.judge.codesandbox;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.fansheng.fansojbackendcommon.common.ErrorCode;
import com.fansheng.fansojbackendcommon.exception.BusinessException;
import com.fansheng.fansojbackendmodel.model.dto.codesandbox.ExecuteQuestionRequest;
import com.fansheng.fansojbackendmodel.model.dto.codesandbox.ExecuteQuestionResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class RemoteSandBox implements CodeSandBox {
//    codeSandBox:
//    url: "http://192.168.64.133:8105/runCode"
//    type: "remote"

    private static String AUTH_HEADER = "fans";
    private static String AUTH_HEADER_KEY = "fansheng";


    @Override
    public ExecuteQuestionResponse doExecute(ExecuteQuestionRequest executeQuestionRequest) {
        String url = "http://192.168.64.133:8105/runCode";
        String jsonStr = JSONUtil.toJsonStr(executeQuestionRequest);
        System.out.println(AUTH_HEADER+AUTH_HEADER_KEY);
        String respStr = HttpUtil.createPost(url)
                .header(AUTH_HEADER,AUTH_HEADER_KEY)
                .body(jsonStr)
                .execute()
                .body();
        if(StringUtils.isBlank(respStr)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"remote sandbox error");
        }
        System.out.println(respStr);
        System.out.println("远程代码沙箱");
        return JSONUtil.toBean(respStr,ExecuteQuestionResponse.class);
    }
}
