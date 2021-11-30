package action;

import actor.Actor;
import actor.ActorsAwards;
import common.Constants;
import entertainment.Video;
import repository.Repository;
import user.User;
import utils.Utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Query extends Action {
    private final String objectType;
    private int number;
    private final List<List<String>> filters;
    private final String sortType;
    private final String criteria;

    public Query(int actionId, String objectType, int number, List<List<String>> filters,
                 String sortType, String criteria) {
        super(actionId);
        this.objectType = objectType;
        this.number = number;
        this.filters = filters;
        this.sortType = sortType;
        this.criteria = criteria;
    }

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
            default -> {return null;}
        }
    }

    /**
     * Returns the first N actors sorted in the specified order
     * after their average rating.
     */
    private String averageActor() {
        Repository repo = Repository.getInstance();
        List<Actor> actors = retrieveTargetObjects(objectType).stream().map(x -> (Actor) x).collect(Collectors.toList());

        Map<Actor, Double> averageRating = actors.stream().collect(Collectors.toMap(x -> x, x -> {
            /**
             * Get only the videos that exists in the database,
             * and then compute the sum of their ratings
             */
            List<Double> validRatings = x.getFilmography().stream().map(y -> (repo.findVideo(y) != null) ?
                            repo.findVideo(y).getAverageRating() : 0.0).filter(y -> Double.compare(y, 0.0) != 0)
                            .collect(Collectors.toList());

            return (validRatings.size() != 0) ? validRatings.stream().reduce(0.0, Double::sum) / validRatings.size() : 0.0;
        }));

        /**
         * Get only the actors that have a rating grater than 0.
         */
        Map<Actor, Double> validActors = averageRating.keySet().stream().filter(x -> Double.compare(averageRating.get(x), 0.0) > 0)
                                            .collect(Collectors.toMap(x -> x, x -> averageRating.get(x)));
        List<Actor> sorted = CustomSort.sortActors(new ArrayList<>(validActors.keySet()), validActors, sortType);

        return "Query result: " + sorted.subList(0, Math.min(sorted.size(), number))
                                    .stream().map(Actor::getName).collect(Collectors.toList());
    }

    /**
     * Returns the first N actors that won every requested award,
     * sorted in the specified order after their total number of awards.
     */
    private String awardsActor() {
        List<Actor> actors = retrieveTargetObjects(objectType).stream().map(x -> (Actor) x).collect(Collectors.toList());
        List<Actor> validActors = CustomFilter.filterActors(actors, filters);

        validActors = CustomSort.sortActors(validActors, validActors.stream().collect(Collectors.toMap(x -> x,
                        x -> (double) x.getAwards().keySet().stream().map(y -> x.getAwards().get(y))
                        .reduce(0, (a, b) -> a + b))), sortType);


        return "Query result: " + validActors.subList(0, Math.min(validActors.size(), number))
                                .stream().map(Actor::getName).collect(Collectors.toList());
    }

    /**
     * Returns the first N actors which has a description that contains the given words,
     * in the exact same order as in the database (aka repository).
     */
    private String keywordsActor() {
        List<Actor> actors = retrieveTargetObjects(objectType).stream().map(x -> (Actor)x).collect(Collectors.toList());
        List<Actor> validActors = CustomFilter.filterActors(actors, filters);

        /**
         * Sorting the actors alphabetically by their name can be simulated by using a hashmap that
         * has the same keys for every actor, so the second sorting criteria for precedent comparators
         * will become the only criteria, since all keys has the same value.
         */
        validActors = CustomSort.sortActors(validActors, validActors.stream()
                                                .collect(Collectors.toMap(x -> x, x -> 0.0)), sortType);

        return "Query result: " + validActors.subList(0, Math.min(validActors.size(), number))
                                    .stream().map(Actor::getName).collect(Collectors.toList());
    }

    /**
     * Returns the first N videos sorted by their average rating.
     */
    private String ratingsVideos() {
        List<Video> videos = retrieveTargetObjects(objectType).stream().map(x -> (Video)x).collect(Collectors.toList());

        /**
         * Only the videos that have an average rating greater than zero are valid.
         */
        List<Video> validVideos = videos.stream().filter(x -> Double.compare(x.getAverageRating(), 0.0) != 0)
                                        .collect(Collectors.toList());
        validVideos = CustomFilter.filterVideos(validVideos, filters);
        validVideos = CustomSort.sortVideos(validVideos, validVideos.stream()
                        .collect(Collectors.toMap(x -> x, x -> x.getAverageRating())), sortType);

        return "Query result: " + validVideos.subList(0, Math.min(validVideos.size(), number))
                                    .stream().map(Video::getTitle).collect(Collectors.toList());
    }

    /**
     * Returns the first N videos sorted by their popularity.
     */
    private String favoritesVideos() {
        List<Video> videos = retrieveTargetObjects(objectType).stream()
                                .map(x -> (Video) x).collect(Collectors.toList());

        /**
         * Get the videos that complies to the specified filters, and
         * only the ones that appear in at least one user's favorites
         * list; sort the new list of videos after the number of
         * favorites lists apparitions.
         */
        List<Video> validVideos = CustomFilter.filterVideos(videos, filters);
        validVideos = validVideos.stream().filter(x -> (Utils.favoriteApparitions(x.getTitle()) != 0))
                        .collect(Collectors.toList());
        validVideos = CustomSort.sortVideos(validVideos, validVideos.stream().collect(Collectors
                    .toMap(x -> x, x -> (double) Utils.favoriteApparitions(x.getTitle()))), sortType);

        return "Query result: " + validVideos.subList(0, Math.min(validVideos.size(), number))
                                    .stream().map(Video::getTitle).collect(Collectors.toList());
    }

    /**
     * Returns the first N videos sorted by their length.
     */
    private String longestVideos() {
        List<Video> videos = retrieveTargetObjects(objectType).stream()
                                .map(x -> (Video)x).collect(Collectors.toList());;

        List<Video> validVideos = CustomFilter.filterVideos(videos, filters);
        validVideos = CustomSort.sortVideos(validVideos, validVideos.stream().collect(Collectors
                     .toMap(x -> x, x -> (double) x.getDuration())), sortType);

        return "Query result: " + validVideos.subList(0, Math.min(validVideos.size(), number))
                .stream().map(Video::getTitle).collect(Collectors.toList());
    }

    /**
     * Returns the first N videos sorted by the number of views.
     */
    private String mostViewedVideos() {
        List<Video> videos = retrieveTargetObjects(objectType).stream()
                                .map(x -> (Video) x).collect(Collectors.toList());

        /**
         * Only the videos viewed at least once are valid.
         */
        List<Video> validVideos = CustomFilter.filterVideos(videos, filters);
        validVideos = validVideos.stream().filter(x -> (Utils.viewsOfAVideo(x.getTitle()) > 0))
                        .collect(Collectors.toList());

        validVideos = CustomSort.sortVideos(validVideos, validVideos.stream()
                        .collect(Collectors.toMap(x -> x,
                                x -> (double) Utils.viewsOfAVideo(x.getTitle()))), sortType);

        return "Query result: " + validVideos.subList(0, Math.min(validVideos.size(), number))
                                    .stream().map(Video::getTitle).collect(Collectors.toList());
    }

    /**
     * Returns the most active users.
     * @return
     */
    private String ratingsUsers() {
        List<User> users = retrieveTargetObjects(objectType).stream()
                            .map(x -> (User) x).collect(Collectors.toList());

        /**
         * Every valid user should've gave at least a review.
         */
        users = users.stream().filter(x -> Utils.ratingsOfAUser(x) >= 1).collect(Collectors.toList());
        users = CustomSort.sortUsers(users, users.stream().collect(Collectors.toMap(x -> x,
                x -> (double) Utils.ratingsOfAUser(x))), sortType);

        return "Query result: " + users.subList(0, Math.min(users.size(), number)).stream()
                                    .map(User::getUsername).collect(Collectors.toList());
    }

    /**
     * Auxiliary function that retrieves a list of the
     * specified objects from the database.
     *
     * @param objectType type of objects gathered
     *                   from database (i.e.: "actors", "users", ...)
     */
    private List<Object> retrieveTargetObjects(String objectType) {
        Repository repo = Repository.getInstance();

        return switch (objectType) {
            case Constants.ACTORS -> new ArrayList<>(repo.getActors());
            case Constants.USERS -> new ArrayList<>(repo.getUsers());
            case Constants.MOVIES -> new ArrayList<>(repo.getMovies());
            case Constants.SHOWS -> new ArrayList<>(repo.getSerials());
            default -> Stream.concat(repo.getMovies().stream(), repo.getSerials().stream()).collect(Collectors.toList());
        };
    }
}
