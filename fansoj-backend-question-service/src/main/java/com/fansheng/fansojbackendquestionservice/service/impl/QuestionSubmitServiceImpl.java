package com.fansheng.fansojbackendquestionservice.service.impl;


import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.fansheng.fansojbackendcommon.common.ErrorCode;
import com.fansheng.fansojbackendcommon.constant.CommonConstant;
import com.fansheng.fansojbackendcommon.exception.BusinessException;
import com.fansheng.fansojbackendcommon.exception.ThrowUtils;
import com.fansheng.fansojbackendcommon.utils.SqlUtils;
import com.fansheng.fansojbackendmodel.model.dto.questionSubmit.QuestionSubmitAddRequest;
import com.fansheng.fansojbackendmodel.model.dto.questionSubmit.QuestionSubmitQueryRequest;
import com.fansheng.fansojbackendmodel.model.entity.Question;
import com.fansheng.fansojbackendmodel.model.entity.QuestionSubmit;
import com.fansheng.fansojbackendmodel.model.entity.User;
import com.fansheng.fansojbackendmodel.model.enums.QuestionSubmitLanguageEnum;
import com.fansheng.fansojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.fansheng.fansojbackendmodel.model.vo.QuestionSubmitVO;
import com.fansheng.fansojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.fansheng.fansojbackendquestionservice.rabbitmq.MyMessageProducer;
import com.fansheng.fansojbackendquestionservice.service.QuestionService;
import com.fansheng.fansojbackendquestionservice.service.QuestionSubmitService;
import com.fansheng.fansojbackendserviceclient.service.JudgeFeignClient;
import com.fansheng.fansojbackendserviceclient.service.UserFeignClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fansheng
 * @description 针对表【question_submit(题目提交表)】的数据库操作Service实现
 * @createDate 2023-12-22 09:18:08
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {
    @Resource
    public QuestionService questionService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private JudgeFeignClient judgeFeignClient;

    @Resource
    private MyMessageProducer myMessageProducer;
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionSubmitAddRequest.getQuestionId());
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(questionSubmitAddRequest.getLanguage());
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "语言类型错误");
        }

        long userId = loginUser.getId();
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setLanguage(questionSubmitAddRequest.getLanguage());
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setQuestionId(questionSubmitAddRequest.getQuestionId());
        questionSubmit.setUserId(userId);
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        Long questionSubmitId = questionSubmit.getId();
        myMessageProducer.sendMessage("code_exchange", "my_routingKey",String.valueOf(questionSubmitId));

        // 异步执行判题
//        CompletableFuture.runAsync(()->{
//            judgeFeignClient.doJudge(questionSubmit.getId());
//        });
        return questionSubmitId;
    }

    @Override
    public void validQuestionSubmit(QuestionSubmit questionSubmit, boolean add) {
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        Long questionId = questionSubmit.getQuestionId();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(code, language) || ObjectUtils.isEmpty(questionId), ErrorCode.PARAMS_ERROR);
            // 有参数则校验
            if (StringUtils.isNotBlank(code) && code.length() > 16384) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码过长");
            }
        }
        QuestionSubmitLanguageEnum enumByValue = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (enumByValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "语言类型错误");
        }
        if (questionId != null) {
            Long count = Db.lambdaQuery(Question.class).eq(Question::getId, questionId).count();
            if (count != 1) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
    }

    /**
     * 获取查询包装类
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        String code = questionSubmitQueryRequest.getCode();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 拼接查询条件
        if (StringUtils.isNotBlank(language)) {
            QuestionSubmitLanguageEnum enumByValue = QuestionSubmitLanguageEnum.getEnumByValue(language);
            if (enumByValue == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "语言类型错误");
            }
            queryWrapper.like("language", language);
        }
        queryWrapper.like(StringUtils.isNotBlank(code), "code", code);
        queryWrapper.like(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        long userId = loginUser.getId();
        // 脱敏，仅本人和管理员能看到用户代码
        if (questionSubmit.getUserId() != userId && !userFeignClient.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
                .collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }
}




