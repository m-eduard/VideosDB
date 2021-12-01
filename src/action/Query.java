package action;

import actor.Actor;
import common.Constants;
import entertainment.Video;
import repository.Repository;
import user.User;
import utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Query extends Action {
    private final String objectType;
    private int number;
    private final List<List<String>> filters;
    private final String sortType;
    private final String criteria;

    public Query(final int actionId, final String objectType, final int number,
                 final List<List<String>> filters, final String sortType, final String criteria) {
        super(actionId);
        this.objectType = objectType;
        this.number = number;
        this.filters = filters;
        this.sortType = sortType;
        this.criteria = criteria;
    }

    /**
     * Make a query on the database.
     * @return query result
     */
    @Override
    public String apply() {
        switch (objectType) {
            case Constants.ACTORS -> {
                return switch (criteria) {
                    case Constants.AVERAGE -> averageActor();
                    case Constants.AWARDS -> awardsActor();
                    case Constants.FILTER_DESCRIPTIONS -> keywordsActor();
                    default -> null;
                };
            }
            case Constants.MOVIES, Constants.SHOWS -> {
                return switch (criteria) {
                    case Constants.RATINGS -> ratingsVideos();
                    case Constants.FAVORITE -> favoritesVideos();
                    case Constants.LONGEST -> longestVideos();
                    case Constants.MOST_VIEWED -> mostViewedVideos();
                    default -> null;
                };
            }
            case Constants.USERS -> {
                return switch (criteria) {
                    case Constants.NUM_RATINGS -> ratingsUsers();
                    default -> null;
                };
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Finds the first N actors sorted in the specified order
     * after their average rating.
     * @return output message as String
     */
    private String averageActor() {
        Repository repo = Repository.getInstance();
        List<Actor> actors = retrieveTargetObjects(objectType).stream().map(x -> (Actor) x)
                                .collect(Collectors.toList());

        Map<Actor, Double> averageRating = actors.stream().collect(Collectors.toMap(x -> x, x -> {
            /**
             * Get only the videos that exist in the database,
             * with a positive rating and then compute the sum of their ratings.
             */
            List<Double> validRatings = x.getFilmography().stream().map(y ->
                                        (repo.findVideo(y) != null) ? repo.findVideo(y)
                                        .getAverageRating() : 0.0).filter(y -> Double
                                        .compare(y, 0.0) != 0).collect(Collectors.toList());

            return (validRatings.size() != 0) ? validRatings.stream().reduce(0.0, Double::sum)
                                                / validRatings.size() : 0.0;
        }));

        /**
         * Remove the key-value pairs where actors that have a rating equal to zero.
         */
        Map<Actor, Double> validRatings = averageRating.keySet().stream()
                                        .filter(x -> Double.compare(averageRating.get(x), 0.0) > 0)
                                        .collect(Collectors.toMap(x -> x, averageRating::get));
        List<Actor> sorted = CustomSort.sortActors(new ArrayList<>(validRatings.keySet()),
                                                    validRatings, sortType);

        return "Query result: " + sorted.subList(0, Math.min(sorted.size(), number))
                                    .stream().map(Actor::getName).collect(Collectors.toList());
    }

    /**
     * Finds the first N actors that won every requested award,
     * sorted in the specified order after their total number of awards.
     * @return output message as String
     */
    private String awardsActor() {
        List<Actor> actors = retrieveTargetObjects(objectType).stream().map(x -> (Actor) x)
                                .collect(Collectors.toList());
        /**
         * Remove the actors that don't have the awards specified by filters.
         */
        List<Actor> validActors = CustomFilter.filterActors(actors, filters);

        /**
         * As second parameter of method sortActors, a map from actors to the
         * total number of the awards won is used.
         */
        validActors = CustomSort.sortActors(validActors, validActors.stream()
                        .collect(Collectors.toMap(x -> x, x -> (double) x.getAwards().keySet()
                        .stream().map(y -> x.getAwards().get(y)).reduce(0, (a, b) -> a + b))),
                        sortType);


        return "Query result: " + validActors.subList(0, Math.min(validActors.size(), number))
                                .stream().map(Actor::getName).collect(Collectors.toList());
    }

    /**
     * Finds the first N actors which has a description that contains the given words,
     * as they appear in the Repository.
     * @return output message as String
     */
    private String keywordsActor() {
        List<Actor> actors = retrieveTargetObjects(objectType).stream().map(x -> (Actor) x)
                                .collect(Collectors.toList());
        /**
         * Remove the actors whose description doesn't contain the given keywords.
         */
        List<Actor> validActors = CustomFilter.filterActors(actors, filters);

        /**
         * Sorting the actors alphabetically by their name can be simulated with the classic sort
         * by a hashmap of properties as the first criteria, and alphabetically as second criteria
         * (CustomSort.sortActors(), with a sortType == Constants.ASC/DESC, which is provided by
         * the current instance with sortType), by using a hashmap that has the same keys for every
         * actor, so the second sorting criteria will become the only criteria, since all keys
         * has the same value.
         */
        validActors = CustomSort.sortActors(validActors, validActors.stream()
                                        .collect(Collectors.toMap(x -> x, x -> 0.0)), sortType);

        return "Query result: " + validActors.subList(0, Math.min(validActors.size(), number))
                                    .stream().map(Actor::getName).collect(Collectors.toList());
    }

    /**
     * Gets the first N videos sorted by their average rating.
     * @return output message as String
     */
    private String ratingsVideos() {
        List<Video> videos = retrieveTargetObjects(objectType).stream().map(x -> (Video) x)
                                .collect(Collectors.toList());

        /**
         * Only the videos that have an average rating greater than zero are valid.
         */
        List<Video> validVideos = videos.stream()
                                    .filter(x -> Double.compare(x.getAverageRating(), 0.0) != 0)
                                    .collect(Collectors.toList());
        /**
         * Filter the videos by the given restrictions provided in filters.
         */
        validVideos = CustomFilter.filterVideos(validVideos, filters);
        /**
         * Sort the videos by their average rating.
         */
        validVideos = CustomSort.sortVideos(validVideos, validVideos.stream()
                        .collect(Collectors.toMap(x -> x, Video::getAverageRating)), sortType);

        return "Query result: " + validVideos.subList(0, Math.min(validVideos.size(), number))
                                    .stream().map(Video::getTitle).collect(Collectors.toList());
    }

    /**
     * Searches the first N videos sorted by their popularity in favorites lists.
     * @return output message as String
     */
    private String favoritesVideos() {
        List<Video> videos = retrieveTargetObjects(objectType).stream()
                                .map(x -> (Video) x).collect(Collectors.toList());
        /**
         * Get the videos that complies to the specified filters, and only the
         * ones that appear in at least one user's favorites list; sort the
         * new list of videos by the number of favorites lists apparitions.
         */
        List<Video> validVideos = CustomFilter.filterVideos(videos, filters);
        validVideos = validVideos.stream()
                        .filter(x -> (Utils.favoriteApparitions(x.getTitle()) != 0))
                        .collect(Collectors.toList());
        validVideos = CustomSort.sortVideos(validVideos, validVideos.stream().collect(Collectors
                    .toMap(x -> x, x -> (double) Utils.favoriteApparitions(x.getTitle()))),
                    sortType);

        return "Query result: " + validVideos.subList(0, Math.min(validVideos.size(), number))
                                    .stream().map(Video::getTitle).collect(Collectors.toList());
    }

    /**
     * Finds the first N videos sorted by their length.
     * @return output message as String
     */
    private String longestVideos() {
        List<Video> videos = retrieveTargetObjects(objectType).stream()
                                .map(x -> (Video) x).collect(Collectors.toList());

        List<Video> validVideos = CustomFilter.filterVideos(videos, filters);
        /**
         * As the second parameter for sortVideos(), a map is created by
         * mapping the validVideos to their total length.
         */
        validVideos = CustomSort.sortVideos(validVideos, validVideos.stream().collect(Collectors
                     .toMap(x -> x, x -> (double) x.getDuration())), sortType);

        return "Query result: " + validVideos.subList(0, Math.min(validVideos.size(), number))
                .stream().map(Video::getTitle).collect(Collectors.toList());
    }

    /**
     * Finds the first N videos sorted by the number of views.
     * @return output message as String
     */
    private String mostViewedVideos() {
        List<Video> videos = retrieveTargetObjects(objectType).stream()
                                .map(x -> (Video) x).collect(Collectors.toList());

        List<Video> validVideos = CustomFilter.filterVideos(videos, filters);
        /**
         * Only the videos viewed at least once are valid.
         */
        validVideos = validVideos.stream().filter(x -> (Utils.viewsOfAVideo(x.getTitle()) > 0))
                        .collect(Collectors.toList());
        validVideos = CustomSort.sortVideos(validVideos, validVideos.stream()
                        .collect(Collectors.toMap(x -> x,
                                x -> (double) Utils.viewsOfAVideo(x.getTitle()))), sortType);

        return "Query result: " + validVideos.subList(0, Math.min(validVideos.size(), number))
                                    .stream().map(Video::getTitle).collect(Collectors.toList());
    }

    /**
     * Finds the most active users.
     * @return output message as String
     */
    private String ratingsUsers() {
        List<User> users = retrieveTargetObjects(objectType).stream().map(x -> (User) x)
                            .collect(Collectors.toList());

        /**
         * Every valid user should've gave at least a review.
         */
        users = users.stream().filter(x -> Utils.ratingsOfAUser(x) >= 1)
                .collect(Collectors.toList());
        users = CustomSort.sortUsers(users, users.stream().collect(Collectors.toMap(x -> x,
                x -> (double) Utils.ratingsOfAUser(x))), sortType);

        return "Query result: " + users.subList(0, Math.min(users.size(), number)).stream()
                                    .map(User::getUsername).collect(Collectors.toList());
    }

    /**
     * Auxiliary function that retrieves a list of the specified objects from the
     * database. (the elements of the returned list have a generic Object type, and
     * a downcast to their original type will be performed in the function that
     * called this method).
     * @param targetType type of objects gathered from database
     *                   (i.e.: "actors", "users", ...); in this case is
     *                   equal to objectType from the current instance
     * @return list of Objects
     */
    private List<Object> retrieveTargetObjects(final String targetType) {
        Repository repo = Repository.getInstance();

        return switch (targetType) {
            case Constants.ACTORS -> new ArrayList<>(repo.getActors());
            case Constants.USERS -> new ArrayList<>(repo.getUsers());
            case Constants.MOVIES -> new ArrayList<>(repo.getMovies());
            case Constants.SHOWS -> new ArrayList<>(repo.getSerials());
            default -> Stream.concat(repo.getMovies().stream(), repo.getSerials().stream())
                        .collect(Collectors.toList());
        };
    }
}
