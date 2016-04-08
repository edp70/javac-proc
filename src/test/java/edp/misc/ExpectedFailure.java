package edp.misc;

public class ExpectedFailure extends RuntimeException {
    public ExpectedFailure() {
        super();
    }

    public ExpectedFailure(final String msg) {
        super(msg);
    }

    public ExpectedFailure(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public ExpectedFailure(final Throwable cause) {
        super(cause);
    }
}
