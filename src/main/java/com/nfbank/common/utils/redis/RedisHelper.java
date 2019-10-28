package com.nfbank.common.utils.redis;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * com.nfbank.common.utils.redis
 * <p>
 * Redis组件
 *
 * @author 孙威
 * @date 2018/12/18 10:58
 */
@Component
public class RedisHelper {

    /**
     * 操作hash信息
     *
     * @return
     */
    public RedisUtil.Hash optForHash() {
        return RedisUtil.getInstance().HASH;
    }

    /**
     * 对key进行操作
     *
     * @return
     */
    public RedisUtil.Keys optForKey() {
        return RedisUtil.getInstance().KEYS;
    }

    /**
     * 对list进行操作
     *
     * @return
     */
    public RedisUtil.Lists optForList() {
        return RedisUtil.getInstance().LISTS;
    }

    /**
     * 对set进行操作
     *
     * @return
     */
    public RedisUtil.Sets optForSet() {
        return RedisUtil.getInstance().SETS;
    }

    /**
     * 对sortSet进行操作
     *
     * @return
     */
    public RedisUtil.SortSet optForSortSet() {
        return RedisUtil.getInstance().SORTSET;
    }

    /**
     * 对String进行操作
     *
     * @return
     */
    public RedisUtil.Strings optForString() {
        return RedisUtil.getInstance().STRINGS;
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
    public String sscan(Set<String> retSet, String setKey, Integer count, String pattern, String cursor) {
        return RedisScan.sscan(retSet, setKey, count, pattern, cursor);
    }

}
