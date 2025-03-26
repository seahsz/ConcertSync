package vttp.server.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.DateOperators.DateFromString;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import vttp.server.models.Setlist;
import vttp.server.models.SetlistSet;
import vttp.server.models.SetlistSong;

@Repository
public class SetlistRepository {

    private static final Logger logger = Logger.getLogger(SetlistRepository.class.getName());

    private static final String C_SETLISTS = "setlists";

    private static final String F_ID = "id";

    @Autowired
    private MongoTemplate template;

    public void saveSetlist(Setlist setlist) {
        try {
            Document doc = setlist.toDocument();
            Query query = new Query(Criteria.where(F_ID).is(setlist.getId()));
    
            // Use upsert
            template.upsert(query, Update.fromDocument(doc), C_SETLISTS);
    
            logger.info("Saved setlist: " + setlist.getId() + " for artist: " + setlist.getArtistName());
        } catch (Exception e) {
            logger.warning("Error saving setlist: " + e.getMessage());       
        }
    }

    //     db.setlists.aggregate([
    //   { $match: { artistName: { $regex: "john", $options: "i" } } },
    //   {
    //     $addFields: {
    //       eventDateAsDate: {
    //         $dateFromString: {
    //           dateString: "$eventDate",
    //           format: "%d-%m-%Y"
    //         }
    //       }
    //     }
    //   },
    //   { $sort: { eventDateAsDate: -1 } },
    //   { $project: { eventDateAsDate: 0 } }
    // ])
    public List<Setlist> findByArtistName(String artistName) {
        MatchOperation matchByName = Aggregation.match(
         Criteria.where("artistName").regex(artistName, "i"));

        AddFieldsOperation addEventDateAsDate = Aggregation.addFields()
            .addFieldWithValue("eventDateAsDate", 
                DateFromString.fromStringOf("$eventDate").withFormat("%d-%m-%Y"))
            .build();
        
        SortOperation sortByDate = Aggregation.sort(Sort.Direction.DESC, "eventDateAsDate");

        ProjectionOperation excludeEventDateAsDateAndId = Aggregation.project()
            .andExclude("eventDateAsDate","_id");

        Aggregation pipeline = Aggregation.newAggregation(matchByName, addEventDateAsDate, sortByDate, excludeEventDateAsDateAndId);

        List<Document> docs = template.aggregate(pipeline, C_SETLISTS, Document.class).getMappedResults();

        return docs.stream()
            .map(this::docToSetlist)
            .toList();
    }

    public List<Setlist> getAllSetlists() {
        return template.findAll(Document.class, C_SETLISTS)
            .stream()
            .map(this::docToSetlist)
            .toList();
    }

    private Setlist docToSetlist(Document doc) {
        Setlist setlist = new Setlist();
        setlist.setId(doc.getString("id"));
        setlist.setArtistName(doc.getString("artistName"));
        setlist.setVenueName(doc.getString("venueName"));
        setlist.setCityName(doc.getString("cityName"));
        setlist.setCountryName(doc.getString("countryName"));
        setlist.setTourName(doc.getString("tourName"));
        setlist.setEventDate(doc.getString("eventDate"));

        // Parse sets and songs recursively
        List<Document> setDocs = doc.getList("sets", Document.class);
        if (setDocs != null && !setDocs.isEmpty()) {
            List<SetlistSet> sets = new ArrayList<>();
            for (Document setDoc : setDocs) {
                sets.add(docToSetlistSet(setDoc));
            }
            setlist.setSets(sets);
        }
        return setlist;
    }

    private SetlistSet docToSetlistSet(Document setDoc) {
        SetlistSet set = new SetlistSet();
        set.setName(setDoc.getString("name"));

        // Check for songs
        List<Document> songDocs = setDoc.getList("songs", Document.class);
        if (songDocs != null && !songDocs.isEmpty()) {
            List<SetlistSong> songs = new ArrayList<>();
            for (Document songDoc : songDocs) {
                songs.add(docToSetlistSong(songDoc));
            }
            set.setSongs(songs);
        }

        return set;
    }

    private SetlistSong docToSetlistSong(Document songDoc) {
        SetlistSong song = new SetlistSong();
        song.setName(songDoc.getString("name"));
        song.setInfo(songDoc.getString("info"));

        // Handle tape as a Boolean, with null check
        Boolean tape = songDoc.getBoolean("tape");
        song.setTape(tape != null ? tape : false);

        return song;
    }

}