package jsonp.decoder;

class InvalidToken extends RuntimeException {
    public InvalidToken(String reason) {
        super(reason);
    }
}