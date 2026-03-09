package io.github.jianglei.mediatr;

public final class ExceptionHandlerState<TResponse> {
    private boolean handled;
    private TResponse response;

    public boolean isHandled() {
        return handled;
    }

    public TResponse response() {
        return response;
    }

    public void setHandled(TResponse response) {
        this.handled = true;
        this.response = response;
    }
}
