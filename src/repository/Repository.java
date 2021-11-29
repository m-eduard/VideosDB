package repository;

import actor.Actor;
import entertainment.Movie;
import entertainment.Serial;
import entertainment.Video;
import fileio.*;
import user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class which stores all the input data in a database that can be changed
 * using various actions.
 */
public class Repository {
    private List<Actor> actors;
    private List<User> users;
    private List<Movie> movies;
    private List<Serial> serials;

    private static Repository instance = null;

    private Repository() {
        this.actors = new ArrayList<>();
        this.users = new ArrayList<>();
        this.movies = new ArrayList<>();
        this.serials = new ArrayList<>();
    }

    /**
     * Constructor that can populate a database using
     * the input data parsed as Input object.
     * @param input Input object which stores the parsed data.
     */
    private Repository(Input input) {
        this();

        for (ActorInputData actor : input.getActors()) {
            this.actors.add(new Actor(actor.getName(), actor.getCareerDescription(),
                    actor.getFilmography(), actor.getAwards()));
        }

        for (UserInputData user : input.getUsers()) {
            this.users.add(new User(user.getUsername(), user.getSubscriptionType(),
                    user.getHistory(), user.getFavoriteMovies()));
        }

        for (MovieInputData movie : input.getMovies()) {
            this.movies.add(new Movie(movie.getTitle(), movie.getCast(), movie.getGenres(),
                    movie.getYear(), movie.getDuration()));
        }

        for (SerialInputData serial : input.getSerials()) {
            this.serials.add(new Serial(serial.getTitle(), serial.getCast(), serial.getGenres(),
                    serial.getNumberSeason(), serial.getSeasons(), serial.getYear()));
        }
    }

    public static Repository getInstance() {
        if (instance == null) {
            instance = new Repository();
        }

        return instance;
    }

    public static Repository getInstance(Input input) {
        if (instance == null) {
            instance = new Repository(input);
        }

        return instance;
    }

    public User findUser(final String username) {
        return getInstance().users.stream().filter(a -> a.getUsername().equals(username))
                .findAny().orElse(null);
    }

    public Video findVideo(final String title) {
        Video target = getInstance().movies.stream().filter(a -> a.getTitle().equals(title))
                .findAny().orElse(null);

        if (target == null) {
            target = getInstance().serials.stream().filter(a -> a.getTitle().equals(title))
                    .findAny().orElse(null);
        }

        return target;
    }
}
