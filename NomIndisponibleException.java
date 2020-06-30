public class NomIndisponibleException extends Exception {
    private static final long serialVersionUID = 8163631555454702627L;

    public NomIndisponibleException() {
        super();
    }

    public NomIndisponibleException(String errorMessage) {
        super(errorMessage);
    }
}