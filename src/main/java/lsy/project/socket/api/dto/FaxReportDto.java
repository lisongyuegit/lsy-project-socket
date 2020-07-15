package lsy.project.socket.api.dto;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class FaxReportDto implements Serializable {

    private final static double a = 6378245.0;
    private final static double pi = 3.14159265358979324;
    private final static double ee = 0.00669342162296594626;

    /**
     * @Fields serialVersionUID : TODO(用一句话描述这个变量表示什么)
     */
    private static final long serialVersionUID = 1L;

    @JSONField(ordinal = 0)
    private String mid = "TSREPORT";

    @JSONField(ordinal = 1)
    private int id;

    @JSONField(ordinal = 2)
    private String tnumber;//终端号码

    @JSONField(ordinal = 3)
    private String datatype;//数据类型 （0：状态正常 2：SOS 告警 3：电量低 4：电量低关机 5：主动关机 6：开机）

    @JSONField(ordinal = 4)
    private String latitude;//原始纬度

    @JSONField(ordinal = 5)
    private String longitude;//原始经度

    @JSONField(ordinal = 6)
    private String clatitude;//纠偏后的纬度（高德）

    @JSONField(ordinal = 7)
    private String clongitude;//纠偏后的纬度（高德）

    @JSONField(ordinal = 12)
    private String address;//定位地址

    @JSONField(ordinal = 8)
    private String loctype;//定位类型 （0：基站 1：GPS）

    @JSONField(ordinal = 9)
    private String power;//电量 (百分比)

    @JSONField(ordinal = 10)
    private String signl;//信息 (百分比)

    @JSONField(ordinal = 11)
    private String created_time;//定位时间

    public String getClongitude() {
        return clongitude;
    }

    public void setClongitude(String clongitude) {
        this.clongitude = clongitude;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTnumber() {
        return tnumber;
    }

    public void setTnumber(String tnumber) {
        this.tnumber = tnumber;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getClatitude() {
        return clatitude;
    }

    public void setClatitude(String clatitude) {
        this.clatitude = clatitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLoctype() {
        return loctype;
    }

    public void setLoctype(String loctype) {
        this.loctype = loctype;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getSignl() {
        return signl;
    }

    public void setSignl(String signl) {
        this.signl = signl;
    }

    public String getCreated_time() {
        return created_time;
    }

    public void setCreated_time(String created_time) {
        this.created_time = created_time;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return mid + "," + id + "," + tnumber + "," +
                datatype + "," + this.getGpsLat() + "," + this.getGpsLng() + "," + loctype + "," + power + "," + signl + "," + created_time + "," + address;
    }

    public String getGpsLng() {
        return FaxReportDto.toWGS84Point(Double.valueOf(clatitude), Double.valueOf(clongitude)).getLng().toString();
    }

    public String getGpsLat() {
        return FaxReportDto.toWGS84Point(Double.valueOf(clatitude), Double.valueOf(clongitude)).getLat().toString();
    }

    //gcj-02  to  wgs-84 高德转GPS
    public static LocationDTO toWGS84Point(double latitude, double longitude) {
        LocationDTO dev = calDev(latitude, longitude);
        double retLat = latitude - dev.getLat().doubleValue();
        double retLon = longitude - dev.getLng().doubleValue();
        dev = calDev(retLat, retLon);
        retLat = latitude - dev.getLat().doubleValue();
        retLon = longitude - dev.getLng().doubleValue();

        return new LocationDTO(new BigDecimal(retLon), new BigDecimal(retLat));
    }

    private static boolean isOutofChina(double lat, double lon) {
        if (lon < 72.004 || lon > 137.8347) {
            return true;
        }
        if (lat < 0.8293 || lat > 55.8271) {
            return true;
        }
        return false;
    }

    public static LocationDTO calDev(double wgLat, double wgLon) {
        if (isOutofChina(wgLat, wgLon)) {
            return new LocationDTO(BigDecimal.ZERO, BigDecimal.ZERO);
        }
        double dLat = calLat(wgLon - 105.0, wgLat - 35.0);
        double dLon = calLon(wgLon - 105.0, wgLat - 35.0);
        double radLat = wgLat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);

        return new LocationDTO(new BigDecimal(dLon), new BigDecimal(dLat));
    }

    private static double calLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;

        return ret;
    }

    private static double calLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
        ;

        return ret;

    }
}

class LocationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 经度，保留小数点6位不够的补0
     */
    private BigDecimal lng;
    /**
     * 纬度，保留小数点6位不够的补0
     */
    private BigDecimal lat;

    public LocationDTO(BigDecimal lng, BigDecimal lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public BigDecimal getLng() {
        return lng;
    }

    public void setLng(BigDecimal lng) {
        this.lng = lng;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }
}

