package io.github.lmikoto.utils;

import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * @author liuyang
 * 2020/8/4 11:12 上午
 */
public class HttpClient {

    private static RestTemplate restTemplate = new RestTemplate();

    public static <T>  T delete(String url, String param, Map<String,String> headers,Class<T> response){
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach((k,v)->{
            httpHeaders.add(k,v);
        });
        HttpEntity<String> entity = new HttpEntity<>(param,httpHeaders);
        ResponseEntity<T> res = restTemplate.exchange(url, HttpMethod.DELETE,entity,response);
        return res.getBody();
    }

    @SneakyThrows
    public static <T>  T get(String url, Class<T> response){
        ResponseEntity<T> res = restTemplate.getForEntity(new URI(url),response);
        return res.getBody();
    }

    public static <T> T get(String url,Map<String,String> headers, Class<T> response){
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach((k,v)->{
            httpHeaders.add(k,v);
        });
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<T> res = restTemplate.exchange(url, HttpMethod.GET,entity,response);
        return res.getBody();
    }

}
