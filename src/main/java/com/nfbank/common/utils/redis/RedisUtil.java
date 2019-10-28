/**
 * Copyright © 2017 北京玖富普惠信息技术有限公司. All rights reserved.
 *
 * @Title: RedisUtil.java
 * @Prject: redis-utils
 * @Package: com.nfbank.common.utils.redis
 * @author: sunwei
 * @date: 2017年12月1日 下午1:46:46
 * @version: V1.0
 */
package com.nfbank.common.utils.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.*;
import redis.clients.util.SafeEncoder;

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
 *
 * @author wujintao
 */
@Slf4j
public class RedisUtil {

    /**
     * 锁
     */
    private static final byte[] lock = new byte[0];

    /**
     * 私有构造器.
     */
    private RedisUtil() {

    }

    /**
     * 存储redisPool实例对象
     */
    private static Map<String, JedisPool> maps = new HashMap<String, JedisPool>();


    /**
     * 操作Key的方法
     */
    public Keys KEYS = new Keys();
    /**
     * 对存储结构为String类型的操作
     */
    public Strings STRINGS = new Strings();
    /**
     * 对存储结构为List类型的操作
     */
    public Lists LISTS = new Lists();
    /**
     * 对存储结构为Set类型的操作
     */
    public Sets SETS = new Sets();
    /**
     * 对存储结构为HashMap类型的操作
     */
    public Hash HASH = new Hash();
    /**
     * 对存储结构为Set(排序的)类型的操作
     */
    public SortSet SORTSET = new SortSet();

    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例 没有绑定关系，而且只有被调用到时才会装载，从而实现了延迟加载。
     */
    private static class RedisUtilHolder {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static RedisUtil instance = new RedisUtil();
    }

    /**
     * 当getInstance方法第一次被调用的时候，它第一次读取
     * RedisUtilHolder.instance，导致RedisUtilHolder类得到初始化；而这个类在装载并被初始化的时候，会初始化它的静
     * 态域，从而创建RedisUtil的实例，由于是静态的域，因此只会在虚拟机装载类的时候初始化一次，并由虚拟机来保证它的线程安全性。
     * 这个模式的优势在于，getInstance方法并没有被同步，并且只是执行一个域的访问，因此延迟初始化并没有增加任何访问成本。
     */
    public static RedisUtil getInstance() {
        return RedisUtilHolder.instance;
    }

    /**
     * 获取连接池.
     *
     * @return 连接池实例
     */
    public JedisPool getPool() {
        String ip = RedisConfig.getIp();
        Integer port = RedisConfig.getPort();
        JedisPool pool = jedisPoolConfig(ip, port);
        return pool;
    }

    /**
     * 获取连接池
     *
     * @param ip   ip地址
     * @param port 端口
     * @return 连接池对象
     */
    public JedisPool getPool(String ip, int port) {
        JedisPool pool = jedisPoolConfig(ip, port);
        return pool;
    }

    /**
     * getJedis:获取redis实例对象. <br/>
     *
     * @return
     * @author sw
     * @since JDK 1.7
     */
    public Jedis getJedis() {
        String ip = RedisConfig.getIp();
        int port = RedisConfig.getPort();
        return getJedisByCondition(ip, port);
    }

    /**
     * 获取Jedis对象
     *
     * @param ip   ip地址
     * @param port 端口
     * @return 连接池中的Jedis对象
     */
    public Jedis getJedis(String ip, int port) {
        return getJedisByCondition(ip, port);
    }

    /**
     * 释放redis实例到连接池.
     *
     * @param jedis redis实例
     * @param ip    ip地址
     * @param port  端口
     */
    @Deprecated
    public void returnJedis(Jedis jedis, String ip, int port) {
        if (jedis != null) {
            getPool(ip, port).returnResource(jedis);
        }
    }

    /**
     * 释放redis实例到连接池.
     *
     * @param jedis redis实例
     */
    public void returnJedis(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }


