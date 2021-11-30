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

    public Query(int actionId, String objectType, int number, List<List<String>> filters, String sortType, String criteria) {
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

    private String averageActor() {
        Repository repo = Repository.getInstance();
        List<Actor> actors = repo.getActors();

        // get the average for every actor in a hashmap,
        // then sort a newly created list of actors after the average
        HashMap<Actor, Double> averageRating = new HashMap<>();

        for (Actor actor : actors) {
            List<Video> videos = new ArrayList<>();
            for (String videoTitle : actor.getFilmography()) {
                if (repo.findVideo(videoTitle) != null) {
                    videos.add(repo.findVideo(videoTitle));
                }
            }

            List<Double> ratings = new ArrayList<>();
            for (Video video : videos) {
                ratings.add(video.getAverageRating());
            }

            double sum = 0.0;
            int nof_videos = 0;

            for (double rating : ratings) {
                if (Double.compare(rating, 0.0) != 0) {
                    nof_videos++;
                    sum += rating;
                }
            }

            if (nof_videos != 0) {
                averageRating.put(actor, sum / nof_videos);
            }
        }

        List<Actor> sorted = sortActors(new ArrayList<>(averageRating.keySet()), averageRating);

        return "Query result: " + sorted.stream().map(Actor::getName).collect(Collectors.toList());
    }

    private String awardsActor() {
        Repository repo = Repository.getInstance();
        List<Actor> actors = repo.getActors();

        // This map will contain every actor that has the specified awards
        HashMap<Actor, Double> totalAwards = new HashMap<>();

        // Get only the actors that have the specified awards
        List<Actor> targetActors = actors.stream().filter(x -> {
            boolean status = true;
            int counter = 0;

            if (filters.get(Constants.AWARDS_POS) != null) {
                for (ActorsAwards award : filters.get(Constants.AWARDS_POS).stream()
                        .map(Utils::stringToAwards).collect(Collectors.toList()))
                {
                    if (!x.getAwards().containsKey(award)) {
                        status = false;
                        break;
                    }

                    counter += x.getAwards().get(award);
                }
            }

            if (status) {
                totalAwards.put(x, (double)counter);
            }

            return status;
        }).collect(Collectors.toList());


        targetActors = sortActors(targetActors, totalAwards);


        // get the average for every actor in a hashmap,
        // then sort a newly created list of actors after the average
        return "Query result: " + targetActors.stream().map(Actor::getName).collect(Collectors.toList());
    }

    private String keywordsActor() {
        Repository repo = Repository.getInstance();
        List<Actor> actors = repo.getActors().stream().filter(x -> {
            boolean status = true;

            String[] descriptionTokens = x.getCareerDescription().split("\\W+");

            for (String keyword : filters.get(Constants.WORDS_POS)) {
                if (!Arrays.asList(descriptionTokens).contains(keyword)) {
                    status = false;
                    break;
                }
            }

            return status;
        }).collect(Collectors.toList());

        // Sorting the actors alphabetically by their name can be simulated by using a hashmap that
        // has the same keys for every actor, so the second sorting criteria for precedent comparators
        // will become the only criteria, since all keys has the same value.
        actors = sortActors(actors, actors.stream().collect(Collectors.toMap(x -> x, x -> 0.0)));

        return "Query result: " + actors.stream().map(Actor::getName).collect(Collectors.toList());
    }

    /**
     * Sort a given list of actors in specified order by their property, stored as value in
     * in the HashMap properties.
     * @param actors
     * @param properties
     * @return A list of sorted actors
     */
    private List<Actor> sortActors(List<Actor> actors, Map<Actor, Double> properties) {
        Comparator<Actor> ascComparator = new Comparator<>() {
            @Override
            public int compare(Actor o1, Actor o2) {
                if (properties.get(o1) > properties.get(o2)) {
                    return 1;
                } else if (properties.get(o1) < properties.get(o2)) {
                    return -1;
                } else {
                    return o1.getName().compareTo(o2.getName());
                }
            }
        };

        Comparator<Actor> descComparator = new Comparator<>() {
            @Override
            public int compare(Actor o1, Actor o2) {
                return ascComparator.compare(o1, o2) * (-1);
            }
        };

        switch (sortType) {
            case Constants.ASC -> {
                actors.sort(ascComparator);
            }
            case Constants.DESC -> {
                actors.sort(descComparator);
            }
        }

        return actors.subList(0, Math.min(actors.size(), number));
    }

    private String ratingsVideos() {
        Repository repo = Repository.getInstance();

        /* Videos that have a rating greater than 0 */
        List<Video> validVideos;

        switch (objectType) {
            case Constants.MOVIES -> {
                validVideos = repo.getMovies().stream().filter(x -> Double.compare(x.getAverageRating(), 0.0) != 0).collect(Collectors.toList());
            }
            case Constants.SHOWS -> {
                validVideos = repo.getSerials().stream().filter(x -> Double.compare(x.getAverageRating(), 0.0) != 0).collect(Collectors.toList());
            }
            default -> {
                validVideos = Stream.concat(repo.getMovies().stream(), repo.getSerials().stream())
                        .filter(x -> Double.compare(x.getAverageRating(), 0.0) != 0).collect(Collectors.toList());
            }
        }

        validVideos = filterVideos(validVideos);

        return "Query result: " + sortVideos(validVideos, validVideos.stream().collect(Collectors.toMap(x -> x, x -> x.getAverageRating()))).stream().map(Video::getTitle).collect(Collectors.toList());
    }

    /**
     * Find how many times a show / movie appears in the favorites list of users.
     */
    private int favoriteApparitions(String videoTitle) {
        Repository repo = Repository.getInstance();

        return (int) repo.getUsers().stream().filter(y -> y.getFavoriteMovies().contains(videoTitle)).count();
    }

    private String favoritesVideos() {
        Repository repo = Repository.getInstance();

        // Get the fav adds for every movie, then sort them
        List<Video> videos;
        switch (objectType) {
            case Constants.MOVIES -> {
                videos = new ArrayList<>(repo.getMovies());
            }
            case Constants.SHOWS -> {
                videos = new ArrayList<>(repo.getSerials());
            }
            default -> {
                videos = Stream.concat(repo.getMovies().stream(), repo.getSerials().stream()).collect(Collectors.toList());
            }
        }

        videos = filterVideos(videos);
        videos = videos.stream().filter(x -> (favoriteApparitions(x.getTitle()) != 0)).collect(Collectors.toList());

        return "Query result: " + sortVideos(videos, videos.stream().collect(Collectors.toMap(x -> x, x -> (double) favoriteApparitions(x.getTitle())))).stream().map(Video::getTitle).collect(Collectors.toList());
    }

    private String longestVideos() {
        Repository repo = Repository.getInstance();

        List<Video> videos;
        switch (objectType) {
            case Constants.MOVIES -> {
                videos = new ArrayList<>(repo.getMovies());
            }
            case Constants.SHOWS -> {
                videos = new ArrayList<>(repo.getSerials());
            }
            default -> {
                videos = Stream.concat(repo.getMovies().stream(), repo.getSerials().stream()).collect(Collectors.toList());
            }
        }

        videos = filterVideos(videos);

        return "Query result: " + sortVideos(videos, videos.stream().collect(Collectors.toMap(x -> x, x -> (double) x.getDuration()))).stream().map(Video::getTitle).collect(Collectors.toList());
    }

    private int viewsOfAVideo(String videoTitle) {
        Repository repo = Repository.getInstance();
        return repo.getUsers().stream().map(x -> x.getHistory().getOrDefault(videoTitle, 0)).reduce(0, Integer::sum);
    }

    private String mostViewedVideos() {
        Repository repo = Repository.getInstance();

        List<Video> videos;
        switch (objectType) {
            case Constants.MOVIES -> {
                videos = new ArrayList<>(repo.getMovies());
            }
            case Constants.SHOWS -> {
                videos = new ArrayList<>(repo.getSerials());
            }
            default -> {
                videos = Stream.concat(repo.getMovies().stream(), repo.getSerials().stream()).collect(Collectors.toList());
            }
        }

        videos = filterVideos(videos);
        videos = videos.stream().filter(x -> (viewsOfAVideo(x.getTitle()) > 0)).collect(Collectors.toList());

        List<Video> sortedVideos = sortVideos(videos, videos.stream().collect(Collectors.toMap(x -> x, x -> (double) viewsOfAVideo(x.getTitle()))));

        return "Query result: " + sortedVideos.stream().map(Video::getTitle).collect(Collectors.toList());
    }

    private List<Video> filterVideos(List<Video> videos) {
        videos = videos.stream().filter(x -> {
            boolean status = true;

            if (filters.get(Constants.YEAR_POS) != null && filters.get(Constants.YEAR_POS).get(0) != null) {
                int year = Integer.parseInt(filters.get(Constants.YEAR_POS).get(0));
                status = status && (year == x.getYear());
            }
            if (filters.get(Constants.GENRE_POS) != null && filters.get(Constants.GENRE_POS).get(0) != null) {
                String genre = filters.get(Constants.GENRE_POS).get(0);
                status = status && (x.getGenres().contains(genre));
            }

            return status;
        }).collect(Collectors.toList());

        return videos;
    }

    private List<Video> sortVideos(List<Video> videos, Map<Video, Double> properties) {
        Comparator<Video> ascComparator = new Comparator<>() {
            @Override
            public int compare(Video o1, Video o2) {
                if (properties.get(o1) > properties.get(o2)) {
                    return 1;
                } else if (properties.get(o1) < properties.get(o2)) {
                    return -1;
                } else {
                    return o1.getTitle().compareTo(o2.getTitle());
                }
            }
        };

        Comparator<Video> descComparator = new Comparator<>() {
            @Override
            public int compare(Video o1, Video o2) {
                return ascComparator.compare(o1, o2) * (-1);
            }
        };

        switch (sortType) {
            case Constants.ASC -> {
                videos.sort(ascComparator);
            }
            case Constants.DESC -> {
                videos.sort(descComparator);
            }
        }

        return videos.subList(0, Math.min(videos.size(), number));
    }

    private String ratingsUsers() {
        Repository repo = Repository.getInstance();

        // Every valid user should've gave at least a review.
        List<User> users = repo.getUsers().stream().filter(x -> ratingsOfAUser(x) >= 1).collect(Collectors.toList());

        return "Query result: " + sortUsers(users, users.stream().collect(Collectors.toMap(x -> x, x -> (double) ratingsOfAUser(x)))).stream().map(User::getUsername).collect(Collectors.toList());
    }

    private int ratingsOfAUser(User user) {
        return user.getRated().keySet().stream().map(x -> user.getRated().get(x).size()).reduce(0, Integer::sum);
    }

    private List<User> sortUsers(List<User> users, Map<User, Double> properties) {
        Comparator<User> ascComparator = new Comparator<>() {
            @Override
            public int compare(User o1, User o2) {
                if (properties.get(o1) > properties.get(o2)) {
                    return 1;
                } else if (properties.get(o1) < properties.get(o2)) {
                    return -1;
                } else {
                    return o1.getUsername().compareTo(o2.getUsername());
                }
            }
        };

        Comparator<User> descComparator = new Comparator<>() {
            @Override
            public int compare(User o1, User o2) {
                return ascComparator.compare(o1, o2) * (-1);
            }
        };

        switch (sortType) {
            case Constants.ASC -> {
                users.sort(ascComparator);
            }
            case Constants.DESC -> {
                users.sort(descComparator);
            }
        }

        return users.subList(0, Math.min(users.size(), number));
    }
}
