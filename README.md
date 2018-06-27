# **工具依赖**
maven<br/>
jdk 1.8

# **使用方法**
执行<br/>
1) ```mvn clean package```<br/>
2) ```chmod a+x target/bin/mib```<br/>
3) ```./target/bin/mib 参数```<br/>

# **执行参数**
```--profile``` 从本地输入数据profile预估服务<br/>
```--log2stats``` 将service log转为aggregate之后的统计信息<br/>
```--kafka``` 读取制定的kafka topics里的数据输出到控制台<br/>
```-t```或```--token``` 指定accessToken<br/>
```-s```或```--schema-json``` 指定schema json或包含此json的文件路径<br/>
```-b```或```--batch-size``` 指定单个request包含的最大的instance数量，仅在```--tsv2req```时生效<br/>
```-a```或```--as-endpoint``` 指定AS的endpoint，仅在```--tsv2score```时生效<br/>
```-c```或```--concurrency``` 指定请求AS的并发数，仅在```--tsv2score```时生效<br/>
```-l```或```--log-path``` 指定service log文件名，仅在```--log2stats```时生效<br/>
```--service``` 指定分析service log时的service名称，仅在```--log2stats```时生效<br/>
```--operation``` 指定分析service log时的operation名称，仅在```--log2stats```时生效<br/>
```--endpoint``` 指定分析service log时的endpoint，仅在```--log2stats```时生效<br/>
```--status``` 指定分析service log时的status，仅在```--log2stats```时生效<br/>
```--start``` 指定分析service log时的数据起始时间，格式为 yyyy-MM-dd_HH:mm:ss，仅在```--log2stats```时生效<br/>
```--end``` 指定分析service log时的数据终止时间，格式为 yyyy-MM-dd_HH:mm:ss，仅在```--log2stats```时生效<br/>

# **示例**
```
./target/bin/mib --profile \
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
