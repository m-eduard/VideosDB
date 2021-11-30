package action;

import actor.Actor;
import common.Constants;
import entertainment.Video;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CustomFilter {
    public static List<Video> filterVideos(List<Video> videos, List<List<String>> filters) {
        return videos.stream().filter(x -> {
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
    }

    public static List<Actor> filterActors(List<Actor> actors, List<List<String>> filters) {
        if (filters.get(Constants.AWARDS_POS) != null) {
            actors = actors.stream().filter(x -> {
                int awards = filters.get(Constants.AWARDS_POS).stream().map(y -> (x.getAwards().containsKey(y)) ? 1 : 0)
                                .reduce(0, Integer::sum);

                return awards == filters.get(Constants.AWARDS_POS).size();
            }).collect(Collectors.toList());
        }

        if (filters.get(Constants.WORDS_POS) != null) {
            actors = actors.stream().filter(x -> {
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
        }

        return actors;
    }
}
