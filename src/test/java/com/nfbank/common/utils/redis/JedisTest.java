package com.nfbank.common.utils.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import org.junit.Test;
import redis.clients.jedis.Tuple;

import java.util.List;
import java.util.Set;

/**
 * com.nfbank.common.utils.redis
 * <p>
 * 测试工具类
 *
 * @author 孙威
 * @date 2018/12/17 18:12
 */
public class JedisTest {

    @Test
    public void testSet() {
        RedisUtil instance = RedisUtil.getInstance();

        instance.KEYS.flushAll();

        //使用guava来对比存和取的结果
        Set<String> orgSet = Sets.newHashSet();

        for (int i = 1; i < 1001; i++) {
            instance.SETS.sadd("haha", String.valueOf(i));
            instance.KEYS.expired("haha", 1000);

            orgSet.add(String.valueOf(i));
        }
//        for (int i = 50; i < 150; i++) {
//            instance.SETS.sadd("hah", String.valueOf(i));
//        }

//        Set<String> sdiff = instance.SETS.sdiff("haha", "hah");
//        System.out.println(JSON.toJSONString(sdiff));
        long count = instance.SETS.sdiffstore("newHaha", "haha", "hah");

        System.out.println("数量：" + count);
        //每页多少数据
        int page = 100;
        //查询的数据量
        int sum = 0;
        //总页码
        long totalPage = count % page == 0 ? count / page : (count / page + 1);

        String cursor = "0";
        //存储查询结果
        Set<String> redSet = Sets.newHashSet();

        for (int i = 0; i <= totalPage + 1; i++) {
            //当前查询时，每页数量
            int pageCount = page;
//            //最后一页
//            if (i == totalPage) {
//                pageCount = Long.valueOf(count).intValue() - (i - 1) * page;
//            }
            System.out.println("当前i值为：" + i + "，计算pageCount ----" + pageCount);
            Set<String> newHaha = Sets.newHashSet();
            cursor = RedisScan.sscan(newHaha, "newHaha", pageCount, null, cursor);
            System.out.println("本次查询数量" + newHaha.size() + ",结果：" + JSON.toJSONString(newHaha));
            sum = sum + newHaha.size();

            //存储到guava
            redSet.addAll(newHaha);

            if (cursor.equals("0")) {
                break;
            }
        }
        System.out.println("共查询到结果数据：" + sum);

        //取出差集
        Sets.SetView<String> difference = Sets.difference(orgSet, redSet);

        System.out.println("guava取出不同结果为：" + JSON.toJSONString(difference));
    }

    @Test
    public void testSet2() {
        RedisUtil instance = RedisUtil.getInstance();

        instance.KEYS.flushAll();

        //使用guava来对比存和取的结果
        Set<String> orgSet = Sets.newHashSet();

        for (int i = 1; i < 1001; i++) {
            instance.SETS.sadd("haha", String.valueOf(i));

            orgSet.add(String.valueOf(i));
        }

        long count = instance.SORTSET.zunionstore("newHaha", "haha", "hah");
        System.out.println("数量：" + count);
        //每页多少数据
        int page = 100;
        //查询的数据量
        int sum = 0;
        //总页码
        long totalPage = count % page == 0 ? count / page : (count / page + 1);

        //存储查询结果
        Set<String> redSet = Sets.newHashSet();

        for (int i = 1; i <= totalPage; i++) {
            //当前查询时，每页数量
            int pageCount = page;
            //最后一页
            if (i == totalPage) {
                pageCount = Long.valueOf(count).intValue() - (i - 1) * page;
            }
            System.out.println("当前i值为：" + i + "，计算pageCount ----" + pageCount);

            List<Tuple> newHaha = RedisScan.zscan("newHaha", pageCount, null, String.valueOf(i * page));

            for (Tuple tuple : newHaha) {
                redSet.add(tuple.getElement());
            }

            System.out.println("本次查询数量" + newHaha.size() + ",结果：" + JSON.toJSONString(newHaha));
            sum = sum + newHaha.size();

        }
        System.out.println("共查询到结果数据：" + sum);

        //取出差集
        Sets.SetView<String> difference = Sets.difference(orgSet, redSet);

        System.out.println("guava取出不同结果数量为：" + difference.size() + "，结果值为：" + JSON.toJSONString(difference));
    }

