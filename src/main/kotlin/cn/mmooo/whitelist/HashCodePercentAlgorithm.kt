package cn.mmooo.whitelist

import kotlin.math.*

/**
 * 默认百分比算法
 *
 */
object HashCodePercentAlgorithm : PercentAlgorithm {

    override fun percent(subject: String, scope: String, percent: Int): Boolean {
        // 用 scope 和 subject 的hash值进行异或,防止不同的 scope 下相同的 subject 的hash值相同
        var hash: Int = hash(scope) xor hash(subject)
        // 小于0的话就取他的绝对值
        if (hash < 0) {
            // 做绝对值判断
            hash = abs(hash)
        }
        // 对100进行取余,然后返回和 percent 比较的结果
        return hash % 100 < percent
    }

    /**
     * hash算法,从hashmap里面copy下来的
     *
     * @param key
     * @return
     */
    private fun hash(key: String): Int {
        val h: Int = key.hashCode()
        // 如果为 null 返回0,否则通过 key 的 hashcode 的高16位和低16位做异或,
        // 保证 hash 值包含了高位和低位的特征
        return h xor (h ushr 16)
    }
}
