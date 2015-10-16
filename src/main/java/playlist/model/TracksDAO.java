package playlist.model;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class TracksDAO extends CassandraData {

	// Hard Coded Genres for now

	private final UUID track_id;
	private final String artist;
	private final String track;
	private final String genre;
	private final String music_file;
	private final int track_length_in_seconds;

	// step2
	private Boolean starred;

	/**
	 * Constructor to create a TracksDAO object when given a single Cassandra
	 * Row object
	 *
	 * @param row
	 *            - a single Cassandra Java Driver Row
	 *
	 */
	private TracksDAO(Row row) {
		track_id = row.getUUID("track_id");
		artist = row.getString("artist");
		track = row.getString("track");
		genre = row.getString("genre");
		music_file = row.getString("music_file");
		track_length_in_seconds = row.getInt("track_length_in_seconds");

		// step2
		try {
			setStarred(row.getBool("starred"));
		} catch (Exception e) {
			// If the field doesn't exist or is null we set it
			// to false
			setStarred(false);
		}
	}

	/**
	 * Constructor to create a TracksDAO object when given attribute data.
	 *
	 * @param artists
	 *            - The artist name
	 * @param track
	 *            - The track name
	 * @param genre
	 *            - The musical genre of the track
	 * @param music_file
	 *            - The digital music file name for the track
	 * @param track_length_in_seconds
	 *            - The lenght of the track in seconds
	 *
	 */
	public TracksDAO(String artist, String track, String genre,
			String music_file, int track_length_in_seconds) {
		this.track_id = UUID.randomUUID(); // We can generate the new UUID right
											// here in the constructor
		this.artist = artist;
		this.track = track;
		this.genre = genre;
		this.music_file = music_file;
		this.track_length_in_seconds = track_length_in_seconds;
		this.setStarred(false);
	}

	// step2
	public void star() {

		PreparedStatement preparedStatement = getSession()
				.prepare(
						"UPDATE track_by_artist  USING TTL 60 SET starred = true where artist = ? and track = ? and track_id = ?");
		BoundStatement boundStatement = preparedStatement.bind(artist, track,
				track_id);
		getSession().execute(boundStatement);

		preparedStatement = getSession()
				.prepare(
						"UPDATE track_by_genre  USING TTL 60 SET starred = true where genre = ? and artist = ? and track = ? and track_id = ?");
		boundStatement = preparedStatement.bind(genre, artist, track, track_id);
		getSession().execute(boundStatement);

	}

	// step2
	public static TracksDAO getTrackById(UUID track_id) {
		PreparedStatement preparedStatement = getSession().prepare(
				"SELECT * FROM track_by_id WHERE track_id = ?");
		BoundStatement boundStatement = preparedStatement.bind(track_id);
		ResultSet resultSet = getSession().execute(boundStatement);

		// Return null if there is no track found

		if (resultSet.isExhausted()) {
			return null;
		}

		return new TracksDAO(resultSet.one());
	}

	// Static finder method

	public static List<TracksDAO> listSongsByArtist(String artist) {

		String queryText = "SELECT * FROM track_by_artist WHERE artist = ?";
		PreparedStatement preparedStatement = getSession().prepare(queryText);
		BoundStatement boundStatement = preparedStatement.bind(artist);
		ResultSet results = getSession().execute(boundStatement);

		List<TracksDAO> tracks = new ArrayList<>();

		for (Row row : results) {
			tracks.add(new TracksDAO(row));
		}

		return tracks;
	}

	public static List<TracksDAO> listSongsByGenre(String genre) {

		String queryText = "SELECT * FROM track_by_genre WHERE genre = ?";
		PreparedStatement preparedStatement = getSession().prepare(queryText);
		BoundStatement boundStatement = preparedStatement.bind(genre);
		ResultSet results = getSession().execute(boundStatement);

		List<TracksDAO> tracks = new ArrayList<>();

		for (Row row : results) {
			tracks.add(new TracksDAO(row));
		}

		return tracks;
	}

	/**
	 * Add this track to the database
	 */

	public void add() {

		// Compute the first letter of the artists name for the
		// artists_by_first_letter table
		String artist_first_letter = this.artist.substring(0, 1).toUpperCase();

		// insert into artists_by_first_letter
		PreparedStatement preparedStatement = getSession()
				.prepare(
						"INSERT INTO artists_by_first_letter (first_letter, artist) VALUES (?, ?)");
		BoundStatement boundStatement = preparedStatement.bind(
				artist_first_letter, this.artist);
		getSession().execute(boundStatement);

		// insert into track_by_artist
		preparedStatement = getSession()
				.prepare(
						"INSERT INTO track_by_artist (genre, track_id, artist, track, track_length_in_seconds) VALUES (?, ?, ?, ?, ?)");
		boundStatement = preparedStatement.bind(this.genre, this.track_id,
				this.artist, this.track, this.track_length_in_seconds);
		getSession().execute(boundStatement);

		// insert into track_by_genre
		preparedStatement = getSession()
				.prepare(
						"INSERT INTO track_by_genre (genre, track_id, artist, track, track_length_in_seconds) VALUES (?, ?, ?, ?, ?)");
		boundStatement = preparedStatement.bind(this.genre, this.track_id,
				this.artist, this.track, this.track_length_in_seconds);
		getSession().execute(boundStatement);
		
		//step2
		preparedStatement = getSession().prepare("INSERT INTO track_by_id (genre, track_id, artist, track, track_length_in_seconds) VALUES (?, ?, ?, ?, ?)");
		boundStatement = preparedStatement.bind(this.genre, this.track_id, this.artist, this.track, this.track_length_in_seconds);
		getSession().execute(boundStatement);

	}

	public UUID getTrack_id() {
		return track_id;
	}

	public String getArtist() {
		return artist;
	}

	public String getTrack() {
		return track;
	}

	public String getGenre() {
		return genre;
	}

	public String getMusic_file() {
		return music_file;
	}

	public int getTrack_length_in_seconds() {
		return track_length_in_seconds;
	}

	public Boolean getStarred() {
		return starred;
	}

	public void setStarred(Boolean starred) {
		this.starred = starred;
	}
}
