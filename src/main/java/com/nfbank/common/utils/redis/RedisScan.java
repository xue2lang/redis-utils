/**
 * Copyright © 2017 北京玖富普惠信息技术有限公司. All rights reserved.
 *
 * @Title: RedisScan.java
 * @Prject: redis-utils
 * @Package: com.nfbank.common.utils.redis
 * @author: sunwei
 * @date: 2017年12月1日 下午1:46:46
 * @version: V1.0
 */
package com.nfbank.common.utils.redis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

/**
 * Title:  RedisScan<br>
 * Description: redis的搜索<br>
 *
 * @author sunwei
 * @Modified by
 * @CreateDate 2017年2月14日下午2:34:22
 * @Version 1.0
 * @Revision
 * @ModifiedDate
 * @since JDK 1.7
 */
public class RedisScan {

    private static final String DEFAULT_CURSOR = "-1";

    private static final String ZERO = "0";
    /**
     * 默认格式
     */
    private static final String DEFAULT_PATTERN = "*";

    /**
     * Title:  zscan<br>
     * Description: 浏览<br>
     *
     * @param redisListKey jedis的key
     * @param pattern      查找的存储对象格式
     * @return
     * @throws Exception
     * @author sunwei
     * @Modified by
     * @CreateDate 2017年2月14日 下午2:34:35
     * @Version
     * @since JDK 1.7
     */
    public static List<Tuple> zscan(String redisListKey, String pattern) throws Exception {
        return zscan(redisListKey, -1, pattern);
    }

    /**
     * Title:  zscan<br>
     *
     * @param redisListKey
     * @param count        表示获取数量(-1表所有)
     * @param pattern      格式字符串
     * @return
     * @throws Exception
     */
    public static List<Tuple> zscan(String redisListKey, Integer count, String pattern) throws Exception {

        RedisUtil instance = RedisUtil.getInstance();
        Jedis jedis = instance.getJedis();
        List<Tuple> list;
        try {
            list = new ArrayList<Tuple>();
            //不可以使用整数类型,会有问题
            String cursor = DEFAULT_CURSOR;
            do {//此处使用游标循环获取,初始位置为0,循环结束位置也是0。此方式比一次性获取出所有数据较好
                ScanResult<Tuple> result;
                ScanParams params = new ScanParams();
                params.match(pattern);
                if (count > 0) {
                    params.count(count);
                }
                result = jedis.zscan(redisListKey, cursor, params);
                cursor = result.getStringCursor();
                List<Tuple> dataList = result.getResult();
                list.addAll(dataList);
                if (count > 0 && list.size() >= count) {
                    break;
                }
            } while (!ZERO.equals(cursor));
        } finally {
            instance.returnJedis(jedis);
        }
        return list;
    }

    /**
     * Title:  zscan<br>
     *
     * @param redisListKey
     * @param count        表示获取数量(-1表所有)
     * @param pattern      格式字符串
     * @param cursor       游标位置
     * @return
     * @throws Exception
     */
    public static List<Tuple> zscan(String redisListKey, Integer count, String pattern, String cursor) {

        RedisUtil instance = RedisUtil.getInstance();
        Jedis jedis = instance.getJedis();
        List<Tuple> list;
        try {
            list = new ArrayList<Tuple>();
            //不可以使用整数类型,会有问题
            //不可以使用整数类型,会有问题
            if (StringUtils.isEmpty(cursor)) {
                cursor = DEFAULT_CURSOR;
            }
            ScanResult<Tuple> result;
            ScanParams params = new ScanParams();
            if (!StringUtils.isEmpty(pattern)) {

                params.match(pattern);
            }
            if (count > 0) {
                params.count(count);
            }
            result = jedis.zscan(redisListKey, cursor, params);
            List<Tuple> dataList = result.getResult();
            list.addAll(dataList);
        } finally {
            instance.returnJedis(jedis);
        }
        return list;
    }

    /**
     *
     * 查询指定数量的结果集（首次查询游标默认值为0，当查询结束，游标结果仍为0），一般分页查询。查询次数由调用者自己控制。判断游标返回结果是否为0即可
     *
     * @param retSet  用于封装返回值的set集合，需要调用者手动创建对象
     * @param setKey  set集合对应的key
     * @param count   表示获取数量，不传值，默认-1(-1表所有)
     * @param pattern 格式字符串
     * @param cursor  游标位置
     * @return 游标位置，即当前已迭代位置
     */
    public static String sscan(Set<String> retSet, String setKey, Integer count, String pattern, String cursor) {
        RedisUtil instance = RedisUtil.getInstance();
        Jedis jedis = instance.getJedis();

        try {
            if (StringUtils.isEmpty(cursor)) {
                cursor = DEFAULT_CURSOR;
            }
            ScanParams params = new ScanParams();
            if (!StringUtils.isEmpty(pattern)) {
                params.match(pattern);
            }
            if (count > 0) {
                params.count(count);
            }
            ScanResult<String> result = jedis.sscan(setKey, cursor, params);
            retSet.addAll(result.getResult());

            cursor = result.getStringCursor();

        } finally {
            instance.returnJedis(jedis);
        }
        return cursor;
    }


}
