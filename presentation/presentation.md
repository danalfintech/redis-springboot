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

---

# sentinel 구축 및 테스트

---

# TODO

- 운영을 위한 명령어, 모니터링 툴 찾아보기
- redis, sentinel configuration

---

# 참고자료

- [규성's notion](https://lilac-artichoke-06d.notion.site/redis-584662e1c4324a61a0f9d47d51339aa0)
- [github redis-springboot](https://github.com/danalfintech/redis-springboot)
- [github jedis-vs-lettuce](https://github.com/danalfintech/jedis-vs-lettuce)
