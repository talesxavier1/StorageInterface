package br.com.tx.storageInterface.services;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

import br.com.tx.storageInterface.SpringContext;

/**
 * Classe responsável po gerenciar o cache no redis.
 */
public class RedisOperationsService {

	/**
	 * Instância do redis.
	 */
	private static RedisTemplate<String, String> _redisTemplate;
	
	/**
	 * Inicia a instância do RedisTemplate.
	 * Utiliza o springContext para iniciar o serviço RedisSevice e obter uma instância do RedisTemplate.
	 */
	static {
		if (_redisTemplate == null) {
			var springContext = SpringContext.getSpringContext();
			var redisSevice = springContext.getBean(RedisSevice.class);
			_redisTemplate = redisSevice.getRedisService();
		}
	}

	/**
	 * Adiciona um novo cache com tempo de 5 horas de validade.
	 * 
	 * @param key   Chave do cache.
	 * @param value valor do cache.
	 */
	public static void addCache(String key, String value) {
		_redisTemplate.opsForValue().set(key, value, 5, TimeUnit.HOURS);
	}

	/**
	 * Adiciona um novo cache.
	 * 
	 * @param key      Chave do cache.
	 * @param value    valor do cache.
	 * @param time     Tempo de validade do cache.
	 * @param timeUnit Unidade do tempo de cache.
	 */
	public static void addCache(String key, String value, long time, TimeUnit timeUnit) {
		_redisTemplate.opsForValue().set(key, value, time, timeUnit);
	}

	/**
	 * 
	 * @param key Chave do cache.
	 * @return Retorna o valo do cache ou null quando não encontrado.
	 */
	public static String getCache(String key) {
		return _redisTemplate.opsForValue().get(key);
	}
}
