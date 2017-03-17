package person.dufei.utils.main;

import com._4paradigm.predictor.PredictorRequest;
import com._4paradigm.predictor.PredictorRequestItem;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import person.dufei.utils.data.NipOriginal;
import person.dufei.utils.io.file.FileReadUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class NipConversionMain {

    private static final ObjectMapper OM = new ObjectMapper();
//    private static final String NIP_SINGLE = "{\"NewsId\":3145613,\"InstanceKey\":\"4686a985-9716-4efd-8836-29928861a1e6-3145613\",\"NewsType\":\"News\",\"TermScores\":{\"maredl\":0.392893,\"mendonc\":0.341811,\"natal\":0.274191,\"cantor\":0.23158,\"casament\":0.188551,\"quant\":0.172175,\"gratide3\":0.11557,\"felic\":0.100595,\"emoe7e3\":0.100496,\"content\":0.099532},\"CategoryId\":[3,27],\"TagScores\":{\"16\":1.0,\"71\":0.5},\"Hot\":false,\"ImageCount\":1,\"ImageCountToDisplay\":1,\"MediaId\":66,\"PornLevel\":0,\"ContentWords\":94,\"TitleWords\":11,\"TotalHoursSincePublish\":6.8364259979722215,\"TotalLikes\":207,\"TotalDislikes\":10,\"TotalFavorites\":0,\"TotalComments\":13,\"TotalImpressions\":111852,\"TotalAdjustImpressions\":63987,\"TotalClicks\":13970,\"TotalShares\":50,\"UserId\":\"992c58c1-e20b-4fc9-b6ee-3ba8e929628c\",\"UserTagScores\":{\"53\":0.8546,\"76\":0.3549,\"50\":0.2943,\"46\":0.1711,\"45\":0.111,\"51\":0.0605,\"41\":0.0484,\"10\":0.0467,\"16\":0.0403,\"75\":0.0394,\"40\":0.0304,\"48\":0.0286,\"44\":0.0276,\"69\":0.0232,\"71\":0.0205,\"56\":0.498087,\"49\":0.0145,\"22\":0.0046,\"52\":0.0041,\"66\":0.262067,\"12\":0.0034,\"55\":0.826577,\"21\":0.0027,\"47\":0.0018,\"3\":0.0018,\"20\":0.0016,\"28\":0.0012,\"70\":0.0012,\"68\":0.0012,\"72\":0.0012},\"UserTermScores\":{\"fot\":0.5076,\"encontr fot\":0.4516,\"hitl\":0.3506,\"eva\":0.3382,\"espos\":0.2605,\"ter\":0.2388,\"encontr\":0.219,\"alemanh\":0.2017,\"colecion\":0.1941,\"mulh\":0.192},\"RequestLatitude\":-23.2972972972973,\"RequestLongitude\":-51.24099566561741,\"RequestLocation\":\"Parane1\",\"NetworkType\":\"WiFi\",\"Resolution\":\"720*1280\",\"DeviceId\":null,\"DevicePlatform\":\"android\",\"DeviceType\":\"samsung  SM-J700M\",\"Dpi\":\"320\",\"Channel\":\"GooglePlay\",\"VersionCode\":\"30022\",\"UpdateVersionCode\":\"0\",\"CountryId\":-1,\"VideoWidth\":0,\"VideoHeight\":0,\"VideoDuration\":0,\"VideoViewCount\":0,\"VideoLikeCount\":0,\"VideoDiskileCount\":0,\"VideoFavoriteCount\":0,\"VideoCommentCount\":0,\"VideoCrawlerTimeSpan\":0.0,\"UserIp\":\"177.154.230.58:44385\"}";

    public static void main(String[] args) throws Exception {
        String content = FileReadUtils.getContent("/Users/dufei/Documents/nip/nip.json");
//        log.error(content);
        JavaType type = OM.getTypeFactory().constructArrayType(NipOriginal.class);
        NipOriginal[] requests = OM.readValue(content, type);
//        System.out.println(OM.writeValueAsString(original2Request(requests)));
//        log.error("{}", requests.length);
//        log.error("{}", requests[0].getTotalHoursSincePublish());
//        NipOriginal nip = OM.readValue(NIP_SINGLE, NipOriginal.class);
        PredictorRequest request = original2Request(requests);
//        System.out.println(OM.writeValueAsString(request));
        Set<String> fields = request.getRawInstances().get(0).getRawFeatures().keySet();
        fields.forEach(field -> {
            System.out.println(field.toLowerCase() + "=discrete(" + field + ")");
        });
//        List<String> tsv = original2Tsv(requests);
//        tsv.forEach(System.out::println);
    }

    private static PredictorRequest original2Request(NipOriginal[] originals) {
        PredictorRequest request = new PredictorRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setAccessToken("news_in_palm");
        request.setResultLimit(50);
        request.setCommonFeatures(Maps.newHashMap());
        List<PredictorRequestItem> items = Lists.newArrayListWithCapacity(originals.length);
        request.setRawInstances(items);
        for (NipOriginal single : originals) {
            PredictorRequestItem item = new PredictorRequestItem();
            item.setId(String.valueOf(single.getNewsId()));
            Map<String, String> rawFeatures = Maps.newHashMap();
            item.setRawFeatures(rawFeatures);
            items.add(item);

            rawFeatures.put("Channel", single.getChannel());
            rawFeatures.put("NewsId", String.valueOf(single.getNewsId()));
            rawFeatures.put("InstanceKey", single.getInstanceKey());
            rawFeatures.put("TermScores", map2String(single.getTermScores()));
            rawFeatures.put("CategoryId", list2String(single.getCategoryId()));
            rawFeatures.put("TagScores", map2String(single.getTagScores()));
            rawFeatures.put("ImageCount", String.valueOf(single.getImageCount()));
            rawFeatures.put("Hot", String.valueOf(single.isHot()));
            rawFeatures.put("ImageCountToDisplay", String.valueOf(single.getImageCountToDisplay()));
            rawFeatures.put("MediaId", String.valueOf(single.getMediaId()));
            rawFeatures.put("Dpi", single.getDpi());
            rawFeatures.put("PornLevel", String.valueOf(single.getPornLevel()));
            rawFeatures.put("ContentWords", String.valueOf(single.getContentWords()));
            rawFeatures.put("TitleWords", String.valueOf(single.getTitleWords()));
            rawFeatures.put("TotalHoursSincePublish", String.valueOf(single.getTotalHoursSincePublish()));
            rawFeatures.put("TotalLikes", String.valueOf(single.getTotalLikes()));
            rawFeatures.put("TotalDislikes", String.valueOf(single.getTotalDislikes()));
            rawFeatures.put("TotalFavorites", String.valueOf(single.getTotalFavorites()));
            rawFeatures.put("TotalComments", String.valueOf(single.getTotalComments()));
            rawFeatures.put("TotalImpressions", String.valueOf(single.getTotalImpressions()));
            rawFeatures.put("TotalAdjustImpressions", String.valueOf(single.getTotalAdjustImpressions()));
            rawFeatures.put("TotalClicks", String.valueOf(single.getTotalClicks()));
            rawFeatures.put("TotalShares", String.valueOf(single.getTotalShares()));
            rawFeatures.put("UserId", single.getUserId());
            rawFeatures.put("UserTagScores", map2String(single.getUserTagScores()));
            rawFeatures.put("UserTermScores", map2String(single.getUserTermScores()));
            rawFeatures.put("RequestLatitude", String.valueOf(single.getRequestLatitude()));
            rawFeatures.put("RequestLongitude", String.valueOf(single.getRequestLongitude()));
            rawFeatures.put("RequestLocation", single.getRequestLocation());
            rawFeatures.put("NetworkType", single.getNetworkType());
            rawFeatures.put("Resolution", single.getResolution());
            rawFeatures.put("DeviceId", single.getDeviceId() == null ? null : String.valueOf(single.getDeviceId() % 100000));
            rawFeatures.put("DevicePlatform", single.getDevicePlatform());
            rawFeatures.put("DeviceType", single.getDeviceType());
            rawFeatures.put("VersionCode", single.getVersionCode());
            rawFeatures.put("UpdateVersionCode", single.getUpdateVersionCode());
            rawFeatures.put("CountryId", String.valueOf(single.getCountryId()));
            rawFeatures.put("VideoWidth", String.valueOf(single.getVideoWidth()));
            rawFeatures.put("VideoHeight", String.valueOf(single.getVideoHeight()));
            rawFeatures.put("VideoDuration", String.valueOf(single.getVideoDuration()));
            rawFeatures.put("VideoViewCount", String.valueOf(single.getVideoViewCount()));
            rawFeatures.put("VideoLikeCount", String.valueOf(single.getVideoLikeCount()));
            rawFeatures.put("VideoDislikeCount", String.valueOf(single.getVideoDislikeCount()));
            rawFeatures.put("VideoFavoriteCount", String.valueOf(single.getVideoFavoriteCount()));
            rawFeatures.put("VideoCommentCount", String.valueOf(single.getVideoCommentCount()));
            rawFeatures.put("VideoCrawlerTimeSpan", String.valueOf(single.getVideoCrawlerTimeSpan()));
            rawFeatures.put("UserIp", single.getUserIp());
        }
        return request;
    }

    private static List<String> original2Tsv(NipOriginal[] originals) {
        List<String> list = Lists.newArrayListWithCapacity(originals.length+1);
        StringBuilder title = new StringBuilder();
        title.append("Channel").append("\t");
        title.append("NewsId").append("\t");
        title.append("InstanceKey").append("\t");
        title.append("TermScores").append("\t");
        title.append("CategoryId").append("\t");
        title.append("TagScores").append("\t");
        title.append("ImageCount").append("\t");
        title.append("Hot").append("\t");
        title.append("ImageCountToDisplay").append("\t");
        title.append("MediaId").append("\t");
        title.append("Dpi").append("\t");
        title.append("PornLevel").append("\t");
        title.append("ContentWords").append("\t");
        title.append("TitleWords").append("\t");
        title.append("TotalHoursSincePublish").append("\t");
        title.append("TotalLikes").append("\t");
        title.append("TotalDislikes").append("\t");
        title.append("TotalFavorites").append("\t");
        title.append("TotalComments").append("\t");
        title.append("TotalImpressions").append("\t");
        title.append("TotalAdjustImpressions").append("\t");
        title.append("TotalClicks").append("\t");
        title.append("TotalShares").append("\t");
        title.append("UserId").append("\t");
        title.append("UserTagScores").append("\t");
        title.append("UserTermScores").append("\t");
        title.append("RequestLatitude").append("\t");
        title.append("RequestLongitude").append("\t");
        title.append("RequestLocation").append("\t");
        title.append("NetworkType").append("\t");
        title.append("Resolution").append("\t");
        title.append("DeviceId").append("\t");
        title.append("DevicePlatform").append("\t");
        title.append("DeviceType").append("\t");
        title.append("VersionCode").append("\t");
        title.append("UpdateVersionCode").append("\t");
        title.append("CountryId").append("\t");
        title.append("VideoWidth").append("\t");
        title.append("VideoHeight").append("\t");
        title.append("VideoDuration").append("\t");
        title.append("VideoViewCount").append("\t");
        title.append("VideoLikeCount").append("\t");
        title.append("VideoDislikeCount").append("\t");
        title.append("VideoFavoriteCount").append("\t");
        title.append("VideoCommentCount").append("\t");
        title.append("VideoCrawlerTimeSpan").append("\t");
        title.append("UserIp").append("\t");
        title.append("FakeLabel");
        list.add(title.toString());
        for (NipOriginal original : originals) {
            StringBuilder sb = new StringBuilder();
            sb.append(original.getChannel()).append("\t");
            sb.append(original.getNewsId()).append("\t");
            sb.append(original.getInstanceKey()).append("\t");
            sb.append(map2String(original.getTermScores())).append("\t");
            sb.append(list2String(original.getCategoryId())).append("\t");
            sb.append(map2String(original.getTagScores())).append("\t");
            sb.append(original.getImageCount()).append("\t");
            sb.append(original.isHot()).append("\t");
            sb.append(original.getImageCountToDisplay()).append("\t");
            sb.append(original.getMediaId()).append("\t");
            sb.append(original.getDpi()).append("\t");
            sb.append(original.getPornLevel()).append("\t");
            sb.append(original.getContentWords()).append("\t");
            sb.append(original.getTitleWords()).append("\t");
            sb.append(original.getTotalHoursSincePublish()).append("\t");
            sb.append(original.getTotalLikes()).append("\t");
            sb.append(original.getTotalDislikes()).append("\t");
            sb.append(original.getTotalFavorites()).append("\t");
            sb.append(original.getTotalComments()).append("\t");
            sb.append(original.getTotalImpressions()).append("\t");
            sb.append(original.getTotalAdjustImpressions()).append("\t");
            sb.append(original.getTotalClicks()).append("\t");
            sb.append(original.getTotalShares()).append("\t");
            sb.append(original.getUserId()).append("\t");
            sb.append(map2String(original.getUserTagScores())).append("\t");
            sb.append(map2String(original.getUserTermScores())).append("\t");
            sb.append(original.getRequestLatitude()).append("\t");
            sb.append(original.getRequestLongitude()).append("\t");
            sb.append(original.getRequestLocation()).append("\t");
            sb.append(original.getNetworkType()).append("\t");
            sb.append(original.getResolution()).append("\t");
            sb.append(original.getDeviceId()).append("\t");
            sb.append(original.getDevicePlatform()).append("\t");
            sb.append(original.getDeviceType()).append("\t");
            sb.append(original.getVersionCode()).append("\t");
            sb.append(original.getUpdateVersionCode()).append("\t");
            sb.append(original.getCountryId()).append("\t");
            sb.append(original.getVideoWidth()).append("\t");
            sb.append(original.getVideoHeight()).append("\t");
            sb.append(original.getVideoDuration()).append("\t");
            sb.append(original.getVideoViewCount()).append("\t");
            sb.append(original.getVideoLikeCount()).append("\t");
            sb.append(original.getVideoDislikeCount()).append("\t");
            sb.append(original.getVideoFavoriteCount()).append("\t");
            sb.append(original.getVideoCommentCount()).append("\t");
            sb.append(original.getVideoCrawlerTimeSpan()).append("\t");
            sb.append(original.getUserIp()).append("\t");
            sb.append(ThreadLocalRandom.current().nextInt(2));
            list.add(sb.toString());
        }
        return list;
    }

    private static String map2String(Map<String, ? extends Number> map) {
        StringBuilder sb = new StringBuilder();
        if (map != null) {
            for (Map.Entry<String, ? extends Number> entry : map.entrySet()) {
                sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
            }
        }
        return (sb.length() > 0) ? sb.deleteCharAt(sb.length()-1).toString() : sb.toString();
    }

    private static String list2String(List<? extends Number> list) {
        StringBuilder sb = new StringBuilder();
        if (list != null) for (Number n : list) sb.append(n).append(",");
        return (sb.length() > 0) ? sb.deleteCharAt(sb.length()-1).toString() : sb.toString();
    }

}
