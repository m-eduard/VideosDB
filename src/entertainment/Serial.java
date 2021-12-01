package entertainment;

import java.util.ArrayList;

public final class Serial extends Video {
    private final int numberOfSeasons;
    private final ArrayList<Season> seasons;

    public Serial(final String title, final ArrayList<String> cast,
                  final ArrayList<String> genres,
                  final int numberOfSeasons, final ArrayList<Season> seasons,
                  final int year) {
        super(title, year, cast, genres);
        this.numberOfSeasons = numberOfSeasons;
        this.seasons = seasons;
    }

    public int getNumberSeason() {
        return numberOfSeasons;
    }

    public ArrayList<Season> getSeasons() {
        return seasons;
    }

    /**
     * Compute the total average rating of a serial based
     * on the ratings of its individual seasons.
     */
    @Override
    public double getAverageRating() {
        return seasons.stream().map(x -> x.averageRating())
                .reduce(0.0, (x, y) -> x + y) / seasons.size();
    }

    /**
     * Returns total duration of a serial.
     */
    @Override
    public int getDuration() {
        return seasons.stream().map(x -> x.getDuration())
                .reduce(0, (x, y) -> x + y);
    }
}
