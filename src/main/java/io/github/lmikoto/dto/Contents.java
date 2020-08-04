package io.github.lmikoto.dto;

import lombok.Data;

/**
 * @author liuyang
 * 2020/8/4 5:58 下午
 */
@Data
public class Contents {

    private String name;

    private String path;

    private String sha;

    private String url;

    private String download_url;
}
