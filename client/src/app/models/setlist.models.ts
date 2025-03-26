export interface SetlisetSong {
    name: string;
    info?: string;
    tape: boolean;
}

export interface SetlistSet {
    name: string;
    songs: SetlisetSong[];
}

export interface Setlist {
    id: string;
    artistName: string;
    venueName: string;
    cityName: string;
    countryName: string;
    tourName?: string;
    eventDate: string;
    sets: SetlistSet[];
}

export interface SetlistSlice {
    setlists: Setlist[];
    selectedArtist: string | null;
    loading: boolean;
    error: string | null;
}