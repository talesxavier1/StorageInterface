package br.com.tx.storageInterface.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisSevice {
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	public RedisTemplate<String, String> getRedisService() {
		return this.redisTemplate;
	}

}
