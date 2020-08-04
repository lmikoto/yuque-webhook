package io.github.lmikoto.dto;

import lombok.Data;

@Data
public class YuqueRequestDto {

    private YuqueData data;

    @Data
    public class YuqueData{

        private String id;

        private String body;

        private String title;

        private String created_at;

        private String updated_at;

        private String webhook_subject_type;
    }
}
