package core.logic.program;

public class SFunction extends SProgramImpl{
    private final String userName;

    public SFunction(String name, String userName) {
        super(name);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

}
