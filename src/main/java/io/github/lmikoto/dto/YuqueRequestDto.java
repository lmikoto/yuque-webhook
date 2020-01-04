package io.github.lmikoto.dto;

import lombok.Data;

@Data
public class YuqueRequestDto {

    private YuqueData data;

    @Data
    public class YuqueData{

        private String body;

        private String title;

        private String created_at;

        private String updated_at;
    }
}
