package action;

import actor.Actor;
import common.Constants;
import entertainment.Video;
import user.User;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CustomSort {
    // Overloading cannot be used because the two methods have the same erasure.
    public static List<Video> sortVideos(List<Video> videos, Map<Video, Double> properties, String sortType) {
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

        return videos;
    }

    public static List<User> sortUsers(List<User> users, Map<User, Double> properties, String sortType) {
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

        return users;
    }

    /**
     * Sort a given list of actors in specified order by their property, stored as value in
     * in the HashMap properties.
     * @param actors
     * @param properties
     * @return A list of sorted actors
     */
    public static List<Actor> sortActors(List<Actor> actors, Map<Actor, Double> properties, String sortType) {
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

        return actors;
    }
}
