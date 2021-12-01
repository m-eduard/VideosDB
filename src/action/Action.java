package action;

public abstract class Action {
    private final int actionId;

    public Action(final int actionId) {
        this.actionId = actionId;
    }

    /**
     * Handler for the operations that can be applied:
     * This method is overridden in every Action() subclass
     * in order to apply the required operations on the movies
     * database (aka repository).
     * @return a custom message for every type of action
     */
    abstract String apply();

    public final int getActionId() {
        return actionId;
    }
}
