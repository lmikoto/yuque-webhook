package io.github.lmikoto.controller;

import io.github.lmikoto.JacksonUtils;
import io.github.lmikoto.dto.YuqueRequestDto;
import io.github.lmikoto.service.CommitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@Slf4j
public class WebHookController {

    @Autowired
    private CommitService commitService;

    private ExecutorService  executorService = Executors.newFixedThreadPool(1);

    @PostMapping("/yuque/webhook")
    public String webHook(@RequestBody YuqueRequestDto req){
        log.info(JacksonUtils.toJson(req));
        executorService.execute(()->{
            commitService.uploadToGitHub(req.getData().getTitle(),req.getData().getBody());
        });
        return "";
    }
}
