package action;

import actor.Actor;
import common.Constants;
import entertainment.Video;
import utils.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CustomFilter {
    private CustomFilter() {
    }

    /**
     * Filters a list of videos using the provided filters
     * (by year/genre).
     * @param videos list of videos that will be filtered
     * @param filters list of filters that will be applied
     */
    public static List<Video> filterVideos(final List<Video> videos,
                                           final List<List<String>> filters) {
        return videos.stream().filter(x -> {
            boolean status = true;

            if (filters.get(Constants.YEAR_POS) != null
                    && filters.get(Constants.YEAR_POS).get(0) != null) {
                int year = Integer.parseInt(filters.get(Constants.YEAR_POS).get(0));
                status = status && (year == x.getYear());
            }
            if (filters.get(Constants.GENRE_POS) != null
                    && filters.get(Constants.GENRE_POS).get(0) != null) {
                String genre = filters.get(Constants.GENRE_POS).get(0);
                status = status && (x.getGenres().contains(genre));
            }

            return status;
        }).collect(Collectors.toList());
    }

    /**
     * Filters a list of actors using the provided filters
     * (by awards won/words that appear in their description).
     * @param actors list of actors that will be filtered
     * @param filters list of filters that will be applied
     * @return
     */
    public static List<Actor> filterActors(final List<Actor> actors,
                                           final List<List<String>> filters) {
        List<Actor> filteredActors = actors;

        /**
         * Eliminate the actors that don't have the requested awards.
         */
        if (filters.get(Constants.AWARDS_POS) != null) {
            filteredActors = filteredActors.stream().filter(x -> {
                int awards = filters.get(Constants.AWARDS_POS).stream().map(Utils::stringToAwards)
                                .map(y -> ((x.getAwards().containsKey(y)) ? 1 : 0))
                                .reduce(0, Integer::sum);

                return awards == filters.get(Constants.AWARDS_POS).size();
            }).collect(Collectors.toList());
        }

        /**
         * Eliminate the actors whose descriptions don't met the words' requirement.
         * (using regex for words separated by any punctuation;
         * the matching is done ignoring the case sensitivity)
         */
        if (filters.get(Constants.WORDS_POS) != null) {
            filteredActors = filteredActors.stream().filter(x -> {
                boolean status = true;

                String[] descriptionTokens = x.getCareerDescription().split("\\W+");
                for (String keyword : filters.get(Constants.WORDS_POS)) {
                    if (!Arrays.asList(descriptionTokens).stream().map(y -> y.toLowerCase())
                            .collect(Collectors.toList()).contains(keyword.toLowerCase())) {
                        status = false;
                        break;
                    }
                }
                return status;
            }).collect(Collectors.toList());
        }

        return filteredActors;
    }
}
