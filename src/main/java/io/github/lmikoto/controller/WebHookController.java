package io.github.lmikoto.controller;

import io.github.lmikoto.JacksonUtils;
import io.github.lmikoto.dto.YuqueRequestDto;
import io.github.lmikoto.service.CommitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;

@RestController
@Slf4j
public class WebHookController {

    @Autowired
    private CommitService commitService;

    private ExecutorService  executorService = Executors.newFixedThreadPool(1);

    @PostMapping("/yuque/webhook")
    public String webHook(@RequestBody YuqueRequestDto req){
        log.info(JacksonUtils.toJson(req));
        FutureTask<String> futureTask = new FutureTask<>(()->{
            commitService.uploadToGitHub(req.getData().getTitle(),req.getData().getBody());
            return "";
        });
        executorService.execute(futureTask);
        try {
            futureTask.get(06000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            futureTask.cancel(true);
        } catch (ExecutionException e) {
            futureTask.cancel(true);
        } catch (TimeoutException e) {
            futureTask.cancel(true);
        } finally {
        }
        return "";
    }
}
