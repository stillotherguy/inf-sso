设置hosts文件，添加两个本地映射
127.0.0.1 ssoclient
127.0.0.1 ssoserver
进入client输入mvn tomcat7:run启动shiro cas客户端
进入server输入mvn jetty:run启动cas服务端（因为tomcat不支持war包的overlay）
打开浏览器输入ssoclient:8081/index会跳到cas server，输入liuyang/liuyang即可登录成功跳回客户端，由于server的证书不受信，所以对shiro源码稍作修改，后续可以自行扩展
