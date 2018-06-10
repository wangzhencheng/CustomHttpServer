步骤-开发1：
1-1 引入http.jar 编写普通java项目，仿SocketHttpHelper.java#main(args)写http服务
1-2 编写index.html普通网页，调用1中的服务

步骤-打包2：
2-1 PackTools.pack指定输出目录 端口号 浏览器路径
2-2 copy网页和你的1-1中的jar到输出目录
2-3 run.vbs启动并自动打开浏览器  stop.vbs关闭http服务