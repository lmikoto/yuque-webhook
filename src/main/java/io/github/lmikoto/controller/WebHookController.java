package io.github.lmikoto.controller;

import io.github.lmikoto.JacksonUtils;
import io.github.lmikoto.dto.YuqueRequestDto;
import io.github.lmikoto.service.GithubService;
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
    private GithubService githubService;

    private ExecutorService  executorService = Executors.newFixedThreadPool(1);

    @PostMapping("/yuque/webhook")
    public String webHook(@RequestBody Object req){
        YuqueRequestDto dto = JacksonUtils.convert(req,YuqueRequestDto.class);
        log.info(JacksonUtils.toJson(req));

        // 防止语雀重复推送
        executorService.execute(()->{
            githubService.syncToGitHub(dto.getData());
        });
        return "";
    }
}
