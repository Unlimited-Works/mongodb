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
写在一个类中,让我们直观的看得出结果.以后再慢慢拆分.

#### Json Structure
```
{
  taskId: "UUID"
  db: "helloworld",
  collection: "test",
  method: "find/insert/update/remove/aggregate/count",//besides, count could be replaced by aggregate
  params:{...}
  
}
```

#### 不需要什么功能?
这里的查询只是针对最基本的操作,虽然mongo也提供的了一些常用的方法,如排序,但是这些操作并不准备
放在这里.或者说,针对数据的整理是另一个进程在处理.总之,如果不提供某种操作也不会影响功能和效率,
那么就可以省略.  
::对于find.limit.sort,可以用aggregate完成.

#### 计划
对于每一个请求,返回一个值给对方,双方通过taskId来标示.接收方的自行处理(将以Future/Observable的形式返回,相比两者,Observable更加通用).