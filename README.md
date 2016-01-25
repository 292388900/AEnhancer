# AEnhancer

*Light Weight & Non-Intrusive Method Enhancer Infrastructure for Application*  
*Mainly for increasing developer productivity and application stability when using Java*

#### ABOVE ALL:
透明接入现有代码，In Most Cases，无代码入侵

# START UP
## 使用场景：
对任意方法调用（METHOD），**提供无侵入，配置式**的增强。不需要修改原来的方法，即可给你的程序加入这些特性：  
    * 1、异常重试  
    * 2、缓存机制  
    * 3、超时控制  
    * 4、并行支持  
    * 6、服务降级  
    * 7、异常模块（比如依赖的远程服务失效）的短路控制  
    * 8、流量限制（方法调用次数控制）  
      
	PS：
		1、上述所有支持的特性都是“正交的”（正交性：互不影响，任意组合） 
		2、在使用并行和超时控制的时候，由于线程会walk away from current thread：
		   所以对于任何线程变量（ThreadLocal）的访问，和参数中非线程安全的对象引用都需要多加小心。 
		3、由于Enhancer的状态机囿于一个Jvm中，所以对于分布式环境，需要更多的考虑：
		   比如流量限制模块就需要结合LoadBalance策略考虑
		4、best practice：可以与分布式配置中心，或Monitor结合，以提供分布式环境的集成
		5、可以实现许多有用的功能比如：
			场景1:有个web项目，现在有功能需要上线发布，由于后方依赖还没上线，不希望用户方法方法。
			用框架的短路功能将这个功能短路即可。
			场景2:比如web项目的一个restful接口，如果输入非常大则耗时很长，同时有大量输入还有可能打满线程池。
			那么传统的做法可以修改代码增加一个参数量或者范围限制，但是这样的弊端就是要改代码，如果要可配置动态调整，			还是有不小的workload，范围太小还会阻碍大部分用户正常使用。所以，可以增加一个超时控制，来更直观地控制
			服务稳定性。
			场景3:比如公司某rpc接口，给各个使用方提供服务。但是对“试用用户”有调用次数限制，那么可以使用短路模块
			（实现ShortCircuitProxy）自定义流量控制功能（比如分用户count，一小时多少次）。
	安利一下：
	只需要在xml中添加两行，就能拥有这些功能！
		<aop:aspectj-autoproxy />
		<context:component-scan base-package="com.baidu.aenhancer" />

## 示例：

```
   a.比如想为某个接口添加超时控制：
		@Enhancer( timeout = 100 )
		public Return task(Param p) {
			...do something
		}
   b.比如想给某给方法提供重试的机制支持：
		@Enhancer( retry = 5 )
		public Return task(Param p) {
			...do something
		}
   c.如下是一个包含了使用默认实现的Enhancer式例：
		(PS：@Aggr是默认实现所定义的注解，用户的实现也能定义任意注解，自行解析）
		@Aggr(sequential = true, batchSize = 1)
    	@Enhancer( //
        	    timeout = 100, // 超时时间
           		cacher = AggrCacher.class, // 缓存策略：按集合对象中的元素缓存
            	spliter = AggrSpliter.class, // 拆分成多次调用的策略：按集合元素个数拆分
            	parallel = true, // 可并行
            	group = "ServiceGroupA", // 所属的组
            	fallback = ReturnNull.class, // 降级策略
            	retry = 3 // 异常重试次数3
    	)
    	public List<TaskResult> runTask(String[] args, Object paramX)
  
```

# Development
##	基本架构:

