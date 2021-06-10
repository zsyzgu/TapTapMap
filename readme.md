在DataCollection.java中添加一类需要存储的数据：

1. 新建一个继承Info类的XXXInfo类
2. 在XXXInfo中声明需要被存储的成员变量
3. 在XXXInfo中实现Info的虚函数collectData：该实现需要更新成员变量，更新完成后调用save()函数，该对象就会被存储在手机文件XXXInfo.json中。
4. 在函数DataCollection.collectData()中添加一句：new XXXInfo().collectData();
5. 如果需要在onCreate时开启某些服务，可以在initService函数中实现。