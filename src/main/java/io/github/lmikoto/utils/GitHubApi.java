package io.github.lmikoto.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.lmikoto.HttpUtils;
import io.github.lmikoto.JacksonUtils;
import io.github.lmikoto.dto.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GitHubApi {

    private String basUrl;

    private Map<String,String> header = Maps.newHashMap();

    private GitHubApi(){

    }

    private GitHubApi(String owner,String repo,String token){
        this.basUrl = "https://api.github.com/repos/" + owner + "/" + repo;
        this.header.put("Authorization","token "+ token);
    }


    public static GitHubApi getInstance(String owner,String repo,String token){
        return new GitHubApi(owner,repo,token);
    }

    public RefDto getRef(){
        return HttpClient.get(basUrl + "/git/refs/heads/master",header,RefDto.class);
    }

    public CommitDto getCommit(String sha){
        String result = HttpUtils.get(basUrl + "/git/commits/" + sha);
        return JacksonUtils.fromJson(result,CommitDto.class);
    }

    public CreateBlobResponse createBlob(String content,String encoding){
        Map<String,String> param = Maps.newHashMap();
        param.put("encoding",encoding);
        param.put("content",content);
        String result = HttpUtils.post(basUrl + "/git/blobs",param,header);
        return JacksonUtils.fromJson(result, CreateBlobResponse.class);
    }

    public CreateTreeResponse createTree(String baseTree, String path, String sha){
        Map<String,Object> param = Maps.newHashMap();
        Map<String,Object> tree = ImmutableMap.of("path",path,"mode","100644","type","blob","sha",sha);
        param.put("base_tree",baseTree);
        param.put("tree", Collections.singletonList(tree));
        return JacksonUtils.fromJson(HttpUtils.post(basUrl + "/git/trees",param,header), CreateTreeResponse.class);
    }

    public CreateTreeResponse createTree(String baseTree, List<Map<String,Object>> tree){
        Map<String,Object> param = Maps.newHashMap();
        param.put("base_tree",baseTree);
        param.put("tree", tree);
        return JacksonUtils.fromJson(HttpUtils.post(basUrl + "/git/trees",param,header), CreateTreeResponse.class);
    }

    public CreateCommitResponse createCommit(String commitSha,String treeSha){
        Map<String,Object> param = Maps.newHashMap();
        param.put("message","update post");
        param.put("tree", treeSha);
        param.put("parents", Collections.singletonList(commitSha));
        return JacksonUtils.fromJson(HttpUtils.post(basUrl + "/git/commits",param,header),CreateCommitResponse.class);
    }

    public UpdataRefResponse updataRef(String newCommitSha){
        Map<String,Object> param = Maps.newHashMap();
        param.put("sha",newCommitSha);
        param.put("force", true);
        return JacksonUtils.fromJson(HttpUtils.post(basUrl + "/git/refs/heads/master",param,header),UpdataRefResponse.class);
    }

    public void delete(String sha,String path,String message) {
        Map<String,String> param = Maps.newHashMap();
        param.put("sha",sha);
        param.put("message", message);
        String url = basUrl + "/contents/" + path;
        HttpClient.delete(url,JacksonUtils.toJson(param),header,Void.class);
    }

    public Contents getContents(String path){
        return HttpClient.get(basUrl + "/contents/" + path,Contents.class);
    }

}
