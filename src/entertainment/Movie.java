package entertainment;

import java.util.ArrayList;

public class Movie extends Video {
    private final int duration;
    private final ArrayList<Double> ratings;
    private int views;

    public Movie(final String title, final ArrayList<String> cast,
                 final ArrayList<String> genres, final int year,
                 final int duration) {
        super(title, year, cast, genres);
        this.duration = duration;
        this.ratings = new ArrayList<>();
        this.views = 0;
    }

    public int getDuration() {
        return duration;
    }

    public ArrayList<Double> getRatings() {
        return ratings;
    }

    public int getViews() {
        return views;
    }
}
