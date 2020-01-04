package io.github.lmikoto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefDto {

    private String ref;

    private String node_id;

    private String url;

    private RefObjDto object;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class RefObjDto {

        private String sha;

        private String type;

        private String url;
    }
}