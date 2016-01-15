# AEnhancer
`*Light Weight Method Enhancer Infrastructure for Application*
`*Mainly for increasing developer productivity and application stability when using Java*

#### ABOVE ALL:
透明接入现有代码，In Most Cases，无代码入侵

# START UP
## 使用场景：
```
主要在对远程接口调用的时候，或者一些等待I/O等的操作。提供轻量级的控制与增强。
	1、提供异常重试（指定重试次数，默认1不重试）
	2、提供对结果的缓存
		－－－提供自定义接口Cacher可以适配不同缓存注解（Spring Cache），以及不同的缓存实现（EhCache，Redis）
	3、提供超时控制（指定任一个方法的超时时间）
	4、提供将一个函数拆分（Split）成多个调用的方式（主要场景：并行、限制批量接口的大小）
		－－－提供自定义接口Spliter可以针对不同的函数进行不同的拆分
	5、提供并行支持（需4的支持,线程池可配置Group隔离）
	6、服务降级支持（Fall Back,默认Fail Silent）
	PS:
	默认实现：
		AggrSpliter：自动拆分集合参数，只需指定每个集合大小
		AggrCacher：
			1）支持类似Spring @Cacheable的注解方式，注解式TTL
    		2）支持配置不同存储介质，已提供对RedisHa的适配实现
    		3）支持spEL自定义缓存key，缓存支持部分命中
			4）可提供手动刷新缓存等方法的支持
```

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
   PS：所有支持的特性都是“正交的”
```

# Development
##	基本架构:

##	模块组件：
   1、processor：最基本的模块，逻辑上代表一个功能点。使用“装饰模式（Decorator）”，类似于标准库的文件IO类型。比如TimeoutProcessor提供超时控制，CacheProcessor提供缓存控制。所有processor继承自Processor，得以引用下一个processor（类似一个processor的引用链）。程序可以修改processor之间的引用顺序，或者实现新的processor从而将多个processor的提供的功能“组装”起来。processor的引用链默认的实现使用了“建造者（Builder）”模式来生成最终对象。

   2、extension：代表了对processor处理过程的进一步抽象。extension（依赖对应的processor）提供“模版方法模式（Template）”，使得用户可以方便地替换processor的实现。CodeBase中现有的几个extension：fallback，hook，split都提供声明式（注解）的扩展，即不用实现特定的接口。

	
## 扩展模式：
   `*1、直接对extension进行实现，这是最常用的开发模式。*
		
		a.方法注解的方式
		比如自定义一个Fallbackable类，对不同的方法进行不同的降级处理：
		
    	@FallbackMock
		public RetType fallbackFuc(Param a, Param b...) {
			...do somthing 
    	    return xxx;
		}
   		
		b.直接实现相应的XXXProxy,其中有相应的接口
   
   `*2、开发新的processor：(使用@Hook来获取完整的扩展能力)*
   其实现在CodeBase中实现的这些逻辑也是使用默认的Hooker对象，hook到切面上实现的。所以如果用户想开发实现更丰富的功能，可以直接实现一个Hookable接口（这个接口的意义基本和Spring的ApplicationContextAware是一样的，使得你的类可以获得当前执行上下文的引用，所以不关心上下文的话，@Override的方法实现为空即可），然后实现任意的方法并用@Hook修饰。那么当程序执行到这个切面的时候便会Hook到用户的方法中，用户便可以自由发挥，定义新的processor，任意改变processor的顺序都可以

	class UserHook implements Hookable {
		@Override
		public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {
						// ...
		}
                   
		@Override
		public void beforeProcess(ProcessContext ctx, Processor currentProcess) {
						// ...
		}
                    
		@Hook
		public AnyType anyName(AnyParam p){
                     	// ...
		}
	}
	
   `*3、Fork此项目，自己修改源代码各个实现吧！*

## 异常处理
	框架定义了2种异常。1、受检框架异常。2、运行期框架异常。
	对于1）
		CodingError一般是注解等写的都问题，需要根据提示在开发期做出修改，会在最上层捕获。
		ShortCricuit异常一般会在超时或者失败的时候抛出,如果有ShortCircuitProcessor支持，则会记录短路信息，并将cause继续向上抛出。否则，在最上层也会将cause抛出，cause会包装在运行时框架异常中。
	对于2）
		运行期异常会被直接抛出
		
# 注：
配置文件参考applicationContext.xml

# 帮助改进:
``` 
1、xml的配置方式
2、对executor的等待队列的配置
3、executor的各个参数的配置
4、代理类的级别优先级控制
5、与Spring Hibernate整合多线程测试
6、短路：流量控制，错误短路。全局的手动控制。
~~7、所有自有实现都使用插件化~~ Done @1.15 by xushuda
8、逻辑流图
```

