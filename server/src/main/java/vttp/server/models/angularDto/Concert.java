package vttp.server.models.angularDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Concert {

    private long id;
    private String artist;
    private String venue;
    private String country;
    private String tour;
    private List<LocalDate> dates = new ArrayList<>();
    private String imageUrl320;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getTour() { return tour; }
    public void setTour(String tour) { this.tour = tour; }

    public List<LocalDate> getDates() { return dates; }
    public void setDates(List<LocalDate> dates) { this.dates = dates; }

    public String getImageUrl320() { return imageUrl320; }
    public void setImageUrl320(String imageUrl320) { this.imageUrl320 = imageUrl320; }

    // Custom methods
    public void addDates(LocalDate date) { this.dates.add(date); }

    
}
