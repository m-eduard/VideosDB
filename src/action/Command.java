package action;

import common.Constants;
import entertainment.Movie;
import entertainment.Serial;
import entertainment.Video;
import repository.Repository;
import user.User;

import java.util.ArrayList;
import java.util.Arrays;

public final class Command extends Action {
    /**
     * The username of the user
     */
    private final String username;
    /**
     * The title of the video
     */
    private final String title;
    /**
     * Season number, if the video is a serial;
     * 0, otherwise
     */
    private final int seasonNumber;
    /**
     * The type of the command
     */
    private final String type;
    /**
     * The grade of a video, for rating command
     */
    private final double grade;

    public Command(final int actionId, final String username, final String title,
                   final Integer seasonNumber, final String type, final double grade) {
        super(actionId);
        this.username = username;
        this.title = title;
        this.seasonNumber = seasonNumber;
        this.type = type;
        this.grade = grade;
    }

    /**
     * Apply a command(e.g.: view, add to favorites, rate)
     * on the database.
     * @return command output
     */
    @Override
    public String apply() {
        return switch (type) {
            case Constants.FAVORITE -> favorite();
            case Constants.VIEW -> view();
            case Constants.RATING -> rating();
            default -> null;
        };
    }

    /**
     * Adds a video inside the favorites list of a user.
     */
    private String favorite() {
        Repository repo = Repository.getInstance();
        User user = repo.findUser(username);

        if (user.getHistory().containsKey(title)) {
            if (!user.getFavoriteMovies().contains(title)) {
                user.getFavoriteMovies().add(title);
                return "success -> " + title + " was added as favourite";
            }
            return "error -> " + title + " is already in favourite list";
        }
        return "error -> " + title + " is not seen";
    }

    /**
     * Adds a video to the history for a user.
     */
    private String view() {
        Repository repo = Repository.getInstance();
        User user = repo.findUser(username);

        user.getHistory().put(title, user.getHistory().getOrDefault(title, 0) + 1);

        return "success -> " + title + " was viewed with total views of "
                + user.getHistory().get(title);
    }

    /**
     * Rates a video on behalf of the current user.
     */
    private String rating() {
        Repository repo = Repository.getInstance();
        User user = repo.findUser(username);

        if (user.getHistory().containsKey(title)) {
            if (user.getRated().containsKey(title)) {
                if (user.getRated().get(title).contains(seasonNumber)) {
                    return "error -> " + title + " has been already rated";
                }

                /**
                 * Rate a season of a serial which already have
                 * some seasons rated by the specified user.
                 */
                user.getRated().get(title).add(seasonNumber);
            } else {
                /**
                 * Rate a season of a serial for the first time/rate a movie.
                 */
                user.getRated().put(title, new ArrayList<>(Arrays.asList(seasonNumber)));
            }

            /**
             * Assign the rating to the specified video.
             */
            Video video = repo.findVideo(title);
            if (seasonNumber == 0) {
                ((Movie) video).getRatings().add(grade);
            } else {
                ((Serial) video).getSeasons().get(seasonNumber - 1).getRatings().add(grade);
            }

            return "success -> " + title + " was rated with " + grade + " by " + username;
        }

        return "error -> " + title + " is not seen";
    }
}
