# CacheAop
Light Weight Cache Infrastructure for Application

使用场景：
类似EHCACHE，但是提供更flexible和强大的配置，
	1、对批量处理友好（应用不需要分批次请求处理）
	2、对集合参数与返回值友好（可以部分命中缓存）
	3、可提供手动刷新缓存等方法的支持
注：本应用为Light Weight意为，不提供缓存的具体组件，只提供一种面向切面的缓存处理方式

	a)最简单使用：
  @Cached
  public data get(param)
 
	b)对集合类接口的使用：（按照data中的Id来缓存数据）
  @Cached(resultK=getId())
  public List<data> get(List<param>)

	c)批量处理：
  使用狼厂某部门代码说明一下，比如有这样一个函数接口，批量根据AppId获取App，是个远程调用：
  public List<App> getUnionAppsByIds(List<Integer> appIds) 
 	// 下面使用伪代码描述
 	for appId in appIds
 		data = get data from cache by appId
 			if data is valid :
 				add data to success set
 			else
 				add appId to fail set
 	query sets = split fail set to several sets // 根据需求将List分为多个List，每个list为远程接口批调用数目上限
 	for set in query sets:
 		data = remote_call(appIds) // get data by remote call
 		for app in data:
 			appId <- getId(app)
 			save app to cache by appId
 			add app to success set
 	return success set
 	
  拆分请求，组合请求的逻辑基本都是一致的，这里写了很多重复代码，并且难以理解
  使用CacheAop能比较优雅地改写代码，并且业务方只需要关注数据的有效性，与远程接口即可：
    @Cached(expiration = 60000, resultK = "getId()", batchLimit = ID_BATCH_SIZE)
    public List<App> getUnionAppsByIds(List<Integer> appIds) 
 	  return remote_call(appIds)
 
只需要配置批量请求数目上限，和如何从App对象获取AppId即可

注：
使用Spring容器托管Bean，面向切面对数据进行缓存
配置文件参考applicationContext.xml

