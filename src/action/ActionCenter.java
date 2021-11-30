package action;

import common.Constants;
import fileio.ActionInputData;
import fileio.Writer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActionCenter {
    private final List<Action> actions;

    public ActionCenter() {
        actions = new ArrayList<>();
    }

    /**
     * Constructor that uses the parsed list of actions and put them in a more
     * organised structure, where actions will be applied on the database.
     * @param commandsData List of actions that will be applied on the database
     */
    public ActionCenter(final List<ActionInputData> commandsData) {
        this();

        for (ActionInputData action : commandsData) {
            Action temp = switch(action.getActionType()) {
                case Constants.COMMAND -> new Command(action.getActionId(), action.getUsername(), action.getTitle(),
                                                        action.getSeasonNumber(), action.getType(), action.getGrade());
                case Constants.QUERY -> new Query(action.getActionId(), action.getObjectType(), action.getNumber(),
                                                    action.getFilters(), action.getSortType(), action.getCriteria());
                case Constants.RECOMMENDATION -> new Recommendation(action.getActionId(), action.getUsername(),
                                                    action.getType(), action.getGenre());
                default -> null;
            };

            this.actions.add(temp);
        }
    }

    public void apply(final Writer fileWriter, final JSONArray arrayResult) {
        actions.forEach(a -> {
            try {
                JSONObject output = fileWriter.writeFile(a.getActionId(), "", a.apply());
                arrayResult.add(output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
