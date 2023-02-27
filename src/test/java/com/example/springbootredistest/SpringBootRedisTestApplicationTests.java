package com.example.springbootredistest;

import com.example.springbootredistest.vo.RedisInfo;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@SpringBootTest
class SpringBootRedisTestApplicationTests {

	@Test
	void 테스트() {
		// 계속 반복
		while (true){
			try {
				Thread.sleep(1000); // 1초 마다 호출

				RedisInfo redisInfo = new RedisInfo();
				redisInfo.setKey("key");
				redisInfo.setValue("hello_3");
				// json 형태로 변환
				String json = new Gson().toJson(redisInfo);

				// post 요청
				HttpClient client = HttpClient.newHttpClient();
				HttpRequest request = HttpRequest.newBuilder()
						.uri(java.net.URI.create("http://localhost:8080/get"))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(json))
						.build();
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

				log.info("### 테스트 결과 => status : {} | response : {}", response.statusCode() , response.body());

			}catch (Exception e){
				log.error("### 테스트 에러 발생 => {}", e.getMessage());
			}
		}
	}

}
