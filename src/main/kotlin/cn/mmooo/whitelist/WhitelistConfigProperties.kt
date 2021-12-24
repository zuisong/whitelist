package cn.mmooo.whitelist

import org.springframework.boot.context.properties.*
import org.springframework.cloud.context.config.annotation.*

@RefreshScope
@ConfigurationProperties(prefix = "whitelist")
open class WhitelistConfigProperties {
    var scopes: Map<String, WConf> = mapOf()
    override fun toString(): String {
        return "WhitelistConfigProperties(scopes=$scopes)"
    }

    /**
     * @param [desc] 描述信息，必填
     * @param [whitelist] 白名单列表 whitelist模式下有效
     * @param [percent] 白名单百分比 百分比模式下有用
     */
    data class WConf(
        var desc: String = "",
        var whitelist: Set<String> = setOf(),
        var blacklist: Set<String> = setOf(),
        var percent: Int = 0,
    )


}
