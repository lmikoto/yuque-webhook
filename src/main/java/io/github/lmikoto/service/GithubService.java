package io.github.lmikoto.service;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.github.lmikoto.dto.*;
import io.github.lmikoto.utils.Base64Utils;
import io.github.lmikoto.utils.GitHubApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class GithubService {

    @Value("${github.owner}")
    private String owner;

    @Value("${github.repo}")
    private String repo;

    @Value("${github.token}")
    private String token;

    private static Pattern imagePattern = Pattern.compile("!\\[.*?]\\((.*?)\\)");

    private GitHubApi gitHubApi;

    @PostConstruct
    public void buildGithubApi(){
        gitHubApi = GitHubApi.getInstance(owner,repo,token);
    }


    public void syncToGitHub(YuqueRequestDto.YuqueData yuqueData){
        if(Type.UPDATE.equals(yuqueData.getWebhook_subject_type())){
            update(yuqueData);
        }else if (Type.PUBLISH.equals(yuqueData.getWebhook_subject_type())) {
            add(yuqueData);
        }else if(Type.DELETE.equals(yuqueData.getWebhook_subject_type())){
            delete(yuqueData);
        }
    }

    private void add(YuqueRequestDto.YuqueData yuqueData){
        update(yuqueData);
    }

    private void update(YuqueRequestDto.YuqueData yuqueData){
        String content = cleanContent(yuqueData.getBody());
        RefDto refDto = gitHubApi.getRef();
        CommitDto commitDto = gitHubApi.getCommit(refDto.getObject().getSha());

        // 提取图片单独上传
        Matcher matcher = imagePattern.matcher(content);
        List<BlobListDto> blobListDtoArrayList = Lists.newArrayList();
        while (matcher.find()) {
            int i = 1;
            try {
                String imageUrl = matcher.group(i);
                URI uri = new URI(imageUrl);
                String path = uri.getPath();
                String imageName = path.substring(path.lastIndexOf('/') + 1);
                String githubPath = "source/images/" + imageName;
                content = content.replace(imageUrl,"/images/" + imageName);
                CreateBlobResponse createBlobResponse = gitHubApi.createBlob(Base64Utils.getImageStrFromUrl(imageUrl),"base64");
                blobListDtoArrayList.add(new BlobListDto(createBlobResponse.getSha(),githubPath));
            }catch (Exception e){
                log.error("{}", Throwables.getStackTraceAsString(e));
            }
        }

        content = new StringBuffer("")
                .append("---\n")
                .append("title: ")
                .append(yuqueData.getTitle())
                .append("\n")
                .append("date: ")
                .append(yuqueData.getCreated_at())
                .append("\n---\n")
                .append(content)
                .toString();

        log.info("content is {}",content);
        CreateBlobResponse createBlobResponse = gitHubApi.createBlob(content,"utf-8");
        blobListDtoArrayList.add(new BlobListDto(createBlobResponse.getSha(), getFilePath(yuqueData.getTitle(),yuqueData.getId())));
        List<Map<String,Object>> treeMpas = Lists.newArrayList();
        blobListDtoArrayList.forEach(i->{
            treeMpas.add(ImmutableMap.of("path",i.getPath(),"mode","100644","type","blob","sha",i.getSha()));
        });
        CreateTreeResponse createTreeResponse = gitHubApi.createTree(commitDto.getTree().getSha(),treeMpas);
        CreateCommitResponse createCommitResponse = gitHubApi.createCommit(refDto.getObject().getSha(),createTreeResponse.getSha());
        gitHubApi.updataRef(createCommitResponse.getSha());
        log.info("upload end");
    }

    private void delete(YuqueRequestDto.YuqueData yuqueData){
        String path = getFilePath(yuqueData.getTitle(),yuqueData.getId());
        Contents contents = gitHubApi.getContents(path);
        gitHubApi.delete(contents.getSha(), path,"yuque-delete");
    }

    /**
     * 清洗掉内容里面的标签
     * @param content
     * @return
     */
    private String cleanContent(String content){
        content = content
                .replaceAll("<br \\/>","\n")
                .replaceAll("<a name=\".*\"></a>","");
        return content;
    }

    private String getFilePath(String title, String id){
        return "source/_posts/" + title + "-yuque-" + id  + ".md";
    }
}
