# libXDUQuery
西电查询服务接口

### 🏆 获取与使用
- Download the ZIP file of this repository or `git clone https://github.com/hwding/libXDUQuery.git`  
- The latest-compiled JAR file is in [./dist/libXDUQuery_jar/](https://github.com/hwding/libXDUQuery/tree/master/dist/libXDUQuery_jar)
- Add the JAR file to your project

### 😀 已支持
- **教务系统 -> EduSystem**
- **校一卡通 -> ECard**
- **体育打卡 -> SportsClock**
- **物理实验 -> PhysicsExperiment**
- **水电用量 -> WaterAndElectricity**

### 😂 待支持
- **校图书馆 -> SchoolLibrary**
- **校网流量 -> CampusNetwork**

### 📜 接口文档
- API documentation is temporarily unavailable
- You can read [Test.java](https://github.com/hwding/libXDUQuery/blob/master/src/test/Test.java) along with those comments in the [source codes](https://github.com/hwding/libXDUQuery/tree/master/src/com/amastigote/xdu/query/module) for a quick understanding

### 😍 特性
- 为每个账户对象实现了Serializable接口, 对象反序列化后如果登录状态检测依旧有效(`checkIsLogin("username")` -> `true`), 则可直接调用查询方法
