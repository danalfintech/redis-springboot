---
marp: true
class: invert
paginate: true
---

# Redis 설치 & 연동

---

# 목차

- 설치 방법
- 기본 사용법 (redis_cli)
- jedis vs lettuce
- sentinel 구축 및 테스트
- TODO
- 참고자료

---

# 설치 방법

requirements : gcc, make, python3

[https://download.redis.io/releases](https://download.redis.io/releases)
[https://download.redis.io/redis-stable.tar.gz](https://download.redis.io/redis-stable.tar.gz)

개발망으로 파일 전송

```
tar -xzvf redis-stable.tar.gz
cd redis-stable
```

dependency 빌드

```
cd deps
sudo make hdr_histogram hiredis jemalloc linenoise lua
cd ..
```

---

# 설치 방법

설치

```
sudo make
sudo make install
```

테스트

```
cd src
./redis-server
./redis-cli
```

---

# 기본 사용법 (redis_cli)

```
redis-cli ping
redis-cli -h {호스트} -p {포트}
set {key} {value}
get {key}
del {key}
flushall
dbsize
```

---

# jedis vs lettuce

## jedis

Jedis는 Redis를 위한 Java 클라이언트로서 성능과 사용 편의성을 고려하여 설계되었습니다.

## lettuce

Lettuce는 동기, 비동기 및 반응형 사용을 위한 확장 가능하며 스레드 안전한 Redis 클라이언트입니다. 여러 스레드는 BLPOP 및 MULTI/EXEC와 같은 블로킹 및 트랜잭션 작업을 피하면 하나의 연결을 공유할 수 있습니다. Lettuce는 Netty로 구축되었으며 Sentinel, Cluster, Pipelining, Auto-Reconnect 및 Redis 데이터 모델과 같은 고급 Redis 기능을 지원합니다.

---

# jedis vs lettuce

jedis 장점:

- 가벼운 라이브러리로, 이해하고 사용하기 쉬움.

jedis 단점:

- 싱글 스레드. thread safe X - JedisPool로 구현가능
- async 지원 X - Jedis Pipeline을 통해 구현 가능 but cluster와 함께 사용 불가

lettuce 장점:

- 더 빠르고 성능이 좋음
- async, clustering 등과 함께 사용하기에 더 적합

lettuce 단점:

- jedis 보다 복잡함
- 소스코드가 더 길어짐.

---

# jedis vs lettuce

- 기본 소스코드 비교 - jedis
- 기본 소스코드 비교 - lettuce
- jedis async (Pipeline)
- lettuce async
- jedis 멀티 쓰레드 (JedisPool)
- 성능 차이
- 결론

---

# jedis vs lettuce

## 기본 소스코드 비교 - jedis

```
import redis.clients.jedis.Jedis;

public class JedisSetGet {

    private static final String YOUR_CONNECTION_STRING = "redis://:foobared@yourserver:6379/0";

    public static void main(String[] args) {

        Jedis jedis = new Jedis(YOUR_CONNECTION_STRING);

        jedis.set("foo", "bar");
        String result = jedis.get("foo");

        jedis.close();

        System.out.println(result); // "bar"
    }
}
```

---

# jedis vs lettuce

## 기본 소스코드 비교 - lettuce

```
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class LettuceSetGet {

    private static final String YOUR_CONNECTION_STRING = "redis://:foobared@yourserver:6379/0";

    public static void main(String[] args) {
        RedisClient redisClient = RedisClient.create(YOUR_CONNECTION_STRING);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> sync = connection.sync();

        sync.set("foo", "bar");

        String result = sync.get("foo");

        connection.close();
        redisClient.shutdown();

        System.out.println(result); // "bar"
    }
}
```

---

# jedis vs lettuce

## jedis async (Pipeline)

```
Jedis jedis = new Jedis(YOUR_CONNECTION_STRING);

Pipeline p = jedis.pipelined();

p.set("foo", "bar");
Response<String> get = p.get("foo");

p.zadd("baz", 13, "alpha");
p.zadd("baz", 23, "bravo");
p.zadd("baz", 42, "charlie");
Response<Set<Tuple>> range = p.zrangeWithScores("baz", 0, -1);

p.sync();

jedis.close();

System.out.println(get.get()); // "bar"
System.out.println(range.get().stream()
        .map(Object::toString)
        .collect(Collectors.joining(" "))); // [alpha,13.0] [bravo,23.0] [charlie,42.0]
```

---

# jedis vs lettuce

## lettuce async

```
RedisClient redisClient = RedisClient.create(YOUR_CONNECTION_STRING);
StatefulRedisConnection<String, String> connection = redisClient.connect();
RedisAsyncCommands<String, String> async = connection.async();

final String[] result = new String[1];

async.set("foo", "bar")
        .thenComposeAsync(ok -> async.get("foo"))
        .thenAccept(s -> result[0] = s)
        .toCompletableFuture()
        .join();

connection.close();
redisClient.shutdown();

System.out.println(result[0]); // "bar"
```

sync를 사용해도 내부에서는 async로 동작 - 추가 확인 필요

---

# jedis vs lettuce

## jedis 멀티 쓰레드 (JedisPool)

```
JedisPool pool = new JedisPool(YOUR_CONNECTION_STRING);

List<String> allResults = IntStream.rangeClosed(1, 5)
        .parallel()
        .mapToObj(n -> {
            Jedis jedis = pool.getResource();

            jedis.set("foo" + n, "bar" + n);
            String result = jedis.get("foo" + n);

            jedis.close();

            return result;
        })
        .collect(Collectors.toList());

pool.close();

System.out.println(allResults); // "bar1, bar2, bar3, bar4, bar5"
```

---

# jedis vs lettuce

## 성능 차이

[https://jojoldu.tistory.com/418](https://jojoldu.tistory.com/418)

|                             | TPS     | Redis CPU | Redis Connection | 응답 속도 |
| :-------------------------: | ------- | --------- | ---------------- | --------- |
| jedis<br/>X connection pool | 31,000  | 20%       | 35               | 100ms     |
| jedis<br/>O connection pool | 55,000  | 69.5%     | 515              | 50ms      |
|           lettuce           | 100,000 | 7%        | 6                | 7.5ms     |

---

# jedis vs lettuce

## 결론

- async, cluster, 멀티 쓰레드 사용할까?
- jedis가 소스코드는 간단할 수 있을듯 하나 큰 차이는 없어보임.
- lettuce가 압도적으로 성능이 좋음.

---

# sentinel 구축 및 테스트

[https://co-de.tistory.com/15](https://co-de.tistory.com/15)

## redis-server 및 redis-sentinel 기동

```
cp redis.conf redis-6382.conf
cp redis.conf redis-6383.conf
cp redis.conf redis-6384.conf
cp sentinel.conf sentinel-5000.conf
cp sentinel.conf sentinel-5001.conf
cp sentinel.conf sentinel-5002.conf

src/redis-server redis-6382.conf
src/redis-server redis-6383.conf
src/redis-server redis-6384.conf
src/redis-sentinel sentinel-5000.conf
src/redis-sentinel sentinel-5001.conf
src/redis-sentinel sentinel-5002.conf
```

---

# sentinel 구축 및 테스트

## redis-server 및 redis-sentinel 설정파일

```
// redis_*.conf 설정파일
port {port} // 포트 설정
daemonize yes // 백그라운드에서 시작하도록 설정
logfile logs/redis_[각자포트].log // log 파일 남도록 설정
slaveof 127.0.0.1 6382 // 슬레이브 서버 설정 (redis_6383.conf, redis_6384.conf)
replicaof 127.0.0.1 6382 // 데이터 복제 관련 설정

// sentinel_*.conf 설정파일
port {port} // 포트 설정
daemonize yes // 백그라운드에서 시작하도록 설정
dir "/home/leekyusung/redis/redis-7.0.8"
logfile "logs/redis_[각자포트].log" // log 파일 남도록 설정
sentinel monitor mymaster 127.0.0.1 6382 2  // 감시할 마스터 정보 및 쿼럼(quorum) 설정
sentinel down-after-milliseconds mymaster 3000  // master에 정기적으로 PING 보내는 주기
```

---

# sentinel 구축 및 테스트

## SpringBoot source code

```
@Bean
public RedisConnectionFactory redisConnectionFactory(){
    RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration()
            .master("mymaster")
            .sentinel("localhost",5000)
            .sentinel("localhost",5001)
            .sentinel("localhost",5002);
    LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisSentinelConfiguration);
    return lettuceConnectionFactory;
}
```

---

# sentinel 구축 및 테스트

```
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
```

---

# TODO

- 정해야할 것들
  - jedice or lettuce
  - sentinel 세팅 (테스트서버, 실서버)
  - sync/async, 멀티스레드, cluster 고려?
- 운영을 위한 명령어, 모니터링 툴 찾아보기. O(N) 고려
- redis, sentinel configuration
- replication 관련 설정
- jedice/lettuce docs 정독, tuna에서 sentinel 연동해보기

---

# 참고자료

- [규성's notion](https://lilac-artichoke-06d.notion.site/redis-584662e1c4324a61a0f9d47d51339aa0)
- [github redis-springboot](https://github.com/danalfintech/redis-springboot)
- [github jedis-vs-lettuce](https://github.com/danalfintech/jedis-vs-lettuce)
