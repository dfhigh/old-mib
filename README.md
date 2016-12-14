**工具依赖**
maven
jdk 1.8

**使用方法**
执行
1) mvn clean package
2) chmod a+x target/bin/dufei-util
3) ./target/bin/dufei-util 参数

**执行参数**
--mode2lreq 将线上模型直接转为request json，从stdout输出
--tsv2req 将本地tsv文件转为request json，从stdout输出
--tsv2score 将本地tsv文件转为prediction score，从stdout输出
-u或--model-url 指定线上模型url，仅在--model2req时生效
-t或--token 指定accessToken
-f或--tsv-file 指定tsv文件路径，仅在--tsv2req时生效
-s或--schema-json 指定schema json文件路径
-b或--batch-size 指定单个request包含的最大的instance数量，仅在--tsv2req时生效
-a或--as-endpoint 指定AS的endpoint，仅在--tsv2score时生效
-c或--concurrency 指定请求AS的并发数，仅在--tsv2score时生效

**示例**
./target/bin/dufei-util --tsv2req -t 123456 -f /Users/dufei/Documents/1212/bank_data_with_date -s /Users/dufei/Documents/1212/model_28_schema.json -b 400 > /Users/dufei/Documents/1212/bank_requests
