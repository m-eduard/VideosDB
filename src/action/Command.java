package action;

import common.Constants;

public class Command extends Action {
    private final String username;
    private final String type;

    public Command(final int actionId, final String username, final String type) {
        super(actionId);
        this.username = username;
        this.type = type;
    }

    @Override
    public String apply() {
        return switch (type) {
            case Constants.FAVORITE -> favorite();
            case Constants.VIEW -> view();
            case Constants.RATING -> rating();
            default -> null;
        };
    }

    private String favorite() {
        String message = new String();


        return message;
    }

    private String view() {
        String message = new String();

        return message;
    }

    private String rating() {
        String message = new String();

        return message;
    }
}
