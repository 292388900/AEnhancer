# AEnhancer
Light Weight Method Enhancer Infrastructure for Application
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
	7、TODO，短路控制（或者降级策略的自动化）
	PS:
	默认实现：
		Spliter：自动拆分集合参数，可指定每个集合大小
		Cacher：
			1）支持类似Spring @Cacheable的注解方式：
    		2）支持配置不同存储介质,提供对RedisHa的适配实现
    		3）支持对集合参数注解getKey，缓存支持部分命中
			4）可提供手动刷新缓存等方法的支持
```
式例：
```
	比如想为某个接口添加超时控制：
		@Enhancer( timeout = 100 )
		public Return task(Param p) {
			...do something
		}
	比如想给某给方法提供重试的机制支持：
		@Enhancer( retry = 5 )
		public Return task(Param p) {
			...do something
		}
	.......
	PS：所有支持的特性都是“正交的”
	.......
	如下是一个包含了使用默认实现的Enhancer式例：
		(PS：@Aggr是默认实现所定义的注解，用户的实现也能定义任意注解，自行解析）
		@Aggr(sequential = true, batchSize = 1)
    	@Enhancer( //
        	    timeout = 100, // 超时时间
           		cacher = AggrCacher.class, // 缓存策略：按集合对象中的元素缓存
            	spliter = AggrSpliter.class, // 拆分成多次调用的策略：反集合元素个数拆分
            	parallel = true, // 可并行
            	group = "ServiceGroupA", // 所属的组
            	fallback = ReturnNull.class, // 降级策略
            	retry = 3 // 异常重试次数3
    	)
    	public List<TaskResult> runTask(String[] args, Object paramX)
    .....
    自定义方法(使用注解来标注用户自定义方法):
    	1、降级策略：
    	@FallbackMock
   		public RetType fallbackFuc(Param a, Param b...) {
	        return null;
    	}
   		2、拆分请求策略：
   		@Split
   		public List<?> splitFuc(Date start,Date end,Param x...) {
   			do something...
   		}
   		@Collapse
   		public RetType collapseFuc(List<RetType> multiResult) {
   			something like combine the result, or join ....
   		}
   		
    
```
注：
使用Spring容器托管Bean，面向切面对数据进行缓存
配置文件参考applicationContext.xml

