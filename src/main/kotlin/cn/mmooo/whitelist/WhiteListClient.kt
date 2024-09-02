package cn.mmooo.whitelist

import org.slf4j.*
import org.springframework.beans.factory.annotation.*
import org.springframework.stereotype.*


/**
 * 白名单工具使用类
 */
@Component
open class WhiteListClient {

    private val log = LoggerFactory.getLogger(WhiteListClient::class.java)

    @Autowired
    private lateinit var whiteListConfig: WhitelistConfigProperties

    /**
     * 根据一种模式来看看是否在白名单中
     *
     * @param scope
     * 维度
     * @param subject
     * 用户
     * @return
     */
    fun isInWhiteList(
        scope: String,
        subject: String,
    ): Boolean {
        return try {

            when {
                // 在白名单里直接返回 true
                getResultInByWhitelist(subject = subject, scope = scope) -> true
                // 在黑名单里直接返回 false
                checkInBlacklist(subject = subject, scope = scope) -> false
                // 如果是兼容模式,那么就先在白名单里面找,不匹配就去进行百分比算法
                else -> getResultByPercentAlgorithm(subject = subject, scope = scope)
            }

        } catch (e: Exception) {
            // 打印异常信息
            log.error("调用isInWhiteList方法出现异常:{}", e.message, e)
            // 代码出差错了不能影响业务
            false
        }
    }

    fun getData(scope: String): Map<String, String> {
        return whiteListConfig.scopes[scope]?.data ?: mapOf()
    }

    /**
     * 看用户是否在某个维度下的白名单中
     *
     * @param scope
     * @param subject
     * @return
     */
    private fun getResultInByWhitelist(subject: String, scope: String): Boolean {
        val wConf = whiteListConfig.scopes[scope] ?: return false
        // 看看是否在whitelist里面
        return subject in wConf.whitelist
    }

    private fun checkInBlacklist(subject: String, scope: String): Boolean {
        val wConf = whiteListConfig.scopes[scope] ?: return false
        // 看看是否在 blacklist 里面
        return subject in wConf.blacklist
    }

    private fun getResultByPercentAlgorithm(
        subject: String,
        scope: String,
    ): Boolean {
        val wConf = whiteListConfig.scopes[scope] ?: return false
        // 调用百分比计算接口来计算
        return HashCodePercentAlgorithm.percent(subject, scope, wConf.percent)
    }
}
