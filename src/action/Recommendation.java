package action;

import common.Constants;
import entertainment.Video;
import repository.Repository;
import user.User;
import utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Recommendation extends Action {
    private final String username;
    private final String type;
    private final String genre;

    public Recommendation(final int actionId, final String username,
                          final String type, final String genre) {
        super(actionId);
        this.username = username;
        this.type = type;
        this.genre = genre;
    }

    /**
     * Generate a recommendation whose type is specified by the
     * class fields, based on the info stored in database.
     * @return message generated after trying to create a recommendation
     */
    @Override
    public String apply() {
        return switch (type) {
            case Constants.STANDARD -> standard();
            case Constants.BEST_UNSEEN -> bestUnseen();
            case Constants.POPULAR_GENRE -> popular();
            case Constants.FAVORITE -> favorite();
            case Constants.SEARCH -> search();
            default -> null;
        };
    }

    /**
     * Returns the first video which haven't been seen by the user.
     */
    private String standard() {
        Repository repo = Repository.getInstance();
        User user = repo.findUser(username);

        if (user == null) {
            return "StandardRecommendation cannot be applied!";
        }

        List<Video> videos = Stream.concat(repo.getMovies().stream(), repo.getSerials().stream())
                                .collect(Collectors.toList());
        Video recommendation = videos.stream()
                                .filter(x -> !user.getHistory().containsKey(x.getTitle()))
                                .findFirst().orElse(null);

        return (recommendation == null) ? "StandardRecommendation cannot be applied!"
                    : "StandardRecommendation result: " + recommendation.getTitle();
    }

    /**
     * Returns the best rated unseen video.
     */
    private String bestUnseen() {
        Repository repo = Repository.getInstance();
        User user = repo.findUser(username);

        if (user == null) {
            return "BestRatedUnseenRecommendation cannot be applied!";
        }

        /**
         * Filter only the videos that wasn't watched.
         */
        List<Video> videos = Stream.concat(repo.getMovies().stream(), repo.getSerials().stream())
                                .collect(Collectors.toList());
        videos = videos.stream().filter(x -> !user.getHistory().containsKey(x.getTitle()))
                                .collect(Collectors.toList());
        /**
         * Sort the unseen videos by their rating, using
         * the database order if ratings are equal.
         */
        videos = CustomSort.sortVideos(videos, videos.stream()
                    .collect(Collectors.toMap(x -> x, Video::getAverageRating)), "db_desc");

        Video targetVideo = videos.stream().findFirst().orElse(null);

        return (targetVideo == null) ? "BestRatedUnseenRecommendation cannot be applied!"
                    : "BestRatedUnseenRecommendation result: " + targetVideo.getTitle();
    }

    /**
     * Returns the first unseen video from the most popular genre.
     */
    private String popular() {
        Repository repo = Repository.getInstance();
        User user = repo.findUser(username);

        if (user == null || !user.getSubscriptionType().equals(Constants.PREMIUM)) {
            return "PopularRecommendation cannot be applied!";
        }

        List<Video> videos = Stream.concat(repo.getMovies().stream(), repo.getSerials().stream())
                                .collect(Collectors.toList());

        /**
         * Get the unique genres.
         */
        List<String> genres = new ArrayList<>();
        for (Video video : videos) {
            for (String currentGenre : video.getGenres()) {
                if (!genres.contains(currentGenre)) {
                    genres.add(currentGenre);
                }
            }
        }
        /**
         * Every genre bucket will have a list of videos
         * sorted by the position in the database (aka unsorted).
         */
        List<List<Video>> genreBuckets = new ArrayList<>();
        for (String currentGenre : genres) {
            List<Video> filteredVideos = videos.stream()
                                            .filter(x -> x.getGenres().contains(currentGenre))
                                            .collect(Collectors.toList());
            genreBuckets.add(filteredVideos);
        }
        /**
         * Sort the genres by their popularity, in descending order.
         */
        genreBuckets.sort(new Comparator<List<Video>>() {
            @Override
            public int compare(final List<Video> o1, final List<Video> o2) {
                return Integer.compare(o1.stream()
                        .map(x -> Utils.viewsOfAVideo(x.getTitle())).reduce(0, Integer::sum),
                        o2.stream().map(x -> Utils.viewsOfAVideo(x.getTitle()))
                        .reduce(0, Integer::sum)) * (-1);
            }
        });

        for (List<Video> bucket : genreBuckets) {
            Video target = bucket.stream().filter(x -> !user.getHistory().containsKey(x.getTitle()))
                                .findFirst().orElse(null);
            if (target != null) {
                return "PopularRecommendation result: " + target.getTitle();
            }
        }

        return "PopularRecommendation cannot be applied!";
    }

    /**
     * Finds the most popular video among favorites videos,
     * which was not seen by the user.
     */
    private String favorite() {
        Repository repo = Repository.getInstance();
        User user = repo.findUser(username);

        if (user == null || !user.getSubscriptionType().equals(Constants.PREMIUM)) {
            return "FavoriteRecommendation cannot be applied!";
        }

        List<Video> videos = Stream.concat(repo.getMovies().stream(), repo.getSerials().stream())
                                .collect(Collectors.toList());
        /**
         * Remove the videos which nobody added to favorites.
         */
        List<Video> validVideos = videos.stream()
                                    .filter(x -> (Utils.favoriteApparitions(x.getTitle()) != 0))
                                    .collect(Collectors.toList());
        /**
         * Sort by the number of apparitions in favorites lists.
         */
        validVideos = CustomSort.sortVideos(validVideos, validVideos.stream()
                        .collect(Collectors.toMap(x -> x,
                        x -> (double) Utils.favoriteApparitions(x.getTitle()))), "db_desc");

        Video targetVideo = validVideos.stream()
                            .filter(x -> !user.getHistory().containsKey(x.getTitle()))
                            .findFirst().orElse(null);

        return (targetVideo == null) ? "FavoriteRecommendation cannot be applied!"
                                    : "FavoriteRecommendation result: " + targetVideo.getTitle();
    }

    /**
     * Get all the unwatched videos from a specific genre, sorted
     * by their average rating.
     */
    private String search() {
        Repository repo = Repository.getInstance();
        User user = repo.findUser(username);

        if (user == null || !user.getSubscriptionType().equals(Constants.PREMIUM)) {
            return "SearchRecommendation cannot be applied!";
        }

        List<Video> videos = Stream.concat(repo.getMovies().stream(), repo.getSerials().stream())
                .collect(Collectors.toList());
        List<Video> validVideos = videos.stream().filter(x -> x.getGenres().contains(genre))
                                    .collect(Collectors.toList());
        /**
         * Remove the seen videos
         */
        validVideos = validVideos.stream().filter(x -> !user.getHistory()
                        .containsKey(x.getTitle())).collect(Collectors.toList());
        /**
         * Sort the remaining videos
         */
        validVideos = CustomSort.sortVideos(validVideos, validVideos.stream()
                        .collect(Collectors.toMap(x -> x, Video::getAverageRating)), Constants.ASC);

        return (validVideos.size() == 0) ? "SearchRecommendation cannot be applied!"
                    : "SearchRecommendation result: " + validVideos.stream().map(Video::getTitle)
                    .collect(Collectors.toList());
    }
}
