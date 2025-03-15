package vttp.server.models;

public class Artist {

    private String spotifyId;
    private String name;
    private String imageUrl640;
    private String imageUrl320;
    private String imageUrl160;

    public String getSpotifyId() { return spotifyId; }
    public void setSpotifyId(String spotifyId) { this.spotifyId = spotifyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl640() { return imageUrl640; }
    public void setImageUrl640(String imageUrl640) { this.imageUrl640 = imageUrl640; }

    public String getImageUrl320() { return imageUrl320; }
    public void setImageUrl320(String imageUrl320) { this.imageUrl320 = imageUrl320; }

    public String getImageUrl160() { return imageUrl160; }
    public void setImageUrl160(String imageUrl160) { this.imageUrl160 = imageUrl160; }

    

}
