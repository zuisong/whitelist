import cn.mmooo.whitelist.*
import org.junit.jupiter.api.*
import org.springframework.boot.autoconfigure.*
import org.springframework.boot.autoconfigure.logging.*
import org.springframework.boot.logging.*
import org.springframework.boot.test.context.*
import org.springframework.boot.test.context.assertj.*
import org.springframework.boot.test.context.runner.*
import org.springframework.cloud.autoconfigure.*

class WhiteListTest {
    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                RefreshAutoConfiguration::class.java,
                WhiteListAutoConfiguration::class.java)
        )

    @Test
    fun someTest() {
        val configFile = "classpath:application.yml"
        contextRunner
            .withInitializer(ConditionEvaluationReportLoggingListener(LogLevel.DEBUG))
            .withInitializer(ConfigFileApplicationContextInitializer())
            .withPropertyValues("spring.config.location=$configFile")
            .run { context: AssertableApplicationContext ->
                val properties = context.getBean(
                    WhitelistConfigProperties::class.java)
                println(properties)
                val whiteListClient = context.getBean(WhiteListClient::class.java)
                assert(whiteListClient.isInWhiteList("scope_1", "user_a"))
                assert(whiteListClient.isInWhiteList("scope_1", "user_b"))
                assert(!whiteListClient.isInWhiteList("scope_1", "user_c"))
            }
    }
}
