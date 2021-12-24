package cn.mmooo.whitelist

/**
 * 百分比算法接口
 *
 */
interface PercentAlgorithm {
    /**
     * 百分比算法
     *
     * @param scope
     * @param subject
     * @param percent
     * @return
     */
    fun percent(subject: String, scope: String, percent: Int): Boolean
}
