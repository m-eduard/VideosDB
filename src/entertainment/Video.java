package entertainment;

import java.util.ArrayList;

public abstract class Video {
    private final String title;
    private final int year;
    private final ArrayList<String> cast;
    private final ArrayList<String> genres;

    public Video(final String title, final int year,
                 final ArrayList<String> cast, final ArrayList<String> genres) {
        this.title = title;
        this.year = year;
        this.cast = cast;
        this.genres = genres;
    }

    /**
     * @return average rating for a video
     * (average based on the sum of average positive ratings
     * of seasons, if the video is a serial, average rating,
     * for movies)
     */
    public abstract double getAverageRating();

    /**
     * @return duration of a video
     * (sum of seasons' length if the video is a serial,
     *  length, if the video is a movie)
     */
    public abstract int getDuration();

    public final String getTitle() {
        return title;
    }

    public final int getYear() {
        return year;
    }

    public final ArrayList<String> getCast() {
        return cast;
    }

    public final ArrayList<String> getGenres() {
        return genres;
    }
}