    /**
     * jedisPoolConfig:连接池的配置 <br/>
     *
     * @param ip   所要连接的redis ip
     * @param port 连接端口
     * @return
     */
    private JedisPool jedisPoolConfig(String ip, Integer port) {
        String key = ip + ":" + port;
        JedisPool pool = null;
        if (!maps.containsKey(key)) {
            synchronized (lock) {
                if (!maps.containsKey(key)) {

                    JedisPoolConfig config = new JedisPoolConfig();
                    //最大连接数
                    config.setMaxTotal(RedisConfig.getMaxTotal());
                    // 最大空闲连接数
                    config.setMaxIdle(RedisConfig.getMaxIdle());
                    // 最大等待时间
                    config.setMaxWaitMillis(RedisConfig.getMaxWaitMillis());
                    // 在获取连接的时候检查有效性,默认false
                    config.setTestOnBorrow(RedisConfig.getTestOnBorrow());

                    config.setTestOnReturn(RedisConfig.getTestOnReturn());
                    config.setTestWhileIdle(RedisConfig.getTestWhileIdle());
                    config.setMinIdle(RedisConfig.getMinIdle());
                    try {
                        /**
                         * 如果你遇到 java.net.SocketTimeoutException: Read timed out
                         * exception的异常信息 请尝试在构造JedisPool的时候设置自己的超时值.
                         * JedisPool默认的超时时间是2秒(单位毫秒)
                         */
                        if (RedisConfig.getPassword() != null) {
                            pool = new JedisPool(config, ip, port, RedisConfig.getTimeout(), RedisConfig.getPassword(), RedisConfig.getDbIndex());
                        } else {
                            pool = new JedisPool(config, ip, port, RedisConfig.getTimeout());
                        }
                        //测试是否可以获取对象
                        Jedis jedis = pool.getResource();
                        if (jedis != null) {
                            //关闭
                            returnJedis(jedis);

                            maps.put(key, pool);
                        }
                    } catch (Exception e) {
                        if (pool != null) {
                            pool.close();
                        }
                    }
                }
            }
        } else {
            pool = maps.get(key);
        }
        return pool;
    }

    /**
     * getJedisByCondition:根据条件获取Redis对象. <br/>
     *
     * @param ip
     * @param port
     * @return
     * @author sw
     * @since JDK 1.7
     */
    private Jedis getJedisByCondition(String ip, int port) {
        Jedis jedis = null;
        //用来保存获取实例对象的次数
        int count = 0;
        do {
            try {
                jedis = getPool(ip, port).getResource();
            } catch (Exception e) {
                log.error("get redis master1 failed!", e);
                // 销毁对象
                returnJedis(jedis);
            }
            count++;
        } while (jedis == null && count < RedisConfig.getRetryNum());

        return jedis;
    }