##	模块组件：
   1、processor：最基本的模块，逻辑上代表一个功能点。使用“装饰模式（Decorator）”，类似于标准库的文件IO类型。比如TimeoutProcessor提供超时控制，CacheProcessor提供缓存控制。所有processor继承自Processor基类，得以引用下一个processor（组成类似一个processor的引用链）。用户程序可以修改processor之间的引用顺序，或者实现新的processor从而将多个processor的提供的功能“组装”起来。processor的引用链默认的实现使用了“建造者（Builder）”模式来生成最终对象。

   2、extension：代表了对processor处理过程的进一步抽象。extension（依赖对应的processor）提供“模版方法模式（Template）”，使得用户可以方便地替换processor的具体实现类。CodeBase中现有的几个extension：fallback，parallel都提供声明式（注解）的扩展，即不用实现特定的接口。

	
## 扩展模式：
   *1、直接对extension进行实现，这是最常用的开发模式。*  
  		比如降级处理：  
		
		a.注解的方式标识实现方法
		
    	@Fallback
		public RetType fallbackFuc(Param a, Param b...) {
			...do somthing 
    	    return xxx;
		}
   		
		b.直接实现相应的XXXProxy,其中有相应完整的接口（实现了contextural接口,能获得当前调用方法的上下文）
   
   *2、开发新的processor：(使用@Hook来获取完整的扩展能力)*  
   其在CodeBase中，提供了钩子：HookProcessor。所以如果用户想开发实现更丰富的功能，可以直接实现一个HookProcessor，这个processor在所有processor之前调用，用户可以自由发挥，定义新的processor，任意改变processor的顺序都可以
	
   *3、Fork此项目，自己修改源代码各个实现吧！*
   
   *4、默认实现（extension）：*  
		AggrSpliter：自动拆分集合参数，只需指定每个集合大小，便可以对接口进行并行化支持  
		AggrCacher：  
			1）支持类似Spring @Cacheable的注解方式，注解式TTL  
    		2）支持配置不同存储介质，已提供对RedisHa的适配实现  
    		3）支持spEL自定义缓存key，缓存支持部分命中  
			4）可提供手动刷新缓存等方法的支持    
	
	PS： Processor和extension都可以是stateful的，可以在一次aop处理流程中保持自己的object member。
	
## 异常处理：
	框架定义了2种异常。1、受检框架异常。2、运行期框架异常。
	对于1）
		ShortCircuit异常一般会在超时或者失败的时候抛出,如果有ShortCircuitProcessor支持，则会记录短路信息，并将cause继续向上抛出。否则，在最上层也会将cause抛出，cause会包装在运行时框架异常中。
	对于2）
		运行期异常会被直接抛出
		
# 注：
配置文件参考applicationContext.xml  
在有继承关系的bean同时存在时，根据class指定extension将会出错    
用户自己run test case 时需要把RedisHa这个类去掉。  

# 帮助改进特性: 
1、xml的配置方式  
~~2、线程池，短路控制模块读取配置文件~~ Done @1.19 by xusoda  
4、代理类的级别优先级控制  
~~5、与Spring Hibernate整合多线程测试~~ Done @1.17 by xusoda  
~~6、短路：流量控制，错误短路。~~ Done @1.16 by xusoda  
~~7、所有自有实现都使用插件化~~ Done @1.15 by xusoda  
8、逻辑流图  
~~9、改为spring加载processor~~ Done @1.20 by xusoda     
~~10、使得配置可以reload，或者说override~~ Done @1.19 by xusoda  
~~11、使用静态织入的方式，不用对Spring依赖，只对Aspectj依赖~~ Pending @1.17 by xusoda  
~~12、短路分method控制，线程池分group控制~~ Done @1.20 by xusoda  
~~13、框架模块失效控制?~~ Discard（由实现类容错） @1.17 by xusoda   
14、注解支持并行度控制，使用信号量模拟多个线程池隔离  
15、信号以及数据传输到Master的中间件，屏蔽传输细节，提供vm之间交互的接口  Designing @1.20
~~16、将部分runtime的解析调整到loadtime~~ Done @1.24 by xusoda  
17、验证直接根据原类获得的method对象在代理类上调用的结果，related to (4)    
18、re fac 动态加载刷新配置模块，支持interface动态刷新用户自定义配置  