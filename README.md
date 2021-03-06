#### How to save document?
data with json save to bson
 
#### How to communicate with this project
1. import this package jar
2. use TCP protocol (em.. thrift? protoBuf? Actor? kafka? custom? or ...)

#### NO model
use json string represent data for simplify

#### Dependency Inject
Cake Pattern, Guice(by java 5 annotation)

#### How to begin?
我经常疑惑如何开始入手,或者说如何用代码表达我的想法.最后,我发现最好的开始方式就是先把最核心的功能流程
写在一个类中,让我们直观的看得出结果,然后再拆分出来.

#### Json Structure
```
{
  taskId: "...",//client-server response connection
  db: "blog",
  collection: "test",
  method: "find/insert/update/remove/aggregate/count",//besides, count could be replaced by aggregate
  params:{...}
  
}
```
Besides, all data represent as string - NEEDN'T, use Bson type flag can create bson by json

#### 不需要什么功能?
这里的查询只是针对最基本的操作,虽然mongo也提供的了一些常用的方法,如排序,但是这些操作并不准备
放在这里.或者说,针对数据的整理是另一个进程在处理.总之,如果不提供某种操作也不会影响功能和效率,
那么就可以省略.  
::对于find.limit.sort,可以用aggregate完成.

####请求和返回值的标示  
使用 线程名 + System.nanoTime来标示,因为socket已经标示了通信的进程,为了在进程中进一步确定通信对象,那就是线程和时间是最合适的.
客户端只要确定收到消息中的线程名和时间就可以认定该响应对应哪一次的请求.(把这个消息放入哈希表中,因为taskId作为hash的key搜索算法复杂度是O(1)).
结构是HashMap(taskId -> (proto => T)),每个taskId对应一个函数,表示响应的事件.

#### 计划
1. 对于每一个请求,返回一个值给对方,双方通过taskId来标示.接收方的自行处理
2. log system (kafka)  
3. rework after msg parse failed at CURD execute method  

规范execute中的自动mongo查询的taskId,及时终止操作.因为taskId现在用在了其他手动编码的场景下了.
当Overview解析某次操作失败后,可以继续接受该类信息.除此之外,其他协议都应该有能力忽略错误的信息.(这里具体指
匹配了taskId,但内容并没有对上而出现的异常)

custom protocol should be GC.

####TODO
* 现在创建了多个Observable同时处理taskId,造成响应的数据并不是合适的地方.应该在一个入口将taskId解析出来,并通过Dispatch方法调度出真正处理的方法.
* 健全的异常处理机制 - 现在会因为发错消息而整个服务不可用.
* 模块不再使用flatmap产生新数据流,而是从Model中dispatch各种子model,子模块通过继承/注册进行横向的业务功能扩展

####为什么写这个程序?
为了细分整个博客项目,更灵活的读取数据而将"数据的获取"分离出来,该项目和Play项目共同组成一个项目.
1. 为什么用socket作为通信协议,而不是jar包?
socket作为进程间的通信方式,更加灵活和自由.
1.1 为什么不用thrift或者protobuf?
1) 现在不能预期需要的数据通信方式是什么样的,自己编写通信方式更加自由
2) thrift和protobuf基于跨语言的数据模板和RPC方法,需要经过编译,在一个人的开发中显得笨重.
