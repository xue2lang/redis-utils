package com.nfbank.common.utils.redis;

import org.junit.Test;

/**
 * com.nfbank.common.utils.redis
 * <p>
 * TODO
 *
 * @author 孙威
 * @date 2019/1/11 11:13
 */
public class JedisTest2 {


    @Test
    public void testDiffCount1() {
        long start = System.currentTimeMillis();
        long count = RedisUtil.getInstance().SETS.sdiffstore("newHaha", "20181222-1001", "20181222-1002");
        System.out.println("差集：" + count + ",耗时：" + (System.currentTimeMillis() - start));
    }

    @Test
    public void testInsert() {
        long start = System.currentTimeMillis();
        RedisUtil instance = RedisUtil.getInstance();
        final int num = 1000;
        for (int i = 0; i < num; i++) {
            instance.SETS.sadd("20181222-1001", "\"" + String.valueOf(i) + "\"");
        }
        System.out.println("用时：" + (System.currentTimeMillis() - start)+" 毫秒");
    }

}
