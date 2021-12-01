package action;

import actor.Actor;
import common.Constants;
import entertainment.Video;
import user.User;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class CustomSort {
    private CustomSort() {
    }

    // Overloading cannot be used because all the methods have the same erasure.

    /**
     * Sort a list of videos by their properties in the requested order.
     * @param videos input list of videos
     * @param properties map where every element from @videos is mapped to
     *                   a value that represents the most important sorting criteria
     * @param sortType how the list will be sorted
     *                 (increasing order / decreasing / respecting the database order)
     * @return sorted list
     */
    public static List<Video> sortVideos(final List<Video> videos,
                                         final Map<Video, Double> properties,
                                         final String sortType) {
        Comparator<Video> ascComparator = new Comparator<>() {
            @Override
            public int compare(final Video o1, final Video o2) {
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
            public int compare(final Video o1, final Video o2) {
                return ascComparator.compare(o1, o2) * (-1);
            }
        };

        /**
         * Comparator that keeps the database order, if the
         * values for the first criteria are equal.
         */
        Comparator<Video> ascDbComparator = new Comparator<>() {
            @Override
            public int compare(final Video o1, final Video o2) {
                return Double.compare(properties.get(o1), properties.get(o2));
            }
        };

        Comparator<Video> descDbComparator = new Comparator<>() {
            @Override
            public int compare(final Video o1, final Video o2) {
                return (-1) * ascDbComparator.compare(o1, o2);
            }
        };

        /**
         * Sort the list using the custom comparators created.
         */
        switch (sortType) {
            case Constants.ASC -> {
                videos.sort(ascComparator);
            }
            case Constants.DESC -> {
                videos.sort(descComparator);
            }
            case Constants.DB_ASC -> {
                videos.sort(ascDbComparator);
            }
            case Constants.DB_DESC -> {
                videos.sort(descDbComparator);
            }
            default -> {
            }
        }

        return videos;
    }

    /**
     * Sort a list of videos by their properties in the requested order.
     * @param users list of users
     * @param properties properties map where every element from @users is mapped to
     *      *                   a value that represents the most important sorting criteria
     * @param sortType order in which the list will be sorted
     * @return sorted list
     */
    public static List<User> sortUsers(final List<User> users, final Map<User, Double> properties,
                                       final String sortType) {
        Comparator<User> ascComparator = new Comparator<>() {
            @Override
            public int compare(final User o1, final User o2) {
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
            public int compare(final User o1, final User o2) {
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
            default -> {
            }
        }

        return users;
    }

    /**
     * Sort a given list of actors in specified order by their property,
     * stored as value in the @properties.
     * @param actors list of actors
     * @param properties properties for every actor
     * @return a list of sorted actors
     */
    public static List<Actor> sortActors(final List<Actor> actors,
                                         final Map<Actor, Double> properties,
                                         final String sortType) {
        Comparator<Actor> ascComparator = new Comparator<>() {
            @Override
            public int compare(final Actor o1, final Actor o2) {
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
            public int compare(final Actor o1, final Actor o2) {
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
            default -> {
            }
        }

        return actors;
    }
}
