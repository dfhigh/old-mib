# **工具依赖**
maven<br/>
jdk 1.8

# **使用方法**
执行<br/>
1) ```mvn clean package```<br/>
2) ```chmod a+x target/bin/mib```<br/>
3) ```./target/bin/mib 参数```<br/>

# **执行参数**
```--mode2lreq``` 将线上模型直接转为request json，从stdout输出<br/>
```--tsv2req``` 将本地tsv文件转为request json，从stdout输出<br/>
```--tsv2score``` 将本地tsv文件转为prediction score，从stdout输出<br/>
```--log2stats``` 将service log转为aggregate之后的统计信息<br/>
```-u```或```--model-url``` 指定线上模型url，仅在```--model2req```时生效<br/>
```-t```或```--token``` 指定accessToken<br/>
```-f```或```--tsv-file``` 指定tsv文件路径，仅在```--tsv2req```时生效<br/>
```-s```或```--schema-json``` 指定schema json文件路径<br/>
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
./target/bin/mib --tsv2req \
-t 123456 \
-f /Users/dufei/Documents/1212/bank_data_with_date \
-s /Users/dufei/Documents/1212/model_28_schema.json \
-b 400 \
> /Users/dufei/Documents/1212/bank_requests
```
