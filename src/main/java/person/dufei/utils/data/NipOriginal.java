package person.dufei.utils.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NipOriginal {
    @JsonProperty("NewsId")
    private int NewsId;
    @JsonProperty("InstanceKey")
    private String InstanceKey;
    @JsonProperty("TermScores")
    private Map<String, Double> TermScores;
    @JsonProperty("CategoryId")
    private List<Integer> CategoryId;
    @JsonProperty("TagScores")
    private Map<String, Double> TagScores;
    @JsonProperty("Hot")
    private boolean Hot;
    @JsonProperty("ImageCount")
    private int ImageCount;
    @JsonProperty("ImageCountToDisplay")
    private int ImageCountToDisplay;
    @JsonProperty("MediaId")
    private int MediaId;
    @JsonProperty("PornLevel")
    private int PornLevel;
    @JsonProperty("ContentWords")
    private int ContentWords;
    @JsonProperty("TitleWords")
    private int TitleWords;
    @JsonProperty("TotalHoursSincePublish")
    private double TotalHoursSincePublish;
    @JsonProperty("TotalLikes")
    private int TotalLikes;
    @JsonProperty("TotalDislikes")
    private int TotalDislikes;
    @JsonProperty("TotalFavorites")
    private int TotalFavorites;
    @JsonProperty("TotalComments")
    private int TotalComments;
    @JsonProperty("TotalImpressions")
    private int TotalImpressions;
    @JsonProperty("TotalAdjustImpressions")
    private int TotalAdjustImpressions;
    @JsonProperty("TotalClicks")
    private int TotalClicks;
    @JsonProperty("TotalShares")
    private int TotalShares;
    @JsonProperty("UserId")
    private String UserId;
    @JsonProperty("UserTagScores")
    private Map<String, Double> UserTagScores;
    @JsonProperty("UserTermScores")
    private Map<String, Double> UserTermScores;
    @JsonProperty("RequestLatitude")
    private double RequestLatitude;
    @JsonProperty("RequestLongitude")
    private double RequestLongitude;
    @JsonProperty("RequestLocation")
    private String RequestLocation;
    @JsonProperty("NetworkType")
    private String NetworkType;
    @JsonProperty("Resolution")
    private String Resolution;
    @JsonProperty("DeviceId")
    private Long DeviceId;
    @JsonProperty("DevicePlatform")
    private String DevicePlatform;
    @JsonProperty("DeviceType")
    private String DeviceType;
    @JsonProperty("Dpi")
    private String Dpi;
    @JsonProperty("Channel")
    private String Channel;
    @JsonProperty("VersionCode")
    private String VersionCode;
    @JsonProperty("UpdateVersionCode")
    private String UpdateVersionCode;
    @JsonProperty("CountryId")
    private int CountryId;
    @JsonProperty("VideoWidth")
    private int VideoWidth;
    @JsonProperty("VideoHeight")
    private int VideoHeight;
    @JsonProperty("VideoDuration")
    private int VideoDuration;
    @JsonProperty("VideoViewCount")
    private int VideoViewCount;
    @JsonProperty("VideoLikeCount")
    private int VideoLikeCount;
    @JsonProperty("VideoDislikeCount")
    private int VideoDislikeCount;
    @JsonProperty("VideoFavoriteCount")
    private int VideoFavoriteCount;
    @JsonProperty("VideoCommentCount")
    private int VideoCommentCount;
    @JsonProperty("VideoCrawlerTimeSpan")
    private long VideoCrawlerTimeSpan;
    @JsonProperty("UserIp")
    private String UserIp;
}
