package action;

public abstract class Action {
    private final int actionId;

    public Action(int actionId) {
        this.actionId = actionId;
    }

    abstract public String apply();

    public int getActionId() {
        return actionId;
    }
}
