/**
 * Copyright © 2017 北京玖富普惠信息技术有限公司. All rights reserved.
 *
 * @Title:RedisConfig.java
 * @Prject: redis-utils
 * @Package: com.nfbank.common.utils.redis
 * @author: sunwei
 * @date: 2017年12月1日 下午1:46:46
 * @version: V1.0
 */
package com.nfbank.common.utils.redis;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Properties;


/**
 * Redis工具类,用于获取RedisPool. 参考官网说明如下： You shouldn't use the same instance from
 * different threads because you'll have strange errors. And sometimes creating
 * lots of Jedis instances is not good enough because it means lots of sockets
 * and connections, which leads to strange errors as well. A single Jedis
 * instance is not threadsafe! To avoid these problems, you should use
 * JedisPool, which is a threadsafe pool of network connections. This way you
 * can overcome those strange errors and achieve great performance. To use it,
 * init a pool: JedisPool pool = new JedisPool(new JedisPoolConfig(),
 * "localhost"); You can store the pool somewhere statically, it is thread-safe.
 * JedisPoolConfig includes a number of helpful Redis-specific connection
 * pooling defaults. For example, Jedis with JedisPoolConfig will close a
 * connection after 300 seconds if it has not been returned.
 * @author 孙威
 */
@Slf4j
public class RedisConfig {
    private static final byte[] lock = new byte[0];
    /**
     * 最大连接数
     */
    private static Integer maxTotal;
    /**
     * 最大空闲连接数
     */
    private static Integer maxIdle;
    /**
     * 初始化连接数量
     */
    private static Integer minIdle;
    /**
     * 表示borrowJedis实例等待间超等待间则直接抛JedisConnectionException
     */
    private static Long maxWaitMillis;
    /**
     * 超时
     */
    private static Integer timeout;
    /**
     * 重试连接次数
     */
    private static Integer retryNum;
    /**
     * borrow jedis实例是否提前进行alidate操作(检查连接可用性,采用ping命令)；true则jedis实例均用
     */
    private static Boolean testOnBorrow;
    /**
     * return给pool是否提前进行validate操作(检查连接可用性,采用ping命令)
     */
    private static Boolean testOnReturn;
    /**
     * idle状态监测用异步线程evict进行检查
     */
    private static Boolean testWhileIdle;
    private static String ip;
    private static Integer port;
    private static Properties pro;
    private static String password;
    /**
     * 数据库索引(默认存储在第0个)
     */
    private static int dbIndex;

    static {
        try {
            //双重验证
            if (pro == null) {
                synchronized (lock) {
                    if (pro == null) {
                        pro = new Properties();
                        InputStream stream = new RedisConfig().getClass().getClassLoader()
                                .getResourceAsStream("redis.properties");
                        pro.load(stream);
                        Integer maxTotal = Integer.parseInt(pro.getProperty("redis.pool.maxTotal", "10000"));
                        Integer maxIdle = Integer.parseInt(pro.getProperty("redis.pool.maxIdle", "10000"));
                        Integer minIdle = Integer.parseInt(pro.getProperty("redis.pool.minIdle", "100"));
                        Long maxWait = Long.parseLong(pro.getProperty("redis.pool.maxWait", "1000"));
                        Integer retryNum = Integer.parseInt(pro.getProperty("redis.pool.retryNum", "2"));
                        Integer timeout = Integer.parseInt(pro.getProperty("redis.pool.timeout", "10000"));
                        Boolean testOnBorrow = Boolean.parseBoolean(pro.getProperty("redis.pool.testOnBorrow", "true"));
                        Boolean testOnReturn = Boolean.parseBoolean(pro.getProperty("redis.pool.testOnReturn", "true"));
                        Boolean testWhileIdle = Boolean.parseBoolean(pro.getProperty("redis.pool.testWhileIdle", "true"));
                        int dbaIndex = Integer.parseInt(pro.getProperty("redis.index", "0"));
                        String ip = pro.getProperty("redis.ip");
                        Integer port = Integer.parseInt(pro.getProperty("redis.port"));
                        String password = pro.getProperty("redis.password");


                        setMaxTotal(maxTotal);
                        setMaxIdle(maxIdle);
                        setMinIdle(minIdle);
                        setMaxWaitMillis(maxWait);
                        setTimeout(timeout);
                        setRetryNum(retryNum);
                        setTestOnBorrow(testOnBorrow);
                        setTestOnReturn(testOnReturn);
                        setTestWhileIdle(testWhileIdle);
                        setIp(ip);
                        setPort(port);
                        setDbIndex(dbaIndex);
                        if (password != null & password.length() > 0) {
                            setPassword(password);
                        }

                        log.info("初始化Redis配置信息为：{}",JSON.toJSONString(pro));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Integer getMaxTotal() {
        return maxTotal;
    }

    public static void setMaxTotal(Integer maxTotal) {
        RedisConfig.maxTotal = maxTotal;
    }

    public static Integer getMaxIdle() {
        return maxIdle;
    }

    public static void setMaxIdle(Integer maxIdle) {
        RedisConfig.maxIdle = maxIdle;
    }

    public static Long getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public static void setMaxWaitMillis(Long maxWaitMillis) {
        RedisConfig.maxWaitMillis = maxWaitMillis;
    }

    public static Integer getTimeout() {
        return timeout;
    }

    public static void setTimeout(Integer timeout) {
        RedisConfig.timeout = timeout;
    }

    public static Integer getRetryNum() {
        return retryNum;
    }

    public static void setRetryNum(Integer retryNum) {
        RedisConfig.retryNum = retryNum;
    }

    public static String getIp() {
        return ip;
    }

    public static void setIp(String ip) {
        RedisConfig.ip = ip;
    }

    public static Integer getPort() {
        return port;
    }

    public static void setPort(Integer port) {
        RedisConfig.port = port;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        RedisConfig.password = password;
    }

    public static Integer getMinIdle() {
        return minIdle;
    }

    public static void setMinIdle(Integer minIdle) {
        RedisConfig.minIdle = minIdle;
    }

    public static Boolean getTestOnBorrow() {
        return testOnBorrow;
    }

    public static void setTestOnBorrow(Boolean testOnBorrow) {
        RedisConfig.testOnBorrow = testOnBorrow;
    }

    public static Boolean getTestOnReturn() {
        return testOnReturn;
    }

    public static void setTestOnReturn(Boolean testOnReturn) {
        RedisConfig.testOnReturn = testOnReturn;
    }

    public static Boolean getTestWhileIdle() {
        return testWhileIdle;
    }

    public static void setTestWhileIdle(Boolean testWhileIdle) {
        RedisConfig.testWhileIdle = testWhileIdle;
    }

    public static int getDbIndex() {
        return dbIndex;
    }

    public static void setDbIndex(int dbIndex) {
        RedisConfig.dbIndex = dbIndex;
    }

}
