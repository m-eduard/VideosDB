package entertainment;

import java.util.ArrayList;

public class Movie extends Video {
    private final int duration;
    private final ArrayList<Double> ratings;

    public Movie(final String title, final ArrayList<String> cast,
                 final ArrayList<String> genres, final int year,
                 final int duration) {
        super(title, year, cast, genres);
        this.duration = duration;
        this.ratings = new ArrayList<>();
    }

    public ArrayList<Double> getRatings() {
        return ratings;
    }

    @Override
    public double getAverageRating() {
        return (ratings.size() == 0) ? 0.0 : ratings.stream().reduce(0.0, (x, y) -> x + y) / ratings.size();
    }

    @Override
    public int getDuration() {
        return duration;
    }
}
