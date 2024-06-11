# whitelist

## 项目说明
本项目源于 XSYX-DBQ 同学的 whitelist 中间件

原项目基于 Apollo 配置中心，后改造为基于 spring-cloud-config，使用 kotlin 重写

保持了和原项目一样的使用接口

## 项目背景

在我们开发一个新功能的时候，我们可以在代码中放置一个标志，来决定是否开启这个新功能。在无须改动代码或者重新部署代码的情况下，我们可以在后台改动这个标志的值，来**实时**的开启和关闭这个功能。这个标记值就是 Feature Flag (功能开关)。

在最简单的用法下，Feature flag 可以只是一个开关。

```java
class someClass {

   private val featureFlagConfig = FeatureFlagsConfig ()
  
   fun someBusinessLogic() {
       // 这个值是一个动态的布尔型配置
       if (featureFlagConfig.shouldEnableNewFeatureA) {
           // 使用新功能
       } else {
           // 使用原有功能
       }
   }
  
}

```

feature flag 也可以是数值型的，控制多少生产流量会使用新功能。

```java
class someClass {

   private val featureFlagConfig = FeatureFlagsConfig ()
  
   fun someBusinessLogic(userId: String) {
       // 这个值是一个动态的整数型配置
       if (hash(userId) %100 < featureFlagConfig.newFeatureAPercentage) {
           // 使用新功能
       } else {
           // 使用原有功能
       }
   }
  
}

```


我们可以给 feature flag 配置用户匹配规则，来控制哪些用户可以使用新功能，比如我们可以针对某些特定的 IP，用户名，或者 org 来开启功能

```java
class someClass {
   private val featureFlagConfig = FeatureFlagsConfig ()
  
   fun someBusinessLogic(orgId: String) {
       // 根据orgId来决定是否开启功能
       if (featureFlagConfig.newFeatureAMatchRule(orgId) == true) {
           // 使用新功能
       } else {
           // 使用原有功能
       }
   }
  
}

```

### 为什么需要 Feature Flag

Feature flag 有很多的应用场景（更多场景描述见 [https://ff4j.github.io/](https://ff4j.github.io/))。

主要的场景有

#### 支持主干开发模式

在**主干开发模式下**，不同发布周期的功能都在主干开发。为了保证未完成的功能不会影响其他功能的测试与上线，我们需要使用 feature flag 来隔离未完成的功能。

#### 线上功能开关

在新版本发布以后如果出现 bug，可以使用功能开关及时关闭掉出问题的功能，不需要回滚整个新版本。在减少用户的影响的同时，给开发和测试人员更多的时间来定位和修复问题。

#### 灰度发布

目前当我们的代码部署到生产环境，新功能就对所有的生产用户可见了。我们可以使用 feature flag 来实现灰度发布，先在小范围生产流量上使用新功能，如果没有问题，才发布给所有用户，如果有问题，及时关闭。

#### 线上测试 （需要 feature flag + 基于 orgId 或者用户 Id 的用户匹配）

在新功能发布给所有生产用户以前，我们可以在生产环境先发布给 internal / test 用户，来实现产品的线上测试

#### 支持产品 A/B test （需要 feature flag + 随机用户分组 + 用户组数据分析）

我们可以做新功能的 A/B test, 也就是把用户分到 control group （关掉 flag，不使用新功能）和 test group （flag 开启，使用新功能）。通过分析各个 group 的用户指标差异， 这样可以帮助产品快速迭代。当然实现这个目标，我们还需要一套围绕 feature flag 的数据分析系统

## 使用方法


### whitelist
假设有如下配置
```yml
whitelist:
  scopes:
    scope_1:
      desc: scope_1 业务说明
      whitelist: [ user_a,
               user_b ]
      blacklist: [user_c, user_d]
      percent: 30
```

使用方法
```java
import cn.mmooo.whitelist.WhiteListClient;
import org.springframework.context.ApplicationContext;

private ApplicationContext context;

WhiteListClient whiteListClient = context.getBean(WhiteListClient.class);
// user_a 在白名单中, 直接返回 true
assert whiteListClient.isInWhiteList("scope_1", "user_a") == true;
// user_c 在黑名单中, 直接返回 false
assert whiteListClient.isInWhiteList("scope_1", "user_c") == false;
// user_z 不在白名单也不在黑名单中  有30%几率返回false
// 一旦 user_z 返回了 true, 则 user_z 在 scope_1下的所有调用都返回 true (和 subject 的 hashCode 相关)
whiteListClient.isInWhiteList("scope_1", "user_z");
```
**重要: 百分比模式的返回值也要和 subject 有关, 不然会存在一个用户第一次进入系统能看到某个功能，第二次进入又看不到了，发生其他问题**
