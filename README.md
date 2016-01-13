# AEnhancer
Light Weight Scheduler Infrastructure for Application
```
使用场景：
在对远程接口调用的时候，或者一些等待I/O等的操作。提供轻量级的控制与增强。
	1、提供异常重试（指定重试次数，默认0不重试）
	2、提供对结果的缓存
		－－－提供自定义接口Cacher可以适配不同缓存注解（Spring Cache），以及不同的缓存实现（EhCache，Redis）
	3、提供超时控制（指定任一个方法的超时时间）
	4、提供将一个函数拆分（Split）成多个调用的方式（主要场景：并行、限制批量接口的大小）
		－－－提供自定义接口Spliter可以针对不同的函数进行不同的拆分
	5、提供并行支持（需4的支持,线程池可配置Group隔离）
	6、服务降级支持（Fall Back,默认Fail Silent）
	PS:
	默认实现：
		Spliter：自动拆分集合参数，可指定每个集合大小
		Cacher：
			1、对RedisHa的适配，支持类似Spring @Cacheable的注解方式：
    		2、支持不同存储介质
    		3、支持对集合参数注解getKey，缓存支持部分命中
			4、可提供手动刷新缓存等方法的支持
```

注：
使用Spring容器托管Bean，面向切面对数据进行缓存
配置文件参考applicationContext.xml

