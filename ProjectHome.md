# 还算比较的小巧的内容管理系统吧 #
## 前言 ##
说实话，要跑起来这东西，不算容易（如果你也是码农，可能好说点）<br>
所以，这个文档其实也是面向二次开发的或者自己没时间写这么一个东西的码农（原谅我用了这个词）<br>
<h2>搭建环境</h2>
<h3>所需的软件环境</h3>
<ul><li>jdk1.6+<br>
</li><li>mysql 5.0+<br>
</li><li>需要支持servlet2.5+的容器<br>
<h3>配置数据库</h3>
因为不小心用了下spring<br>
也就不小心的多了个application.properties<br>
因此，需要修改数据库配置，就应该在这里<br>
它是这样子的：<br>
<pre><code>#jdbc settings<br>
jdbc.dirverClass=com.mysql.jdbc.Driver<br>
jdbc.read.url=jdbc:mysql://your-readdb-address/dbname?useUnicode=true&amp;characterEncoding=UTF-8<br>
jdbc.read.username=readdb-username<br>
jdbc.read.password=readdb-password<br>
jdbc.write.url=jdbc:mysql://writedb-adress/dbname?useUnicode=true&amp;characterEncoding=UTF-8<br>
jdbc.write.username=writedb-username<br>
jdbc.write.password=writedb-password<br>
</code></pre>
看这里，可能你会疑惑：配两个db地址？？？<br>
其实是这样的，因为mimo做了mysql的读写分离（读写分离！！！google code都废弃了哇），所以需要两个库<br>
如果你数据库没有做读写分离，copy一下就可以（如果RMB也可以这样，那该有多好）<br>
<h3>导入数据</h3>
mimo提供了一个建表，以及初始数据的sql文件，导一下，就可以了<br>
sql文件在哪里？别懒，找找吧。。。其实我比你更懒，不好意思啦<br>
<h3>运行项目</h3>
如果你下载的是war包，那么用rar的工具，把你修改后的application.properties覆盖进去，然后把war放到jee容器中（tomcat,resin。。。），再启动容器<br>
接着访问以下路径（以tomcat为例，默认端口为8080，war包叫mimo.war）：<br>
<a href='http://localhost:8080/mimo/login'><a href='http://localhost:8080/mimo/login'>http://localhost:8080/mimo/login</a></a><br>
这时，你就会看到mimo后台的登陆页面了，对了，忘记告诉你，默认账号是admin，密码是admin135（记得修改）<br>
如果还看不到登陆页面的话，联系我吧。。。<br>
<h3>关于classpath下的lexicon.txt</h3>
该文件为敏感词库文件，每个词为一行<br>
因为这个社会需要和谐，我们要过滤掉我们认为（或者party认为）不是很和谐的词语<br>
这个词库就是用来搜集那些可能会引起不和谐的词语<br>
默认提供了一个词库，1200多个词，有需要，请自己补充<br>
目前来看，过滤的速度还不错，将就用吧<br>
因为我偷了下懒，所以你更新了词库，需要重启才能生效哦<br>
ps:用户留言发现不和谐的东西时，会自动调整状态为审核不通过，当然，像过滤敏感词这么伟大的东西，总是需要人这种伟大的生物参与的<br>
<h3>sql优化</h3>
提供的sql文件里面，做了部分查询语句的优化，这部分仅仅就是一小部分，其它的，你来吧<br>
如果你优化完，突然发现快了好多，一定会很有成就感（谢谢我吧）