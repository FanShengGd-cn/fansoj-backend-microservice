package com.fansheng.fansojbackendjudgeservice.judge.proxy;


import com.fansheng.fansojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import com.fansheng.fansojbackendmodel.model.dto.codesandbox.ExecuteQuestionRequest;
import com.fansheng.fansojbackendmodel.model.dto.codesandbox.ExecuteQuestionResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SandBoxProxy implements CodeSandBox {
    private final CodeSandBox codeSandBox;

    public SandBoxProxy(CodeSandBox codeSandBox) {
        this.codeSandBox = codeSandBox;
    }

    @Override
    public ExecuteQuestionResponse doExecute(ExecuteQuestionRequest executeQuestionRequest) {
        log.info("代理模式方法执行前");
        ExecuteQuestionResponse executeQuestionResponse = this.codeSandBox.doExecute(executeQuestionRequest);
        log.info("代理模式方法执行后");
        return executeQuestionResponse;
    }
}
