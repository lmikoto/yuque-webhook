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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CommitService {

    @Value("${github.owner}")
    private String owner;

    @Value("${github.repo}")
    private String repo;

    @Value("${github.token}")
    private String token;

    public void uploadToGitHub(String title,String originContent){
        String content = cleanContent(originContent);
        GitHubApi gitHubApi = GitHubApi.getInstance(owner,repo,token);
        RefDto refDto = gitHubApi.getRef();
        CommitDto commitDto = gitHubApi.getCommit(refDto.getObject().getSha());

        // 提取图片单独上传
        Pattern pattern = Pattern.compile("!\\[image.png]\\((.*?)\\)");
        Matcher matcher = pattern.matcher(content);
        List<BlobListDto> blobListDtoArrayList = Lists.newArrayList();
        while (matcher.find()) {
            int i = 1;
            try {
                String imageUrl = matcher.group(i);
                URI uri = new URI(imageUrl);
                String path = uri.getPath();
                String imageName = path.substring(path.lastIndexOf('/') + 1);
                String githubPath = "assets/" + imageName;
                content = content.replace(imageUrl,"/" + githubPath);
                CreateBlobResponse createBlobResponse = gitHubApi.createBlob(Base64Utils.getImageStrFromUrl(imageUrl),"base64");
                blobListDtoArrayList.add(new BlobListDto(createBlobResponse.getSha(),"content/" + githubPath));
            }catch (Exception e){
                log.error("{}", Throwables.getStackTraceAsString(e));
            }
            i++;
        }
        log.info("content is {}",content);
        CreateBlobResponse createBlobResponse = gitHubApi.createBlob(content,"utf-8");
        blobListDtoArrayList.add(new BlobListDto(createBlobResponse.getSha(),"content/blog/" + title  + ".md"));
        List<Map<String,Object>> treeMpas = Lists.newArrayList();
        blobListDtoArrayList.forEach(i->{
            treeMpas.add(ImmutableMap.of("path",i.getPath(),"mode","100644","type","blob","sha",i.getSha()));
        });
        CreateTreeResponse createTreeResponse = gitHubApi.createTree(commitDto.getTree().getSha(),treeMpas);
        CreateCommitResponse createCommitResponse = gitHubApi.createCommit(refDto.getObject().getSha(),createTreeResponse.getSha());
        gitHubApi.updataRef(createCommitResponse.getSha());
    }

    private String cleanContent(String content){
        content = content
                .replaceAll("<br \\/>","\n")
                .replaceAll("<a name=\".*\"></a>","");
        return content;
    }
}
