package entertainment;

import java.util.ArrayList;
import java.util.List;

public class Serial extends Video {
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

    @Override
    public double getAverageRating() {
        List<Double> ratings = new ArrayList<>();
        seasons.forEach(x -> ratings.add(x.averageRating()));

        return ratings.stream().reduce(0.0, (x, y) -> x + y) / ratings.size();
    }

    @Override
    public int getDuration() {
        return seasons.stream().map(x -> x.getDuration()).reduce(0, (x, y) -> x + y);
    }
}