    @Test
    public void testDiff() {
        RedisUtil instance = RedisUtil.getInstance();

//        instance.KEYS.flushAll();
        int num = 3000000;
        for (int i = 0; i < num; i++) {
            instance.SETS.sadd("20181222-1001", "\"" + String.valueOf(i) + "\"");
//            instance.SETS.sadd("20181222-1001", "\""+String.valueOf(i)+"\"");
        }
        for (int i = 0; i < num; i++) {
            instance.SETS.sadd("20181225-1002", String.valueOf(i));
        }
        long count = instance.SETS.sdiffstore("newHaha", "20181222-1001", "20181222-1002");

        System.out.println("数量：" + count);
        //每页多少数据
        int page = 100;
        //查询的数据量
        int sum = 0;
        //总页码
        long totalPage = count % page == 0 ? count / page : (count / page + 1);

        String cursor = "0";
        //存储查询结果
        Set<String> redSet = Sets.newHashSet();

        for (int i = 0; i <= totalPage + 1; i++) {
            //当前查询时，每页数量
            int pageCount = page;
//            //最后一页
//            if (i == totalPage) {
//                pageCount = Long.valueOf(count).intValue() - (i - 1) * page;
//            }
            System.out.println("当前i值为：" + i + "，计算pageCount ----" + pageCount);
            Set<String> newHaha = Sets.newHashSet();
            cursor = RedisScan.sscan(newHaha, "newHaha", pageCount, null, cursor);
//            System.out.println("本次查询数量" + newHaha.size() + ",结果：" + JSON.toJSONString(newHaha).replaceAll("\"",""));
            for (String s : newHaha) {
                System.out.println("返回结果：" + s.replace("\"", ""));
            }
            sum = sum + newHaha.size();

            //存储到guava
            redSet.addAll(newHaha);

            if (cursor.equals("0")) {
                break;
            }
        }
        System.out.println("共查询到结果数据：" + sum);


    }

    @Test
    public void testScard() {
        RedisUtil instance = RedisUtil.getInstance();
        long scard = instance.SETS.scard("20181222-1001");
        System.out.println("查询到数量为：" + scard);
    }

    @Test
    public void testTTl() {
        RedisUtil instance = RedisUtil.getInstance();
//        instance.KEYS.flushAll();
        //提前设置有效期，会被赋值后重新覆盖掉
//        instance.KEYS.expired("20181222-1003", 10);
        for (int i = 0; i < 10; i++) {
            instance.SETS.sadd("20181222-1003", String.valueOf(i));
        }
        for (int i = 10; i < 20; i++) {
            instance.SETS.sadd("20181222-1003", "\"" + String.valueOf(i) + "\"");
        }
//        instance.KEYS.expired("20181222-1003", 100);
//        instance.SETS.srem("20181222-1003", "\"" + String.valueOf(1) + "\"");
    }



    @Test
    public void testDiffCount() {
        RedisUtil instance = RedisUtil.getInstance();
        final int num = 3000000;
        for (int i = 0; i < num; i++) {
            instance.SETS.sadd("20181222-1001", "\"" + String.valueOf(i) + "\"");
        }

        for (int i = 0; i < num; i++) {
            instance.SETS.sadd("20181225-1002", String.valueOf(i));
        }

        long count = RedisUtil.getInstance().SETS.sdiffstore("newHaha", "20181222-1001", "20181222-1002");

        System.out.println("数量：" + count);
    }

    @Test
    public void testRemove() {
        long start = System.currentTimeMillis();
        RedisUtil instance = RedisUtil.getInstance();
        final int num = 1000;
        for (int i = 0; i < num; i++) {
            instance.SETS.srem("20181222-1001", "\"" + String.valueOf(i) + "\"");
        }
        System.out.println("用时：" + (System.currentTimeMillis() - start) + " 毫秒");
    }

    @Test
    public void josnTest() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("haha", "小明是花心");
        RedisUtil instance = RedisUtil.getInstance();
        instance.SETS.sadd("json", jsonObject.toJSONString());
    }

    @Test
    public void expireTest() {
        RedisUtil instance = RedisUtil.getInstance();

        instance.KEYS.flushAll();

        //使用guava来对比存和取的结果
        Set<String> orgSet = Sets.newHashSet();

        for (int i = 1; i < 1001; i++) {
            instance.SETS.sadd("haha", String.valueOf(i));

            orgSet.add(String.valueOf(i));
        }
        instance.KEYS.expired("haha", 50);
    }
}
