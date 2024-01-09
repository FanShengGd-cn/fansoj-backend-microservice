package com.fansheng.fansojbackendjudgeservice.judge.codesandbox;


import com.fansheng.fansojbackendmodel.model.dto.codesandbox.ExecuteQuestionRequest;
import com.fansheng.fansojbackendmodel.model.dto.codesandbox.ExecuteQuestionResponse;

public interface CodeSandBox {
    public ExecuteQuestionResponse doExecute(ExecuteQuestionRequest executeQuestionRequest);
}
