package come.htk.bean;

import android.graphics.Bitmap;

/**
 * 我的主页的缩略图信息
 *
 * Created by Administrator on 2015/3/20.
 */
public class SmallPhotoBean {

    private int userId;
    private int rs_id;
    private String addrPath;
    private String albumName;

    private Bitmap bitmap;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRs_id() {
        return rs_id;
    }

    public void setRs_id(int rs_id) {
        this.rs_id = rs_id;
    }

    public String getAddrPath() {
        return addrPath;
    }

    public void setAddrPath(String addrPath) {
        this.addrPath = addrPath;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }
    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