    public class Keys {
        /**
         * 清空redis中所有数据
         *
         * @return 状态码
         */
        public String flushAll() {
            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                return jedis.flushAll();
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 更改key
         *
         * @param oldkey
         * @param newkey
         * @return 状态码
         */
        public String rename(String oldKey, String newKey) {
            return rename(SafeEncoder.encode(oldKey), SafeEncoder.encode(newKey));
        }

        /**
         * 更改key,仅当新key不存在时才执行
         *
         * @param oldKey
         * @param newkey
         * @return 状态码
         */
        public long renamenx(String oldKey, String newKey) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long status = jedis.renamenx(oldKey, newKey);
                return status;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 更改key
         *
         * @param oldKey
         * @param newKey
         * @return 状态码
         */
        public String rename(byte[] oldKey, byte[] newKey) {
            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String status = jedis.rename(oldKey, newKey);
                return status;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 设置key的过期时间，以秒为单位
         *
         * @param key
         * @param seconds 时间,已秒为单位
         * @return 影响的记录数
         */
        public long expired(String key, int seconds) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long count = jedis.expire(key, seconds);
                return count;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 设置key的过期时间,它是距历元（即格林威治标准时间 1970 年 1 月 1 日的 00:00:00，格里高利历）的偏移量。
         *
         * @param key
         * @param timestamp 时间，已毫秒为单位
         * @return 影响的记录数
         */
        public long expireAt(String key, long timestamp) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long count = jedis.expireAt(key, timestamp);
                return count;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 查询key的过期时间
         *
         * @param key
         * @return 以秒为单位的时间表示
         */
        public long ttl(String key) {
            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                return jedis.ttl(key);
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 取消对key过期时间的设置
         *
         * @param key
         * @return 影响的记录数
         */
        public long persist(String key) {
            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long count = jedis.persist(key);
                return count;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 删除keys对应的记录,可以是多个key
         *
         * @param keys
         * @return 删除的记录数
         */
        public long del(String... keys) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                //删除
                return jedis.del(keys);
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 删除keys对应的记录,可以是多个key
         *
         * @param keys
         * @return 删除的记录数
         */
        public long del(byte[]... keys) {
            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long count = jedis.del(keys);
                return count;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 判断key是否存在
         *
         * @param key
         * @return boolean
         */
        public boolean exists(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                return jedis.exists(key);
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 指定库中是否存在某个key
         *
         * @param dbIndex 制定个库
         * @param key
         * @return
         */
        public boolean exists(int dbIndex, String key) {
            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                jedis.select(dbIndex);
                return jedis.exists(key);
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 对List,Set,SortSet进行排序,如果集合数据较大应避免使用这个方法
         *
         * @param key
         * @return List<String> 集合的全部记录
         **/
        public List<String> sort(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                return jedis.sort(key);
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 对List,Set,SortSet进行排序或limit
         *
         * @param key
         * @param params 定义排序类型或limit的起止位置.
         * @return List<String> 全部或部分记录
         **/
        public List<String> sort(String key, SortingParams params) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                return jedis.sort(key, params);
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 返回指定key存储的类型
         *
         * @param key
         * @return String string|list|set|zset|hash
         **/
        public String type(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String type = jedis.type(key);
                return type;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 查找所有匹配给定的模式的键
         *
         * @param pattern key的表达式,*表示多个，？表示一个
         * @return
         */
        public Set<String> keys(String pattern) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                Set<String> set = jedis.keys(pattern);
                return set;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }
    }

    public class Sets {

        /**
         * 向Set添加一条记录，如果member已存在返回0,否则返回1
         *
         * @param key
         * @param member
         * @return 操作码, 0或1
         */
        public long sadd(String key, String member) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.sadd(key, member);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 向Set添加一条记录，如果member已存在返回0,否则返回1
         *
         * @param key
         * @param member
         * @return 操作码, 0或1
         */
        public long sadd(byte[] key, byte[] member) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.sadd(key, member);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 获取给定key中元素个数
         *
         * @param key
         * @return 元素个数
         */
        public long scard(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long len = jedis.scard(key);
                return len;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 返回从第一组和所有的给定集合之间的差异的成员
         *
         * @param keys
         * @return 差异的成员集合
         */
        public Set<String> sdiff(String... keys) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                Set<String> set = jedis.sdiff(keys);
                return set;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 这个命令等于sdiff,但返回的不是结果集,而是将结果集存储在新的集合中，如果目标已存在，则覆盖。
         *
         * @param newKey 新结果集的key
         * @param keys   比较的集合
         * @return 新集合中的记录数
         **/
        public long sdiffstore(String newKey, String... keys) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.sdiffstore(newKey, keys);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 返回给定集合交集的成员,如果其中一个集合为不存在或为空，则返回空Set
         *
         * @param keys
         * @return 交集成员的集合
         **/
        public Set<String> sinter(String... keys) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                Set<String> set = jedis.sinter(keys);
                return set;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 这个命令等于sinter,但返回的不是结果集,而是将结果集存储在新的集合中，如果目标已存在，则覆盖。
         *
         * @param newkey 新结果集的key
         * @param keys   比较的集合
         * @return 新集合中的记录数
         **/
        public long sinterstore(String newkey, String... keys) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.sinterstore(newkey, keys);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 确定一个给定的值是否存在
         *
         * @param key
         * @param member 要判断的值
         * @return 存在返回1，不存在返回0
         **/
        public boolean sismember(String key, String member) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                boolean s = jedis.sismember(key, member);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 返回集合中的所有成员
         *
         * @param key
         * @return 成员集合
         */
        public Set<String> smembers(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                Set<String> set = jedis.smembers(key);
                return set;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 返回集合中的所有成员
         *
         * @param key
         * @return 成员集合
         */
        public Set<byte[]> smembers(byte[] key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                Set<byte[]> set = jedis.smembers(key);
                return set;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 将成员从源集合移出放入目标集合 <br/>
         * 如果源集合不存在或不包哈指定成员，不进行任何操作，返回0<br/>
         * 否则该成员从源集合上删除，并添加到目标集合，如果目标集合中成员已存在，则只在源集合进行删除
         *
         * @param srcKey 源集合
         * @param dstKey 目标集合
         * @param member 源集合中的成员
         * @return 状态码，1成功，0失败
         */
        public long smove(String srcKey, String dstKey, String member) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.smove(srcKey, dstKey, member);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 从集合中删除成员
         *
         * @param key
         * @return 被删除的成员
         */
        public String spop(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String s = jedis.spop(key);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 从集合中删除指定成员
         *
         * @param key
         * @param member 要删除的成员
         * @return 状态码，成功返回1，成员不存在返回0
         */
        public long srem(String key, String member) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.srem(key, member);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 合并多个集合并返回合并后的结果，合并后的结果集合并不保存<br/>
         *
         * @param keys
         * @return 合并后的结果集合
         */
        public Set<String> sunion(String... keys) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                Set<String> set = jedis.sunion(keys);
                return set;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 合并多个集合并将合并后的结果集保存在指定的新集合中，如果新集合已经存在则覆盖
         *
         * @param newKey 新集合的key
         * @param keys   要合并的集合
         **/
        public long sunionstore(String newKey, String... keys) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.sunionstore(newKey, keys);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }
    }

    public class SortSet {

        /**
         * 向集合中增加一条记录,如果这个值已存在，这个值对应的权重将被置为新的权重
         *
         * @param key
         * @param score  权重
         * @param member 要加入的值，
         * @return 状态码 1成功，0已存在member的值
         */
        public long zadd(byte[] key, double score, byte[] member) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.zadd(key, score, member);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 向集合中增加一条记录,如果这个值已存在，这个值对应的权重将被置为新的权重
         *
         * @param key
         * @param score  权重
         * @param member 要加入的值，
         * @return 状态码 1成功，0已存在member的值
         */
        public long zadd(String key, int score, String member) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.zadd(key, score, member);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }




        /**
         * 获取集合中元素的数量
         *
         * @param key
         * @return 如果返回0则集合不存在
         */
        public long zcard(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long len = jedis.zcard(key);
                return len;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }
        /**
         * 获取集合的并集
         *
         * @param destKey 目的地
         * @param keys 源
         * @return 返回并集集合
         */
        public long zunionstore(String destKey,String... keys) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long len = jedis.zunionstore(destKey, keys);
                return len;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 获取指定权重区间内集合的数量
         *
         * @param key
         * @param min 最小排序位置
         * @param max 最大排序位置
         */
        public long zcount(String key, double min, double max) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long len = jedis.zcount(key, min, max);
                return len;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 获得set的长度
         *
         * @param key
         * @return
         */
        public long zlength(String key) {
            Set<String> set = zrange(key, 0, -1);
            long len = set.size();
            return len;
        }

        /**
         * 权重增加给定值，如果给定的member已存在
         *
         * @param key
         * @param score  要增的权重
         * @param member 要插入的值
         * @return 增后的权重
         */
        public double zincrby(String key, double score, String member) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                double s = jedis.zincrby(key, score, member);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 权重增加给定值，如果给定的member已存在
         *
         * @param key
         * @param score  要增的权重
         * @param member 要插入的值
         * @return 增后的权重
         */
        public double zincrby(String key, int score, String member) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                double s = jedis.zincrby(key, score, member);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
         *
         * @param key
         * @param start 开始位置(包含)
         * @param end   结束位置(包含)
         * @return Set<String>
         */
        public Set<String> zrange(String key, int start, int end) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                Set<String> set = jedis.zrange(key, start, end);
                return set;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
         *
         * @param key
         * @param start 开始位置(包含)
         * @param end   结束位置(包含)
         * @return Set<String>
         */
        public Set<byte[]> zrange(byte[] key, int start, int end) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                Set<byte[]> set = jedis.zrange(key, start, end);
                return set;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 返回指定权重区间的元素集合
         *
         * @param key
         * @param min 上限权重
         * @param max 下限权重
         * @return Set<String>
         */
        public Set<String> zrangeByScore(String key, double min, double max) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                Set<String> set = jedis.zrangeByScore(key, min, max);
                return set;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 获取指定值在集合中的位置，集合排序从低到高
         *
         * @param key
         * @param member
         * @return long 位置
         */
        public long zrank(String key, String member) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long index = jedis.zrank(key, member);
                return index;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 获取指定值在集合中的位置，集合排序从高到低
         *
         * @param key
         * @param member
         * @return long 位置
         */
        public long zrevrank(String key, String member) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long index = jedis.zrevrank(key, member);
                return index;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 从集合中删除成员
         *
         * @param key
         * @param member
         * @return 返回1成功
         */
        public long zrem(String key, String member) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.zrem(key, member);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 删除
         *
         * @param key
         * @return
         */
        public long zrem(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.del(key);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 删除给定位置区间的元素
         *
         * @param key
         * @param start 开始区间，从0开始(包含)
         * @param end   结束区间,-1为最后一个元素(包含)
         * @return 删除的数量
         */
        public long zremrangeByRank(String key, int start, int end) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.zremrangeByRank(key, start, end);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 删除给定权重区间的元素
         *
         * @param key
         * @param min 下限权重(包含)
         * @param max 上限权重(包含)
         * @return 删除的数量
         */
        public long zremrangeByScore(String key, double min, double max) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.zremrangeByScore(key, min, max);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 获取给定区间的元素，原始按照权重由高到低排序
         *
         * @param key
         * @param start
         * @param end
         * @return Set<String>
         */
        public Set<String> zrevrange(String key, int start, int end) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                Set<String> set = jedis.zrevrange(key, start, end);
                return set;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 获取给定值在集合中的权重
         *
         * @param key
         * @param memeber
         * @return double 权重
         */
        public double zscore(String key, String memebr) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                Double score = jedis.zscore(key, memebr);
                if (score != null)
                    return score;
                return 0;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }
    }

    public class Hash {

        /**
         * 从hash中删除指定的存储
         *
         * @param key
         * @param field 存储的名字
         * @return 状态码，1成功，0失败
         */
        public long hdel(String key, String field) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.hdel(key, field);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 从hash中删除指定的存储
         *
         * @param key
         * @param field 存储的名字
         * @return 状态码，1成功，0失败
         */
        public long hdel(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.del(key);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 测试hash中指定的存储是否存在
         *
         * @param key
         * @param field 存储的名字
         * @return 1存在，0不存在
         */
        public boolean hexists(String key, String field) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                boolean s = jedis.hexists(key, field);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 返回hash中指定存储位置的值
         *
         * @param key
         * @param field 存储的名字
         * @return 存储对应的值
         */
        public String hget(String key, String field) {
            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String s = jedis.hget(key, field);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 返回hash中指定存储位置的值
         *
         * @param key
         * @param field 存储的名字
         * @return 存储对应的值
         */
        public byte[] hget(byte[] key, byte[] field) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                byte[] s = jedis.hget(key, field);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 以Map的形式返回hash中的存储和值
         *
         * @param key
         * @return Map<Strinig , String>
         */
        public Map<String, String> hgetAll(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                Map<String, String> map = jedis.hgetAll(key);
                return map;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 向指定的key中添加map集合
         *
         * @param key
         * @param map 所要添加的集合
         */
        public void hsetAll(String key, Map<String, String> map) {
            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                Pipeline pipeline = jedis.pipelined();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    pipeline.hset(key, entry.getKey(), entry.getValue());
                }
                pipeline.sync();
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 添加一个对应关系
         *
         * @param key
         * @param field
         * @param value
         * @return 状态码 1成功，0失败，field已存在将更新，也返回0
         **/
        public long hset(String key, String field, String value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.hset(key, field, value);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 添加一个对应关系
         *
         * @param key
         * @param field
         * @param value
         * @return 状态码 1成功，0失败，field已存在将更新，也返回0
         **/
        public long hset(String key, String field, byte[] value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.hset(key.getBytes(), field.getBytes(), value);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 添加对应关系，只有在field不存在时才执行
         *
         * @param key
         * @param field
         * @param value
         * @return 状态码 1成功，0失败field已存
         **/
        public long hsetnx(String key, String field, String value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.hsetnx(key, field, value);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 获取hash中value的集合
         *
         * @param key
         * @return List<String>
         */
        public List<String> hvals(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                List<String> list = jedis.hvals(key);
                return list;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 在指定的存储位置加上指定的数字，存储位置的值必须可转为数字类型
         *
         * @param key
         * @param field 存储位置
         * @param long  value 要增加的值,可以是负数
         * @return 增加指定数字后，存储位置的值
         */
        public long hincrby(String key, String field, long value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long s = jedis.hincrBy(key, field, value);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 返回指定hash中的所有存储名字,类似Map中的keySet方法
         *
         * @param key
         * @return Set<String> 存储名称的集合
         */
        public Set<String> hkeys(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                Set<String> set = jedis.hkeys(key);
                return set;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 获取hash中存储的个数，类似Map中size方法
         *
         * @param key
         * @return long 存储的个数
         */
        public long hlen(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long len = jedis.hlen(key);
                return len;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 根据多个key，获取对应的value，返回List,如果指定的key不存在,List对应位置为null
         *
         * @param key
         * @param fields 存储位置
         * @return List<String>
         */
        public List<String> hmget(String key, String... fields) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                List<String> list = jedis.hmget(key, fields);
                return list;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 根据多个key，获取对应的value，返回List,如果指定的key不存在,List对应位置为null
         *
         * @param key
         * @param fields 存储位置
         * @return List<String>
         */
        public List<byte[]> hmget(byte[] key, byte[]... fields) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                List<byte[]> list = jedis.hmget(key, fields);
                return list;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 添加对应关系，如果对应关系已存在，则覆盖
         *
         * @param key
         * @param map 对应关系
         * @return 状态，成功返回OK
         */
        public String hmset(String key, Map<String, String> map) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String s = jedis.hmset(key, map);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 添加对应关系，如果对应关系已存在，则覆盖
         *
         * @param key
         * @param map 对应关系
         * @return 状态，成功返回OK
         */
        public String hmset(byte[] key, Map<byte[], byte[]> map) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String s = jedis.hmset(key, map);
                return s;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

    }

    public class Strings {
        /**
         * 根据key获取记录
         *
         * @param key
         * @return 值
         */
        public String get(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String value = jedis.get(key);
                return value;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 根据key获取记录
         *
         * @param key
         * @return 值
         */
        public byte[] get(byte[] key) {


            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                byte[] value = jedis.get(key);
                return value;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 添加有过期时间的记录
         *
         * @param key
         * @param seconds 过期时间，以秒为单位
         * @param value
         * @return String 操作状态
         */
        public String setEx(String key, int seconds, String value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String str = jedis.setex(key, seconds, value);
                return str;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 添加有过期时间的记录
         *
         * @param key
         * @param seconds 过期时间，以秒为单位
         * @param value
         * @return String 操作状态
         */
        public String setEx(byte[] key, int seconds, byte[] value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String str = jedis.setex(key, seconds, value);
                return str;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 添加一条记录，仅当给定的key不存在时才插入
         *
         * @param key
         * @param value
         * @return long 状态码，1插入成功且key不存在，0未插入，key存在
         */
        public long setnx(String key, String value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long str = jedis.setnx(key, value);
                return str;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 添加记录,如果记录已存在将覆盖原有的value
         *
         * @param key
         * @param value
         * @return 状态码
         */
        public String set(String key, String value) {
            return set(SafeEncoder.encode(key), SafeEncoder.encode(value));
        }

        /**
         * 添加记录,如果记录已存在将覆盖原有的value
         *
         * @param key
         * @param value
         * @return 状态码
         */
        public String set(String key, byte[] value) {
            return set(SafeEncoder.encode(key), value);
        }

        /**
         * 添加记录,如果记录已存在将覆盖原有的value
         *
         * @param key
         * @param value
         * @return 状态码
         */
        public String set(byte[] key, byte[] value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String status = jedis.set(key, value);
                return status;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 从指定位置开始插入数据，插入的数据会覆盖指定位置以后的数据<br/>
         * 例:String str1="123456789";<br/>
         * 对str1操作后setRange(key,4,0000)，str1="123400009";
         *
         * @param key
         * @param offset
         * @param value
         * @return long value的长度
         */
        public long setRange(String key, long offset, String value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long len = jedis.setrange(key, offset, value);
                return len;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 在指定的key中追加value
         *
         * @param key
         * @param value
         * @return long 追加后value的长度
         **/
        public long append(String key, String value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long len = jedis.append(key, value);
                return len;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 将key对应的value减去指定的值，只有value可以转为数字时该方法才可用
         *
         * @param key
         * @param number 要减去的值
         * @return long 减指定值后的值
         */
        public long decrBy(String key, long number) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long len = jedis.decrBy(key, number);
                return len;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * <b>可以作为获取唯一id的方法</b><br/>
         * 将key对应的value加上指定的值，只有value可以转为数字时该方法才可用
         *
         * @param key
         * @param number 要减去的值
         * @return long 相加后的值
         */
        public long incrBy(String key, long number) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long len = jedis.incrBy(key, number);
                return len;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 对指定key对应的value进行截取
         *
         * @param key
         * @param startOffset 开始位置(包含)
         * @param endOffset   结束位置(包含)
         * @return String 截取的值
         */
        public String getrange(String key, long startOffset, long endOffset) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String value = jedis.getrange(key, startOffset, endOffset);
                return value;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 获取并设置指定key对应的value<br/>
         * 如果key存在返回之前的value,否则返回null
         *
         * @param key
         * @param value
         * @return String 原始value或null
         */
        public String getSet(String key, String value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String str = jedis.getSet(key, value);
                return str;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 批量获取记录,如果指定的key不存在返回List的对应位置将是null
         *
         * @param keys
         * @return List<String> 值得集合
         */
        public List<String> mget(String... keys) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                List<String> str = jedis.mget(keys);
                return str;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 批量存储记录
         *
         * @param keysValues 例:keysvalues="key1","value1","key2","value2";
         * @return String 状态码
         */
        public String mset(String... keysValues) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String str = jedis.mset(keysValues);
                return str;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 获取key对应的值的长度
         *
         * @param key
         * @return value值得长度
         */
        public long strlen(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long len = jedis.strlen(key);
                return len;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }
    }

    public class Lists {
        /**
         * List长度
         *
         * @param key
         * @return 长度
         */
        public long llen(String key) {
            return llen(SafeEncoder.encode(key));
        }

        /**
         * List长度
         *
         * @param key
         * @return 长度
         */
        public long llen(byte[] key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long count = jedis.llen(key);
                return count;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 覆盖操作,将覆盖List中指定位置的值
         *
         * @param key
         * @param index 位置
         * @param value 值
         * @return 状态码
         */
        public String lset(byte[] key, int index, byte[] value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String status = jedis.lset(key, index, value);
                return status;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 覆盖操作,将覆盖List中指定位置的值
         *
         * @param key
         * @param index 位置
         * @param value 值
         * @return 状态码
         */
        public String lset(String key, int index, String value) {
            return lset(SafeEncoder.encode(key), index, SafeEncoder.encode(value));
        }

        /**
         * 在value的相对位置插入记录
         *
         * @param key
         * @param where 前面插入或后面插入
         * @param pivot 相对位置的内容
         * @param value 插入的内容
         * @return 记录总数
         */
        public long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value) {
            return linsert(SafeEncoder.encode(key), where, SafeEncoder.encode(pivot), SafeEncoder.encode(value));
        }

        /**
         * 在指定位置插入记录
         *
         * @param key
         * @param where 前面插入或后面插入
         * @param pivot 相对位置的内容
         * @param value 插入的内容
         * @return 记录总数
         */
        public long linsert(byte[] key, BinaryClient.LIST_POSITION where, byte[] pivot, byte[] value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long count = jedis.linsert(key, where, pivot, value);
                return count;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 获取List中指定位置的值
         *
         * @param key
         * @param index 位置
         * @return 值
         **/
        public String lindex(String key, int index) {
            return SafeEncoder.encode(lindex(SafeEncoder.encode(key), index));
        }

        /**
         * 获取List中指定位置的值
         *
         * @param key
         * @param index 位置
         * @return 值
         **/
        public byte[] lindex(byte[] key, int index) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                byte[] value = jedis.lindex(key, index);
                return value;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 将List中的第一条记录移出List
         *
         * @param key
         * @return 移出的记录
         */
        public String lpop(String key) {
            return SafeEncoder.encode(lpop(SafeEncoder.encode(key)));
        }

        /**
         * 将List中的第一条记录移出List
         *
         * @param key
         * @return 移出的记录
         */
        public byte[] lpop(byte[] key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                byte[] value = jedis.lpop(key);
                return value;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 将List中最后第一条记录移出List
         *
         * @param key
         * @return 移出的记录
         */
        public String rpop(String key) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String value = jedis.rpop(key);
                return value;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 向List尾部追加记录
         *
         * @param key
         * @param value
         * @return 记录总数
         */
        public long lpush(String key, String value) {
            return lpush(SafeEncoder.encode(key), SafeEncoder.encode(value));
        }

        /**
         * 向List头部追加记录
         *
         * @param key
         * @param value
         * @return 记录总数
         */
        public long rpush(String key, String value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long count = jedis.rpush(key, value);
                return count;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 向List头部追加记录
         *
         * @param key
         * @param value
         * @return 记录总数
         */
        public long rpush(byte[] key, byte[] value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long count = jedis.rpush(key, value);
                return count;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 向List中追加记录
         *
         * @param key
         * @param value
         * @return 记录总数
         */
        public long lpush(byte[] key, byte[] value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long count = jedis.lpush(key, value);
                return count;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 获取指定范围的记录，可以做为分页使用
         *
         * @param key
         * @param start
         * @param end
         * @return List
         */
        public List<String> lrange(String key, long start, long end) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                List<String> list = jedis.lrange(key, start, end);
                return list;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 获取指定范围的记录，可以做为分页使用
         *
         * @param key
         * @param start
         * @param end   如果为负数，则尾部开始计算
         * @return List
         */
        public List<byte[]> lrange(byte[] key, int start, int end) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                List<byte[]> list = jedis.lrange(key, start, end);
                return list;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 删除List中c条记录，被删除的记录值为value
         *
         * @param key
         * @param c     要删除的数量，如果为负数则从List的尾部检查并删除符合的记录
         * @param value 要匹配的值
         * @return 删除后的List中的记录数
         */
        public long lrem(byte[] key, int c, byte[] value) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                long count = jedis.lrem(key, c, value);
                return count;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 删除List中c条记录，被删除的记录值为value
         *
         * @param key
         * @param c     要删除的数量，如果为负数则从List的尾部检查并删除符合的记录
         * @param value 要匹配的值
         * @return 删除后的List中的记录数
         */
        public long lrem(String key, int c, String value) {
            return lrem(SafeEncoder.encode(key), c, SafeEncoder.encode(value));
        }

        /**
         * 算是删除吧，只保留start与end之间的记录
         *
         * @param key
         * @param start 记录的开始位置(0表示第一条记录)
         * @param end   记录的结束位置（如果为-1则表示最后一个，-2，-3以此类推）
         * @return 执行状态码
         */
        public String ltrim(byte[] key, int start, int end) {

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                String str = jedis.ltrim(key, start, end);
                return str;
            } finally {
                if (jedis != null) {
                    returnJedis(jedis);
                }
            }
        }

        /**
         * 算是删除吧，只保留start与end之间的记录
         *
         * @param key
         * @param start 记录的开始位置(0表示第一条记录)
         * @param end   记录的结束位置（如果为-1则表示最后一个，-2，-3以此类推）
         * @return 执行状态码
         */
        public String ltrim(String key, int start, int end) {
            return ltrim(SafeEncoder.encode(key), start, end);
        }
    }

}