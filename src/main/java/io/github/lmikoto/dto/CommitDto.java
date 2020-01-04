package io.github.lmikoto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitDto {

    private TreeDto tree;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class TreeDto{
        private String sha;
    }
}
