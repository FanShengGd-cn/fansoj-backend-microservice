package com.fansheng.fansojbackendserviceclient.service;


import com.fansheng.fansojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "fansoj-backend-judge-service", path = "/api/judge/inner")
public interface JudgeFeignClient {
    @PostMapping("/doJudge")
    QuestionSubmit doJudge(@RequestParam(value = "questionSubmitId") Long questionSubmitId);
}
