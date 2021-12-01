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

public class Recommendation extends Action {
    private final String username;
    private final String type;
    private final String genre;

    public Recommendation(final int actionId, final String username, final String type,
                          String genre) {
        super(actionId);
        this.username = username;
        this.type = type;
        this.genre = genre;
    }

    @Override
    public String apply() {
        return switch (type) {
            case Constants.STANDARD -> standardRecommendation();
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
    private String standardRecommendation() {
        Repository repo = Repository.getInstance();
        User user = repo.findUser(username);
        if (user == null) return "StandardRecommendation cannot be applied!";

        /**
         *  Find the first unwatched video for the specified user
         */
        List<Video> videos = Stream.concat(repo.getMovies().stream(), repo.getSerials().stream())
                .collect(Collectors.toList());
        Video recommendation = videos.stream().filter(x -> !user.getHistory().containsKey(x.getTitle()))
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
        if (user == null) return "BestRatedUnseenRecommendation cannot be applied!";

        /**
         * Sort the unseen videos by their rating.
         */
        List<Video> videos = Stream.concat(repo.getMovies().stream(), repo.getSerials().stream())
                                .collect(Collectors.toList());
        videos = videos.stream().filter(x -> !user.getHistory().containsKey(x.getTitle()))
                                .collect(Collectors.toList());
        videos = CustomSort.sortVideos(videos, videos.stream()
                    .collect(Collectors.toMap(x -> x, x -> x.getAverageRating())), "db_desc");

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
            for (String genre : video.getGenres()) {
                if (!genres.contains(genre)) {
                    genres.add(genre);
                }
            }
        }

        /**
         * In every genre bucket add a list of sorted videos by
         * their number of views and position in the database.
         */
        List<List<Video>> genreBuckets = new ArrayList<>();
        for (String genre : genres) {
            List<Video> filteredVideos = videos.stream().filter(x -> x.getGenres().contains(genre))
                                            .collect(Collectors.toList());

            genreBuckets.add(filteredVideos);
        }

        /**
         * Sort the genres by their popularity
         */
        genreBuckets.sort(new Comparator<List<Video>>() {
            @Override
            public int compare(List<Video> o1, List<Video> o2) {
                return (-1) * Integer.compare(o1.stream().map(x -> Utils.viewsOfAVideo(x.getTitle()))
                                .reduce(0, Integer::sum), o2.stream().map(x ->
                                Utils.viewsOfAVideo(x.getTitle())).reduce(0, Integer::sum));
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

    private String favorite() {
        Repository repo = Repository.getInstance();
        User user = repo.findUser(username);

        if (user == null || !user.getSubscriptionType().equals(Constants.PREMIUM))
            return "FavoriteRecommendation cannot be applied!";

        List<Video> videos = Stream.concat(repo.getMovies().stream(), repo.getSerials().stream())
                                .collect(Collectors.toList());

        List<Video> validVideos = videos.stream().filter(x -> (Utils.favoriteApparitions(x.getTitle()) != 0))
                                    .collect(Collectors.toList());
        validVideos = CustomSort.sortVideos(validVideos, validVideos.stream().collect(Collectors
                .toMap(x -> x, x -> (double) Utils.favoriteApparitions(x.getTitle()))), "db_desc");

        Video targetVideo = validVideos.stream().filter(x -> !user.getHistory().containsKey(x.getTitle()))
                            .findFirst().orElse(null);

        return (targetVideo == null) ? "FavoriteRecommendation cannot be applied!"
                                    : "FavoriteRecommendation result: " + targetVideo.getTitle();
    }

    private String search() {
        Repository repo = Repository.getInstance();
        User user = repo.findUser(username);

        if (user == null || !user.getSubscriptionType().equals(Constants.PREMIUM)) return "SearchRecommendation cannot be applied!";

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
                        .collect(Collectors.toMap(x -> x, x -> x.getAverageRating())), Constants.ASC);

        return (validVideos.size() == 0) ? "SearchRecommendation cannot be applied!"
                    : "SearchRecommendation result: " + validVideos.stream().map(x -> x.getTitle())
                    .collect(Collectors.toList());
    }
}
