package io.github.lmikoto.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.lmikoto.HttpUtils;
import io.github.lmikoto.JacksonUtils;
import io.github.lmikoto.dto.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String result = HttpUtils.get(basUrl + "/git/refs/heads/master");
        return JacksonUtils.fromJson(result, RefDto.class);
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



    public static void main(String[] args) throws URISyntaxException {

        //
        String imageStr = "aaa ![image.png](https://cdn.nlark.com/yuque/0/2020/png/328252/1578129354411-201e2a74-5691-49e0-aec8-2fe4bb5212c8.png#align=left&display=inline&height=390&name=image.png&originHeight=390&originWidth=928&size=211466&status=done&style=none&width=928)aaa";

//        String a = HttpUtils.get("");

//        String url = "https://cdn.nlark.com/yuque/0/2020/png/328252/1578129354411-201e2a74-5691-49e0-aec8-2fe4bb5212c8.png#align=left&display=inline&height=390&name=image.png&originHeight=390&originWidth=928&size=211466&status=done&style=none&width=928";
////        System.out.println(a);
////        System.out.println();
//        BASE64Encoder encoder = new BASE64Encoder();
//
//        GitHubApi gitHubApi = GitHubApi.getInstance("lmikoto","api","571e5de395954f43219490f7ef6dc7ff04486e06");
//        RefDto refDto = gitHubApi.getRef();
//        CommitDto commitDto = gitHubApi.getCommit(refDto.getObject().getSha());
//        CreateBlobResponse createBlobResponse = gitHubApi.createBlob(Base64Utils.getImageStrFromUrl(url),"base64");
//        CreateTreeResponse createTreeResponse = gitHubApi.createTree(commitDto.getTree().getSha(),"src/b.png", createBlobResponse.getSha());
//        CreateCommitResponse createCommitResponse = gitHubApi.createCommit(refDto.getObject().getSha(),createTreeResponse.getSha());
//        UpdataRefResponse updataRefResponse = gitHubApi.updataRef(createCommitResponse.getSha());

        Pattern pattern = Pattern.compile("!\\[image.png]\\((.*?)\\)");
        Matcher matcher = pattern.matcher(imageStr);
        while (matcher.find()) {
            int i = 1;
            URI uri = new URI(matcher.group(i));
            String path = uri.getPath();
            String imageName = path.substring(path.lastIndexOf('/') + 1);
            System.out.println(imageName);
            i++;
        }
        System.out.println('e');

    }
}
