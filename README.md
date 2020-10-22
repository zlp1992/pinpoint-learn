

此项目最初fork pinpoint官方插件样例（https://github.com/pinpoint-apm/pinpoint-plugin-sample）

项目旨在介绍pinpoint使用及原理等内容，欢迎贡献



# Pinpoint 分析插件样例

你可以通过编写pinpoint分析插件扩展pinpoint的分析能力。此样例工程展示了如何编写插件，工程包含三个模块：

* plugin-sample-target: 目标类库
* plugin-sample-plugin: 样例插件
* plugin-sample-agent: agent distribution with sample plugin


# 实现一个分析插件
Pinpoint分析插件必须实现 [ProfilerPlugin](https://github.com/naver/pinpoint/blob/master/bootstrap-core/src/main/java/com/navercorp/pinpoint/bootstrap/plugin/ProfilerPlugin.java) 以及 [TraceMetadataProvider](https://github.com/naver/pinpoint/blob/master/commons/src/main/java/com/navercorp/pinpoint/common/trace/TraceMetadataProvider.java)
`ProfilerPlugin` 只会在agent使用， `TraceMetadataProvider` 会被Pinpoint的agent、collector和web都使用。

Pinpoint 通过Java的 [ServiceLoader](https://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html)加载上述提到的实现类。因此插件jar包必须包含下面两个配置文件：

* META-INF/services/com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin
* META-INF/services/com.navercorp.pinpoint.common.trace.TraceMetadataProvider 

每个文件里必须包含实现类的类全名。


### TraceMetadataProvider
Pinpoint通过 TraceMetadataProvider 添加 [ServiceType](https://github.com/naver/pinpoint/blob/master/commons/src/main/java/com/navercorp/pinpoint/common/trace/ServiceType.java)s 以及 [AnnotationKey](https://github.com/naver/pinpoint/blob/master/commons/src/main/java/com/navercorp/pinpoint/common/trace/AnnotationKey.java)s 。

ServiceType 和 AnnotationKey的code 必须唯一. 如果你编写的是个人私有的插件， 你可以使用pinpoint提供的保留的私有code，Pinpoint不会将这些私有code用到其他任何地方 。如果你是开发一个公用的插件，你需要联系Pinpoint开发团队给你的插件分配code。

* 个人私人使用的ServiceType codes如下：
  * Server: 1900 ~ 1999
  * DB client: 2900 ~ 2999
  * Cache client: 8900 ~ 8999
  * RPC client: 9900 ~ 9999
  * Others: 7500 ~ 7999

* 个人私人使用的AnnotaionKey codes 如下：
  * 900 ~ 999


### ProfilerPlugin
分析插件会添加 [TransformCallback](https://github.com/naver/pinpoint/blob/master/bootstrap-core/src/main/java/com/navercorp/pinpoint/bootstrap/instrument/transformer/TransformCallback.java)s 到Pinpoint。

TransformCallback通过添加拦截器、getter以及字段变量转换目标类，可以在样例工程中看到。

样例工程中哪个插件拦截的哪个类，可以在实现了```ProfilerPlugin```的类```SamplePlugin```中看到

