# **工具依赖**
maven<br/>
jdk 1.8

# **使用方法**
执行<br/>
1) ```make all```<br/>
2) ```cd release && tar zxf mib.tar.gz && cd mib```<br/>
3) ```./bin/mib 参数```<br/>

# **执行参数**
```--profile``` 从本地输入数据profile预估服务<br/>
```--log2stats``` 将service log转为aggregate之后的统计信息<br/>
```--kafka``` 读取指定的kafka topics里的数据输出到控制台<br/>
```--parquet2text``` 将本地的parquet文件转换为text文件<br/>
```-f```或```--file``` 指定输入文件的绝对路径
```-t```或```--token``` 指定accessToken<br/>
```-s```或```--schema-json``` 指定schema json或包含此json的文件路径<br/>
```-b```或```--batch-size``` 指定单个request包含的最大的instance数量，仅在```--tsv2req```时生效<br/>
```-c```或```--concurrency``` 指定并发数<br/>
```-o```或```--output``` 指定输出文件的绝对路径<br/>
```-h```或```--help``` 输出帮助信息然后退出<br/>
```--first-line-schema``` 指定输入文件的首行是否是schema名称，尽在```--profile```时生效<br/>
```--delimiter``` 指定输入文件或输出文件的列分隔符，默认为```\t```<br/>
```--service``` 指定分析service log时的service名称，仅在```--log2stats```时生效<br/>
```--operation``` 指定分析service log时的operation名称，仅在```--log2stats```时生效<br/>
```--endpoint``` 指定分析service log时的endpoint，仅在```--log2stats```时生效<br/>
```--status``` 指定分析service log时的status，仅在```--log2stats```时生效<br/>
```--start``` 指定分析service log时的数据起始时间，格式为 yyyy-MM-dd_HH:mm:ss，仅在```--log2stats```时生效<br/>
```--end``` 指定分析service log时的数据终止时间，格式为 yyyy-MM-dd_HH:mm:ss，仅在```--log2stats```时生效<br/>

# **示例**
```
./bin/mib --profile \
-t mib \
-f /Users/dufei/Documents/bank/test_data.csv \
-s /Users/dufei/Documents/bank/3date_schema.json \
-b 1 \
-c 2 \
-d , \
--endpoint http://172.27.128.25:31107/api/predict \
--first-line-schema \
-o /tmp/score
```
