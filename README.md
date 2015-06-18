还算比较的小巧的内容管理系统吧
前言
说实话，要跑起来这东西，不算容易（如果你也是码农，可能好说点）
所以，这个文档其实也是面向二次开发的或者自己没时间写这么一个东西的码农（原谅我用了这个词）

搭建环境
所需的软件环境
jdk1.6+
mysql 5.0+
需要支持servlet2.5+的容器
配置数据库
因为不小心用了下spring
也就不小心的多了个application.properties
因此，需要修改数据库配置，就应该在这里
它是这样子的：
#jdbc settings
jdbc.dirverClass=com.mysql.jdbc.Driver
jdbc.read.url=jdbc:mysql://your-readdb-address/dbname?useUnicode=true&characterEncoding=UTF-8
jdbc.read.username=readdb-username
jdbc.read.password=readdb-password
jdbc.write.url=jdbc:mysql://writedb-adress/dbname?useUnicode=true&characterEncoding=UTF-8
jdbc.write.username=writedb-username
jdbc.write.password=writedb-password
看这里，可能你会疑惑：配两个db地址？？？
其实是这样的，因为mimo做了mysql的读写分离（读写分离！！！google code都废弃了哇），所以需要两个库
如果你数据库没有做读写分离，copy一下就可以（如果RMB也可以这样，那该有多好）
导入数据
mimo提供了一个建表，以及初始数据的sql文件，导一下，就可以了
sql文件在哪里？别懒，找找吧。。。其实我比你更懒，不好意思啦
运行项目
如果你下载的是war包，那么用rar的工具，把你修改后的application.properties覆盖进去，然后把war放到jee容器中（tomcat,resin。。。），再启动容器
接着访问以下路径（以tomcat为例，默认端口为8080，war包叫mimo.war）：
http://localhost:8080/mimo/login
这时，你就会看到mimo后台的登陆页面了，对了，忘记告诉你，默认账号是admin，密码是admin135（记得修改）
如果还看不到登陆页面的话，联系我吧。。。
关于classpath下的lexicon.txt
该文件为敏感词库文件，每个词为一行
因为这个社会需要和谐，我们要过滤掉我们认为（或者party认为）不是很和谐的词语
这个词库就是用来搜集那些可能会引起不和谐的词语
默认提供了一个词库，1200多个词，有需要，请自己补充
目前来看，过滤的速度还不错，将就用吧
因为我偷了下懒，所以你更新了词库，需要重启才能生效哦
ps:用户留言发现不和谐的东西时，会自动调整状态为审核不通过，当然，像过滤敏感词这么伟大的东西，总是需要人这种伟大的生物参与的
sql优化
提供的sql文件里面，做了部分查询语句的优化，这部分仅仅就是一小部分，其它的，你来吧
如果你优化完，突然发现快了好多，一定会很有成就感（谢谢我吧）
原作者：小小小明
